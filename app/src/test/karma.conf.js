// Karma configuration
// Generated on Fri Sep 12 2014 09:33:53 GMT-0400 (EDT)

module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '../..',


    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['jasmine'],


    // list of files / patterns to load in the browser
    files: [
    "src/main/webapp/bower_components/jquery/dist/jquery.min.js",
    "src/main/webapp/bower_components/bootstrap/dist/js/bootstrap.min.js",
    "src/main/webapp/bower_components/angular/angular.min.js",
    "src/main/webapp/bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js",
    "src/main/webapp/bower_components/angular-route/angular-route.min.js",
    "src/main/webapp/bower_components/angular-mocks/angular-mocks.js",
    "src/main/webapp/bower_components/ng-grid/build/ng-grid.min.js",
    "src/main/webapp/bower_components/angucomplete-keyvalue/angucomplete.js",
    "src/main/webapp/bower_components/angular-loading-bar/build/loading-bar.min.js",
    "src/main/webapp/bower_components/ngQuickDate/dist/ng-quick-date.min.js",
    "src/main/webapp/bower_components/message-center/message-center.js",
    "src/main/webapp/js/*.js",
    "src/test/js/*.js"
    ],


    // list of files to exclude
    exclude: [
    //"src/test/js/termsListCtrlTest.js"
    ],


    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
    },


    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: ['progress'],


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,


    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: ['Chrome'],


    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: false
  });
};
