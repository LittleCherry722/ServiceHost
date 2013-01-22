SBPM.Service.Instance = {


newInstance : function(name) {

	// create the instance

	SBPM.Storage.set("instanceid", createInstance(getProcessID(name)));
	//instanceID        = createInstance(getProcessID(name));

	SBPM.Storage.set("instanceProcessID", getProcessID(name));
	// get the data
	//var data          = loadInstanceData(instanceID);

	SBPM.Storage.set("instancedata", loadInstanceData(SBPM.Storage.get("instanceid")));
	// get the graph
	//var instancegraph = loadInstanceGraph(instanceID);

	SBPM.Storage.set("instancegraph", loadInstanceGraph(SBPM.Storage.get("instanceid")));
	// get our groups
	
	SBPM.Storage.set("userid", getUserID(SBPM.Storage.get("loggedin_user")));
	

	
},


abortInstance  : function (){
deleteInstance(SBPM.Storage.get("instanceid"));
$("#freeow").freeow("Instanz abbrechen", "Instance aborted.", {
	classes: [,"ok"],
	autohide: true
});
writeSumActiveInstances();
document.getElementById("welcome").style.display = "block";
document.getElementById('ausfuehrung').style.display = 'none';
document.getElementById("graph").style.display = "none";
}

}