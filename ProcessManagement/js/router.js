define([ "director" ], function( Director ) {
	var routes, showHome, initialize;

	// Every route and action is defined in this object
	routes = {
		'/': showHome
	}

	// Custom route actions go here. Keep it concice!
	showHome = function() {

	}

	initialize = function() {
		Director(routes).init();
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});
