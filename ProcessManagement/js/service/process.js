SBPM.Service.Process = {
	newProcess : function(processName) {
        if(this.createProcess(processName) == 0) {
        	$("#freeow").freeow("Create process", "Could not create process \"" + processName +"\".", {
        		classes: [,"error"],
        		autohide: true
        	});
        } 
        else {
        	$("#freeow").freeow("Create process", "Process \"" + processName + "\" successfully created.", {
        		classes: [,"ok"],
        		autohide: true
        	});
        
        
        
        
        		//showverantwortliche();
        		//setSubjectIDs();
        
        
        }
	},

	loadProcess : function(processName) {

		gf_loadGraph(loadGraph(getProcessID(processName)));

	},
	saveAsProcess : function(processName) {

	},
	saveProcess : function() {

	},
	// create/remove process
createProcess : function (processname){
	return SBPM.DB.syncQuery("process.php", {"processname" : processname, "action" : "new"}, 0, defaultIDReturn);
},
deleteProcess : function (processname){
	return SBPM.DB.syncQuery("process.php", {"processname" : processname, "action" : "remove"}, false, defaultRemoveReturn);
},
deleteProcessByID : function (processid){
	return SBPM.DB.syncQuery("process.php", {"processid" : processid, "action" : "remove"}, false, defaultRemoveReturn);
},
// get processes
getProcessName : function (processid){
	return SBPM.DB.syncQuery("process.php", {"processid" : processid, "action" : "getname"}, "", function (json){
		if (json["code"] == "ok")
			return json["name"];});
},
getProcessID : function (processname){
	return SBPM.DB.syncQuery("process.php", {"processname" : processname, "action" : "getid"}, 0, defaultIDReturn);
},
getAllProcesses : function (){
	return SBPM.DB.syncQuery("process.php", {"action" : "getallprocesses"}, "", function (json){
		if (json["code"] == "ok")
			return json["processes"];});
},
getAllProcessesIDs : function (){
	return SBPM.DB.syncQuery("process.php", {"action" : "getallprocessesids"}, "", function (json){
		if (json["code"] == "ok")
			return json["ids"];});
},
getAllStartableProcessIDs : function (userid){
	return SBPM.DB.syncQuery("process.php", {"action" : "getallstartable", "userid" : userid}, "", function (json){
		if (json["code"] == "ok")
			return json["ids"];});
},
// load/save graph
loadGraph : function (processid){
	return SBPM.DB.syncQuery("process.php", {"processid" : processid, "action" : "load"}, "", function (json){
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
},
saveGraph : function (processid, graphAsJSON, startSubjectsAsJSON){
	return SBPM.DB.syncQuery("process.php", {"processid" : processid, "action" : "save", "graph" : graphAsJSON, "subjects" : startSubjectsAsJSON}, false, function (json){
		if (json["code"] == "ok")
			return true;});	
},
}
