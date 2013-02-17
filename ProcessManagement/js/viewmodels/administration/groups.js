define([
	"knockout",
	"app",
	"underscore",
	"models/group",
	"async",
	"notify"
], function( ko, App, _, Group, async, notify ) {

	var ViewModel = function() {
		this.groups = Group.all;

		this.save = function( group ) {
			group.save(function( error ) {
				if ( error ) {
					if ( group.errors().length > 0 ) {
						notify.error( "Error", error + " Errors: " + group.errors().join(" ") );
					} else {
						notify.error( "Error", error )
					}
				} else {
					notify.info( "Succcess", "Group " + group.name() + " has successfully been saved." )
				}
			});
		}

		this.saveAll = function() {
			_( Group.all() ).each(function( group ) {
				group.save();
			});
		}

		this.reset = function( group ) {
			group.reset();
		}

		this.resetAll = function( group ) {
			Group.resetAll();
		}

		this.create = function() {
			Group.build().id("Save to \nreceive Id")

			$("#listOfGroups input.inline").last().focus()
			$("#listOfGroups tr:last-child .chzn-select").chosen();
		}

		this.remove = function( group ) {
			group.destroy();
		}
	}


	var initialize = function( callback ) {
		var viewModel;

		viewModel = new ViewModel();

		// Get the required template.
		App.loadTemplate( "administration/groups", viewModel, "right_content", callback );
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});





