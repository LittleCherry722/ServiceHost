define([
	"knockout",
	"app",
	"underscore"
], function( ko, App, _ ) {

	// Just a stub at the moment.
	// This viewmodel does not do anything but loading a template.
	// Will be used in the feature. At the moment its just to ensure unloading
	// of other views when switching to the home view.
	var ViewModel = function() {
	}

	var initialize = function( subSite ) {
		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "home", viewModel, null, function() {
			// Maybe do something?
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


