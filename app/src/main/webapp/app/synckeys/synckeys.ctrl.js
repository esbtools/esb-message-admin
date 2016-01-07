esbMessageAdminApp.controller('SyncKeysCtrl', [
  '$scope',
  'EsbMessageService',
  function($scope, EsbMessageService) {

    $scope.entities = {
      "id": 0,
      "name": "Entities",
      "type": "Entities",
      "value": "entities",
      "children": []
    };

    EsbMessageService.getSyncKeysTree().then(
      function(response) {
        $scope.parent = response.data.tree || $scope.parent;
        $scope.entities = response.data.tree || $scope.entities;
        $scope.crumbs = [$scope.entities];
      }
    );

    $scope.crumbs = [$scope.entities];

    $scope.parent = $scope.entities;

    $scope.addMode = false;
    $scope.updateMode = false;

    $scope.cantHaveChild = function(field) {
      if (field.type === "SyncKey") {
        return true;
      }
    };

    $scope.addChild = function(parent) {
      $scope.addMode = true;
      $scope.parent = parent;
      var newCrumb = {
        "name": "Add new " + $scope.getNextType(parent.type)
      };
      $scope.crumbs.push(newCrumb);
    };

    $scope.requestAdd = function() {
      var type;
      switch ($scope.parent.type) {
        case "Entities":
          type = "Entity";
          break;
        case "Entity":
          type = "System";
          break;
        case "System":
          type = "SyncKey";
          break;
      }

      EsbMessageService.addKey(
        $scope.parent.id,
        $scope.addFormName, type,
        $scope.addFormValue
      ).then(
        function(response) {
          $scope.entities = response.data.tree || $scope.entities;
          $scope.parent = response.data.result || $scope.parent;
        }
      );
      $scope.addFormName = "";
      $scope.addFormValue = "";
      $scope.addMode = false;
      $scope.crumbs.pop();
    };

    $scope.editChild = function(field) {
      $scope.updateMode = true;
      $scope.parent = field;
      $scope.crumbs.push(field);
    };

    $scope.requestUpdate = function() {
      EsbMessageService
        .updateKey(
          $scope.parent.id,
          $scope.parent.name,
          $scope.parent.type,
          $scope.parent.value
        )
        .then(
          function(response) {
            $scope.entities = response.data.tree || $scope.entities;
            $scope.parent = response.data.result || $scope.parent;
          }
        );
      $scope.crumbs.pop();
      $scope.updateMode = false;
    };

    $scope.deleteChild = function(field) {
      EsbMessageService
        .deleteKey(field.id)
        .then(
          function(response) {
            $scope.entities = response.data.tree || $scope.entities;
            $scope.parent = response.data.result || $scope.parent;
          });
    };

    $scope.gotoCrumb = function(crumb) {
      $scope.addMode = false;
      $scope.updateMode = false;

      if (crumb.type === "Entities") {
        $scope.parent = $scope.entities;
      } else {
        var currChildren = $scope.entities.children;
        for (i = 1; i < $scope.crumbs.length; i++) {
          for (j = 0; j < currChildren.length; j++) {
            if (crumb.id == currChildren[j].id) {
              $scope.parent = currChildren[j];
              // exit all loops
              j = currChildren.length + 1;
              i = $scope.crumbs.length + 1;
            } else if ($scope.crumbs[i].id === currChildren[j].id) {
              currChildren = currChildren[j].children;
              // exit inner loop
              j = currChildren.length + 1;
            }
          }
        }
      }
      if (crumb.type === "Entities") {
        $scope.crumbs = $scope.crumbs.slice(0, 1);
      } else if (crumb.type === "Entity") {
        $scope.crumbs = $scope.crumbs.slice(0, 2);
      } else if (crumb.type === "System") {
        $scope.crumbs = $scope.crumbs.slice(0, 3);
      }
    };

    $scope.manageChildren = function(field) {
      $scope.parent = field;
      $scope.crumbs.push(field);
    };

    $scope.getNextType = function(currType, grandChild) {
      if (currType === "Entities") {
        if (grandChild) {
          return "systems";
        } else {
          return "entity";
        }
      } else if (currType === "Entity") {
        if (grandChild) {
          return "keys";
        } else {
          return "system";
        }
      } else if (currType === "System") {
        if (!grandChild) {
          return "key";
        }
      }
    };
  }
]);
