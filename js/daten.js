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


function showverantwortliche() {
var groups = getAllGroups();
var insert = "";
for (var x = 0; x < groups.length; ++x) {
	var responsibles = getResponsiblesForUserForGroup(getUserID(storage.get("loggedin_user")), getGroupID(groups[x]), getProcessID(processName));
	if (responsibles.length == 0) insert += "<tr><td align=\"center\">" + groups[x] + "</td><td></td><td align=\"center\"></td></tr>";
	for (var i = 0; i < responsibles.length; ++i) {
		insert += "<tr><td align=\"center\">" + groups[x] + "</td><td align=\"center\">" + getUserName(responsibles[i]) + "</td><td align=\"center\"><a style=\"cursor:pointer\" onclick=\"removeResponsibleForUserForGroup("+getUserID(storage.get("loggedin_user"))+ ","+getGroupID(groups[x])+","+responsibles[i]+","+getProcessID(processName)+");showverantwortliche();\" >L&ouml;schen</a></td></tr>";
	}
}
document.getElementById('responsibles').innerHTML = insert;
}

function setSubjectIDs() {
var insert ="";
var groups = getAllGroups();
for(var i = 0; i < groups.length; ++i)
	insert += "<option>" + groups[i] +"</option>";
document.getElementById('ge_cv_id').innerHTML = insert;
//Fire change event for listeners
$('#ge_cv_id').change();
}

function writeSumMsgs() {
var msgs = getMessages(-1, -1, getUserID(parent.storage.get("loggedin_user")), 0);
if(msgs.length > 1)
	document.getElementById('newMSG').innerHTML = msgs.length + " new messages";
else if(msgs.length == 1)
	document.getElementById('newMSG').innerHTML = msgs.length + " new messages";
else
	document.getElementById('newMSG').innerHTML = "no new messages";
	
window.setTimeout("writeSumMsgs()", 120000);
}

function writeSumActiveInstances() {
var inst = getAllInstancesForUser(getUserID(storage.get("loggedin_user")));
var result = 0;
for (var i = 0; i < inst.length; ++i) {
	var data = loadInstanceData(inst[i]);
	if(typeof(data[getUserID(storage.get("loggedin_user"))]) == 'undefined') {result++;}
	else if(!data[getUserID(storage.get("loggedin_user"))]['done']) result++;
}
document.getElementById('runningInstances').innerHTML = "Running instances (" +result+")";
}

function einloggen(name) {
code = login(name);
if(code['code'] == 'ok') {
	storage.set("loggedin_user", name);
	storage.set("userid", getUserID(name));
	return true;
}
}

function newUser(name) {

if(createUser(name) == 0) {
	$("#freeow").freeow("Create new user", "Could not create user \"" + name +"\".", {
		classes: [,"error"],
		autohide: true
	});
}
else {
	$("#freeow").freeow("Create user", "User \"" + name + "\" successfully created.", {
		classes: [,"ok"],
		autohide: true
	});
}
}

function newGroup(name) {

if(createGroup(name) == 0) {
	$("#freeow").freeow("Create group", "Could not create group \"" + name +"\".", {
		classes: [,"error"],
		autohide: true
	});
}
else {
	$("#freeow").freeow("Create group", "Group \"" + name + "\" successfully created", {
		classes: [,"ok"],
		autohide: true
	});
}
showverantwortliche();
setSubjectIDs();
}

function newProcess(name) {

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
		document.getElementById('ausfuehrung').style.display = 'none';
		document.getElementById("graph").style.display = "block";
		document.getElementById('process_name').innerHTML = "Process: " + name;
		document.getElementById("save").style.display = "block";
		document.getElementById("saveAs").style.display = "block";
		document.getElementById('tab3_user').innerHTML = "Person in charge for user: " + storage.get("loggedin_user");
		processName = name;
		showverantwortliche();
		setSubjectIDs();
		//gv_graph.clearGraph();
	}
}
}

function user_to_group(user, group) {

if(addUserToGroup(getUserID(user), getGroupID(group)) != 0) {
	$("#freeow").freeow("Group assignment", "User \"" + user +"\" could not be assigned to group \"" + group + "\".", {
		classes: [,"error"],
		autohide: true
	});
}
else {
	$("#freeow").freeow("Group assignment", "User \"" + user +"\" successfully assigned to group \"" + group + "\".", {
		classes: [,"ok"],
		autohide: true
	});
}
}

function addResponsible(user, group) {

if(createResponsibleForUserForGroup(getUserID(storage.get("loggedin_user")), getGroupID(group), getUserID(user), getProcessID(processName))) {
		$("#freeow").freeow("Person in charge", "User \"" + user +"\" was added as person in charge for group \"" + group + "\".", {
		classes: [,"ok"],
		autohide: true
	});
}
else {
	$("#freeow").freeow("Group assignment", "User \"" + user +"\" could not be added as person in charge for group \"" + group + "\".", {
		classes: [,"error"],
		autohide: true
	});
}
showverantwortliche();
}

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

    gv_graph.clearGraph();

	gv_graph.loadFromJSON(loadGraph(getProcessID(name)));

	processName = name;
	document.getElementById("welcome").style.display = "none";
	document.getElementById('ausfuehrung').style.display = 'none';
	document.getElementById("graph").style.display = "block";
	document.getElementById('process_name').innerHTML = "Process: " + processName;
	document.getElementById("save").style.display = "block";
	document.getElementById("saveAs").style.display = "block";
	document.getElementById('tab3_user').innerHTML = "Person in charge for user: " + storage.get("loggedin_user");
	shownothing();
	showverantwortliche();
	setSubjectIDs();
	$("#freeow").freeow("Load process", "Process \"" + name + "\" successfully loaded.", {
		classes: [,"ok"],
		autohide: true
	});
	updateListOfSubjects();
	$("input[id=tab2]").trigger("click");  

}

function clearListOfSubjects(){
	$(".chzn-select").val('').trigger("liszt:updated");
	$(".chzn-select").html('').trigger("liszt:updated");
}

function updateListOfSubjects(){
	
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

}




function goToInternalBehaviorOf(subject){
	//alert(subject);

	gv_graph.selectedSubject = null; 
	gf_clickedCVnode(subject);
	showtab1();
	updateListOfSubjects();
	
}




function addHistory(data, userid, subjectid, node){ 
     if(typeof(data[userid]) == 'undefined') data[userid] = JSON.parse("{}"); 
     if(typeof(data[userid]['history']) == 'undefined') data[userid]['history'] = new Array(); 
 
     var entry = JSON.parse("{}"); 
      
     entry['nodeid'] = node['id']; 
     entry['text']   = node['text']; 
     entry['type']   = "node"; 
 
     data[userid]['history'].push(entry); 
     data[storage.get("userid")]['subjectid'] = subjectid; 
}


function setUserState(data, userid, node, done){
	data[userid]['position'] = node['id'];
	data[userid]['done']     = done;
}

function findStartNodesForGroup(graph, subjectid){
	//alert("findStartNodesForGroup: "+ JSON.stringify(subjectid) +" in "+ JSON.stringify(graph));
	var ret = new Array();

	for (group in graph){
		if (getGroupID(graph[group]['id']) == subjectid){
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

function drawHistory(data) {
	var insert = "";
	if((typeof(data[storage.get("userid")]) != 'undefined') && (typeof(data[storage.get("userid")]['history']) != 'undefined')) {
		for(var i = 0; i < data[storage.get("userid")]['history'].length; i++) {
			if (data[storage.get("userid")].history[i].type == "node"){
				var text = data[storage.get("userid")].history[i].text;
				if(data[storage.get("userid")].history[i].text == "S") text = "Message sent";
				if(data[storage.get("userid")].history[i].text == "R") text = "Wait for messages";
				insert += "<tr><td align=\"center\">"+ (i+1) +"<td align=\"center\">"+ text +"</td></tr>";
			}else if (data[storage.get("userid")].history[i].type == "rcv"){
				//insert += "<tr><td align=\"center\">"+ (i+1) +"<td align=\"center\"> [RVC]MSG !!! </td></tr>"
				insert += "<tr><td align=\"center\">"+ (i+1) +"</td><td align=\"center\">Message received<br><br>";
				insert += "<table class=\"data\" style=\"width:600px\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:30%\">Von</th><th style=\"width:30%\">Typ</th><th style=\"width:40%\">Message</th></tr></thead><tbody>";
				insert += "<tr><td align=\"center\">"+getUserName(data[storage.get("userid")].history[i]['from'])+"</td><td align=\"center\">"+data[storage.get("userid")].history[i]['msgtype']+"</td><td><pre style=\"float:left\">"+data[storage.get("userid")].history[i]['text']+"</pre></td></tr></tbody></table></td></tr>";
			}else if (data[storage.get("userid")].history[i].type == "snd"){
				//insert += "<tr><td align=\"center\">"+ (i+1) +"<td align=\"center\"> [SND]MSG !!! </td></tr>";
				insert += "<tr><td align=\"center\">"+ (i+1) +"</td><td align=\"center\">Message sent<br><br>";
				insert += "<table class=\"data\" style=\"width:600px\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:30%\">An</th><th style=\"width:30%\">Typ</th><th style=\"width:40%\">Message</th></tr></thead><tbody>";
				insert += "<tr><td align=\"center\">"+getUserName(data[storage.get("userid")].history[i]['to'])+"</td><td align=\"center\">"+data[storage.get("userid")].history[i]['msgtype']+"</td><td><pre style=\"float:left\">"+data[storage.get("userid")].history[i]['text']+"</pre></td></tr></tbody></table></td></tr>";
			}
		}
	}
	document.getElementById('instance_history').innerHTML = insert;
}

function selectNextNode(subjectid, nodeid, msgtext){
	alert(subjectid +"->"+ nodeid);
	var data = storage.get("instancedata");
	drawHistory(data);
	
	var node = findNode(storage.get("instancegraph"), subjectid, nodeid);
	addHistory(storage.get("instancedata"), storage.get("userid"),subjectid, node);	// < aktuelle node
	
	// TODO the current node is known here -> highlight it in canvas
	
	saveInstanceData(storage.get("instanceid"), storage.get("instancedata")); // speichern
	var insert = "";
	
	// node anzeigen
	//alert(JSON.stringify(node));
	if (node['type'] == "action") {
		insert += "<tr><td align=\"center\">"+storage.get("instancedata")[storage.get("userid")]['history'].length +"</td><td align=\"center\">"+ node['text'] +"<br><br>";
		insert += "<table class=\"data\" style=\"width:200px\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:100%\">Next</th></tr></thead><tbody id=\"TableContent\"></tbody></table>";
		document.getElementById('instance_history').innerHTML += insert;
	}
	else if (node['type'] == "send"){
		insert += "<tr><td align=\"center\">"+ storage.get("instancedata")[storage.get("userid")]['history'].length +"</td><td align=\"center\"><p><label>"+"Nachricht:" /*"+ data[storage.get("userid")].history[storage.get("instancedata")[storage.get("userid")]['history'].length-1].text +":*/ +"</label><br><textarea id=\"tosend\" style=\"resize:none;height:100px;width:600px\"></textarea><br><br><br>";
		insert += "<form><table class=\"data\" style=\"width:600px\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:30%\">Group</th><th style=\"width:50%\">Person in charge</th><th style=\"width:20%\">Send</th></tr></thead><tbody id=\"TableContent\"></tbody></table>";
		document.getElementById('instance_history').innerHTML += insert;
	}
	else if (node['type'] == "receive") {
		insert += "<tr><td align=\"center\">"+storage.get("instancedata")[storage.get("userid")]['history'].length +"</td><td align=\"center\">Wait for messages:<br><br>";
		insert += "<table class=\"data\" style=\"width:400px\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:50%\">Von (Gruppe)</th><th style=\"width:50%\">Typ</th></tr></thead><tbody id=\"TableContent\"></tbody></table>";
		document.getElementById('instance_history').innerHTML += insert;
	}
	else if (node['type'] == "end"){
		insert += "<tr><td align=\"center\">"+storage.get("instancedata")[storage.get("userid")]['history'].length +"</td><td align=\"center\">"+ node['text'] +"<br><br><b>Instance stopped.</b>";
		document.getElementById('instance_history').innerHTML += insert + "</td></tr>";
		storage.get("instancedata")[storage.get("userid")]['done'] = true;
		saveInstanceData(storage.get("instanceid"), storage.get("instancedata")); // speichern
		document.getElementById("abortInstanceButton").style.display = "none";
		return;
	}
	// nachfolger finden
	var nodeedges = findNodeEdges(storage.get("instancegraph"), subjectid, node);
	//alert(JSON.stringify(nodeedges));
	
	// option(en) anzeigen
	var TableInsert = "";
	for (i = 0; i < nodeedges.length; i++){
		var buttonText = nodeedges[i]['text'];
		if(buttonText == "") buttonText = "Weiter";
		// schreiben anpassen
		if (node['type'] == "receive"){
			TableInsert += "<tr><td align=\"center\">"+ nodeedges[i]['target'] +"</td><td align=\"center\">"+ buttonText + "</td></tr>";
		}
		else {
			if ( nodeedges[i]['target'] != ""){
				var receiver = "";
				receiver = getResponsiblesForUserForGroup(storage.get("userid"), getGroupID(nodeedges[i].target), storage.get("instanceProcessID"));
				if(receiver == "") {
					var users = getallusersforgroup(getGroupID(nodeedges[i].target));
					TableInsert += "<tr><td align=\"center\">"+ nodeedges[i].target + "</td><td align=\"center\"><select id=\"receive_user"+i+"\">";
					for(var x = 0; x < users.length; x++) {
						TableInsert += "<option>"+ getUserName(users[x]) +"</option>";
					}
					TableInsert += "</select></td><td align=\"center\"><input type=\"button\" value=\""+ nodeedges[i].text.replace(/<br>/gi, " ") +"\" onClick=\"if (sendTextMessage('"+ buttonText +"', getUserID(this.form.receive_user"+i+".options[this.form.receive_user"+i+".selectedIndex].value))) selectNextNode('"+ subjectid +"','"+ nodeedges[i]['end'] +"');\" /></td></tr>"
				} else {
					for(var x = 0; x < receiver.length; x++)
						TableInsert += "<tr><td align=\"center\">"+ nodeedges[i].target + "</td><td align=\"center\">"+ getUserName(receiver[x]) +"</td><td align=\"center\"><input type=\"button\" value=\""+buttonText.replace(/<br>/gi, " ")+"\" onClick=\"if (sendTextMessage('"+ buttonText +"','"+ receiver[x] +"')) selectNextNode('"+ subjectid +"','"+ nodeedges[i]['end'] +"');\" /></tr>";
				}
				
			}

			else
				TableInsert += "<tr><td align=\"center\"><input type=\"button\" value=\""+ buttonText +"\" onClick=\"selectNextNode('"+ subjectid +"','"+ nodeedges[i]['end'] +"');\" /></td></tr>";
		}
	}

	document.getElementById('TableContent').innerHTML += TableInsert;
	
}

function sendTextMessage(type, receiver){
	var data = JSON.parse("{}");
	data['type'] = type;
	data['text'] = document.getElementById('tosend').value;
	var msgid = sendMessage(storage.get("instanceid"), storage.get("userid"), receiver, data);
	addHistoryMessage(storage.get("instancedata"), storage.get("userid"), getMessage(msgid), data, true);
	return true;
}

function newInstance(name) {

	// create the instance
	storage.set("instanceid", createInstance(getProcessID(name)));
	//instanceID        = createInstance(getProcessID(name));
	storage.set("instanceProcessID", getProcessID(name));
	// get the data
	//var data          = loadInstanceData(instanceID);
	storage.set("instancedata", loadInstanceData(storage.get("instanceid")));
	// get the graph
	//var instancegraph = loadInstanceGraph(instanceID);
	storage.set("instancegraph", loadInstanceGraph(storage.get("instanceid")));
	// get our groups
	storage.set("userid", getUserID(storage.get("loggedin_user")));
	var groups = getAllGroupsForUser(storage.get("userid"));

	document.getElementById("welcome").style.display = "none";
	document.getElementById('ausfuehrung').style.display = 'block';
	document.getElementById("graph").style.display = "none";
	document.getElementById('instance_from_process').innerHTML = "Instance of process: " + name;
	document.getElementById("abortInstanceButton").style.display = "block";
	
	var insert = "<tr><td align=\"center\">Startknoten w&auml;hlen</td><td align=\"center\">";
	insert += "<table class=\"data\" width=\"60%\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:40%\">Subjekt</th><th style=\"width:60%\">Node</th></tr></thead><tbody>";
	for (group in groups){
		var groupid = groups[group];
		var nodes = findStartNodesForGroup(storage.get("instancegraph"), groupid);
		for (i = 0; i < nodes.length; i++){					
			insert += "<tr><td align=\"center\">" + getGroupName(groupid) + "</td><td align=\"center\"><input type=\"button\" value=\""+ nodes[i].text +"\" onClick=\"selectNextNode('"+ groupid +"','"+ nodes[i].id +"');writeSumActiveInstances();\"/></td></tr>";
		}
	}
	insert += "</tbody></table>";
	document.getElementById('instance_history').innerHTML = insert;
}

function abortInstance(){
deleteInstance(storage.get("instanceid"));
$("#freeow").freeow("Instanz abbrechen", "Instance aborted.", {
	classes: [,"ok"],
	autohide: true
});
writeSumActiveInstances();
document.getElementById("welcome").style.display = "block";
document.getElementById('ausfuehrung').style.display = 'none';
document.getElementById("graph").style.display = "none";
}

