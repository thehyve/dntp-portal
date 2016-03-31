/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function (window) {
    'use strict';

    /* global messages: true,
     */

    var messages = window.messages || (window.messages = {});
    
    messages.en = {
        /* ========= */
        /* Global */
        /* ========= */
        'DNTP': 'PALGA',
        'An email has been sent to ? with instructions on how to reset your password.': 'An email has been sent to {{email}} with instructions on how to reset your password.',
        'Registration has been completed. An email with an activation link should have been sent to ?.': 'Registration has been completed. An email with an activation link should have been sent to {{email}}.',

        /* ========= */
        /* Request */
        /* ========= */
        'popover: Principal investigator': 'Principal investigator of this research project.',
        'popover: Pathologist': 'Pathologist involved in this research project. ',
        'popover: Pathologist email' : 'E-mail adres of the pathologist.',
        'popover: Address': 'Address where the invoice should be sent to.',
        'popover: E-mail': 'e-mail.',
        'popover: Province': 'Province.',
        'popover: Charge number': 'Account where the project can be booked.',
        'popover: Research number': 'Project number of Subsidy Grant.',
        'Running title': 'Title',
        'popover: Title' : 'Title of the research project.',
        'popover: Background': 'Project background.',
        'popover: Research question': 'Research question.',
        'popover: Hypothesis': 'Hypothesis',
        'popover: Methods': 'Used techniques ( e.a. germline mutation analysis, moleculair techniques, immuno histochemistry, tissue micro array ). How many patients do you need (at least) to answer this research question?',
        'popover: Postal code': 'Postal code',
        'popover: City': 'City',
        'popover: Telephone': 'Telephone',
        'popover: Previous contact' : '',
        'popover: Description of previous contact': '',
        'popover: Request type': 'Request type.',
        'popover: Data linkage' : 'Linking information',
        'popover: Notes for linkage with personal data' :  'Indicate the database you want us to link to ',
        'popover: Reason using personal data': 'Reason for absence IC',
        'popover: Upload file' : 'Upload file.',
        'popover: Upload medical ethical committee approval' : 'Upload medical ethical committee approval.',
    
        'Are you sure you want to delete file ?' : 'Are you sure you want to delete file {{name}}?',
        'File too large': 'File too large ({{mb_size}} MB). Maximum size is 10 MB.',
        'Are you sure you want to delete request ?' : 'Are you sure you want to delete request {{id}}?',
        'Edit user ...' : 'Edit user {{username}}',
        'Download ...' : 'Download {{filename}}',
    
        'ACCEPTED' : 'Accepted',
        'REJECTED' : 'Rejected',
    
        'Request_Type_1' : 'Request for numbers only (OZV=Orientererende zoekvraag)',
        'Request_Type_2' : 'Request for excerpts (LZV=Landelijke zoekvraag)',
        'Request_Type_3' : 'Request for excerpts + PA reports',
        'Request_Type_4' : 'Request for excerpts + materials',
        'Request_Type_5' : 'Request for excerpts + PA reports + materials',
        'Request_Type_6' : 'Request for PA-reports only',
        'Request_Type_7' : 'Request for materials only',
    
        /* ===================== */
        /* Agreement overview    */
        /* ===================== */
        'popover:Markdown Help': 'Markdown Help',
        'Agreement form template': 'Agreement form template',

        /* ===================== */
        /* Status */
        /* ===================== */
        'Open': 'Not submitted',
        'Review': 'Under review',
        'Approval': 'Waiting for approval',
        'DataDelivery': 'Data delivery and selection',
        'SelectionReview': 'Waiting for excerpt selection approval',
        'LabRequest': 'Lab request',
        'Waiting for lab approval': 'Waiting for lab approval',
        'Approved': 'Approved',
        'Sending': 'Materials sent',
        'Received': 'Received',
        'Returning': 'Sent in return',
        'Returned': 'Returned',
        'Completed': 'Completed',
        'Closed': 'Closed',

        /* ===================== */
        /* My Lab */
        /* ===================== */
        null: null
    };
})(window);
