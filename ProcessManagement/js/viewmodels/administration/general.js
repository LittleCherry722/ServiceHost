define([
	"knockout",
	"app",
	"underscore",
	"notify",
	"model"
], function( ko, App, _, Notify, Model ) {

	// TODO Actually do something here
	var ViewModel = function() {

		this.save = function() {
			console.log("Nothing to be done really...")
		}

		this.dataForUI = function() {

			return [];
		}
	}

	var initialize = function() {
		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "administration/general", viewModel, "right_content", null );
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});

