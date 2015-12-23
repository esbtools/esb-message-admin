esbMessageAdminApp.controller('ErrorDetailsCtrl', [
    '$scope',
    '$rootScope',
    'EsbMessageService',
    function($scope, $rootScope, EsbMessageService) {
        // on message select, fetch message details
        $rootScope.$watch('selectedMessage',
            function() {
                if ($scope.selectedMessage) {
                    EsbMessageService.getMessage($scope.selectedMessage.id).then(
                        function(result) {
                            $scope.message = result.data.messages[0];
                            if (result.data.messages[0].payload) {
                                var payload = result.data.messages[0].payload;
                                var formatted = '';
                                var reg = /(>)\s(<)(\/*)/g;
                                payload = payload.replace(reg, '$1\r\n$2$3');
                                var pad = 0;
                                jQuery.each(payload.split('\r\n'), function(index, node) {
                                    var indent = 0;
                                    if (node.match(/.+<\/\w[^>]*>$/)) {
                                        indent = 0;
                                    } else if (node.match(/^<\/\w/)) {
                                        if (pad != 0) {
                                            pad -= 1;
                                        }
                                    } else if (node.match(/^<\w[^>]*[^\/]>.*$/)) {
                                        indent = 1;
                                    } else {
                                        indent = 0;
                                    }

                                    var padding = '';
                                    for (var i = 0; i < pad; i++) {
                                        padding += '  ';
                                    }

                                    formatted += padding + node + '\r\n';
                                    pad += indent;
                                });
                                $scope.message.payload = formatted;
                            }
                        }
                    );
                } else {
                    $scope.message = null;
                }
            }
        );

        $scope.resubmitMessage = function() {
            alert("Not implemented yet");
        };
    }
]);
