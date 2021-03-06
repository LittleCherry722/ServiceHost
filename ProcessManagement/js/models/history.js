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
	"models/processInstance"
], function( ko, Model, _, ProcessInstances ) {

	// Our main model that will be returned at the end of the function.
	History = Model( "History", {remotePath: 'processinstance/history'}  );

	History.attrs({
		process: {
			type: "json",
			defaults: {
				processName: "string",
				processInstanceId: "integer"
			},
      lazy: false
		},
		subject: "string",
		processStarted: "string",
		timeStamp: {
			type: "json",
			lazy: false
		},
		transitionEvent: {
			type: "json",
			defaults: {
				fromState: {
					type: "json",
					defaults: {
						text: "string",
						stateType: "string"
					}
				},
				text: "string",
				transitionType: "string",
				toState: {
					type: "json",
					defaults: {
						text: "string",
						stateType: "string"
					}
				},
				message: {
					type: "json",
					defaults: {
						messageId: "string",
						fromSubject: "string",
            toSubject: "string",
            messageType: "string",
            text: "string"
					},
					lazy: true
				}
			},
			lazy: false
		},
		// ID des Users, der für diesen Zustandsübergang verantwortlich war
		userId: "integer"
	});

  History.enablePolling();

	History.all = ko.observableArray();

	History.include({
    initialize: function( data ) {
      var self = this;

      this.instanceName = ko.computed(function() {
        return self.process().processInstanceName;
      });

      this.processName = ko.computed(function() {
        return self.process().processName;
      });
    }
  });

	return History;
});
