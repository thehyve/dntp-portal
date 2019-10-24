/*
 * Copyright (C) 2019  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

var webpack = require('webpack');
const path = require('path');

module.exports = {
    entry: {
        'vendor': [
            'jquery',
            'lodash',
            'angular',
            'angular-aria',
            'angular-cookies',
            'angular-loading-bar',
            'angular-messages',
            'angular-mocks',
            'angular-resource',
            'angular-route',
            'angular-sanitize',
            'angular-touch',
            'angular-smart-table',
            'angular-strap',
            'angular-toastr',
            'angular-translate',
            'bootbox',
            'malarkey',
            'moment',
            'ng-tags-input',
            'rangy',
            'restangular',
            'textangular'
        ]
    },
    resolve: {
        extensions: ['.ts', '.js'],
        modules: ['node_modules']
    },
    module: {
        exprContextCritical: false,
        rules: [
            {
                test: /\.(jpe?g|png|gif|svg|woff|woff2|ttf|eot)$/i,
                loaders: [
                    'file-loader?hash=sha512&digest=hex&name=[hash].[ext]',
                    'image-webpack-loader?bypassOnDebug&optimizationLevel=7&interlaced=false'
                ]
            }
        ]
    },
    output: {
        filename: '[name].dll.js',
        path: path.resolve('./target/www'),
        library: '[name]'
    },
    plugins: [
        new webpack.DllPlugin({
            name: '[name]',
            path: path.resolve('./target/www/[name].json')
        })
    ]
};
