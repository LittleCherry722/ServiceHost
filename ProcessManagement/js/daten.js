/*
 * S-BPM Groupware v0.8
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2012 Johannes Decher, Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

//Variablen
var processName = false; //aktueller Prozessname

function testphp(){

console.log("Deprecated: daten.js");	
	
	 return error_log("false"=="false");
}

function setSubjectIDs() {
	console.log("Deprecated: daten.js");
var insert ="";
var groups = getAllGroups();
for(var i = 0; i < groups.length; ++i)
	insert += "<option>" + groups[i] +"</option>";
document.getElementById('ge_cv_id').innerHTML = insert;
//Fire change event for listeners
$('#ge_cv_id').change();
}

function writeSumActiveInstances() {
	console.log("Deprecated: daten.js");
var inst = getAllInstancesForUser(getUserID(SBPM.Storage.get("loggedin_user")));
var result = 0;
for (var i = 0; i < inst.length; ++i) {
	var data = loadInstanceData(inst[i]);
	if(typeof(data[getUserID(SBPM.Storage.get("loggedin_user"))]) == 'undefined') {result++;}
	else if(!data[getUserID(SBPM.Storage.get("loggedin_user"))]['done']) result++;
}
document.getElementById('runningInstances').innerHTML = "Running instances (" +result+")";
}

function einloggen(name, password) {
	console.log("Deprecated: daten.js");
    var json = SBPM.Service.Authentication.login(name, password);
    var user = json['user'];
      
    if(json['code'] == 'ok') {
        SBPM.Storage.set("user", JSON.stringify(json['user']));
    	SBPM.Storage.set("loggedin_user", user.name);
    	SBPM.Storage.set("userid", user.id);
    	return true;
    }
}
/*
function newProcess(name) {

console.log("Create PROCESS");

if(createProcess(name) == 0) {
	$("#freeow").freeow("Create process", "Could not create process \"" + name +"\".", {
		classes: [,"error"],
		autohide: true
	});
}
else {
	$("#freeow").freeow("Create process", "Process \"" + name + "\" successfully created.", {
		classes: [,"ok"],
		autohide: true
	});
	if(processName == false) {
		
		document.getElementById("welcome").style.display = "none";
		//document.getElementById('ausfuehrung').style.display = 'none';
		console.log("add ausfuehrung here again");
		document.getElementById("graph").style.display = "block";
		document.getElementById('process_name').innerHTML = "Process: " + name;
		document.getElementById("save").style.display = "block";
		document.getElementById("saveAs").style.display = "block";
		document.getElementById('tab3_user').innerHTML = "Person in charge for user: " + SBPM.Storage.get("loggedin_user");
		processName = name;
		setSubjectIDs();
		//gv_graph.clearGraph();
		
	}
}
}
*/

function GraphSpeichern() {

    var graphAsJSON = gv_graph.saveToJSON();
    
    var startSubjects = [];
    
    for (var subject in gv_graph.subjects)
        startSubjects.push(getGroupID(subject));
    
    var startSubjectsAsJSON = JSON.stringify(startSubjects);
    
      
    if(saveGraph(getProcessID(processName), graphAsJSON, startSubjectsAsJSON)) {
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

}

function GraphSpeichernAls(newName) {

	
	var graphAsJSON = gv_graph.saveToJSON();
    
    var startSubjects = [];
    
    for (var subject in gv_graph.subjects)
        startSubjects.push(getGroupID(subject));
    
    var startSubjectsAsJSON = JSON.stringify(startSubjects);
		
	createProcess(newName);
	
	//saveGraph(getProcessID(newName), graphAsJSON, startSubjectsAsJSON);
	
	    if(saveGraph(getProcessID(processName), graphAsJSON, startSubjectsAsJSON)) {
    	$("#freeow").freeow("Save process", "Process \"" + newName +"\" successfully saved.", {
    		classes: [,"ok"],
    		autohide: true
    	});
    } else {
    	$("#freeow").freeow("Save process", "Process \"" + newName + "\" could not be saved.", {
    		classes: [,"error"],
    		autohide: true
    	});
    }
}
function ProzessLaden(name) {

    console.log(arguments.callee);

    gv_graph.clearGraph();

	gv_graph.loadFromJSON(loadGraph(getProcessID(name)));

	processName = name;
	document.getElementById("welcome").style.display = "none";
	document.getElementById('ausfuehrung').style.display = 'none';
	document.getElementById("graph").style.display = "block";
	document.getElementById('process_name').innerHTML = "Process: " + processName;
	document.getElementById("save").style.display = "block";
	document.getElementById("saveAs").style.display = "block";
	document.getElementById('tab3_user').innerHTML = "Person in charge for user: " + SBPM.Storage.get("loggedin_user");
	shownothing();
	setSubjectIDs();
	$("#freeow").freeow("Load process", "Process \"" + name + "\" successfully loaded.", {
		classes: [,"ok"],
		autohide: true
	});
	updateListOfSubjects();
	$("input[id=tab2]").trigger("click");  

}



function clearListOfSubjects(){
	console.log("Deprecated: daten.js");
	$(".chzn-select").val('').trigger("liszt:updated");
	$(".chzn-select").html('').trigger("liszt:updated");
}

function updateListOfSubjects(){
	console.log("Deprecated: daten.js");
	
	//console.log(gv_graph.subjects);
	
	clearListOfSubjects();
	var html = "<option></option>";
	
	for(var subject in gv_graph.subjects){
	
	if (""+subject == gv_graph.selectedNode){

		html += "<option selected id=\""+subject+"\">"+gv_graph.subjects[subject].getText()+"</option>";
		
	}
	else{
		html += "<option id=\""+subject+"\">"+gv_graph.subjects[subject].getText()+"</option>";

	}
}


	$('#slctSbj').html(html);
	$("#slctSbj").trigger("liszt:updated");
	//Workaround, Chosen fails to reset, can't select same internal behavior twice with dropdown. Fix: add click listener to every chosen option (not native select option).
$(".active-result").click(function(){goToInternalBehaviorOf($('#slctSbj option:selected').attr('id'))});
}




function goToInternalBehaviorOf(subject){
	console.log("Deprecated: daten.js");
	//alert(subject);

	gv_graph.selectedSubject = null; 
	gf_clickedCVnode(subject);
	showtab1();
	updateListOfSubjects();
	
	SBPM.VM.contentVM().activeViewIndex(1);
}




function addHistory(data, userid, subjectid, node){ 
	console.log("Deprecated: daten.js");
     if(typeof(data[userid]) == 'undefined') data[userid] = JSON.parse("{}"); 
     if(typeof(data[userid]['history']) == 'undefined') data[userid]['history'] = new Array(); 
 
     var entry = JSON.parse("{}"); 
      
     entry['nodeid'] = node['id']; 
     entry['text']   = node['text']; 
     entry['type']   = "node"; 
 
     data[userid]['history'].push(entry); 
     data[SBPM.Storage.get("userid")]['subjectid'] = subjectid; 
     //console.log(data);
     SBPM.Storage.set("instancedata", data);

}


function setUserState(data, userid, node, done){
	console.log("Deprecated: daten.js");
	data[userid]['position'] = node['id'];
	data[userid]['done']     = done;
}

function findStartNodesForGroup(graph, subjectid){
	console.log("Deprecated: daten.js");
	//alert("findStartNodesForGroup: "+ JSON.stringify(subjectid) +" in "+ JSON.stringify(graph));
	var ret = new Array();

	for (group in graph){
		//console.log("group: " + group + " groupID: " + graph[group]['id'])
		if (getGroupID(graph[group]['id']) == subjectid){
					//if (graph[group]['id'] == subjectid){

			for (node in graph[group]['nodes']){  
				if (graph[group]['nodes'][node]['start']){
					ret.push((graph[group]['nodes'][node]));
				}
			}
		}
	}
	
	return ret;
}

/**
 * @return all edges which starts in node
 */
function findNodeEdges(graph, subjectid, node){
	console.log("Deprecated: daten.js");

	var ret = new Array();

	for (group in graph){
		if (getGroupID(graph[group]['id']) == subjectid){
			for (edge in graph[group]['edges']){  
				if (graph[group]['edges'][edge]['start'] == node['id']){
					ret.push(graph[group]['edges'][edge]);
				}
			}
		}
	}
	
	return ret;
}

function findNode(graph, subjectid, nodeid){
	console.log("Deprecated: daten.js");
	
	for (group in graph){
		if (getGroupID(graph[group]['id']) == subjectid){
			for (node in graph[group]['nodes']){  
				if (graph[group]['nodes'][node]['id'] == nodeid){
					return (graph[group]['nodes'][node]);
				}
			}
		}
	}
	return null;
}









function sendTextMessage(type, receiver){
	console.log("Deprecated: daten.js");
	var data = JSON.parse("{}");
	data['type'] = type;
	data['text'] = document.getElementById('tosend').value;
	var msgid = sendMessage(SBPM.Storage.get("instanceid"), SBPM.Storage.get("userid"), receiver, data);
	addHistoryMessage(SBPM.Storage.get("instancedata"), SBPM.Storage.get("userid"), getMessage(msgid), data, true);
	return true;
}
/*
function newInstance(name) {

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
	var groups = getAllGroupsForUser(SBPM.Storage.get("userid"));

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
}
*/


