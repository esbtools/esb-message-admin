var baseConfig = require('./karma.conf.headless.js');
module.exports = function(config) {
    // Load base config
    baseConfig(config);
    // Override base config
    config.set({
        singleRun : false,
        autoWatch : true,
        browsers : [ 'Chrome', 'Firefox' ]
    });
};