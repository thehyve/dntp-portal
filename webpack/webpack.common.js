/*
 * Copyright (C) 2019  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

const webpack = require('webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const StringReplacePlugin = require('string-replace-webpack-plugin');
const AddAssetHtmlPlugin = require('add-asset-html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const path = require('path');

module.exports = (options) => {
    return {
        entry: {
            main: './src/main/webapp/app/processapp'
        },
        resolve: {
            extensions: ['.js'],
            modules: ['node_modules']
        },
        optimization: {
            //minimize: false
        },
        module: {
            rules: [
                { test: /bootstrap\/dist\/js\/umd\//, loader: 'imports-loader?jQuery=jquery' },
                {
                    test: /\.(js)$/,
                    exclude: /node_modules/,
                    use: ['babel-loader', 'eslint-loader']
                },
                {
                    test: /\.html$/,
                    loader: 'html-loader',
                    options: {
                        //minimize: true,
                        caseSensitive: true,
                        removeAttributeQuotes: false,
                        minifyJS: false,
                        minifyCSS: false
                    }
                },
                {
                    test: /\.(css|less)$/,
                    use: [
                        {
                            loader: MiniCssExtractPlugin.loader,
                            options: {
                                // you can specify a publicPath here
                                // by default it uses publicPath in webpackOptions.output
                                //publicPath: '../',
                                hmr: process.env.NODE_ENV === 'development',
                            },
                        },
                        'css-loader',
                    ],
                },
                {
                    test: /\.(jpe?g|png|gif|svg|woff2?|ttf|eot)$/i,
                    loaders: ['file-loader?hash=sha512&digest=hex&name=content/[hash].[ext]']
                }
            ]
        },
        plugins: [
            new webpack.ProvidePlugin({
                $: 'jquery',
                jQuery: 'jquery',
                _: 'lodash',
                Flow: '@flowjs/flow.js'
            }),
            new webpack.DllReferencePlugin({
                context: __dirname,
                manifest: require('../target/www/vendor.json')
            }),
            new CopyWebpackPlugin([
                { from: './node_modules/core-js/client/shim.min.js', to: 'core-js-shim.min.js' },
                { from: './src/main/webapp/robots.txt', to: 'robots.txt' },
                { from: './src/main/webapp/css/print.css', to: 'css/print.css' },
                { from: './src/main/webapp/images/logo_palga-transparent.png', to: 'images/logo_palga-transparent.png' }
            ]),
            new HtmlWebpackPlugin({
                template: './src/main/webapp/index.html',
                chunksSortMode: 'dependency',
                inject: 'body'
            }),
            new AddAssetHtmlPlugin([
                { filepath: path.resolve('./target/www/vendor.dll.js'), includeSourcemap: false }
            ]),
            //new StringReplacePlugin(),
            new MiniCssExtractPlugin({
                // Options similar to the same options in webpackOptions.output
                // all options are optional
                filename: '[name].css',
                chunkFilename: '[id].css',
                ignoreOrder: false // Enable to remove warnings about conflicting order
            })
        ]
    };
};
