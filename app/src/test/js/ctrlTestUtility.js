function loadTest(scope, response) {
    expect(scope.entities || scope.searchKeys).toEqual(response.data.tree);
    expect(scope.parent).toEqual(response.data.tree);
}

function crumbTest(scope, response) {
    expect(scope.crumbs.length).toEqual(1);
    expect(scope.crumbs[0]).toEqual(response.data.tree);
    expect(scope.parent).toEqual(response.data.tree);

    scope.gotoCrumb(scope.crumbs[0]);
    expect(scope.crumbs.length).toEqual(1);
    expect(scope.crumbs[0]).toEqual(response.data.tree);
    expect(scope.parent).toEqual(response.data.tree);
}

function manageChildrenTest(scope, response) {
    expect(scope.crumbs.length).toEqual(1);
    expect(scope.crumbs[0]).toEqual(response.data.tree);
    expect(scope.parent).toEqual(response.data.tree);
    scope.manageChildren(scope.parent.children[0]);

    expect(scope.crumbs.length).toEqual(2);
    expect(scope.crumbs[0]).toEqual(response.data.tree);
    expect(scope.crumbs[1]).toEqual(response.data.tree.children[0]);
    expect(scope.parent).toEqual(response.data.tree.children[0]);

    scope.manageChildren(scope.parent.children[0]);
    expect(scope.crumbs.length).toEqual(3);
    expect(scope.crumbs[0]).toEqual(response.data.tree);
    expect(scope.crumbs[1]).toEqual(response.data.tree.children[0]);
    expect(scope.crumbs[2]).toEqual(response.data.tree.children[0].children[0]);
    expect(scope.parent).toEqual(response.data.tree.children[0].children[0]);
}

function addTest(rootScope, scope, response, name, type, value) {
    expect(scope.addMode).toEqual(false);
    expect(scope.crumbs.length).toEqual(1);
    scope.addChild(scope.parent);
    expect(scope.addMode).toEqual(true);
    expect(scope.crumbs.length).toEqual(2);
    scope.addFormName = name;
    scope.addFormValue = value;
    spyOn(mockService, "addKey").and.returnValue($q.when(response));
    scope.requestAdd();
    rootScope.$apply();
    expect(scope.parent).toEqual(response.data.result);
    expect(scope.entities || scope.searchKeys).toEqual(response.data.tree);
}

function updateTests(rootScope, scope, response, name, type, value) {
    expect(scope.updateMode).toEqual(false);
    expect(scope.crumbs.length).toEqual(1);
    scope.editChild(scope.parent.children[0]);
    expect(scope.updateMode).toEqual(true);
    expect(scope.crumbs.length).toEqual(2);
    expect(scope.parent).toEqual(response.data.tree.children[0]);
    scope.parent.name = name;
    scope.parent.value = value;
    spyOn(mockService, "updateKey").and.returnValue($q.when(response));
    scope.requestUpdate();
    rootScope.$apply();
    expect(scope.parent).toEqual(response.data.result);
    expect(scope.entities || scope.searchKeys).toEqual(response.data.tree);
}

function deleteTests(rootScope, scope, response) {
    expect(scope.entities || scope.searchKeys).toEqual(response.data.tree);
    expect(scope.parent).toEqual(response.data.tree);
    spyOn(mockService, "deleteKey").and.returnValue($q.when(response));
    scope.deleteChild(response.data.tree.children[0]);
    rootScope.$apply();
    expect(scope.parent).toEqual(response.data.result);
    expect(scope.entities || scope.searchKeys).toEqual(response.data.tree);
}
