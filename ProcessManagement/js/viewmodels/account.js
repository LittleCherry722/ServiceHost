define([
	"knockout",
	"app",
	"underscore"
], function( ko, App, _ ) {

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
	}
	
	var deleteEmail = function(mail){
		currentUser().providerMail.remove(mail);
	};
	
	var save = function(){
		currentUser().save();
	};
	
	var savePassword = function(){
		
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
