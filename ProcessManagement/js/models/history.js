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
			}
		},
		subject: "string",
		processStarted: "string",
		timeStamp: {
			type: "json",
			defaults: {
				date: "string"
			},
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
			lazy: true
		},
		// ID des Users, der für diesen Zustandsübergang verantwortlich war
		userId: "integer"
	});

  History.enablePolling();

	History.all = ko.observableArray();
	History.include({
    initialize: function( data ) {
      var self = this;
      if(self.process()){
        this.processinstance = ko.computed(function() {
          var processId = null;
          var processInstanceId = self.process().processInstanceId;
          _.each(ProcessInstances.all(), function(element) {
            if (element.id() === processInstanceId) {
              //processName = element.processName();
              var instanceName = element.name();
            }
          });
        });
      }
    }
  });

	return History;
});
