/*
 * S-BPM Groupware v0.8
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2012 Thorsten Jacobi, Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

var db_directory = "db/";

var SBPM = {
    Constant : {
        DEBUG : true
    },
    Service : {},
    Utilities: {},
    VM : {},
    Dialog : {},
    Notification : {},
    Graph : {}
};

// turn off debug messages
if(!SBPM.Constant.DEBUG){
    window.console = {
        log : function(){}
    }
}

SBPM.DB = {    
    syncQuery : function(url, data, defaultreturn, successfunction ){
        var ret = defaultreturn;
        $.ajax({
          url: db_directory + url,
          data: data,
          cache: false,
          async: false,
          success: function(dataAsJson){
          
            if (dataAsJson !== ""){
                ret = successfunction(jQuery.parseJSON(dataAsJson),dataAsJson);
            }
          },
          error : function(err){
              console.log(err);
          }
        });
        return ret;
    },
    defaultIDReturn : function(json){
        if ((json["code"] == "added") || (json["code"] == "ok"))
            return json["id"];
        return 0;
    },
    defaultRemoveReturn : function(json){
        if (json["code"] == "removed")
            return true;
        return false;
    },
    defaultOKReturnBoolean : function(json){
        if (json["code"] == "ok")
            return true;
        return false;
    }
};


// wrapper for sync queries
function syncQuery(url, data, defaultreturn, successfunction ){
	var ret = defaultreturn;
	$.ajax({
	  url: url,
	  data: data,
	  cache: false,
	  async: false,
	  success: function(data){
		//alert(html);
		if (data != "")
			ret = successfunction(JSON.parse(data));
	  }
	});
	return ret;
}	

// default functions
function defaultIDReturn(json){
	if ((json["code"] == "added") || (json["code"] == "ok"))
		return json["id"];
	return 0;
}
function defaultRemoveReturn(json){
	if (json["code"] == "removed")
		return true;
	return false;
}
function defaultOKReturnBoolean(json){
	if (json["code"] == "ok")
		return true;
	return false;
}

// add/remove relationships
function createResponsibleForUserForGroup(userid, groupid, responsibleid, processid){
	return syncQuery(db_directory + "usersgroups.php", {"userid" : userid, "groupid" : groupid, "responsibleid" : responsibleid, "action" : "addrelation", "processid" : processid}, false, defaultOKReturnBoolean);
}
function removeResponsibleForUserForGroup(userid, groupid, responsibleid, processid){
	return syncQuery(db_directory + "usersgroups.php", {"userid" : userid, "groupid" : groupid, "responsibleid" : responsibleid, "action" : "removerelation", "processid" : processid}, false, defaultRemoveReturn);
}
function getResponsiblesForUserForGroup(userid, groupid, processid){
	return syncQuery(db_directory + "usersgroups.php", {"userid" : userid, "groupid" : groupid, "action" : "getrelations", "processid" : processid}, false, function (json){
		if (json["code"] == "ok")
			return json["users"];});
}
function getResponsiblesForUser(userid, processid){
	return syncQuery(db_directory + "usersgroups.php", {"userid" : userid, "action" : "getresponsiblesforuser", "processid" : processid}, false, function (json){
		if (json["code"] == "ok")
			return json;});
}
function getGroupIDforResponsibleUser(userid, processid){
	return syncQuery(db_directory + "usersgroups.php", {"userid" : userid, "action" : "getgroupIDforuser", "processid" : processid}, false, function (json){
		if (json["code"] == "ok")
			return json;});
}

// get ID's
function getUserID(name){
	return syncQuery(db_directory + "users.php", {"username" : name, "action" : "getid"}, 0, defaultIDReturn);
}
function getGroupID(name){
	return syncQuery(db_directory + "groups.php", {"groupname" : name, "action" : "getid"}, 0, defaultIDReturn);
}
// get Names
function getUserName(userid){
	return syncQuery(db_directory + "users.php", {"userid" : userid, "action" : "getname"}, "", function (json){
		if (json["code"] == "ok")
			return json["name"];});
}
function getGroupName(groupid){
	return syncQuery(db_directory + "groups.php", {"groupid" : groupid, "action" : "getname"}, "", function (json){
		if (json["code"] == "ok")
			return json["name"];});
}

// get users/groups
function getAllUsers(){
	return syncQuery(db_directory + "users.php", {"action" : "getallusers"}, {}, function (json){
		if (json["code"] == "ok")
			return json["users"];});
}
function getAllGroups(){
	return syncQuery(db_directory + "groups.php", {"action" : "getallgroups"}, {}, function (json){
		if (json["code"] == "ok")
			return json["groups"];});
}
function getallusersforgroup(group){
	return syncQuery(db_directory + "groups.php", {"action" : "getallusers", "groupid" : group}, {}, function (json){
		if (json["code"] == "ok")
			return json["users"];});
}

// create/remove process
function createProcess(processname){
	return syncQuery(db_directory + "process.php", {"processname" : processname, "action" : "new"}, 0, defaultIDReturn);
}
function deleteProcess(processname){
	return syncQuery(db_directory + "process.php", {"processname" : processname, "action" : "remove"}, false, defaultRemoveReturn);
}
function deleteProcessByID(processid){
	return syncQuery(db_directory + "process.php", {"processid" : processid, "action" : "remove"}, false, defaultRemoveReturn);
}
// get processes
function getProcessName(processid){
	return syncQuery(db_directory + "process.php", {"processid" : processid, "action" : "getname"}, "", function (json){
		if (json["code"] == "ok")
			return json["name"];});
}
function getProcessID(processname){
	return syncQuery(db_directory + "process.php", {"processname" : processname, "action" : "getid"}, 0, defaultIDReturn);
}
function getAllProcesses(){
	return syncQuery(db_directory + "process.php", {"action" : "getallprocesses"}, "", function (json){
		if (json["code"] == "ok")
			return json["processes"];});
}
function getAllProcessesIDs(){
	return syncQuery(db_directory + "process.php", {"action" : "getallprocessesids"}, "", function (json){
		if (json["code"] == "ok")
			return json["ids"];});
}
function getAllStartableProcessIDs(userid){
	return syncQuery(db_directory + "process.php", {"action" : "getallstartable", "userid" : userid}, "", function (json){
		if (json["code"] == "ok")
			return json["ids"];});
}
// load/save graph
function loadGraph(processid){
	return syncQuery(db_directory + "process.php", {"processid" : processid, "action" : "load"}, "", function (json){
		if (json["code"] == "ok"){
			try {
				return json["graph"];
			}catch (e){
				return "{}";
			}
		}else{
			return "{}";
		}
	});
}
function saveGraph(processid, graphAsJSON, startSubjectsAsJSON){
	return syncQuery(db_directory + "process.php", {"processid" : processid, "action" : "save", "graph" : graphAsJSON, "subjects" : startSubjectsAsJSON}, false, function (json){
		if (json["code"] == "ok")
			return true;});	
}

// instances
function createInstance(processid){
	return syncQuery(db_directory + "instance.php", {"processid" : processid, "action" : "new"}, 0, defaultIDReturn);
}
function deleteInstance(instanceid){
	return syncQuery(db_directory + "instance.php", {"instanceid" : instanceid, "action" : "delete"}, false, defaultOKReturnBoolean);
}

function getAllInstances(){
	return syncQuery(db_directory + "instance.php", {"action" : "getallinstances"}, 0, function (json){
		if (json["code"] == "ok")
			return json["instances"];});
}
function getAllInstancesForProcess(processid){
	return syncQuery(db_directory + "instance.php", {"processid" : processid, "action" : "getallinstances"}, 0, function (json){
		if (json["code"] == "ok")
			return json["instances"];});
}
function getAllInstancesForUser(userid){
	return syncQuery(db_directory + "instance.php", {"userid" : userid, "action" : "getallinstances"}, 0, function (json){
		if (json["code"] == "ok")
			return json["instances"];});
}

function loadInstanceData(instanceid){
	return syncQuery(db_directory + "instance.php", {"instanceid" : instanceid, "action" : "load"}, "", function (json){
		if (json["code"] == "ok"){
			try {
				var ret = JSON.parse(json["data"]);
				return ret;
			}catch (e){
				return JSON.parse("{}");
			}
		}else{
			return JSON.parse("{}");
		}
	});
}
function saveInstanceData(instanceid, data){
	var users = new Array(); 
	
	for (key in data){
		users.push([key]);
    }

	return syncQuery(db_directory + "instance.php", {"instanceid" : instanceid, "action" : "save", "data" : JSON.stringify(data), "involvedusers" : JSON.stringify(users)}, false, function (json){
		if (json["code"] == "ok")
			return true;});
}

function loadInstanceGraph(instanceid){
	return syncQuery(db_directory + "instance.php", {"instanceid" : instanceid, "action" : "graph"}, "", function (json){
		if (json["code"] == "ok"){
			try {
				var ret = JSON.parse(json["graph"]);
				return ret;
			}catch (e){
				return JSON.parse("{}");
			}
		}else{
			return JSON.parse("{}");
		}
	});
}
function getProcessIDforInstance(instanceid){
	return syncQuery(db_directory + "instance.php", {"instanceid" : instanceid, "action" : "getprocess"}, 0, defaultIDReturn);
}

// messages
function sendMessage(instanceid, fromid, toid, data){
	return syncQuery(db_directory + "messages.php", {"instanceid" : instanceid, "from" : fromid, "to" : toid, "data" : JSON.stringify(data), "action" : "send"}, 0, defaultIDReturn);
}
function setMessageRead(msgid){
	return syncQuery(db_directory + "messages.php", {"msgid" : msgid, "action" : "setread"}, false, defaultOKReturnBoolean);
}
function getMessages(instanceid, fromid, toid, read){
	if ((typeof(instanceid) == 'undefined')||(instanceid == null)) instanceid = -1;
	if ((typeof(fromid) == 'undefined')||(fromid  == null)) fromid = -1;
	if ((typeof(toid) == 'undefined')||(toid  == null)) toid = -1;
	if ((typeof(read) == 'undefined')||(read == null)) read = -1;
	return syncQuery(db_directory + "messages.php", {"instanceid" : instanceid, "from" : fromid, "to" : toid , "read" : read, "action" : "get"}, JSON.parse("{}"), function (json){
		if (json["code"] == "ok"){
			try {
				return json["msgs"];
			}catch (e){
				return JSON.parse("{}");
			}
		}else{
			return JSON.parse("{}");
		}
	});
}
function getMessage(msgid){
	return syncQuery(db_directory + "messages.php", {"msgid" : msgid, "action" : "get"}, JSON.parse("{}"), function (json){
		if (json["code"] == "ok"){
			try {
				if (json["msgs"].length == 1)
					return json["msgs"][0];
				else
					return json["msgs"];
			}catch (e){
				return JSON.parse("{}");
			}
		}else{
			return JSON.parse("{}");
		}
	});
}

function isLogedIn(){
	return syncQuery(db_directory + "auth.php", {}, false, function (json){
		if (json["code"] != "no login")
			return true;});
}