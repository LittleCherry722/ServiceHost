define([
	"knockout",
	"app",
	"underscore",
	"notify",
	"model"
], function( ko, App, _, Notify, Model ) {

	var ViewModel = function() {

		this.rebuildDatabase = rebuildDatabase;
	}

	var debugQuery = function( callback ) {
		var json, data;

    if ( typeof callback === "undefined" ) {
      callback = function() {};
    }

		$.ajax({
			url: "/debug",
			type: "GET",
			data: data,
			cache: false,
			success: function() {
        callback();
			},
			error : function( error ){
				callback( error );
			}
		});
	}


	var rebuildDatabase = function() {
		debugQuery(function( error, json ) {
			if ( error ) {
				Notify.error( "Error", "Rebuilding the database failed with error: " + error );
			} else {
				Notify.info( "Success", "Successflly rebuilt database." );
				Model.fetchAll();
			}
		});
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

