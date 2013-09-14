define([
	"knockout",
	"app",
	"underscore",
	"text!api_description.json"
], function( ko, App, _, apiDescription ) {


	var ViewModel = function() {
		var that = this;

		this.selectedMethod = ko.observable();

		this.apis = JSON.parse(apiDescription);

		this.methodSelected = function(method){
			that.selectedMethod(method);
		}

	};


	var initialize = function() {
		var viewModel = new ViewModel();

		App.loadTemplate( "console", viewModel, null, function() {
			//nothing here
		});
	};


	return {
		init: initialize
	}
});
