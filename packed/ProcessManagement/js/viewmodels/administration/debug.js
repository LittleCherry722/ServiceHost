define([
	"knockout",
	"app",
	"underscore",
	"notify",
	"model"
], function( ko, App, _, Notify, Model ) {

	var ViewModel = function() {

		this.createUsers     = createUsers;
		this.clearDatabase   = clearDatabase;
		this.createProcess1  = createProcess1;
		this.rebuildDatabase = rebuildDatabase;

		this.save = save;
	}

	var debugQuery = function( action, callback ) {
		var json, data;

		if ( typeof action === "object" ) {
			data = action;
		} else {
			data = { action: action }
		}

		$.ajax({
			url: "db/debug.php",
			type: "POST",
			data: data,
			cache: false,
			success: function( dataAsJson ){
				try {
					json = jQuery.parseJSON( dataAsJson );
				} catch( error ) {
					callback( error, null );
				}
				if ( json['code'] === 'ok' ) {
					callback( null, json );
				} else {
					callback( "DB Error", json );
				}
			},
			error : function( error ){
				callback( error, null );
			}
		});
	}

	var createUsers = function() {
		debugQuery( "user", function( error, json ) {
			if ( error ) {
				Notify.error( "Error", "User creation failed with error: " + error );
			} else {
				Notify.info( "Success", "Users successfully created." );
				Model.fetchAll();
			}
		});
	}

	var clearDatabase = function() {
		debugQuery( "clear", function( error, json ) {
			if ( error ) {
				Notify.error( "Error", "Error clearing the databse: " + error );
			} else {
				Notify.info( "Success", "Database successfully cleared." );
				Model.fetchAll();
			}
		});
	}

	var createProcess1 = function() {
		var data = {
			action: "process",
			process: "travelapplication"
		}
		debugQuery( data, function( error, json ) {
			if ( error ) {
				Notify.error( "Error", "Creating the travelapplication process failed with error: " + error );
			} else {
				Notify.info( "Success", "Successfully created travelapplication processes." );
				Model.fetchAll();
			}
		});
	}

	var rebuildDatabase = function() {
		debugQuery( "rebuild", function( error, json ) {
			if ( error ) {
				Notify.error( "Error", "Rebuilding the database failed with error: " + error );
			} else {
				Notify.info( "Success", "Successflly rebuilt database." );
				Model.fetchAll();
			}
		});
	}

	var save = function() {
		return true;
	}

	var initialize = function() {
		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "administration/debug", viewModel, "right_content", null );
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});

