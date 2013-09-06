define([
	"knockout",
	"app",
	"underscore",
	"models/user",
	"models/group",
	"async",
	"notify",
	"select2"
], function( ko, App, _, User, Group, async, notify ) {

	var ViewModel = function() {
		this.users = User.all;
		this.groups = Group.all;

		this.save = function( user ) {
			user.save(null, {
				success: function( error ) {
					notify.info( "Succcess", "User " + user.name() + " has successfully been saved." );
				},
				error: function( textStatus, error ) {
					if ( user.errors().length > 0 ) {
						notify.error( "Error", error + " Errors: " + user.errors().join(" ") );
					} else {
						notify.error( "Error", error );
					}
				}
			});
		};

		this.saveAll = function() {
			var self = this;

			async.eachLimit( User.all(), 5, function( model, callback ) {
				model.save(null, {
					success: function( textStatus ) {
						callback();
					},
					error: function( textStatus, error ) {
						callback( "error" );
					}
				});
			}, function( error, results) {
				if ( error ) {
					notify.error( "Error", "Error saving users. Not all users have been saved. Please check your input and try again." );
				} else {
					notify.info( "Succcess", "All users have successfully been saved." );
				}
			});
		};

		this.reset = function( user ) {
			user.reset();
			user.groupIdsReset();
			setTimeout(function() {
				$('#user' + user.id()).find("select.chzn-select").trigger("liszt:updated");
			}, 1);
		};

		this.resetAll = function() {
			User.resetAll();
			_( User.all() ).each(function( user ) {
				user.groupIdsReset();
			});
			setTimeout(function() {
				$(".chzn-select").trigger("liszt:updated");
			}, 1);
		};

		this.create = function() {alert("TEST");
			User.build().id("Save to \nreceive Id");
			alert("TEST");
			$("#listOfUsers input.form-control").last().focus();
			$("#listOfUsers tr:last-child .chzn-select").select2();
		};

		this.remove = function( user ) {
			user.destroy();
		};
		this.addEmail =function ( user ){
			user.providerMail.push(jQuery.parseJSON( '{"provider":"","mail":""}'));
		};
		this.deleteEmail = function (user, mail){
			user.providerMail.remove(mail);
		};
	};

	var initialize = function( callback ) {
		var viewModel;

		viewModel = new ViewModel();
		window.uView = viewModel; //TODO: remove debug
		// Get the required template;
		App.loadTemplate( "administration/users", viewModel, "right_content", function() {
			$(".chzn-select").select2();

			if ( typeof callback === "function" ) {
				callback();
			}
		});

		_( User.all() ).each(function( user ) {
			groupIds = _( user.groups() ).map(function( group ) {
				return group.id();
			});
			user.groupIds( groupIds );
		});
	};
	// Everything in this object will be the public API
	return {
		init: initialize
	};
});

