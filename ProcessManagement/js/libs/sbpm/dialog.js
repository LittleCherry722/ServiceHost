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

define([], function() {

	var genericDialog = function( title, text, buttons ) {
		var buildButtonString = function(buttons) {
			var buttonStr = "";

			for (var i = 0; i < buttons.length; i++) {
				buttonStr += '<input id="' + buttons[i].id + '" type="button" value="' + buttons[i].name + '" />';
			}

			return buttonStr;
		}

		return $.fancybox({
			modal : true,
			autoScale: true,
			width : '400px',
			content : '<div id="dialog-header"> <div id="dialog-logo"> <h3>' + title +
				'</h3></div></div><div id="fancyboxGenericPopup" style="margin:10px; width:270px">' +
				'	<br>' + text + '<br><br><br>' +
				'	<div id="dialog-buttons" align="center" >' +
				buildButtonString(buttons) + '</div></div>',
			onComplete : function() {

				// assign actions
				for (var i = 0; i < buttons.length; i++) {
					jQuery( "#" + buttons[i].id ).click( buttons[i].action );
				}

			}
		});
	}

	var Dialog = function() {
		this.urgentNotice = function(title, text) {
			return genericDialog(title || "Notice", text);
		}

		this.notice = function(title, text, okAction) {
			return genericDialog(title || "Notice", text, [{
				id : "okBtn",
				name : "Ok",
				action : okAction ||
					function() {
					$.fancybox.close();
				}
			}]);
		}

		this.yesNo = function(title, text, yesAction, noAction) {
			return genericDialog(title || "Confirmation prompt", text || "Do you really want to proceed?", [{
				id : "yesBtn",
				name : "Yes",
				action : yesAction ||
					function() {
					$.fancybox.close();
				}
			}, {
				id : "noBtn",
				name : "No",
				action : noAction ||
					function() {
					$.fancybox.close();
				}
			}]);
		}
	}

	return new Dialog();
});
