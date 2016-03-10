/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.controllers')
    .controller('LoadTestController',['$rootScope', '$scope',
                                      '$route', '$routeParams',
                                      'Request',
                                      '$alert',
                                      '$q',
        function ($rootScope, $scope,
                  $route, $routeParams,
                  Request,
                  $alert,
                  $q) {
            'use strict';

            var patt = /gijs(\+)?[a-zA-Z0-9]*@thehyve.nl/g;
            var result = patt.test($rootScope.globals.currentUser.username);
            //console.log('pattern: ' + patt.source + ', username: ' + $rootScope.globals.currentUser.username + ', match: ' + result);
            $scope.loadTestCounter = 1;

            $scope.loadTestEnabled = function() {
                return result;
            };

            if (!result) {
                console.log('LoadTestController not loaded.');
                return;
            }

            /**
             * Creates and submits a test request.
             *
             * @return promise that when executed creates and submits a test request.
             */
            var createAndSubmitRequest = function() {
                return $q(function(resolve, reject) {
                    var req = new Request();
                    req.$save(function(req) {
                        console.log('created: ' + req.processInstanceId);
                        req.title = 'Test ' + ($scope.loadTestCounter++);
                        req.contactPersonName = 'Contact Person';
                        req.pathologistName = 'P.A. Thologist';
                        req.pathologistEmail = 'p.a.thologist@test.org';

                        req.background = 'Load testing';
                        req.researchQuestion = 'How does the system perform under heavy load?';
                        req.hypothesis = 'Overall okay, but slow in dealing with lab requests.';
                        req.methods = 'Javascript test script emulating many requests.';
                        req.type = '4';

                        req.billingAddress = {};
                        req.billingAddress.address1 = 'Teststraat 123';
                        req.billingAddress.postalCode = '1234 AB';
                        req.billingAddress.city = 'Utrecht';

                        req.chargeNumber = 'PROJ-567890';
                        req.linkageWithPersonalData = false;

                        Request.convertRequestTypeToOpts(req); // convert request type

                        req.$submit(function(result) {
                            console.log('Submitted: ' + result.requestNumber);
                            resolve(result);
                        }, function(err) {
                            reject(err);
                        });
                    }, function(err) {
                        reject(err);
                    });
                });
            };

            /**
             * Creates and submits 20 test requests.
             * Reloads the current page afterwards.
             */
            $scope.createTestRequests = function() {
                var start = new Date().getTime();
                var newRequestPromises = [];
                for(var i=0; i<10; i++) {
                    newRequestPromises.push(createAndSubmitRequest());
                }
                console.log('Start load test (creating and submitting ' + newRequestPromises.length + ' requests).');
                $q.all(newRequestPromises).then(function(result) {
                    var end = new Date().getTime();
                    console.log('Load test took ' + (end - start) + ' ms.');
                    $route.reload();
                }, function(err) {
                    var end = new Date().getTime();
                    console.log('Load test aborted after ' + (end - start) + ' ms.');
                    console.log('Error: ' + err);
                    $route.reload();
                });
            };

            var isCurrentUser = function(user) {
                return ($scope.globals.currentUser.userid === user);
            };

            /**
             * Tries to move a request forward in the process, by submitting for
             * approval, approving, uploading excerpt lists, approving selection,
             * until lab requests have been created.
             * Only tries one forward step.
             */
            var forwardRequest = function(req) {
                return $q(function(resolve, reject) {
                    //console.log('Considering request ' + req.requestNumber);
                    if (isCurrentUser(req.assignee)) {
                        if (req.status == 'Review') {
                            req.requesterLabValid = true;
                            req.requesterValid = true;
                            req.requesterAllowed = true;
                            req.contactPersonAllowed = true;
                            req.agreementReached = true;

                            req.$submitForApproval(function(result) {
                                console.log('Submitted for approval: ' + result.requestNumber);
                                resolve(result);
                            }, function(err) {
                                reject(err);
                            });
                        }
                        else if (req.status == 'Approval') {
                            req.scientificCouncilApproved = true;
                            req.privacyCommitteeApproved = true;
                            req.$finalise(function(result) {
                                console.log('Finalised: ' + result.requestNumber);
                                resolve(result);
                            }, function(err) {
                                reject(err);
                            });
                        } else if (req.status == 'DataDelivery') {
                            req.$get(function(res) {
                                if (res.excerptList === null) {
                                    res.$useExampleExcerptList(function(result) {
                                        console.log('Using example excerpt list for:' + result.requestNumber);
                                        resolve();
                                    }, function(err) {
                                        reject(err);
                                    });
                                } else {
                                    res.$selectAll(function(result) {
                                        console.log('Select all for:' + result.requestNumber);
                                        resolve(result);
                                    }, function(err) {
                                        reject(err);
                                    });
                                }
                            }, function(err) {
                                reject(err);
                            });
                        } else if (req.status == 'SelectionReview') {
                            req.selectionApproved = true;
                            req.$updateExcerptSelectionApproval(function(result) {
                                console.log('Excerpt selection approved for: ' + result.requestNumber);
                                resolve(result);
                            }, function(err) {
                                reject(err);
                            });
                        } else {
                            resolve(req);
                        }
                    } else {
                        resolve(req);
                    }
                });
            };

            var claimRequest = function(req) {
                return $q(function(resolve, reject) {
                    req.$claim(function(result) {
                        console.log('Task claimed: ' + result.requestNumber);
                        resolve(result);
                    }, function(err) {
                        reject(err);
                    });
                });
            };

            $scope.claimableStates = Request.claimableStates;

            /**
             * Reads the list of requests from the parent scope and tries to forward
             * the requests one step.
             */
            $scope.loadTestPalga = function() {
                if (!$scope.$parent.allRequests) {
                    console.log('No requests found in parent scope.');
                    return;
                }
                console.log($scope.$parent.allRequests.length + ' requests found in parent scope.');

                var claimAllPromise = $q(function(resolve, reject) {
                    var claimStart = new Date().getTime();
                    // claim all
                    var claimPromises = [];
                    $scope.$parent.allRequests.forEach(function (req) {
                        if (req.assignee === null && _.includes($scope.claimableStates, req.status)) {
                            claimPromises.push(claimRequest(req));
                        }
                    });
                    console.log('Start claiming requests (' + claimPromises.length + ' requests).');
                    $q.all(claimPromises).then(function(result) {
                        var claimEnd = new Date().getTime();
                        console.log('Claiming requests test took ' + (claimEnd - claimStart) + ' ms.');
                        resolve(result);
                    }, function(err) {
                        var claimEnd = new Date().getTime();
                        console.log('Claiming requests aborted after ' + (claimEnd - claimStart) + ' ms.');
                        reject(err);
                    });
                });

                var forwardAllPromise = $q(function(resolve, reject) {
                    var forwardStart = new Date().getTime();
                    var forwardRequestPromises = [];
                    $scope.$parent.allRequests.forEach(function(req) {
                        forwardRequestPromises.push(forwardRequest(req));
                    });
                    console.log('Start Palga load test (' + forwardRequestPromises.length + ' actions).');
                    $q.all(forwardRequestPromises).then(function(result) {
                        var forwardEnd = new Date().getTime();
                        console.log('Palga load test took ' + (forwardEnd - forwardStart) + ' ms.');
                        resolve(result);
                    }, function(err) {
                        var forwardEnd = new Date().getTime();
                        console.log('Palga load test aborted after ' + (forwardEnd - forwardStart) + ' ms.');
                        reject(err);
                    });
                });

                var promises = [claimAllPromise, forwardAllPromise];
                $q.all(promises).then(function(result) {
                    console.log('Load tests completed.');
                    $route.reload();
                }, function(err) {
                    console.log('Error: ' + err);
                    $route.reload();
                });
            };

            console.log('LoadTestController loaded.');

        }
    ]);
