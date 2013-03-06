
// Require.js allows us to configure shortcut alias
require.config({
	paths: {
		"text":                "libs/require/plugins/text",
		"jade":                "libs/require/plugins/jade",
		"director":            "libs/director/director",
		"jquery.ui":           "libs/jquery/plugins/jquery-ui",
		"jquery.freeow":       "libs/jquery/plugins/jquery.freeow",
		"jquery.qtip":         "libs/jquery/plugins/jquery.qtip",
		"jquery.scrollTo":     "libs/jquery/plugins/jquery.scrollTo",
		"jquery.chosen":       "libs/jquery/plugins/jquery.chosen",
		"jquery.bootstrap":    "libs/jquery/bootstrap.min",
		"keymaster":           "libs/keymaster/keymaster",
		"knockout":            "libs/knockout/knockout",
		"knockout.mapping":    "libs/knockout/plugins/knockout.mapping",
		"knockout.custom":     "libs/knockout/plugins/knockout.custom-bindings",
		"underscore":          "libs/underscore/underscore",
		"model":               "libs/knockout_model/model",
		"model/associations":  "libs/knockout_model/model/associations",
		"model/attributes":    "libs/knockout_model/model/attributes",
		"notify":              "libs/sbpm/notify",
		"dialog":              "libs/sbpm/dialog",
		"async":               "libs/async/async",
		"moment":              "libs/moment/moment"
	},
	shim: {
		// Legacy libararies that do not follow the common.js module pattern.
		"director": {
			exports: "Router"
		},
		"underscore": {
			exports: "_"
		}
	}

});

require([ "app", "router", "knockout.custom" ], function( App, Router ){
	
			//Is backend reachable? -> Start application.
	$.ajax({ 
		url : '/isalive',
		type : "GET",
		async : false, // defaults to false
		success : function(data, textStatus, jqXHR) {

			$(function() {
				// Initialize our application.
				App.init(function() {
					// And load our router so we can actually navigate the page.
					Router.init();
				});
			});

		},
		error : function(jqXHR, textStatus, error) {

		alert("Can not reach backend!");

		},
		complete : function(jqXHR, textStatus) {
		}
	}); 

	

});
