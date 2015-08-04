var baseConfig = require('./karma.conf.headless.js');
module.exports = function(config) {
  // Load base config
  baseConfig(config);
  // Override base config
  config.set({
    reporters: ['coverage', 'coveralls'],
    coverageReporter: {
      type: 'lcov', // lcov or lcovonly are required for generating lcov.info files
      dir: 'coverage/'
    }
  });
};
