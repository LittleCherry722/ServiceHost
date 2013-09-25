define([
	"knockout",
	"app",
	"underscore",
	"models/role",
	"models/group",
	"async",
	"notify"
], function( ko, App, _, Role, Group, async, notify ) {

	var ViewModel = function() {
		this.roles = Role.all;
		this.groups = Group.all;

		this.save = function( role ) {
			role.save(null, {
				success: function( textStatus ) {
					notify.info( "Succcess", "Role " + role.name() + " has successfully been saved." )
				},
				error: function( textStatus, error ) {
					if ( role.errors().length > 0 ) {
						notify.error( "Error", error + " Errors: " + role.errors().join(" ") );
					} else {
						notify.error( "Error", error )
					}
				}
			});
		}

		this.saveAll = function() {
			var self = this;

			async.eachLimit( Role.all(), 5, function( model, callback ) {
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
					notify.error( "Error", "Error saving roles. Not all roles have been saved. Please check your input and try again." )
				} else {
					notify.info( "Succcess", "All roles have successfully been saved." )
				}
			});
		}

		this.reset = function( role ) {
			role.reset();
			role.groupIdsReset();
			setTimeout(function() {
				$('#role' + role.id()).find("select.chzn-select").trigger("liszt:updated");
			}, 1);
		}

		this.resetAll = function() {
			Role.resetAll();
			_( Role.all() ).each(function( role ) {
				role.groupIdsReset();
			});
			setTimeout(function() {
				$(".chzn-select").trigger("liszt:updated");
			}, 1);
		}

		this.create = function() {
			Role.build().id("Save to \nreceive Id");

			$("#listOfRoles input.inline").last().focus()
			$("#listOfRoles tr:last-child .chzn-select").chosen();
		}

		this.remove = function( role ) {
			role.destroy();
		}
	}

	var initialize = function( callback ) {
		var viewModel;

		viewModel = new ViewModel();

		// Get the required template;
		App.loadTemplate( "administration/roles", viewModel, "right_content", function() {
			$(".chzn-select").chosen();

			if ( typeof callback === "function" ) {
				callback();
			}
		});

		_( Role.all() ).each(function( role ) {
			groupIds = _( role.groups() ).map(function( group ) {
				return group.id();
			});
			role.groupIds( groupIds );
		});
	}
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


