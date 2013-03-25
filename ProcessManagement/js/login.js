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
	//window.Lview = this;

	self = this;
	self.user = ko.observable("");
	self.pass = ko.observable("");



	self.isBackendAlive = function() {
		$.ajax({
			url : '/isalive',
			type : "GET",
			async : false, // defaults to false
			success : function(data, textStatus, jqXHR) {
				return true
			},
			error : function(jqXHR, textStatus, error) {
				return false
			},
			complete : function(jqXHR, textStatus) {
			}
		});
	}; 





	self.login = function() {
		
		if (!isBackendAlive ){
			alert("Can not reach backend!");
		}
		
		var data = { user: self.user(), pass: self.pass()};
		data = JSON.stringify(data); 
		self.pass("");

		$.ajax({
			url : '/user/login',
			type : "POST",
			data : data,
			async : true, // defaults to false
			dataType : "json",
			contentType : "application/json; charset=UTF-8",
			success : function(data, textStatus, jqXHR) {
				window.location = "./#/";

			},
			error : function(jqXHR, textStatus, error) {
				alert("E-Mail or Password wrong, please try again.");

			},
			complete : function(jqXHR, textStatus) {

			}
		});

	}
}

ko.applyBindings(new LoginViewModel());

