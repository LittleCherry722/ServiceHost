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
	
	SBPM.VM.executionVM.showView();
	
/*	var groups = getAllGroupsForUser(SBPM.Storage.get("user").id);

	document.getElementById("welcome").style.display = "none";
	document.getElementById('ausfuehrung').style.display = 'block';
	document.getElementById("graph").style.display = "none";
	document.getElementById('instance_from_process').innerHTML = "Instance of process: " + name;
	document.getElementById("abortInstanceButton").style.display = "block";
	
	var insert = "<tr><td align=\"center\">Startknoten w&auml;hlen</td><td align=\"center\">";
	insert += "<table class=\"data\" width=\"60%\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:40%\">Subjekt</th><th style=\"width:60%\">Node</th></tr></thead><tbody>";
	for (group in groups){
		var groupid = groups[group];
		var nodes = findStartNodesForGroup(SBPM.Storage.get("instancegraph"), groupid);
		for (i = 0; i < nodes.length; i++){					
			insert += "<tr><td align=\"center\">" + getGroupName(groupid) + "</td><td align=\"center\"><input type=\"button\" value=\""+ nodes[i].text +"\" onClick=\"selectNextNode('"+ groupid +"','"+ nodes[i].id +"');writeSumActiveInstances();\"/></td></tr>";
		}
	}
	insert += "</tbody></table>";
	document.getElementById('instance_history').innerHTML = insert;
	*/
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