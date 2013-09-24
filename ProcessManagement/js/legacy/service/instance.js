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