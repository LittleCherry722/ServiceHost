define([
	"knockout",
	"app",
	"underscore",
	"models/user",
	"models/group",
	"async"
], function( ko, App, _, User, Group, async ) {

	var ViewModel = function() {
		this.users = User.all;
		this.groups = Group.all;

		this.save = function( user ) {
			user.save();
		}

		this.saveAll = function() {
			_( User.all() ).each(function( user ) {
				user.save();
			});
		}

		this.reset = function( user ) {
			user.reset();
		}

		this.resetAll = function( user ) {
			User.resetAll();
		}

		this.create = function() {
			new User.build( { id: "Save to \nreceive ID" } )
			$(".scrollable input.inline").last().focus()
		}

		this.remove = function( user ) {
			user.destroy();
		}
	}

	var initialize = function( callback ) {
		var viewModel;

		viewModel = new ViewModel();

		// Get the required template;
		App.loadTemplate( "administration/users", viewModel, "right_content", callback);

		_( User.all() ).each(function( user ) {
			console.log(user.name())
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

