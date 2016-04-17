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
