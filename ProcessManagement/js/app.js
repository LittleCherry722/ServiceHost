define([
  "knockout",
  "require",
	"underscore",
	"async"
], function( ko, require, _, async ) {
	var currentUser = ko.observable();

	var _loadedView = "";

	var initializeViews = function( callback ) {
		// initialize our header. We have to do this asynchronously
		// since our header and menu ViewModel also require this "App" module
		require([
			"viewmodels/header",
			"viewmodels/menu"
		], function( HeaderViewModel, MenuViewModel ) {
			HeaderViewModel.init();
			MenuViewModel.init();

			callback();
		});
	}

	var initialize = function( callback ) {
		require([
			"model",
			"models/user",
			"models/process",
			"models/graph",
			"models/group",
			"models/role",
			"models/groupsUsers",
			"models/groupsRoles"
			// "models/roles",
		], function( Model, User, Process, Graph, Group, Role ) {

			// The current user logged in to our system
			currentUser( new User( { name: "no user" } ) );


			// Initially fetch all Models, then initialize the views and after that,
			// tell everyone that we are done (call the callback).
			async.auto({
				// fetchAll: Model.fetchAll,
				fetchAll: function( callback ) {
					User.fetch();
					Role.fetch();
					Group.fetch();
					GroupsUsers.fetch();
					// GrupsRoles.fetch();
					callback();
				},
				initViews: [ "fetchAll", initializeViews ],
				callback: [ "initViews", callback ]
			});

		});
	}


	// The current ViewModel loaded for the "main" view
	var currentMainViewModel = ko.observable();


	// Does exactly what "loadView" does but does not set the current main
	// ViewModel and does not unload the old viewModel.
	var loadSubView = function( viewName, args, callback ) {
    // just load our new viewmodel and call the init method.
		require([ "viewmodels/" + viewName ], function( viewModel ) {
      viewModel.init( callback );
		});
	}

  // Load a new viewModel as our ContentViewModel.
  // Unloads the old model (calling "unload()" on it of available).
  // unloading can be aborted by returning "false" in the unload function.
	var loadView = function( viewName, args, callback ) {
		if ( !_.isArray(args) ) {
			args = [ args ];
		}
		args.push( callback )

		if ( !unloadViewModel() ) {
			return;
		}

    // just load our new viewmodel and call the init method.
		require([ "viewmodels/" + viewName ], function( viewModel ) {
			viewModel.init.apply(viewModel, args );
			currentMainViewModel( viewModel );
		});

		_loadedView = viewName;
	}

	var loadedView = function() {
		if ( !currentMainViewModel() ) {
			return "";
		}

		return _loadedView;
	}

	var isViewLoaded = function( viewName ) {
		return loadedView() === viewName;
	}

	var unloadViewModel = function() {
		if ( currentMainViewModel() ) {
			// check if the unload method is actually set
			if ( typeof currentMainViewModel().unload === 'function' ) {
				// call "unload" and exit early if it retunes a falsey value
				if ( !currentMainViewModel().unload() ) {
					return false;
				}
			}
		}

		return true;
	}

	var viewCanUnload = function() {
		if ( currentMainViewModel() ) {

			console.log("viewmodel exists")
			// check if the unload method is actually set
			if ( typeof currentMainViewModel().canUnload === 'function' ) {
				console.log("calling specific canUnload")
				return currentMainViewModel().canUnload();
			}
		}

		return true;
	}

	/**
	 * loads a simple template as the main content of the site.
	 * Very usefull to display static content that is saved inside a template
	 * and is not associated to a ViewModel.
	 *
	 * @param {String} templateName specifiec the template to be loaded.
	 *  Templates have to be in the "/templates" root path.
	 *
	 * @param {ViewModel} viewModel the viewModel to be applied to the new content.
	 *  Optional.
	 *
	 * @param {String} nodeId the id of the element whose content (innerHTML) is
	 *  to be replaced by the template. Defaults to "main".
	 *
	 * @param {Function} callback the function to be executed after the template
	 *  has been loaded and the viewModel (if supplied) has been applied.
	 *
	 * example: loadTemplate('home', new ViewModel(), 'text')
		 */
	var loadTemplate = function( templateName, viewModel, nodeId, callback ) {
		var path;

		// Set the defaults for nodeId
		if ( !nodeId ) {
			nodeId = "main"
		}

		// create the path from:
		// * template handler (type). Must be a requirejs plugin.
		// * the default path to all templates.
		// * the template name.
		path = "text!../templates/" + templateName + ".html";

		// load the template from the server
		require([ path ], function( template ) {
			templateNode = document.getElementById( nodeId );

			templateNode.innerHTML = template;

			// Apply the viewModel to the newly inserted content if available.
			if ( viewModel ) {
				ko.applyBindings( viewModel, templateNode )
			}

			// if a callback is set, execute it.
			if ( typeof callback === "function" ) {
				callback();
			}
		});
	}

	/**
	 * asynchronously load an array of templates.
	 * works mostly like "loadTemplate" but only accepts one viewmodel.
	 * The callback will be called when all templates have successfully been
	 * applied.
	 *
	 * @param {array} templates - The array of templates. This array is itself an array of
	 *   [ templateName, templateNode ]. Example:
	 *     [ "process/subject", "tab2_content" ],
	 *     [ "process/internal", "tab1_content" ]
	 * @param {ViewModel} viewModel - the viewmodel to be applied to every
	 *   template. Can be empty.
	 * @param {function} callback - The callback to execute once all templates
	 *   have been lodaded.
	 */
	var loadTemplates = function( templates, viewModel, callback ) {
		var templateName, templateNode;

		// for every array in our array of templates, apply a function that
		// calls loadTemplate with the appropiate params and execute the
		// callback needed for async.js once the template has successfully been
		// applied.
		async.map( templates , function( template, cb ) {

			// First array entry must be the template name, second one the nodeId this
			// template should be inseted into
			templateName = template[0];
			templateNode = template[1];

			loadTemplate( templateName, viewModel, templateNode, cb);
		}, callback);
	}


	// Everything in this object will be the public API
	return {
		init: initialize,
		currentUser: currentUser,
		loadTemplate: loadTemplate,
		loadTemplates: loadTemplates,
		currentMainViewModel: currentMainViewModel,
		loadView: loadView,
		isViewLoaded: isViewLoaded,
		loadSubView: loadSubView,
		viewCanUnload: viewCanUnload,
		unloadViewModel: unloadViewModel
	}
});
