/*
 * Copyright (C) 2019  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

const webpack = require('webpack');
const path = require('path');
const commonConfig = require('./webpack.common.js');
const writeFilePlugin = require('write-file-webpack-plugin');
const webpackMerge = require('webpack-merge');
const BrowserSyncPlugin = require('browser-sync-webpack-plugin');
const ENV = 'dev';
const execSync = require('child_process').execSync;
const fs = require('fs');
const ddlPath = path.resolve('./target/www/js/vendor.json');

if (!fs.existsSync(ddlPath)) {
    execSync('webpack --config webpack/webpack.vendor.js');
}

module.exports = webpackMerge(commonConfig({env: ENV}), {
    mode: 'development',
    devtool: 'inline-source-map',
    devServer: {
        contentBase: './target/www',
        proxy: [{
            context: [
                '/api',
                '/login',
                '/logout',
                '/favicon.ico'
            ],
            target: 'http://127.0.0.1:8092',
            secure: false
        }]
    },
    output: {
        path: path.resolve('./target/www') ,
        filename: '[name].bundle.js',
        chunkFilename: '[id].chunk.js',
        publicPath: '/'
    },
    plugins: [
        new BrowserSyncPlugin({
            host: 'localhost',
            port: 9000,
            proxy: 'http://localhost:9060'
        }, {
            reload: false
        }),
        new webpack.NoEmitOnErrorsPlugin(),
        new webpack.NamedModulesPlugin(),
        new writeFilePlugin()
    ]
});
