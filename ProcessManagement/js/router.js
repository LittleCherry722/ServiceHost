define([ "director" ], function( Director ) {

	// Every route and action is defined in this object
	routes = {
		'/': showHome
	}

	// Custom route actions go here. Keep it concice!
	showHome = function() {

	}

	// Everything here will is private
	initialize = function() {
		Director(routes).init();
	}

	// Everything in this object will be the public API
	return function() {
		init: initialize
	}
});
