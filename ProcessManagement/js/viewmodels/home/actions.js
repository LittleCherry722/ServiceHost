define([
	"knockout",
	"app",
	"underscore",
	"models/actions",
        "models/process"
], function( ko, App, _, Actions, Process ) {

	var ViewModel = function() {
            self.actions = Actions.all;
            
            this.filterProcess = ko.observable(1);
            this.filterUser = ko.observable(1);
	}

	var initialize = function( ) {
            var viewModel = new ViewModel();
            
            App.loadTemplate( "home/actions", viewModel, "executionContent", function() { });            
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


