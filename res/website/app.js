var outcastApp = angular.module('OutcastApp', []);

outcastApp.controller('MasterCtrl', function ($scope) {
    $scope.addNewFeed = function() {
        $("#newFeedDiv").toggle(true);
    };
});

outcastApp.controller('NewFeedCtrl', function ($scope) {
    
});

outcastApp.controller('RssFeedCtrl', function ($scope) {
    $scope.feeds = [{title: "Test", location: "www.google.com"}];
});
