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
	"underscore"
], function( ko, Model, _ ) {

	// Our main model that will be returned at the end of the function.
	//
	// Process is responsible for everything associated with processes directly.
	//
	// For example: Getting a list of all processes, saving a process,
	// validating the current process etc.
	ProcessInstance = Model( "ProcessInstance" );

	ProcessInstance.belongsTo( "process" );

	ProcessInstance.attrs({
		processId: "integer",
		instancename: "string",
		graph: {
			type: "json",
			defaults: {
				routings: [],
				definition: {
					conversationCounter: 1,
					conversations: {},
					messageCounter: 0,
					messages: {},
					nodeCounter: 0,
					process: []
				}
			},
			lazy: true
		},
		history: {
			type: "json",
			defaults: "{}",
			lazy: true
		},
		actions: {
			type: "json",
			defaults: [],
			lazy: true
		},
		isTerminated: {
			type: "boolean",
			defaults: false,
			lazy: true
		}
	});

	ProcessInstance.include({
		initialize: function() {
			var self = this;

			this.instanceName = ko.computed(function() {
				return "Instance #" + self.id();
			});

			this.graphString = ko.computed({
				deferEvaluation: true,
				read: function() {
					if ( self.graph() ) {
						return JSON.stringify( self.graph() );
					} else {
						return {};
					}
				},
				write: function( graphString ) {
					var graph = self.graph();
					graph.definitions = JSON.parse( graphString );
					self.graph( graph );
				}
			});
		}
	});

	return ProcessInstance;
});
