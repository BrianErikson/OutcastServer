var outcastApp = angular.module('OutcastApp', []);

outcastApp.controller('MasterCtrl', function ($scope) {
    $scope.toggleNewFeed = function() {
        $("#newFeedDiv").toggle(true);
    };
});

outcastApp.controller('NewFeedCtrl', function ($scope) {
    $scope.feed = {title: '', location: ''};
    $scope.submitNewFeed = function() {
        if ($scope.feed.title && $scope.feed.location) {
            
        }
        else {

        }
    }
});

outcastApp.controller('RssFeedCtrl', function ($scope, $http) {
    $scope.feeds = [{title: "", location: ""}];
    $http.get("/feeds").then(function successCallback(response) {
        $scope.feeds = response.data;
    }, function errorCallback(response) {
        // called asynchronously if an error occurs
        // or server returns response with an error status.
    });
});
