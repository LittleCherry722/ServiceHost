define([
	"knockout",
	"app",
	"models/user",
	"text!../../templates/header.html"
], function( ko, App, User, headerTemplate ) {

	// Our header viewmodel. Make this private and only export some methods as
	// public API so we stay in tighter controll of everything.
	
	var ViewModel = function() {
		currentUser = App.currentUser;
		this.logout = logout;
		
	}
	
	var logout = function() {
		console.log("logout");

		$.ajax({
			url : '/user/logout',
			type : "POST",

			async : true, // defaults to false

			success : function(data, textStatus, jqXHR) {
				window.location = "./login.html";

			},
			error : function(jqXHR, textStatus, error) {
				console.log("Error")
				console.log(error)
			},
			complete : function(jqXHR, textStatus) {
				console.log("complete")

			}
		});

	};


	var initialize = function() {
		headerNode = document.getElementById( 'header' )
		headerNode.innerHTML = headerTemplate;
		
		viewModel = new ViewModel();
		
		ko.applyBindings( viewModel, headerNode )
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});
