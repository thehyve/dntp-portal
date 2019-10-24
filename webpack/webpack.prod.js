/*
 * Copyright (C) 2019  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

const commonConfig = require('./webpack.common');
const webpackMerge = require('webpack-merge');
const Visualizer = require('webpack-visualizer-plugin');
const ENV = 'prod';
const path = require('path');

module.exports = webpackMerge(commonConfig({env: ENV}), {
    devtool: 'source-map',
    output: {
        path: path.resolve('target/www'),
        filename: '[hash].[name].bundle.js',
        chunkFilename: '[hash].[id].chunk.js'
    },
    plugins: [
        new Visualizer({
            // Webpack statistics in target folder
            filename: '../stats.html'
        })
    ]
});
