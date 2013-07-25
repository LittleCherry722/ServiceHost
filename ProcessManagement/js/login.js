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

var LoginViewModel = function() {
	var self = this,
		tryUrlLogin,
		loginUserFn;

	/**
	 * Checks if the query parameters contain 'user' and 'pass' values. If true, the user will be logged in using
	 * those values.
	 */
	tryUrlLogin = function(){
		var queryParams = {},
			queryParamsRegex = /\??(?:([^&=]*)=([^&=]*)&?)/g,
			matches;

		while (matches = queryParamsRegex.exec(window.location.search)){
			queryParams[decodeURIComponent(matches[1])] = decodeURIComponent(matches[2]);
		}

		if(queryParams.hasOwnProperty('user') && queryParams.hasOwnProperty('pass')){
			loginUserFn(queryParams['user'], queryParams['pass'], queryParams['target']);
		}
	};

	/**
	 * Tries to login a user
	 * @param {string} user email of the user
	 * @param {string} pass password of the user
	 */
	loginUserFn = function(user, pass, target){
		$.get( '/isalive' ).done( function() {
			$.post('/user/login', {
				user: user,
				pass: pass
			}).done( function() {
				if(target && target!=null) {
					window.location = "./#/"+target;
				} else {
					window.location = "./#/";
				}
				
			}).fail( function() {
				alert( "E-Mail or Password wrong, please try again." );
			});

		}).fail(function() {
			alert( "Can not reach backend!" );
		});
	};

	self.user = ko.observable("");
	self.pass = ko.observable("");

	self.login = function() {
		loginUserFn( self.user(), self.pass(), null );
		self.pass( "" );
	};

	tryUrlLogin();
};

ko.applyBindings(new LoginViewModel());
