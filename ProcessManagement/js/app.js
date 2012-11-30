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
	var loadViewModel = function( viewModel ) {
    // check if the unload method is actually set
		if ( typeof contentViewModel.unload === 'function' ) {
      // call "unload" and exit early if it retunes a falsey value
			if ( !contentViewModel.unload() ) {
				return
			}
		}

    // just load our new viewmodel if unload was successfull (truethy return
    // value) or was not defined at all.
		cuntentViewModel( viewModel );
	}

	// Everything in this object will be the public API
	return {
		init: initialize,
		currentUser: currentUser
	}
});
