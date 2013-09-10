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

SBPM.Service.Process = {
	_default : {
		endpoint : "process.php"
	},
	query : function( param, defaultvalue, callback ) {
		return SBPM.DB.syncQuery( this._default.endpoint, param, defaultvalue, callback );
	},
	processExists : function( name ) {
		return name && name.length > 0 && SBPM.Service.Process.getProcessID(name) > 0;
	},
	saveProcess : function( graphAsJSON, startSubjectsAsJSON, name, forceOverwrite, saveAs, isProcess ) {
		var id;
		if( saveAs ) {

			// if process already exists
			if( !forceOverwrite && this.processExists(name) )

				// return error code
				return {
					code:"duplicated"
				};

			id = this.createProcess(name, forceOverwrite, isProcess);
		} else
			if( !this.processExists( name ) ) {
				id = this.createProcess( name, null, isProcess );
			}

		return this.query({
			"processid" : id || this.getProcessID( name ),
			"action" : "save",
			"graph" : graphAsJSON,
			"subjects" : startSubjectsAsJSON
		}, false, defaultOKReturnBoolean );
	},
	// create/remove process
	createProcess : function( processname, forceOverwrite, isProcess ) {
		// if the process should be overwritten
		if ( forceOverwrite )
			this.deleteProcess( processname );

		return this.query({
			"processname" : processname,
			"action" : "new",
			"isProcess": isProcess
		}, 0, SBPM.DB.defaultIDReturn );
	},

	createProcessFromTable : function( subjects, messages ){
		gf_createFromTable( subjects, messages );
		updateListOfSubjects();
	},
	deleteProcess : function( processname ) {
		return this.query({
			"processname" : processname,
			"action" : "remove"
		}, false, SBPM.DB.defaultRemoveReturn);
	},
	deleteProcessByID : function( processid ) {
		return this.query({
			"processid" : processid,
			"action" : "remove"
		}, false, SBPM.DB.defaultRemoveReturn );
	},
	// get processes
	getProcessName : function( processid ) {
		return this.query({
			"processid" : processid,
			"action" : "getname"
		}, "", function( json ) {
			if ( json["code"] == "ok" )
				return json["name"];
		});
	},
	getProcessID : function( processname ) {
		return this.query({
			"processname" : processname,
			"action" : "getid"
		}, 0, defaultIDReturn );
	},
	getIsProcess : function( processname ) {
		return this.query({
			"processname" : processname,
			"action" : "getIsProcess"
		}, true, function( json ) {
			if ( json['code'] === "ok" ) {
				return json['isProcess'] == "1";
			}
		});
	},
	getAllProcesses : function( limit, orderby ) {
		return this.query({
			"action" : "getallprocesses",
			"limit" : limit,
			"orderby": orderby
		}, "", function( json ) {
			if ( json["code"] == "ok" )
				return json["processes"];
		});
	},
	getAllProcessesIDs : function( limit ) {
		return this.query({
			"action" : "getallprocessesids",
			"limit" : limit
		}, "", function( json ) {
			if ( json["code"] == "ok" )
				return json["ids"];
		});
	},
	getAllStartableProcessIDs : function( userid ) {
		return this.query({
			"action" : "getallstartable",
			"userid" : userid
		}, "", function( json ) {
			if ( json["code"] == "ok" )
				return json["ids"];
		});
	},
	// load/save graph
	loadGraph : function( processid ) {
		return this.query({
			"processid" : processid,
			"action" : "load"
		}, "", function( json ) {
			if ( json["code"] == "ok" ) {
				try {
					return json["graph"];
				} catch ( e ) {
					return "{}";
				}
			} else {
				return "{}";
			}
		});
	},
	saveGraph : function( processid, graphAsJSON, startSubjectsAsJSON ) {
		return this.query({
			"processid" : processid,
			"action" : "save",
			"graph" : graphAsJSON,
			"subjects" : startSubjectsAsJSON
		}, false, SBPM.DB.defaultOKReturnBoolean );
	},

	/**
	 * Returns elements that are not send, receive, action or end elements or single subjects.
	 */
	isExecutbale : function( processID ){
		var graph = JSON.parse( self.loadGraph( processID ) ),
			error = [];

		//console.log(graph);
		//only single subjects
		graph.process.map( function( element ) {
			//console.log(element.type);
			if( element.type !== 'single' ) {
				error.push( element.type );
			}

			//only send, receive, action
			element.nodes.map(function( element ){
				var v = element.type;
				if( v !== "send" && v !== "receive" && v !== "action" && v !== "end" ) {
					error.push( element.type );
				}
			});
		});
		return error;
	}

}
