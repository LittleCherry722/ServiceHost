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
	Hisory = Model( "Hisory" );

	Hisory.attrs({
		id: "string",
		processName: "string",
		processInstanceId: "integer",
		processStarted: "string",
		timestamp: "string",
		// ID des Users, der für diesen Zustandsübergang verantwortlich war
		userId: "integer",
		subjectId: "integer",
		fromState: {
			type: "json",
			defaults: {
				text: "string",
				stateType: "string"
			},
			lazy: true
		},
		overTransition: {
			type: "json",
			defaults: {
				text: "string",
				transitionType: "string"
			},
			lazy: true
		},
		toState: {
			type: "json",
			defaults: {
				text: "string",
				stateType: "string"
			},
			lazy: true
		},
		messages: {
			type: "json",
			defaults: [],
			lazy: true
		},		
	});
	
	// Backend does not know about the message model, this stubs
    // the loading method to do nothing and always be successfull
    Hisory.fetch = function( options, callbacks ) {
        if ( callbacks ) {
            callbacks.success.call();
        }
    };
	
	Hisory.all = function( options, callbacks ) {
		return [
			{
			  	"id": 1,
			  	"processName": "Travel Request",
			  	"processInstanceId": 0,
			  	"processStarted": 1370362134785,
			  	"timestamp": 1270362137008,
				// ID des Users, der für diesen Zustandsübergang verantwortlich war
				"userId": "integer",
				"subjectId": "integer",
				"fromState": {
					"text": "something",
					"stateType": "send"
				},
				"toState": {
    				"text": "something",
    				"stateType": "receive"
  				},
				"messages": [
					{
						"messageId": 42,
						"fromUserId": 1,
    					"toUserIds": [2, 3],
    					"messageType": "Travel Application",
    					"text": "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
					},
					{
						"messageId": 43,
						"fromUserId": 1,
    					"toUserIds": [2, 3, 234],
    					"messageType": "Travel Application23",
    					"text": "test123 Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren"
					}
				]
			},
			{
				"id": 2,
			  	"processName": "Travel Request2",
			  	"processInstanceId": 0,
				"processStarted": 1370362134785,
			  	"timestamp": 1270362137008,
				"fromState": {
					"text": "string",
					"stateType": "send"
				},
				"toState": {
    				"text": "something",
    				"stateType": "receive"
  				},
			},
			{
				"id": 3,
			  	"processName": "Order",
			  	"processInstanceId": 0,
				"processStarted": 1370362134785,
			  	"timestamp": 1370362137008,
				"fromState": {
					"text": "something",
					"stateType": "send"
				},
				"toState": {
    				"text": "something",
    				"stateType": "receive"
  				},
			}
		];
	}

	//Hisory.hasMany( "processInstances" );

	return Hisory;
});
