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

		this.currentUser = App.currentUser;

		this.oauth2callback = oauthLogin;
	}

	var oauthLogin = function() {
		if( App.currentUser().id() === 0 ) {
			Notify.error( "Error", "You have to be logged in to link you account with google." );
			return;
		}

		var data = {
			id: App.currentUser().id()
		}

		$.ajax({
			url: "/oauth2callback/init_auth",
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
