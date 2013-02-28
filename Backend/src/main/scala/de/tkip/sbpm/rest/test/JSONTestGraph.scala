package de.tkip.sbpm.rest.test

object MyJSONTestGraph {
  val processGraph: String = """
    {

"process": [{

"id": "Subj1",

"name": "Purchaser",

"type": "single",

"deactivated": false,

"inputPool": 100,

"relatedProcess": "",

"relatedSubject": "",

"externalType": "external",

"role": "Purchase_Requisitions",

"comment": "",

"macros": [{

"id": "##main##",

"name": "internal behavior",

"nodes": [{

"id": 0,

"text": "Prepare Order Request",

"start": true,

"end": false,

"type": "action",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": true,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 1,

"text": "",

"start": false,

"end": false,

"type": "send",

"options": {

"message": "*",

"subject": "*",

"correlationId": "*",

"channel": "*",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 2,

"text": "Wait for answer",

"start": false,

"end": false,

"type": "action",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 3,

"text": "",

"start": false,

"end": false,

"type": "receive",

"options": {

"message": "*",

"subject": "*",

"correlationId": "*",

"channel": "*",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 4,

"text": "",

"start": false,

"end": true,

"type": "end",

"options": {

"message": "*",

"subject": "*",

"correlationId": "*",

"channel": "*",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 5,

"text": "",

"start": false,

"end": false,

"type": "receive",

"options": {

"message": "*",

"subject": "*",

"correlationId": "*",

"channel": "*",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 6,

"text": "",

"start": false,

"end": false,

"type": "send",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 7,

"text": "internal action",

"start": false,

"end": false,

"type": "receive",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 8,

"text": "Check Order",

"start": false,

"end": false,

"type": "action",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 9,

"text": "Process Order Date",

"start": false,

"end": false,

"type": "action",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 10,

"text": "Check Order",

"start": false,

"end": true,

"type": "end",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}],

"edges": [{

"start": 0,

"end": 1,

"text": "Done",

"type": "exitcondition",

"target": "",

"deactivated": false,

"optional": false,

"priority": 1,

"manualTimeout": false,

"variable": "",

"correlationId": "",

"comment": "",

"transportMethod": ["internal"]

}, {

"start": 1,

"end": 2,

"text": "m0",

"type": "exitcondition",

"target": {

"id": "Subj4",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 2,

"end": 3,

"text": "Await Denial",

"type": "exitcondition",

"target": "",

"deactivated": false,

"optional": false,

"priority": 1,

"manualTimeout": false,

"variable": null,

"correlationId": "",

"comment": "",

"transportMethod": ["internal"]

}, {

"start": 3,

"end": 4,

"text": "m1",

"type": "exitcondition",

"target": {

"id": "Subj4",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 2,

"end": 5,

"text": "Await Accept",

"type": "exitcondition",

"target": "",

"deactivated": false,

"optional": false,

"priority": 1,

"manualTimeout": false,

"variable": null,

"correlationId": "",

"comment": "",

"transportMethod": ["internal"]

}, {

"start": 5,

"end": 6,

"text": "m2",

"type": "exitcondition",

"target": {

"id": "Subj4",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 6,

"end": 7,

"text": "m3",

"type": "exitcondition",

"target": {

"id": "Subj3",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 7,

"end": 8,

"text": "m4",

"type": "exitcondition",

"target": {

"id": "Subj3",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 7,

"end": 9,

"text": "m5",

"type": "exitcondition",

"target": {

"id": "Subj3",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 9,

"end": 7,

"text": "",

"type": "exitcondition",

"target": "",

"deactivated": false,

"optional": false,

"priority": 1,

"manualTimeout": false,

"variable": null,

"correlationId": "",

"comment": "",

"transportMethod": ["internal"]

}, {

"start": 8,

"end": 10,

"text": "",

"type": "exitcondition",

"target": "",

"deactivated": false,

"optional": false,

"priority": 1,

"manualTimeout": false,

"variable": null,

"correlationId": "",

"comment": "",

"transportMethod": ["internal"]

}],

"nodeCounter": 11

}],

"macroCounter": 0,

"variables": {},

"variableCounter": 0

}, {

"id": "Subj2",

"name": "Supplier",

"type": "external",

"deactivated": false,

"inputPool": 100,

"relatedProcess": "Supplier (E)",

"relatedSubject": "",

"externalType": "external",

"role": "Warehouse",

"comment": "",

"macros": [{

"id": "##main##",

"name": "internal behavior",

"nodes": [],

"edges": [],

"nodeCounter": 0

}],

"macroCounter": 0,

"variables": {},

"variableCounter": 0

}, {

"id": "Subj3",

"name": "Warehouse",

"type": "single",

"deactivated": false,

"inputPool": 100,

"relatedProcess": "",

"relatedSubject": "",

"externalType": "external",

"role": "Warehouse",

"comment": "",

"macros": [{

"id": "##main##",

"name": "internal behavior",

"nodes": [{

"id": 0,

"text": "new",

"start": true,

"end": false,

"type": "receive",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": true,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 1,

"text": "Check Stock",

"start": false,

"end": false,

"type": "action",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 2,

"text": "internal action",

"start": false,

"end": false,

"type": "send",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 3,

"text": "internal action",

"start": false,

"end": true,

"type": "end",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 4,

"text": "Check Offers",

"start": false,

"end": false,

"type": "action",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 5,

"text": "",

"start": false,

"end": false,

"type": "send",

"options": {

"message": "*",

"subject": "*",

"correlationId": "*",

"channel": "*",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 6,

"text": "",

"start": false,

"end": false,

"type": "receive",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 7,

"text": "",

"start": false,

"end": false,

"type": "send",

"options": {

"message": "*",

"subject": "*",

"correlationId": "*",

"channel": "*",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 8,

"text": "",

"start": false,

"end": false,

"type": "receive",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 9,

"text": "",

"start": false,

"end": false,

"type": "send",

"options": {

"message": "*",

"subject": "*",

"correlationId": "*",

"channel": "*",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 10,

"text": "",

"start": false,

"end": true,

"type": "end",

"options": {

"message": "*",

"subject": "*",

"correlationId": "*",

"channel": "*",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}],

"edges": [{

"start": 0,

"end": 1,

"text": "m3",

"type": "exitcondition",

"target": {

"id": "Subj1",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 1,

"end": 2,

"text": "Goods in Stock",

"type": "exitcondition",

"target": "",

"deactivated": false,

"optional": false,

"priority": 1,

"manualTimeout": false,

"variable": "",

"correlationId": "",

"comment": "",

"transportMethod": ["internal"]

}, {

"start": 2,

"end": 3,

"text": "m4",

"type": "exitcondition",

"target": {

"id": "Subj1",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 1,

"end": 4,

"text": "Goods not in Stock",

"type": "exitcondition",

"target": "",

"deactivated": false,

"optional": false,

"priority": 1,

"manualTimeout": false,

"variable": "",

"correlationId": "",

"comment": "",

"transportMethod": ["internal"]

}, {

"start": 4,

"end": 5,

"text": "",

"type": "exitcondition",

"target": "",

"deactivated": false,

"optional": false,

"priority": 1,

"manualTimeout": false,

"variable": null,

"correlationId": "",

"comment": "",

"transportMethod": ["internal"]

}, {

"start": 5,

"end": 6,

"text": "m3",

"type": "exitcondition",

"target": {

"id": "Subj2",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 6,

"end": 7,

"text": "m5",

"type": "exitcondition",

"target": {

"id": "Subj2",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 7,

"end": 8,

"text": "m5",

"type": "exitcondition",

"target": {

"id": "Subj1",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 8,

"end": 9,

"text": "m4",

"type": "exitcondition",

"target": {

"id": "Subj2",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 9,

"end": 10,

"text": "m4",

"type": "exitcondition",

"target": {

"id": "Subj1",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}],

"nodeCounter": 13

}],

"macroCounter": 0,

"variables": {},

"variableCounter": 0

}, {

"id": "Subj4",

"name": "Manager",

"type": "single",

"deactivated": false,

"inputPool": 100,

"relatedProcess": "",

"relatedSubject": "",

"externalType": "external",

"role": "Cost_Center_Manager",

"comment": "",

"macros": [{

"id": "##main##",

"name": "internal behavior",

"nodes": [{

"id": 0,

"text": "new",

"start": true,

"end": false,

"type": "receive",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": true,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 1,

"text": "Process Order Request",

"start": false,

"end": false,

"type": "action",

"options": {

"subject": "",

"message": "",

"channel": "",

"correlationId": "",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 2,

"text": "",

"start": false,

"end": false,

"type": "send",

"options": {

"message": "*",

"subject": "*",

"correlationId": "*",

"channel": "*",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 3,

"text": "",

"start": false,

"end": true,

"type": "end",

"options": {

"message": "*",

"subject": "*",

"correlationId": "*",

"channel": "*",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 4,

"text": "",

"start": false,

"end": false,

"type": "send",

"options": {

"message": "*",

"subject": "*",

"correlationId": "*",

"channel": "*",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}, {

"id": 5,

"text": "",

"start": false,

"end": true,

"type": "end",

"options": {

"message": "*",

"subject": "*",

"correlationId": "*",

"channel": "*",

"state": ""

},

"deactivated": false,

"majorStartNode": false,

"channel": "",

"variable": "",

"varMan": {

"var1": "",

"var2": "",

"operation": "and",

"storevar": ""

},

"macro": "",

"comment": ""

}],

"edges": [{

"start": 0,

"end": 1,

"text": "m0",

"type": "exitcondition",

"target": {

"id": "Subj1",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 1,

"end": 2,

"text": "Accept",

"type": "exitcondition",

"target": "",

"deactivated": false,

"optional": false,

"priority": 1,

"manualTimeout": false,

"variable": "",

"correlationId": "",

"comment": "",

"transportMethod": ["internal"]

}, {

"start": 2,

"end": 3,

"text": "m2",

"type": "exitcondition",

"target": {

"id": "Subj1",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}, {

"start": 1,

"end": 4,

"text": "Denial",

"type": "exitcondition",

"target": "",

"deactivated": false,

"optional": false,

"priority": 1,

"manualTimeout": false,

"variable": "",

"correlationId": "",

"comment": "",

"transportMethod": ["internal"]

}, {

"start": 4,

"end": 5,

"text": "m1",

"type": "exitcondition",

"target": {

"id": "Subj1",

"min": "-1",

"max": "-1",

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

"transportMethod": ["internal"]

}],

"nodeCounter": 6

}],

"macroCounter": 0,

"variables": {},

"variableCounter": 0

}],

"messages": {

"m0": "Order Request",

"m1": "Denied Order Request",

"m2": "Accepted Order Request",

"m3": "Order",

"m4": "Goods",

"m5": "Order Date",

"m6": "Ask for Offer",

"m7": "Offer",

"m8": "Order Goods"

},

"messageCounter": 9,

"nodeCounter": 4,

"channels": {},

"channelCounter": 0

}"""

  val webgraph =
    """{
    "process": [
        {
            "id":               "S1",               
            "name":             "Subject1",         
            "type":             "single",           
                                                    
            "deactivated":      false,              
            "inputPool":        -1,                 
            "relatedProcess":   null,               
            "relatedSubject":   null,               
            "externalType":     "external",         
                                                    
            "role":             "S1",               
            "comment":          "Any comment",      
            "macros":   [
                {
                    "id":               "##main##",         
                    "name":             "internal behavior",
                    "nodeCounter":      3,                  
                                                            
                    "nodes":    [
                        {
                            "id":                  "n0",    
                            "text":                "S",     
                            "start":               true,    
                                                            
                            "end":                 false,   
                                                            
                            "type":                "send",  
                                                            
                            "deactivated":         false,   
                            "options":                      
                                {
                                    "message":       "*",   
                                    "subject":       "*",   
                                                            
                                    "correlationId": "*",   
                                                            
                                    "channel":        "*",   
                                    "state":          ""     
                                },
                            "majorStartNode":      true,    
                            "channel":             "c0",    
                            "variable":            "",      
                            "macro":               "",      
                            "comment":      "Any comment",  
                            "varMan":                       
                                {
                                    "var1":        "",
                                    "var2":        "",   
                                    "operation":   "and",   
                                    "storevar":    ""       
                                }
                        }
                    ],
                    "edges":    [
                        {
                            "start":            "n0",                   
                            "end":              "n1",                   
                            "text":             "m0",                   
                                                                        
                                                                        
                            "type":             "exitcondition",        
                            "target":                         
                                                              
                                                              
                                {
                                    "id":           "S2",     
                                                              
                                    "min":          "-1",     
                                                              
                                                              
                                    "max":          "-1",     
                                                              
                                                              
                                    "createNew":    false,    
                                                              
                                                              
                                    "variable":     ""        
                                                              
                                },
                            "deactivated":      false,      
                                                                     
                            "optional":         false,               
                                                                     
                                                                     
                            "priority":         1,                   
                                                                     
                                                                     
                            "manualTimeout":    false,               
                                                                     
                            "variable":         "",                  
                                                                     
                            "correlationId":    "",                  
                                                                     
                            "comment":          "Any comment",       
                            "transportMethod":  "googleMail"         
                        }
                    ]
                }
            ]
        }
    ],
    "messages":                                 
        {
            "m0":        "some message"              
        },
    "messageCounter":    1,                          
    "nodeCounter":       2,                           
    "channels":                                       
        {
            "c0":        "Channel1"                
        },
    "channelCounter":    1                            
}"""
}