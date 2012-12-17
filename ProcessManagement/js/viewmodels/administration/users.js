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

		// Get the required template and all users, groups if not already done.
		App.loadTemplate( "administration/users", viewModel, "right_content", function() {
			if ( Group.all().length === 0 ) {
				Group.fetch();
			}
			if ( User.all().length === 0 ) {
				User.fetch();
			}
			if ( typeof callback === "function" ) {
				callback();
			}
		});
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});





