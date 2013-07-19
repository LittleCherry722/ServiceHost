/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
define([
  "knockout",
  "model",
], function( ko, Model ) {

  Interface = Model( "Interfaces", { remotePath: "repo" } );
  
  Interface.attrs({
    name: "string",
    creator: "string",
    description: "string",
    graph: {
			type: "json",
			lazy: false
    }
  });

  Interface.nameAlreadyTaken = function( name ) {
    return !! Interface.all().filter(function( i ) {
      return i.name() == name;
    }).length;
  }

  var graph = {
		"id": "New Interface",
		"name": "New Interface",
		"type": "external",
		"deactivated": false,
		"startSubject": false,
		"inputPool": 100,
		"relatedSubject": "",
		"relatedProcess": null,
		"externalType": "interface",
		"role": "noRole",
		"comment": "",
		"variables": {},
		"variableCounter": 1,
		"macroCounter": 1,
		"macros": [{
			"id": "##main##",
			"name": "internal behavior",
			"nodeCounter": 3,
			"nodes": [{
				"id": 0,
				"text": "",
				"start": true,
				"end": false,
				"type": "receive",
				"deactivated": false,
				"majorStartNode": true,
				"options": {
					"message": "*",
					"subject": "*",
					"correlationId": null,
					"conversation": null,
					"state": null
				},
				"macro": "",
				"varMan": {
					"var1": "",
					"var2": "",
					"operation": "and",
					"storevar": ""
				}
			}, {
				"id": 1,
				"text": "Handle Travel Application",
				"start": false,
				"end": false,
				"type": "action",
				"deactivated": false,
				"majorStartNode": false,
				"options": {
					"message": "*",
					"subject": "*",
					"correlationId": null,
					"conversation": null,
					"state": null
				},
				"macro": "",
				"varMan": {
					"var1": "",
					"var2": "",
					"operation": "and",
					"storevar": ""
				}
			}, {
				"id": 2,
				"text": "End process",
				"start": false,
				"end": true,
				"type": "end",
				"deactivated": false,
				"majorStartNode": false,
				"options": {
					"message": "*",
					"subject": "*",
					"correlationId": null,
					"conversation": null,
					"state": null
				},
				"macro": "",
				"varMan": {
					"var1": "",
					"var2": "",
					"operation": "and",
					"storevar": ""
				}
			}],
      "edges": [{
        "start": 0,
        "end": 1,
        "text": "m4",
        "type": "exitcondition",
        "target": {
          "id": "Manager",
          "min": -1,
          "max": -1,
          "createNew": false
        },
        "deactivated": false,
        "optional": false,
        "priority": 1,
        "manualTimeout": false,
        "correlationId": "",
        "comment": "",
        "transportMethod": ["internal"]
      }, {
        "start": 1,
        "end": 2,
        "text": "Travel Application filed",
        "type": "exitcondition",
        "deactivated": false,
        "optional": false,
        "priority": 1,
        "manualTimeout": false,
        "correlationId": "",
        "comment": "",
        "transportMethod": ["internal"]
      }]
		}]
  }

  // Interface.all = ko.observableArray([
  //   new Interface({ id: 1, name: "Travel Process", creator: "Arne", graph: graph }),
  // ]);

  return Interface;
});
