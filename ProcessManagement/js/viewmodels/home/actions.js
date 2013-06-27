define([
	"knockout",
	"app",
	"underscore",
	"models/actions",
        "models/process"
], function( ko, App, _, Actions, Process ) {

	var ViewModel = function() {
            //this.actions = ko.observableArray(Actions.all());
            
            this.actions = ko.computed(function() {return Actions.all()});                  
            this.filterProcess = ko.observable(1);
            this.filterUser = ko.observable(1);
	};

	var initialize = function() {
            var viewModel = new ViewModel();
            
            App.loadTemplate( "home/actions", viewModel, "executionContent", function() { });   
            Actions.fetch();
	};
        
	// Everything in this object will be the public API
	return {
		init: initialize
	};
});


