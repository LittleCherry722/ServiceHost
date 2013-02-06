define([ "director", "app"], function( Director, App ) {
	var router;

	var _globalCallback;

	var globalCallback = function( callback ) {
		var cb;

		if ( typeof callback === "function" ) {
			_globalCallback = callback;
			return callback;
		} else {
			cb = _globalCallback;
			_globalCallback = undefined;
			return cb;
		}
	}

	/*
	 * Custom route actions go here. Keep it concise!
	 */

	var showProcess = function( processId, subjectId ) {
		expandListOfProcesses();

		if ( subjectId ) {
			subjectId = subjectId.replace(/___/, " ");
		}

		if ( App.currentMainViewModel() && App.currentMainViewModel().loadProcessByIds ) {
			App.currentMainViewModel().loadProcessByIds( processId, subjectId, globalCallback() );
		} else {
			loadView( "process", [ processId, subjectId ], globalCallback() );
		}
	}

	// Show the home (index) page.
	var showHome = function() {
		App.loadView( "home", null, globalCallback() );
	}

	var showNewProcess = function() {
		expandListOfProcesses();
		loadView( "newProcess", null, globalCallback() );
	}

	var showProcessList = function() {
		expandListOfProcesses();
		loadView( "processList", null, globalCallback() );
	}

	var showAdministration = function( tab ) {
		if ( App.isViewLoaded( "administration" ) ) {
			App.currentMainViewModel().currentTab( tab )
		} else {
			loadView( "administration", tab, globalCallback() );
		}
	}

	var showProcessExecution = function( tab ) {
		if ( !tab ) {
			loadView( "processExecutionList", [ tab ], globalCallback() );
		} else {
			if ( App.isViewLoaded( "execution" ) ) {
				App.currentMainViewModel().currentTab( tab )
			} else {
				loadView( "execution", [ tab ], globalCallback() );
			}
		}
	}

	var showRouting = function( processId ) {
		loadView( "process/routing", processId );
	}

	/*
	 *	Every possible route gets defined here
	 */
	var routes = {
		"/":  showHome,
		"/home":  showHome,
		"/administration": {
			on: showAdministration,
			"/:tab" : showAdministration
		},
		"/processList":  showProcessList,
		"/processes": {
			"/new": showNewProcess,
			"/:process/": {
				on: showProcess,
				"/routing": showRouting,
				"/(.+)": showProcess
			}
		},
		"/execution": {
			on: showProcessExecution,
			"/:tab": showProcessExecution
		}
	}

	var expandListOfProcesses = function() {
		Menu = require("viewmodels/menu");
		Menu.expandListOfProcesses( true );
	}

	/*
	 * other private methods (helper methods etc.) go here
	 */

	// Load a custom viewmodel.
	// Path is always prepended with "/viewmodels"
	var loadView = function( viewName, args, callback ) {
		App.loadView( viewName, args, callback );
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

		path = "#/" + pluralize( modelName ) + "/" + model.id();
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
	var goTo = function( path, callback ) {
		var route;

		// check whether the given path is of type "object", that is most likely a
		// model. If this is the case, we assume "object" actually means that
		// "path" is a model and modelPath knows what to do with it.
		//
		// If not, we assume it is a string or another type of object that the
		// director library knows how to handle, so we supply it directly to our
		// Router.
		//
		// TODO: Could need a slight refactoring.
		if ( _( path ).isArray() ) {
			route = "";
			_( path ).each( function( fragment ) {
				if (!fragment) {
					return;
				}
				route += "/" + fragment.replace(/ /, "___").replace(/^#/, "");
			});
		} else if ( typeof path === "object" ) {
			route = modelPath( path );
		} else if ( path ) {
			route = path;
		} else {
			route = "/"
		}

		route = route.replace(/\/{2,}/, "/");
		if ( route[0] === "#" ) {
			route = route.substr(1);
		}

		if ( route === currentPath() ) {
			return false;
		}

		if ( typeof callback === "function" ) {
			globalCallback( callback );
		}

		router.setRoute( route );

		return true;
	}

	var currentPath = function() {
		return "/" + router.getRoute().join("/");
	}

	// Initialize our Router.
	// Applies the routes to the router and sets a default route.
	var initialize = function() {
		router = Director(routes);
		window.r = router;
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
