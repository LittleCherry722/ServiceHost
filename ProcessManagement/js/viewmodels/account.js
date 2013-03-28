define([
	"knockout",
	"app",
	"notify",
	"underscore"
	
], function( ko, App, Notify, _ ) {

	// Just a stub at the moment.
	// This viewmodel does not do anything but loading a template.
	// Will be used in the feature. At the moment its just to ensure unloading
	// of other views when switching to the home view.
	var ViewModel = function() {
		
		this.currentUser = App.currentUser;
		this.save = save;
		this.reset = reset;
		this.savePassword = savePassword;
		this.deleteEmail = deleteEmail;
		this.addEmail = addEmail;
		this.newPassword1 = newPassword1;
	this.newPassword2 = newPassword2;
	this.oldPassword = oldPassword;
	this.passwordsEqual = passwordsEqual;
	this.passwordSaveable = passwordSaveable;
	}
	
	//{"oldPassword":"s1234", "newPasswword":"password"} 
	
	var newPassword1 = ko.observable("");
	var newPassword2 = ko.observable("");
	var oldPassword = ko.observable("");
	
	var passwordsEqual = ko.computed(function(){
		return newPassword1() == newPassword2();
	});
	
	var passwordSaveable = ko.computed(function(){
	return	newPassword1() !== "" && oldPassword() !== "" && passwordsEqual();
	});
	
	var deleteEmail = function(mail){
		currentUser().providerMail.remove(mail);
	};
	
	var save = function(){
		currentUser().save();
	};
	
	var savePassword = function(){
		var data = {};
		data.oldPassword = oldPassword()
		data.newPassword = newPassword1()
		data = JSON.stringify(data);
console.log (data)
		$.ajax({
			url : '/user/'+currentUser().id(),
			type : "PUT",
			data : data,
			async : true, // defaults to false
			dataType : "json",
			contentType : "application/json; charset=UTF-8",
			success : function(data, textStatus, jqXHR) {
			Notify.info("Success", "New password has been saved.");

			},
			error : function(jqXHR, textStatus, error) {
			Notify.error("Error", "New password was not saved. Please make sure the old password is correct.");

			},
			complete : function(jqXHR, textStatus) {

	newPassword1("");
	newPassword2("");
	oldPassword("");

			}
		});
	}
	
	var reset = function(){
	//	currentUser().reset();
	};
	
	var addEmail = function(){
					currentUser().providerMail.push(jQuery.parseJSON( '{"provider":"","mail":""}'));
	};
	
	var initialize = function( subSite ) {
		var viewModel;

		viewModel = new ViewModel();
		window.aView = viewModel;
		App.loadTemplate( "account", viewModel, null, function() {
			// Maybe do something?
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});
