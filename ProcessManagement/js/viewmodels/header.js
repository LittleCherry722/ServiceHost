define([
	"knockout",
	"app",
	"models/user",
	"jade!../../templates/header"
], function( ko, App, User, headerTemplate ) {

	// Our header viewmodel. Make this private and only export some methods as
	// public API so we stay in tighter controll of everything.
	var viewModel = {
		currentUser: App.currentUser()
	}

	var initialize = function() {
		headerNode = document.getElementById( 'header' )
		headerNode.innerHTML = headerTemplate();
		ko.applyBindings( viewModel, headerNode )
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});
