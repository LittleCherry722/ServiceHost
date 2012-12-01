define([
  "knockout",
  "models/user",
  "require"
], function( ko, User, require ) {
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
	}

	// The current user logged in to our system
	var currentUser = ko.observable( new User( "no user" ) );

	// The current ViewModel loaded for the "main" view
	var contentViewModel = ko.observable();

  // Load a new viewModel as our ContentViewModel.
  // Unloads the old model (calling "unload()" on it of available).
  // unloading can be aborted by returning "false" in the unload function.
	var loadView = function( viewName ) {
    if ( contentViewModel() ) {
      // check if the unload method is actually set
      if ( typeof contentViewModel().unload === 'function' ) {
        // call "unload" and exit early if it retunes a falsey value
        if ( !contentViewModel().unload() ) {
          return
        }
      }
    }

    // just load our new viewmodel if unload was successfull (truthy return
    // value) or was not defined at all.
		require([ "viewmodels/" + viewName ], function( viewModel ) {
      viewModel.init();
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
   * @param {String} type specifies the type of the template. Default is jade.
   * Supported template handlers are: jade, text. Optional.
   *
   * example: loadTemplate('home', new ViewModel(), 'text')
   */
  var loadTemplate = function( templateName, viewModel, type ) {
    if ( !type ) {
      type = "jade";
    }

    // create the path from:
    // * template handler (type). Must be a requirejs plugin.
    // * the default path to all templates.
    // * the template name.
    var path = type + "!../templates/" + templateName;

    // load the template from the server
    require([ path ], function( template ) {
      mainNode = document.getElementById("main");

      // If the type of the template is text, we get a simple string. We
      // would not want to call a string as a function. Every other template
      // handler should return a function to convert the template to a string
      // we can insert into our main node.
      if ( type === "text" ) {
        mainNode.innerHTML = template;
      } else {
        mainNode.innerHTML = template();
      }

      // Apply the viewModel to the newly inserted content if available
      if ( viewModel ) {
        ko.applyBindings(viewModel, mainNode)
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
