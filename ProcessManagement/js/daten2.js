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

function resumeInstance(instanceid){
	// set the instance
	SBPM.Storage.set("instanceid", instanceid);
	SBPM.Storage.set("instanceProcessID", getProcessIDforInstance(instanceid));
	// get the data
	SBPM.Storage.set("instancedata", loadInstanceData(instanceid));
	// get the graph
	SBPM.Storage.set("instancegraph", loadInstanceGraph(instanceid));
	SBPM.Storage.set("userid", getUserID(SBPM.Storage.get("loggedin_user")));

	var data = SBPM.Storage.get("instancedata");
	/*
	document.getElementById("welcome").style.display = "none";
	document.getElementById('ausfuehrung').style.display = 'block';
	document.getElementById("graph").style.display = "none";
	
	document.getElementById('instance_from_process').innerHTML = "Instance of process: " + getProcessName(SBPM.Storage.get("instanceProcessID"));
	document.getElementById("abortInstanceButton").style.display = "block";
*/
	//delete last
	var count = data[SBPM.Storage.get("userid")]['history'].length;
	if (count > 0){
		var last = data[SBPM.Storage.get("userid")]['history'][count-1];
		data[SBPM.Storage.get("userid")]['history'].pop();
		/*if (count ==1)
			delete data[SBPM.Storage.get("userid")]['history'];*/
		SBPM.VM.executionVM.selectNextNode(data[SBPM.Storage.get("userid")]['subjectid'], last['nodeid']);
	}
}

function resumeInstanceMessage(msgid){

	setMessageRead(msgid);
	writeSumActiveInstances();
	
	var msg = getMessage(msgid);
	var data = JSON.parse(msg['data']);
	
	// set the instance
	SBPM.Storage.set("instanceid", msg['instanceid']);
	SBPM.Storage.set("instanceProcessID", getProcessIDforInstance(msg['instanceid']));
	// get the data
	SBPM.Storage.set("instancedata", loadInstanceData(SBPM.Storage.get("instanceid")));
	// get the graph
	SBPM.Storage.set("instancegraph", loadInstanceGraph(SBPM.Storage.get("instanceid")));
	SBPM.Storage.set("userid", getUserID(SBPM.Storage.get("loggedin_user")));
	
	document.getElementById("welcome").style.display = "none";
	document.getElementById('ausfuehrung').style.display = 'block';
	document.getElementById("graph").style.display = "none";
	document.getElementById('instance_from_process').innerHTML = "Instance of process: " + getProcessName(SBPM.Storage.get("instanceProcessID"));
	
	var insert = "<tr><td align=\"center\">Startknoten w&auml;hlen</td><td align=\"center\">";
	insert += "<table class=\"data\" width=\"60%\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:40%\">Subjekt</th><th style=\"width:60%\">Knoten</th></tr></thead><tbody>";
	
	var g = SBPM.Storage.get("instancegraph");
	var target = -1; var subjectid = "";
	for (group in g){
		for (edge in g[group]['edges']){
			if (g[group]['edges'][edge].text == data['type']){ // edge found
				var src = findNode(g, getGroupID(g[group]['id']), g[group]['edges'][edge].start);
				if (src['type'] == "receive"){
					target    = g[group]['edges'][edge].end;
					subjectid = getGroupID(g[group]['id']);
				}
			}
		}
	}
	
	if (target == -1){
		insert = "Kein Receive-Knoten f�r die Nachricht definiert!";
	}else{
		insert = "CONTINUE -> "+ target +"@"+ subjectid;
	}

	insert += "</tbody></table>";
	document.getElementById('instance_history').innerHTML = insert;

	addHistoryMessage(SBPM.Storage.get("instancedata"), SBPM.Storage.get("userid"), msg, data, false);
	saveInstanceData(SBPM.Storage.get("instanceid"), SBPM.Storage.get("instancedata")); // speichern

	selectNextNode(subjectid, target, data['text']);
}


function addHistoryMessage(data, userid, msg, msgdata, isSend){
	if(typeof(data[userid]) == 'undefined') data[userid] = JSON.parse("{}");
	if(typeof(data[userid]['history']) == 'undefined') data[userid]['history'] = new Array();

	var entry = JSON.parse("{}");
	
	entry['type']    = (isSend) ? "snd" : "rcv";
	entry['msgid']   = msg['id'];
	entry['msgtype'] = msgdata['type'];
	entry['text']    = msgdata['text'];
	entry['from']    = msg['from'];
	entry['to']      = msg['to'];

	data[userid]['history'].push(entry);
}