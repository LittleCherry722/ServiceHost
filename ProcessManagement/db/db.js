// wrapper for sync queries
function syncQuery(url, data, defaultreturn, successfunction ){
	var ret = defaultreturn;
	$.ajax({
	  url: url,
	  data: data,
	  cache: false,
	  async: false,
	  success: function(html){
		//alert(html);
		if (html != "")
			ret = successfunction(JSON.parse(html));
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

var db_directory = "db/";

// add/remove users
function createUser(name){
	return syncQuery(db_directory + "users.php", {"username" : name, "action" : "add"}, 0, defaultIDReturn);
}

function deleteUser(name){
	return syncQuery(db_directory + "users.php", {"username" : name, "action" : "remove"}, false, defaultRemoveReturn);
}

// add/remove groups
function createGroup(name){
	return syncQuery(db_directory + "groups.php", {"groupname" : name, "action" : "add"}, 0, defaultIDReturn);
}

function deleteGroup(name){
	return syncQuery(db_directory + "groups.php", {"groupname" : name, "action" : "remove"}, false, defaultRemoveReturn);
}

// add/remove users to group
function addUserToGroup(userid, groupid){
	return syncQuery(db_directory + "usersgroups.php", {"userid" : userid, "groupid" : groupid, "action" : "addgroup"}, 0, defaultIDReturn);
}
function removeUserFromGroup(userid, groupid){
	return syncQuery(db_directory + "usersgroups.php", {"userid" : userid, "groupid" : groupid, "action" : "removegroup"}, false, defaultRemoveReturn);
}
function getAllGroupsForUser(userid){
	return syncQuery(db_directory + "usersgroups.php", {"userid" : userid, "action" : "getgroups"}, {}, function (json){
		if (json["code"] == "ok")
			return json["groups"];});
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
function saveGraph(processid, graph){
    var subjects = new Array();;
      
     for (key in graph){   
          for (node in graph[key]['nodes']){   
               if ((graph[key]['nodes'][node]['start']) && (graph[key]['nodes'][node]['type'] != "receive")){ 
                    subjects.push(getGroupID(graph[key]['id'])); 
                    break; 
               } 
          } 
    }

	return syncQuery(db_directory + "process.php", {"processid" : processid, "action" : "save", "graph" : JSON.stringify(graph), "subjects" : JSON.stringify(subjects)}, false, function (json){
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

// login/relog/logout
function login(name){
	return syncQuery(db_directory + "auth.php", {"username" : name, "action" : "login"}, {}, function (json){
		return json;});
}
function isLogedIn(){
	return syncQuery(db_directory + "auth.php", {}, false, function (json){
		if (json["code"] != "no login")
			return true;});
}