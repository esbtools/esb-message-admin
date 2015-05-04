describe(
        'SyncCtrl',
        function() {

            // Instantiate a new version of my module before each test
            beforeEach(module('esbMessageAdminApp', function($provide) {
                $provide.value("EsbMessageService", mockService);
            }));

            var syncCtrl, scope, rootScope;

            beforeEach(inject(function($rootScope, $controller, _$q_) {
                rootScope = $rootScope;
                scope = $rootScope.$new();
                $q = _$q_;
                syncCtrl = $controller('SyncCtrl', {
                    '$scope' : scope
                });

                deferred.resolve(entitiesSuccessResponse);
                $rootScope.$apply();
            }));

            it('loads Sync Tab', function() {
                expect(scope.entities).toEqual(
                        entitiesSuccessResponse.data.tree);
            });

            it(
                    'selects key',
                    function() {

                        scope.syncEntity = entitiesSuccessResponse.data.tree.children[0];
                        expect(scope.systems.length).toEqual(0);
                        scope.entityChange();
                        expect(scope.systems)
                                .toEqual(
                                        entitiesSuccessResponse.data.tree.children[0].children);
                        scope.syncSystem = scope.systems[0];
                        expect(scope.keys.length).toEqual(0);
                        scope.systemChange();
                        expect(scope.keys)
                                .toEqual(
                                        entitiesSuccessResponse.data.tree.children[0].children[0].children);

                    });

            it(
                    'enable submit button tests',
                    function() {

                        scope.syncEntity = entitiesSuccessResponse.data.tree.children[0];
                        scope.syncSystem = scope.syncEntity.children[0];
                        scope.syncKey = scope.syncSystem.children[0];
                        expect(scope.disableSubmit()).toEqual(true);
                        scope.syncValues = [ "12" ];
                        expect(scope.disableSubmit()).toEqual(false);

                    });

            it(
                    'sync trigger tests',
                    function() {

                        scope.syncEntity = entitiesSuccessResponse.data.tree.children[0];
                        scope.syncSystem = scope.syncEntity.children[0];
                        scope.syncKey = scope.syncSystem.children[0];
                        scope.syncValues = [ "12", "21" ];
                        spyOn(mockService, 'sync').and.callThrough();
                        scope.sync();
                        deferred.resolve(syncSuccessResponse);
                        rootScope.$apply();
                        expect(mockService.sync).toHaveBeenCalledWith(
                                scope.syncEntity.value, scope.syncSystem.value,
                                scope.syncKey.value, '12,21,');
                    });

        });