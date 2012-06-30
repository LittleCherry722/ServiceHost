SBPM.Service.Process = {
	newProcess : function(processName) {
		console.log("newProcess " + processName);
        if(this.createProcess(processName) == false) {
        	SBPM.Notification.Error('Create process',"Could not create process \"" + processName +'"\.')
        } 
        else {
        	SBPM.Notification.Info("Create process", "Process \"" + processName + "\" successfully created.")
                }
	},


	processExists : function(name){
		if(name == '' | name == null)  return null;
		else{
		return getProcessID(name) > 0;
		}
	},


	loadProcess : function(processName) {

		gf_loadGraph(loadGraph(getProcessID(processName)));

	},
	saveAsProcess : function(newName) {
		
			var graphAsJSON = gv_graph.saveToJSON();
    
    var startSubjects = [];
    
    for (var subject in gv_graph.subjects)
        startSubjects.push(getGroupID(subject));
    
    var startSubjectsAsJSON = JSON.stringify(startSubjects);
		
	this.createProcess(newName);

	    if(this.saveGraph(getProcessID(processName), graphAsJSON, startSubjectsAsJSON)) {
    	$("#freeow").freeow("Save process", "Process \"" + newName +"\" successfully saved.", {
    		classes: [,"ok"],
    		autohide: true
    	});
    	this.loadProcess(newName);
    } else {
    	$("#freeow").freeow("Save process", "Process \"" + newName + "\" could not be saved.", {
    		classes: [,"error"],
    		autohide: true
    	});
    }
		
		
console.log("saveAs "+ newName);
	},
	saveProcess : function() {
		    var graphAsJSON = gv_graph.saveToJSON();
    
    var startSubjects = [];
    
    for (var subject in gv_graph.subjects)
        startSubjects.push(getGroupID(subject));
    
    var startSubjectsAsJSON = JSON.stringify(startSubjects);
    
      
    if(this.saveGraph(getProcessID(processName), graphAsJSON, startSubjectsAsJSON)) {
    	$("#freeow").freeow("Save process", "Process \"" + processName +"\" successfully saved.", {
    		classes: [,"ok"],
    		autohide: true
    	});
    } else {
    	$("#freeow").freeow("Save process", "Process \"" + processName + "\" could not be saved.", {
    		classes: [,"error"],
    		autohide: true
    	});
    }
		
		
		
		
console.log("save");
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
