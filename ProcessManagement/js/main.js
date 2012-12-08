
// Require.js allows us to configure shortcut alias
require.config({
	paths: {
		"text":              "libs/require/plugins/text",
		"jade":              "libs/require/plugins/jade",
		"director":          "libs/director/director",
		"jquery":            "libs/jquery/jquery",
		"jquery.ui":         "libs/jquery/plugins/jquery-ui",
		"jquery.freeow":     "libs/jquery/plugins/jquery.freeow",
		"jquery.qtip":       "libs/jquery/plugins/jquery.qtip",
		"jquery.scrollTo":   "libs/jquery/plugins/jquery.scrollTo",
		"jquery.chosen":     "libs/jquery/plugins/jquery.chosen",
		"jquery.bootstrap":  "libs/jquery/bootstrap.min",
		"keymaster":         "libs/keymaster/keymaster",
		"knockout":          "libs/knockout/knockout",
		"knockout.mapping":  "libs/knockout/plugins/knockout.mapping",
		"underscore":        "libs/underscore/underscore",
		"model":             "libs/arne/model",
		"notify":            "libs/sbpm/notify"
	},
	shim: {
		// Plugins that dont need anything to export, just depend on other
		// libraries that are not specified inside the plugins
		"jquery.ui":         ["jquery"],
		"jquery.freeow":     ["jquery"],
		"jquery.chosen":     ["jquery"],
		"jquery.qtip":       ["jquery"],
		"jquery.scrollTo":   ["jquery"],
		"jquery.bootstrap":  ["jquery"],

		// Legacy libararies that do not follow the common.js module pattern.
		"director": {
			exports: "Router"
		},
		"underscore": {
			exports: "_"
		}
	}

});

require([ "app", "router", "jquery" ], function( App, Router, $ ){
	
	$(function() {
		// Initialize our application.
		App.init();

		// And load our router so we can actually navigate the page.
		Router.init();
	});
});
