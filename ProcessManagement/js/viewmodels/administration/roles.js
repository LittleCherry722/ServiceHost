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
			role.save(function() {
				notify.info( "Succcess", "Role " + role.name() + " has successfully been saved." )
			});
		}

		this.saveAll = function() {
			queue = async.queue(function( model, callback ) {
				model.save( callback );
			}, 5);
			queue.drain = function( error, results) {
				notify.info( "Succcess", "All roles have successfully been saved." )
			}

			_( Role.all() ).each(function( role ) {
				queue.push( role );
			});
		}

		this.reset = function( role ) {
			role.reset();
			role.groupIDsReset();
			setTimeout(function() {
				$('#role' + role.id()).find("select.chzn-select").trigger("liszt:updated");
			}, 1);
		}

		this.resetAll = function() {
			Role.resetAll();
			_( Role.all() ).each(function( role ) {
				role.groupIDsReset();
			});
			setTimeout(function() {
				$(".chzn-select").trigger("liszt:updated");
			}, 1);
		}

		this.create = function() {
			Role.build().id("Save to \nreceive ID");

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
			groupIDs = _( role.groups() ).map(function( group ) {
				return group.id();
			});
			role.groupIDs( groupIDs );
		});
	}
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


