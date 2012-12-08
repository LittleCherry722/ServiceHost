define([
  "knockout",
  "require",
	"underscore"
], function( ko, require, _ ) {
	var currentUser = ko.observable();

	var initialize = function() {
		// initialize our header. We have to do this asynchronously
    // since our header and menu ViewModel also require this "App" module
    require([
      "viewmodels/header",
      "viewmodels/menu"
    ], function( HeaderViewModel, MenuViewModel ) {
      HeaderViewModel.init();
      MenuViewModel.init();
    });

		require([
			"models/user",
			"models/process"
		], function( User, Process ) {

			// The current user logged in to our system
			currentUser( new User( "no user" ) );

			// Initially fetch all Models
			Process.fetch();
		})
	}


	// The current ViewModel loaded for the "main" view
	var contentViewModel = ko.observable();


	// Does exactly what "loadView" does but does not set the contentViewModel
	// and does not unload the old viewModel.
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

    if ( contentViewModel() ) {
      // check if the unload method is actually set
      if ( typeof contentViewModel().unload === 'function' ) {
        // call "unload" and exit early if it retunes a falsey value
        if ( !contentViewModel().unload() ) {
          return
        }
      }
    }

		

    // just load our new viewmodel and call the init method.
		require([ "viewmodels/" + viewName ], function( viewModel ) {
      viewModel.init.apply(viewModel, args );
      contentViewModel( viewModel );
		});
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
   * @param {String} nodeID the id of the element whose content (innerHTML) is
   *  to be replaced by the template. Defaults to "main".
   *
   * @param {Function} callback the function to be executed after the template
   *  has been loaded and the viewModel (if supplied) has been applied.
   *
   * example: loadTemplate('home', new ViewModel(), 'text')
   */
  var loadTemplate = function( templateName, viewModel, nodeID, callback ) {
    var path;

    // Set the defaults for nodeID
    if ( !nodeID ) {
      nodeID = "main"
    }

    // create the path from:
    // * template handler (type). Must be a requirejs plugin.
    // * the default path to all templates.
    // * the template name.
    path = "text!../templates/" + templateName + ".html";

    // load the template from the server
    require([ path ], function( template ) {
      templateNode = document.getElementById(nodeID);

      // load our template and insert it into the document.
      templateNode.innerHTML = template;

      // Apply the viewModel to the newly inserted content if available.
      if ( viewModel ) {
        ko.applyBindings(viewModel, templateNode)
      }

      // if a callback is set, execute it.
      if ( typeof callback === "function" ) {
        callback();
      }
    });
  }

	// Everything in this object will be the public API
	return {
		init: initialize,
		currentUser: currentUser,
    loadTemplate: loadTemplate,
    loadView: loadView
	}
});
