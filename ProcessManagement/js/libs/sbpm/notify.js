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
