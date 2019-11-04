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
const ENV = 'test';
const execSync = require('child_process').execSync;
const fs = require('fs');
const ddlPath = path.resolve('./target/www/vendor.json');

if (!fs.existsSync(ddlPath)) {
    execSync('webpack --config webpack/webpack.vendor.js');
}

module.exports = webpackMerge(commonConfig({env: ENV}), {
    mode: 'development',
    devtool: 'inline-source-map',
    output: {
        path: path.resolve('./target/www') ,
        filename: '[name].bundle.js',
        chunkFilename: '[id].chunk.js',
        publicPath: '/'
    },
    plugins: [
        new webpack.NoEmitOnErrorsPlugin(),
        new webpack.NamedModulesPlugin(),
        new writeFilePlugin()
    ]
});
