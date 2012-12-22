define([
	"knockout",
	"app",
	"underscore"
], function( ko, App, _ ) {

	var ViewModel = function() {

		this.tabs = tabs;

		this.currentTab = currentTab;

		this.save = function() {
			var success = true;

			for(var site in this.subsites()) {
				// save all tabs
				success = success && this.subsites()[site].save();
			}

			// and re-init the current tab
			this.subsite().init();

			if(success) {
				SBPM.Notification.Info("Information", "The administration has been saved.");
			} else {
				SBPM.Notification.Error("Error", "An Error occured while saving the administration.");
			}
		}
	}

	var tabs = ['General', 'Users', 'Roles', 'Groups', 'Debug'];

	var currentTab = ko.observable();

	currentTab.subscribe(function( newTab ) {
		App.loadSubView( "administration/" + newTab.toLowerCase() );
	});


	var initialize = function( subSite ) {
		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "administration", viewModel, null, function() {
			currentTab( subSite || tabs[0] )
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize,
		currentTab: currentTab
	}
});

