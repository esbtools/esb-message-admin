// Karma configuration
// Generated on Fri Sep 12 2014 09:33:53 GMT-0400 (EDT)

module.exports = function(config) {
  config
    .set({
      // base path that will be used to resolve all patterns (eg.
      // files, exclude)
      basePath: '../../..',

      // frameworks to use
      // available frameworks:
      // https://npmjs.org/browse/keyword/karma-adapter
      frameworks: ['jasmine'],

      // list of files / patterns to load in the browser
      files: [
        "src/main/webapp/bower_components/jquery/dist/jquery.js",
        "src/main/webapp/bower_components/bootstrap/dist/js/bootstrap.js",
        "src/main/webapp/bower_components/angular/angular.js",
        "src/main/webapp/bower_components/angular-bootstrap/ui-bootstrap-tpls.js",
        "src/main/webapp/bower_components/angular-route/angular-route.js",
        "src/main/webapp/bower_components/angular-mocks/angular-mocks.js",
        "src/main/webapp/bower_components/ng-grid/build/ng-grid.js",
        "src/main/webapp/bower_components/ng-grid/plugins/ng-grid-layout.js",
        "src/main/webapp/bower_components/angucomplete-keyvalue/angucomplete.js",
        "src/main/webapp/bower_components/ngQuickDate/dist/ng-quick-date.js",
        "src/main/webapp/bower_components/angular-loading-bar/build/loading-bar.js",
        "src/main/webapp/bower_components/message-center/message-center.js",
        "src/main/webapp/bower_components/angular-ui-layout/ui-layout.js",
        "src/main/webapp/js/**/*.js",
        "src/test/webapp/**/*.js"
      ],

      // list of files to exclude
      exclude: [],

      // preprocess matching files before serving them to the browser
      // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
      preprocessors: {
        'src/main/webapp/js/**/*.js': ['coverage']
      },

      // test results reporter to use
      // possible values: 'dots', 'progress'
      // available reporters: https://npmjs.org/browse/keyword/karma-reporter
      reporters: ['progress', 'coverage'],

      coverageReporter: {
        type: 'text-summary'
      },

      // web server port
      port: 9876,

      // enable / disable colors in the output (reporters and logs)
      colors: true,

      // level of logging
      // possible values: config.LOG_DISABLE || config.LOG_ERROR ||
      // config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
      logLevel: config.LOG_INFO,

      // enable / disable watching file and executing tests whenever
      // any file changes
      autoWatch: false,

      // start these browsers
      // available browser launchers:
      // https://npmjs.org/browse/keyword/karma-launcher
      browsers: ["PhantomJS"],

      // Continuous Integration mode
      // if true, Karma captures browsers, runs the tests and exits
      singleRun: true
    });
};
