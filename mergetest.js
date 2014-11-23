{
  "process": [
    {
      "id": "Subj4:73348d5c-e748-4be5-b1b9-82640e551b2d",
      "name": "Local 2",
      "type": "single",
      "subjectType": "single",
      "mergedSubjects": [
        {
          "id": "Subj4:73348d5c-e748-4be5-b1b9-82640e551b2d",
          "name": "Local 2"
        },
        {
          "id": "Subj2:c9f89284-6f22-44af-ae9b-d7e59cae3aa5",
          "name": "Local 1"
        }
      ],
      "deactivated": false,
      "inputPool": -1,
      "blackboxname": "",
      "relatedInterface": null,
      "relatedProcess": null,
      "relatedSubject": "",
      "isImplementation": false,
      "externalType": "external",
      "role": "noRole",
      "startSubject": true,
      "implementations": [],
      "comment": "",
      "macros": [
        {
          "id": "##main##",
          "name": "internal behavior",
          "nodes": [
            {
              "id": 0,
              "text": "",
              "start": false,
              "autoExecute": false,
              "end": false,
              "type": "send",
              "nodeType": "send",
              "options": {
                "subject": "",
                "message": "",
                "conversation": "",
                "correlationId": "",
                "state": ""
              },
              "deactivated": false,
              "majorStartNode": false,
              "conversation": "",
              "variable": "",
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            },
            {
              "id": 1,
              "text": "",
              "start": false,
              "autoExecute": false,
              "end": false,
              "type": "receive",
              "nodeType": "receive",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": false,
              "conversation": "",
              "variable": null,
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            },
            {
              "id": 2,
              "text": "internal action",
              "start": false,
              "autoExecute": false,
              "end": false,
              "type": "receive",
              "nodeType": "receive",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": false,
              "conversation": "",
              "variable": null,
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            },
            {
              "id": 3,
              "text": "internal action",
              "start": false,
              "autoExecute": false,
              "end": true,
              "type": "end",
              "nodeType": "end",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": false,
              "conversation": "",
              "variable": null,
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": -1
            },
            {
              "id": 4,
              "text": "",
              "start": true,
              "autoExecute": false,
              "end": false,
              "type": "send",
              "nodeType": "send",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": true,
              "conversation": "",
              "variable": null,
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            },
            {
              "id": 5,
              "text": "",
              "start": false,
              "autoExecute": false,
              "end": false,
              "type": "receive",
              "nodeType": "receive",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "*",
                "conversation": "*",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": false,
              "conversation": "",
              "variable": null,
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            },
            {
              "id": 6,
              "text": "",
              "start": false,
              "autoExecute": false,
              "end": false,
              "type": "send",
              "nodeType": "send",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": false,
              "conversation": "",
              "variable": null,
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            }
          ],
          "edges": [
            {
              "start": 0,
              "end": 1,
              "text": "m3",
              "type": "exitcondition",
              "edgeType": "exitcondition",
              "target": {
                "id": "Subj7:3b717e6d-b46c-4d32-b6fb-69bff927b353",
                "exchangeOriginId": null,
                "exchangeTargetId": null,
                "min": -1,
                "max": -1,
                "createNew": false,
                "variable": ""
              },
              "deactivated": false,
              "optional": false,
              "priority": 1,
              "manualTimeout": false,
              "variable": null,
              "correlationId": "",
              "comment": "",
              "transportMethod": [
                "internal"
              ],
              "manualPositionOffsetLabelX": 0,
              "manualPositionOffsetLabelY": 0
            },
            {
              "start": 1,
              "end": 2,
              "text": "m4",
              "type": "exitcondition",
              "edgeType": "exitcondition",
              "target": {
                "id": "Subj7:3b717e6d-b46c-4d32-b6fb-69bff927b353",
                "exchangeOriginId": null,
                "exchangeTargetId": null,
                "min": -1,
                "max": -1,
                "createNew": false,
                "variable": ""
              },
              "deactivated": false,
              "optional": false,
              "priority": 1,
              "manualTimeout": false,
              "variable": null,
              "correlationId": "",
              "comment": "",
              "transportMethod": [
                "internal"
              ],
              "manualPositionOffsetLabelX": 0,
              "manualPositionOffsetLabelY": 0
            },
            {
              "start": 2,
              "end": 3,
              "text": "m6",
              "type": "exitcondition",
              "edgeType": "exitcondition",
              "target": {
                "id": "Subj355:b8d0f18e-cc23-4051-b849-75b3ab25b57d",
                "exchangeOriginId": null,
                "exchangeTargetId": null,
                "min": -1,
                "max": -1,
                "createNew": false,
                "variable": ""
              },
              "deactivated": false,
              "optional": false,
              "priority": 1,
              "manualTimeout": false,
              "variable": null,
              "correlationId": "",
              "comment": "",
              "transportMethod": [
                "internal"
              ],
              "manualPositionOffsetLabelX": 0,
              "manualPositionOffsetLabelY": 0
            },
            {
              "start": 4,
              "end": 5,
              "text": "m1",
              "type": "exitcondition",
              "edgeType": "exitcondition",
              "target": {
                "id": "Subj3:4f985aa2-011f-4788-8b60-49fd5459639c",
                "exchangeOriginId": "Subj2:c9f89284-6f22-44af-ae9b-d7e59cae3aa5",
                "exchangeTargetId": null,
                "min": -1,
                "max": -1,
                "createNew": false,
                "variable": ""
              },
              "deactivated": false,
              "optional": false,
              "priority": 1,
              "manualTimeout": false,
              "variable": null,
              "correlationId": "",
              "comment": "",
              "transportMethod": [
                "internal"
              ],
              "manualPositionOffsetLabelX": 0,
              "manualPositionOffsetLabelY": 0
            },
            {
              "start": 5,
              "end": 6,
              "text": "m2",
              "type": "exitcondition",
              "edgeType": "exitcondition",
              "target": {
                "id": "Subj3:4f985aa2-011f-4788-8b60-49fd5459639c",
                "exchangeOriginId": null,
                "exchangeTargetId": null,
                "min": -1,
                "max": -1,
                "createNew": false,
                "variable": ""
              },
              "deactivated": false,
              "optional": false,
              "priority": 1,
              "manualTimeout": false,
              "variable": null,
              "correlationId": "",
              "comment": "",
              "transportMethod": [
                "internal"
              ],
              "manualPositionOffsetLabelX": 0,
              "manualPositionOffsetLabelY": 0
            },
            {
              "start": 6,
              "end": 0,
              "text": "m5",
              "type": "exitcondition",
              "edgeType": "exitcondition",
              "target": {
                "id": "Subj355:b8d0f18e-cc23-4051-b849-75b3ab25b57d",
                "exchangeOriginId": "Subj2:c9f89284-6f22-44af-ae9b-d7e59cae3aa5",
                "exchangeTargetId": null,
                "min": -1,
                "max": -1,
                "createNew": false,
                "variable": ""
              },
              "deactivated": false,
              "optional": false,
              "priority": 1,
              "manualTimeout": false,
              "variable": null,
              "correlationId": "",
              "comment": "",
              "transportMethod": [
                "internal"
              ],
              "manualPositionOffsetLabelX": 0,
              "manualPositionOffsetLabelY": 0
            }
          ],
          "nodeCounter": 4
        }
      ],
      "macroCounter": 1,
      "variables": {},
      "variableCounter": 0
    },
    {
      "id": "Subj3:4f985aa2-011f-4788-8b60-49fd5459639c",
      "name": "Interface 1",
      "type": "external",
      "subjectType": "external",
      "mergedSubjects": [
        {
          "id": "Subj3:4f985aa2-011f-4788-8b60-49fd5459639c",
          "name": "Interface 1"
        }
      ],
      "deactivated": false,
      "inputPool": -1,
      "blackboxname": "",
      "relatedInterface": null,
      "relatedProcess": null,
      "relatedSubject": "",
      "isImplementation": false,
      "externalType": "interface",
      "role": "noRole",
      "startSubject": false,
      "implementations": [],
      "comment": "",
      "macros": [
        {
          "id": "##main##",
          "name": "internal behavior",
          "nodes": [
            {
              "id": 0,
              "text": "",
              "start": true,
              "autoExecute": false,
              "end": false,
              "type": "receive",
              "nodeType": "receive",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": true,
              "conversation": "",
              "variable": "",
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            },
            {
              "id": 1,
              "text": "",
              "start": false,
              "autoExecute": false,
              "end": false,
              "type": "send",
              "nodeType": "send",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": false,
              "conversation": "",
              "variable": "",
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            },
            {
              "id": 2,
              "text": "internal action",
              "start": false,
              "autoExecute": false,
              "end": true,
              "type": "end",
              "nodeType": "end",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": false,
              "conversation": "",
              "variable": "",
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            }
          ],
          "edges": [
            {
              "start": 0,
              "end": 1,
              "text": "m1",
              "type": "exitcondition",
              "edgeType": "exitcondition",
              "target": {
                "id": "Subj4:73348d5c-e748-4be5-b1b9-82640e551b2d",
                "exchangeOriginId": null,
                "exchangeTargetId": "Subj2:c9f89284-6f22-44af-ae9b-d7e59cae3aa5",
                "min": -1,
                "max": -1,
                "createNew": false,
                "variable": ""
              },
              "deactivated": false,
              "optional": false,
              "priority": 1,
              "manualTimeout": false,
              "variable": "",
              "correlationId": "",
              "comment": "",
              "transportMethod": [
                "internal"
              ],
              "manualPositionOffsetLabelX": 0,
              "manualPositionOffsetLabelY": 0
            },
            {
              "start": 1,
              "end": 2,
              "text": "m2",
              "type": "exitcondition",
              "edgeType": "exitcondition",
              "target": {
                "id": "Subj4:73348d5c-e748-4be5-b1b9-82640e551b2d",
                "exchangeOriginId": null,
                "exchangeTargetId": "Subj2:c9f89284-6f22-44af-ae9b-d7e59cae3aa5",
                "min": -1,
                "max": -1,
                "createNew": false,
                "variable": ""
              },
              "deactivated": false,
              "optional": false,
              "priority": 1,
              "manualTimeout": false,
              "variable": "",
              "correlationId": "",
              "comment": "",
              "transportMethod": [
                "internal"
              ],
              "manualPositionOffsetLabelX": 0,
              "manualPositionOffsetLabelY": 0
            }
          ],
          "nodeCounter": 3
        }
      ],
      "macroCounter": 1,
      "variables": {},
      "variableCounter": 1
    },
    {
      "id": "Subj7:3b717e6d-b46c-4d32-b6fb-69bff927b353",
      "name": "Interface 2",
      "type": "external",
      "subjectType": "external",
      "mergedSubjects": [
        {
          "id": "Subj7:3b717e6d-b46c-4d32-b6fb-69bff927b353",
          "name": "Interface 2"
        }
      ],
      "deactivated": false,
      "inputPool": -1,
      "blackboxname": "",
      "relatedInterface": null,
      "relatedProcess": null,
      "relatedSubject": "",
      "isImplementation": false,
      "externalType": "interface",
      "role": "noRole",
      "startSubject": false,
      "implementations": [],
      "comment": "",
      "macros": [
        {
          "id": "##main##",
          "name": "internal behavior",
          "nodes": [
            {
              "id": 0,
              "text": "",
              "start": true,
              "autoExecute": false,
              "end": false,
              "type": "receive",
              "nodeType": "receive",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": true,
              "conversation": "",
              "variable": "",
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            },
            {
              "id": 1,
              "text": "",
              "start": false,
              "autoExecute": false,
              "end": false,
              "type": "send",
              "nodeType": "send",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": false,
              "conversation": "",
              "variable": "",
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            },
            {
              "id": 2,
              "text": "internal action",
              "start": false,
              "autoExecute": false,
              "end": true,
              "type": "end",
              "nodeType": "end",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": false,
              "conversation": "",
              "variable": "",
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            }
          ],
          "edges": [
            {
              "start": 0,
              "end": 1,
              "text": "m3",
              "type": "exitcondition",
              "edgeType": "exitcondition",
              "target": {
                "id": "Subj4:73348d5c-e748-4be5-b1b9-82640e551b2d",
                "exchangeOriginId": null,
                "exchangeTargetId": null,
                "min": -1,
                "max": -1,
                "createNew": false,
                "variable": ""
              },
              "deactivated": false,
              "optional": false,
              "priority": 1,
              "manualTimeout": false,
              "variable": "",
              "correlationId": "",
              "comment": "",
              "transportMethod": [
                "internal"
              ],
              "manualPositionOffsetLabelX": 0,
              "manualPositionOffsetLabelY": 0
            },
            {
              "start": 1,
              "end": 2,
              "text": "m4",
              "type": "exitcondition",
              "edgeType": "exitcondition",
              "target": {
                "id": "Subj4:73348d5c-e748-4be5-b1b9-82640e551b2d",
                "exchangeOriginId": null,
                "exchangeTargetId": null,
                "min": -1,
                "max": -1,
                "createNew": false,
                "variable": ""
              },
              "deactivated": false,
              "optional": false,
              "priority": 1,
              "manualTimeout": false,
              "variable": "",
              "correlationId": "",
              "comment": "",
              "transportMethod": [
                "internal"
              ],
              "manualPositionOffsetLabelX": 0,
              "manualPositionOffsetLabelY": 0
            }
          ],
          "nodeCounter": 3
        }
      ],
      "macroCounter": 1,
      "variables": {},
      "variableCounter": 1
    },
    {
      "id": "Subj355:b8d0f18e-cc23-4051-b849-75b3ab25b57d",
      "name": "Local 3",
      "type": "single",
      "subjectType": "single",
      "mergedSubjects": [
        {
          "id": "Subj355:b8d0f18e-cc23-4051-b849-75b3ab25b57d",
          "name": "Local 3"
        }
      ],
      "deactivated": false,
      "inputPool": -1,
      "blackboxname": "",
      "relatedInterface": null,
      "relatedProcess": null,
      "relatedSubject": "",
      "isImplementation": false,
      "externalType": "external",
      "role": "noRole",
      "startSubject": false,
      "implementations": [],
      "comment": "",
      "macros": [
        {
          "id": "##main##",
          "name": "internal behavior",
          "nodes": [
            {
              "id": 0,
              "text": "",
              "start": true,
              "autoExecute": false,
              "end": false,
              "type": "receive",
              "nodeType": "receive",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": true,
              "conversation": "",
              "variable": "",
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            },
            {
              "id": 1,
              "text": "",
              "start": false,
              "autoExecute": false,
              "end": false,
              "type": "send",
              "nodeType": "send",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": false,
              "conversation": "",
              "variable": "",
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            },
            {
              "id": 2,
              "text": "internal action",
              "start": false,
              "autoExecute": false,
              "end": true,
              "type": "end",
              "nodeType": "end",
              "options": {
                "message": "*",
                "subject": "*",
                "correlationId": "",
                "conversation": "",
                "state": null
              },
              "deactivated": false,
              "majorStartNode": false,
              "conversation": "",
              "variable": "",
              "varMan": {
                "var1": "",
                "var2": "",
                "operation": "and",
                "storevar": ""
              },
              "createSubjects": {
                "subject": null,
                "storevar": "",
                "min": -1,
                "max": -1
              },
              "chooseAgentSubject": null,
              "macro": "",
              "blackboxname": "",
              "comment": "",
              "manualPositionOffsetX": 0,
              "manualPositionOffsetY": 0
            }
          ],
          "edges": [
            {
              "start": 0,
              "end": 1,
              "text": "m5",
              "type": "exitcondition",
              "edgeType": "exitcondition",
              "target": {
                "id": "Subj4:73348d5c-e748-4be5-b1b9-82640e551b2d",
                "exchangeOriginId": null,
                "exchangeTargetId": "Subj2:c9f89284-6f22-44af-ae9b-d7e59cae3aa5",
                "min": -1,
                "max": -1,
                "createNew": false,
                "variable": ""
              },
              "deactivated": false,
              "optional": false,
              "priority": 1,
              "manualTimeout": false,
              "variable": "",
              "correlationId": "",
              "comment": "",
              "transportMethod": [
                "internal"
              ],
              "manualPositionOffsetLabelX": 0,
              "manualPositionOffsetLabelY": 0
            },
            {
              "start": 1,
              "end": 2,
              "text": "m6",
              "type": "exitcondition",
              "edgeType": "exitcondition",
              "target": {
                "id": "Subj4:73348d5c-e748-4be5-b1b9-82640e551b2d",
                "exchangeOriginId": null,
                "exchangeTargetId": null,
                "min": -1,
                "max": -1,
                "createNew": false,
                "variable": ""
              },
              "deactivated": false,
              "optional": false,
              "priority": 1,
              "manualTimeout": false,
              "variable": "",
              "correlationId": "",
              "comment": "",
              "transportMethod": [
                "internal"
              ],
              "manualPositionOffsetLabelX": 0,
              "manualPositionOffsetLabelY": 0
            }
          ],
          "nodeCounter": 3
        }
      ],
      "macroCounter": 1,
      "variables": {},
      "variableCounter": 1
    }
  ],
  "messages": {
    "m2": "antwort",
    "m4": "antwort 2",
    "m1": "anfrage",
    "m6": "From Local 3",
    "m3": "anfrage 2",
    "m5": "To Local 3"
  },
  "messageCounter": 7,
  "nodeCounter": 354,
  "conversations": {},
  "conversationCounter": 1
}