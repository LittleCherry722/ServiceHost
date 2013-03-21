define([
	"knockout",
	"app",
	"models/user",
	"text!../../templates/header.html",
	"notify"
], function( ko, App, User, headerTemplate, Notify ) {

	// Our header viewmodel. Make this private and only export some methods as
	// public API so we stay in tighter controll of everything.
	
	var ViewModel = function() {
		currentUser = App.currentUser;
		this.logout = logout;

		this.oauth2callback = function() {
			var data = {
				id: 3
			}

			$.ajax({
				url: "oauth2callback/init_auth",
				cache: false,
				data: data,
				type: "POST",
				success: function( data ) {
					if ( !data ) {
						Notify.info( "Success", "User account is already linked with Google." );
					} else {
						window.open( data, "Google OAuth 2", "width=600,height=400" );
					}
				},
				error: function() {
					Notify.error( "Error", "Error while linking user account with Google." );
				}
			});
		}
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
