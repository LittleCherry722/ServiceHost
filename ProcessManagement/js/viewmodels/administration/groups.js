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
			group.save(null, {
				error: function( textStatus, error ) {
					if ( group.errors().length > 0 ) {
						notify.error( "Error", error + " Errors: " + group.errors().join(" ") );
					} else {
						notify.error( "Error", error )
					}
				},
				success: function( textStatus ) {
					notify.info( "Succcess", "Group " + group.name() + " has successfully been saved." )
				}
			});
		}

		this.saveAll = function() {
			async.eachLimit(Group.all(), 5, function( model, callback ) {
				model.save(null, {
					success: function( textStatus ) {
						callback();
					},
					error: function( textStatus, error ) {
						callback( "error" );
					}
				});
			}, function( error, results ) {
				if ( error ) {
					notify.error( "Error", "Error saving groups. Not all groups have been saved. Please check your input and try again." );
				} else {
					notify.info( "Succcess", "All groups have successfully been saved." );
				}
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





