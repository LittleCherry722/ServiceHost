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

define([
	"jquery.freeow"
], function( jQuery ) {

	var Notify = function() {
		var freeOwId = "#freeow",
			infoClass = "ok",
			warnClass = "notice",
			errClass  = "error",
			context;

		if ( parent ) {
			context = parent;
		} else {
			context = window;
		}

		this.info = function( title, text ) {
			return context.jQuery( freeOwId ).freeow( title, text, {
				classes: [ infoClass ],
				autohide: true
			});
		}

		this.error = function( title, text ) {
			return context.jQuery( freeOwId ).freeow( title, text, {
				classes: [ errClass ],
				autohide: true
			});
		}

		this.warning = function( title, text ) {
			return context.jQuery( freeOwId ).freeow( title, text, {
				classes: [ warnClass ],
				autohide: true
			});
		}
	}

	// Everything in this object will be the public API
	return new Notify()
});

// SBPM.Notification = new Notification();
