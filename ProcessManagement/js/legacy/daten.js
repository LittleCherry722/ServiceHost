/*
 * S-BPM Groupware v1.2
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
	var content = "";

	var activeProcess = SBPM.VM.contentVM().processName();
	var isProcess = SBPM.Service.Process.getIsProcess(activeProcess);

	if(isProcess != true){
		//var users = getAllInstancesForUser(getUserID(SBPM.Storage.get("loggedin_user")));
		var users = getAllUsers();
		content = users;
		document.getElementById('AssignRoleWarning').innerHTML = "You have to create Users to assign them.";
	} else {
		var groups = getAllGroups();
		content = groups;
	}

	for(var i = 0; i < content.length; ++i)
	insert += "<option>" + content[i] +"</option>";
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

function clearListOfMacros(){
	console.log("Deprecated: daten.js");
	$("#slctMacro").val('').trigger("liszt:updated");
	$("#slctMacro").html('').trigger("liszt:updated");
}

function updateListOfMacros(){
	console.log("Deprecated: daten.js");
	
	clearListOfMacros();
	var macroList	= gf_getMacros();
	
	if (macroList.length > 0)
	{
		$('#slctMacro').chosen();
		
		var html = "<option></option>";
		
		for(var mid in macroList)
		{
			if ("" + mid == gf_getSelectedMacro())
			{
				html += "<option selected id=\""+mid+"\">"+macroList[mid]+"</option>";
			}
			else if (mid != "length")
			{
				html += "<option id=\""+mid+"\">"+macroList[mid]+"</option>";
			}
		}
		
		$('#slctMacro').html(html);
		$("#slctMacro").trigger("liszt:updated");
		$(".active-result").click(function(){gf_selectMacro($('#slctMacro option:selected').attr('id'))});
	}
	else
	{
		$('#slctMacro').hide();
	}
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


