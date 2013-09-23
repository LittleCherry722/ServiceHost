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
	"knockout",
	"model",
	"underscore",
	"async",
	"notify"
], function( ko, Model, _, async, notify ) {

	UserMessage = Model( "UserMessage" , {
		remotePath : 'message/inbox'
	});

	UserMessage.attrs({
		fromUser: "integer",
		toUser: "integer",
		title: "string",
		content: "string",
		date: "json"
	});

  UserMessage.all = ko.observableArray();

  UserMessage.enablePolling();

	UserMessage.include({
		initialize: function( data ) {
			var self = this;
			this.formattedDate = ko.computed(function(){
				if (self.date()) {
          return new Date(self.date().date).toLocaleString();
        }
        else {
          return self.date();
        }
			});
		},

    save: function() {
      $.ajax({
        type: "POST",
        url: "/message",
        data: JSON.stringify({ toUser: this.toUser(),
                               title: this.title(),
                               content: this.content()}),
        success: function() {
          window.location.hash = "#/messages/messagesOverview";
          notify.info("Success!", "Message successfully sent.");
        },
        contentType:"application/json; charset=utf-8"
      });


    }
	})

	return UserMessage;
});
