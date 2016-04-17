var outcastApp = angular.module('OutcastApp', []);

// URL regex expression taken from https://mathiasbynens.be/demo/url-regex. @diegoperini's version
var urlExpression = /^(?:(?:https?|ftp):\/\/)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,}))\.?)(?::\d{2,5})?(?:[/?#]\S*)?$/i;

outcastApp.directive('url', function() {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            ctrl.$validators.url = function(modelValue, viewValue) {
                return urlExpression.test(viewValue);
            };
        }
    };
});

outcastApp.controller('MasterCtrl', function ($scope) {

});

outcastApp.controller('NewFeedCtrl', function ($rootScope, $scope, $http) {
    $scope.feed = '';
    $scope.submitNewFeed = function() {
        if ($scope.feed) {
            $http.get("/feeds/add/location=" + $scope.feed)
                .then(function successCallback(response) {
                    $rootScope.$emit("UpdateFeeds", {});
                }, function errorCallback(response) {
                    console.error(response.data);
                });
        }
    }
});

outcastApp.controller('RssFeedCtrl', function ($rootScope, $scope, $http) {
    $rootScope.$on("UpdateFeeds", function() {
        getFeeds();
    });
    $scope.feeds = [{title: "", url: "", date: ""}];

    function getFeeds() {
        $http.get("/feeds").then(function successCallback(response) {
            $scope.feeds = response.data;
        }, function errorCallback(response) {
            // called asynchronously if an error occurs
            // or server returns response with an error status.
        });
    }

    getFeeds();
});
