define([
	"app"
], function( App ) {

	var ViewModel = function() {
	}

	var initialize = function() {
		var viewModel = new ViewModel();

		App.loadTemplate( "messages", viewModel );
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


