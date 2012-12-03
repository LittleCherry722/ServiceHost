define([ "director", "app" ], function( Director, App ) {

	/*
	 * Custom route actions go here. Keep it concice!
	 */

	// Show the home (index) page.
	var showHome = function() {
		App.loadTemplate("home");
	}

	var showProcess = function(processName) {
		console.log("loading process");
	}

	var showNewProcess = function() {
		loadView("newProcess");
	}

	/*
	 *	Every possible route gets defined here
	 */
	var routes = {
		"/":  showHome,
		"/home":  showHome,
		"/processes": {
			"/new": showNewProcess,
			"/:process": showProcess
		}
	}


	/*
	 * other private methods (helper methods etc.) go here
	 */

	// Load a custom viewmodel.
	// Path is always prepended with "/viewmodels"
	var loadView = function(viewName) {
		App.loadView(viewName);
	}

	// given a model, creates a path for this model.
	// This path can be used for internal navigation only as it does not contain
	// host information, only the path.
	//
	// Model path is generated from the model name (className) in lowercase,
	// pluralized (by simply appending an s).
	//
	// example:
	// var process = new Model("Process");	// => { className: "process", id: 3 }
	// modelPath(process) =									// => #/process/3
	var modelPath = function(model) {
		var modelName = model.className.toLowerCase();
		path = "#/" + modelName + "/" + model.id
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
		init: initialize,
		modelPath: modelPath
	}
});
