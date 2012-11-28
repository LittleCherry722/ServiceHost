
// Require.js allows us to configure shortcut alias
require.config({
	paths: {
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
		"underscore":        "libs/underscore/underscore"
	},
	shim: {
		"jquery.ui":         ["jquery"],
		"jquery.freeow":     ["jquery"],
		"jquery.chosen":     ["jquery"],
		"jquery.qtip":       ["jquery"],
		"jquery.scrollTo":   ["jquery"],
		"jquery.bootstrap":  ["jquery"],
	}

});

require([ 'router' ], function(Router){

	// The "app" dependency is passed in as "App"
	Router.init();
});
