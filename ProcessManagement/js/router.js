define([ "director", "app" ], function( Director, App ) {
	var router;

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
	// Example:
	// var process = new Model("Process");	// => { className: "process", id: 3 }
	// modelPath(process) =									// => #/process/3
	var modelPath = function( model ) {
		var modelName = model.className.toLowerCase();
		path = "#/" + pluralize( modelName ) + "/" + model.id
		return path;
	}

	// Pluralises a given string.
	// Works only for (most) english words that have a regular plural form.
	// Examples: "tree" => "trees", "process" => "processes
	var pluralize = function( str ) {
		var lastChar = str[str.length - 1];
		if ( lastChar === "s" ) {
			return str + "es"
		} else {
			return str + "s"
		}
	}

	// Go to the specified page.
	// This changes the hash fragment URL in the browser.
	//
	// The supplied argument can either be of type "object" and has to be a
	// "Model" or another object that can be handled by the "modelPath" method
	// or is a string that director can handle.
	//
	// Example:
	// goTo( (new Process()).save );
	// goTo( "/processes/new" );
	var goTo = function( path ) {
		var route;

		// check whether the given path is of type "object", that is most likely a
		// model. If this is the case, we assume "object" actually means that
		// "path" is a model and modelPath knows what to do with it.
		//
		// If not, we assume it is a string or another type of object that the
		// director library knows how to handle, so we supply it directly to our
		// Router.
		if ( typeof path === "object" ) {
			route = modelPath( path );
		} else  {
			route = path;
		}

		router.setRoute( route );
	}

	// Initialize our Router.
	// Applies the routes to the router and sets a default route.
	var initialize = function() {
		router = Director(routes);
		// Set our default route to "/" (if no /#/ could be found)
		router.init("/");
	}

	// Everything in this object will be the public API
	return {
		init: initialize,
		modelPath: modelPath,
		goTo: goTo
	}
});
