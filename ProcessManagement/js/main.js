/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

// Require.js allows us to configure shortcut alias
require.config({
	paths: {
		"text":                "libs/require/plugins/text",
		"jade":                "libs/require/plugins/jade",
		"director":            "libs/director/director",
		"jquery":           "libs/jquery/jquery",
		"jquery.ui":           "libs/jquery/plugins/jquery-ui",
		"jquery.freeow":       "libs/jquery/plugins/jquery.freeow",
		"jquery.scrollTo":     "libs/jquery/plugins/jquery.scrollTo",
		"jquery.chosen":       "libs/jquery/plugins/jquery.chosen",
		"jquery.pubsub":       "libs/jquery/plugins/jquery.pubsub",
		"jquery.chardin":       "libs/jquery/plugins/jquery.chardinjs",
		"jquery.bootstrap.modal":"libs/jquery/plugins/jquery.bootstrap-modal",
        "jquery.fancybox":     "../fancybox/jquery.fancybox",
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
		"moment":              "libs/moment/moment",
		"select2":             "libs/select2/select2.min",
		"intro":               "libs/intro/intro",
		"rainbow":			   "libs/rainbow/rainbow-custom.min",
		"rainbow":			   "libs/rainbow/rainbow-custom.min",
		"js_beautify":		   "libs/beautify/js_beautify"
	},
	shim: {
		// Legacy libararies that do not follow the common.js module pattern.
		"director": {
			exports: "Router"
		},
		"underscore": {
			exports: "_"
		},
		"rainbow": {
			exports: "Rainbow"
		},
        "jquery.ui": ["jquery"],
        "jquery.fancybox": ["jquery"],
        "jquery.chardin": ["jquery"],
        "jquery.pubsub": ["jquery"],
        "jquery.chosen": ["jquery"],
        "jquery.scrollTo": ["jquery"],
        "jquery.freeow": ["jquery"],
        "jquery.bootstrap.modal": ["jquery"],
        "select2": ["jquery"],
	},
});

require([ "app", "router", "knockout.custom" ], function( App, Router ){

	//Is backend reachable? -> Start application.
	$.ajax({
		url : '/isalive',
		type : "GET",
		async : false, // defaults to true
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
