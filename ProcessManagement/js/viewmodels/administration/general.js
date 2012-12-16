define([
	"knockout",
	"app",
	"underscore",
	"notify",
	"model"
], function( ko, App, _, Notify, Model ) {

	var ViewModel = function() {

		this.save = function() {
			var success = SBPM.Service.Configuration.write(ko.toJS(this.data()));

			this.initialized = false;

			return success;
		}

		this.dataForUI = function() {// TODO return actual data
			return [];
		}
	}

	var initialize = function() {
		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "administration/debug", viewModel, "right_content", null );
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});

