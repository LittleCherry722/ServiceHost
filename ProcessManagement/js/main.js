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
	paths: (function() {
		var thirdpartyURI = '../thirdparty/';
		var ownURI = 'libs/';
		return {
			"text": thirdpartyURI + "require-text/text",
			"director": thirdpartyURI + "director/director",
			"keymaster": thirdpartyURI + "keymaster/keymaster",
			"underscore": thirdpartyURI + "underscore/underscore",
			"async": thirdpartyURI + "async/async",
			"moment": thirdpartyURI + "moment/moment",
			"select2": thirdpartyURI + "select2/select2.min",
			"intro": thirdpartyURI + "intro/intro",
			"rainbow": thirdpartyURI + "rainbow/rainbow-custom.min",
			"js_beautify": thirdpartyURI +"beautify/js_beautify",
			"bootstrap": thirdpartyURI + "bootstrap/bootstrap",
			"knockout": thirdpartyURI + "knockout/knockout",
				"knockout.mapping": thirdpartyURI + "knockout/plugins/knockout.mapping",
				"knockout.custom": thirdpartyURI + "knockout/plugins/knockout.custom-bindings",
			"jquery": thirdpartyURI + "jquery/jquery",
				"jquery.ui": thirdpartyURI + "jquery-ui/jquery-ui",
				"jquery.freeow": thirdpartyURI + "jquery-freeow/jquery.freeow",
				"jquery.scrollTo": thirdpartyURI + "jquery-scrollTo/jquery.scrollTo",
				"jquery.chosen": thirdpartyURI + "jquery-chosen/jquery.chosen",
				"jquery.pubsub": thirdpartyURI + "jquery-pubsub/jquery.pubsub",
				"jquery.chardin": thirdpartyURI + "jquery-chardin/jquery.chardinjs",
				"jquery.fancybox": thirdpartyURI + "jquery-fancybox/jquery.fancybox",
			// our own libraries
			"model": ownURI + "model",
			"notify": ownURI + "notify",
			"model/associations": ownURI + "model/associations",
			"model/storage": ownURI + "model/storage",
			"model/polling": ownURI + "model/polling",
			"model/attributes": ownURI + "model/attributes",
			"shortcuts": ownURI + "shortcuts",
			"uuid": ownURI + "uuid",
			"dialog": ownURI + "dialog",
	};})(),
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
		// ensure that all jquery plugins depend on jquery. Not all do in a requirejs-compatible way
		"jquery.ui": ["jquery"],
		"jquery.fancybox": ["jquery"],
		"jquery.chardin": ["jquery"],
		"jquery.pubsub": ["jquery"],
		"jquery.chosen": ["jquery"],
		"jquery.scrollTo": ["jquery"],
		"jquery.freeow": ["jquery"],
		"bootstrap": ["jquery"],
		"select2": ["jquery"],
	},
	urlArgs: (function() {
		// ensure that browser caches aren't used, but only on localhost
		// see https://stackoverflow.com/questions/8315088/prevent-requirejs-from-caching-required-scripts
		if (window.location.hostname === 'localhost')
			return "cachebust=" + (new Date()).getTime();
		else
			return "";
	})()
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
