define([ "director", "app" ], function( Director, App ) {

	/*
	 * Custom route actions go here. Keep it concice!
	 */

	// Show the home (index) page.
	var showHome = function() {
		App.loadTemplate("home");
	}

	var showProcess = function(processName) {

	}

	/*
	 *	Every possible route gets defined here
	 */
	var routes = {
		"/":  showHome,
		"/home":  showHome,
		"/processes/:process": showProcess
	}


	/*
	 * other private methods (helper methods etc.) go here
	 */

	// Load a custom viewmodel.
	// Path is always prepended with "/viewmodels"
	var loadViewModel = function(model) {
		require([ "viewmodels/" + model ], function( viewModel ) {
			App.loadViewModel(viewModel);
		});
	}

	// given a process, creates a path for this process.
	// This path can be used for internal navigation only as it does not contain
	// host information, only the path.
	var pathForProcess = function(process) {
		// prepend our baisc processes route and append the process name with
		// whitespace converted to underscores
		path = "#/processes/" + process.name.replace(/ /g, "_")
		return path;
	}

	// Initialize our Router.
	// Applies the routes to the router and sets a default route.
	var initialize = function() {
		var router = Director(routes);
		// Set our default route to "/" (if no /#/ could be found)
		router.init("/");
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});
