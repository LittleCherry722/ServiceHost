define([
	"knockout",
	"app",
	"underscore",
	"models/user",
	"models/group",
	"async",
	"notify"
], function( ko, App, _, User, Group, async, notify ) {

	var ViewModel = function() {
		this.users = User.all;
		this.groups = Group.all;

		this.save = function( user ) {
			user.save(function() {
				notify.info( "Succcess", "User " + user.name() + " has successfully been saved." )
			});
		}

		this.saveAll = function() {
			queue = async.queue(function( model, callback ) {
				model.save( callback );
			}, 5);
			queue.drain = function( error, results) {
				notify.info( "Succcess", "All users have successfully been saved." )
			}

			_( User.all() ).each(function( user ) {
				queue.push( user );
			});
		}

		this.reset = function( user ) {
			user.reset();
			user.groupIDsReset();
			setTimeout(function() {
				$('#user' + user.id()).find("select.chzn-select").trigger("liszt:updated");
			}, 1);
		}

		this.resetAll = function() {
			User.resetAll();
			_( User.all() ).each(function( user ) {
				user.groupIDsReset();
			});
			setTimeout(function() {
				$(".chzn-select").trigger("liszt:updated");
			}, 1);
		}

		this.create = function() {
			User.build().id("Save to \nreceive ID");

			$("#listOfUsers input.inline").last().focus()
			$("#listOfUsers tr:last-child .chzn-select").chosen();
		}

		this.remove = function( user ) {
			user.destroy();
		}
	}

	var initialize = function( callback ) {
		var viewModel;

		viewModel = new ViewModel();

		// Get the required template;
		App.loadTemplate( "administration/users", viewModel, "right_content", function() {
			$(".chzn-select").chosen();

			if ( typeof callback === "function" ) {
				callback();
			}
		});

		_( User.all() ).each(function( user ) {
			groupIDs = _( user.groups() ).map(function( group ) {
				return group.id();
			});
			user.groupIDs( groupIDs );
		});
	}
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});

