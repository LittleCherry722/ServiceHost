/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

define([ "director", "app"], function( Director, App ) {
	var router,
		_globalCallback,
		hasUnsavedChanges = false
		hasUnsavedChangesMessage = "There are unsaved changes which will be lost. Continue?";

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

	/**
	 * Sets the value that indicates that some opened document has or has not unsaved changes
	 * @param {boolean} value the value
	 */
	var setHasUnsavedChanges = function(value) {
		hasUnsavedChanges = value;

		if( true === value ) {
			window.onbeforeunload = function() { return hasUnsavedChangesMessage; };
		} else {
			window.onbeforeunload = null;
		}
	}

	/*
	 * Custom route actions go here. Keep it concise!
	 */

	var showProcess = function( processId, subjectId ) {
            
                if(hasUnsavedChanges && !confirm(hasUnsavedChangesMessage)) {
			return;
		}
		setHasUnsavedChanges(false);
                
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
	var showHome = function(tab) {
		if ( App.isViewLoaded( "home" ) ) {
			App.currentMainViewModel().setView(tab)
		} else {
			loadView( "home", [ tab ], globalCallback() );
		}
	}

	var showAccount = function() {
		App.loadView( "account", null, globalCallback() );
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

	var showProcessExecution = function( id, tab, subject ) {
		subjectId = subject ? subject.replace(/___/, " ") : undefined;

		if ( App.isViewLoaded( "execution" ) ) {
			App.currentMainViewModel().setView( id, tab, subjectId )
		} else {
			loadView( "execution", [ id, tab, subjectId ], globalCallback() );
		}
	}

	var showRouting = function( processId ) {
		loadView( "process/routing", processId );
	}

	var showMessages = function ( tab ) {
		if ( App.isViewLoaded( "messages" ) ) {
			App.currentMainViewModel().setView( tab )
		} else {
			App.loadView( "messages", [tab], globalCallback() );
		}
	}

	/*
	 *	Every possible route gets defined here
	 */
	var routes = {
		"/":  showHome,
		"/home":  {
			on: showHome,
			"/:tab": {
			 	on: showHome
			}
		},
		"/account": showAccount,
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
		"/processinstances": {
			"/:process/": {
				on: showProcessExecution,
				"/:tab": {
					on: showProcessExecution,
					":subject": showProcessExecution
				}
			}
		},
		"/messages": {
			on: showMessages,
			"/:tab": {
				on: showMessages
			}
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
		if(hasUnsavedChanges && !confirm(hasUnsavedChangesMessage)) {
			return;
		}
		setHasUnsavedChanges(false);
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
				route += "/" + fragment.toString().replace(/ /, "___").replace(/^#/, "");
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
		goTo: goTo,
		setHasUnsavedChanges: setHasUnsavedChanges
	}
});
