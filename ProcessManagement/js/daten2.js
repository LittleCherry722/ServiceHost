function resumeInstance(instanceid){
	// set the instance
	storage.set("instanceid", instanceid);
	storage.set("instanceProcessID", getProcessIDforInstance(instanceid));
	// get the data
	storage.set("instancedata", loadInstanceData(instanceid));
	// get the graph
	storage.set("instancegraph", loadInstanceGraph(instanceid));
	storage.set("userid", getUserID(storage.get("loggedin_user")));

	var data = storage.get("instancedata");
	
	document.getElementById("welcome").style.display = "none";
	document.getElementById('ausfuehrung').style.display = 'block';
	document.getElementById("graph").style.display = "none";
	document.getElementById('instance_from_process').innerHTML = "Instance of process: " + getProcessName(storage.get("instanceProcessID"));
	document.getElementById("abortInstanceButton").style.display = "block";
	
	//delete last
	var count = data[storage.get("userid")]['history'].length;
	if (count > 0){
		var last = data[storage.get("userid")]['history'][count-1];
		data[storage.get("userid")]['history'].pop();
		/*if (count ==1)
			delete data[storage.get("userid")]['history'];*/
		selectNextNode(data[storage.get("userid")]['subjectid'], last['nodeid']);
	}
}

function resumeInstanceMessage(msgid){

	setMessageRead(msgid);
	writeSumMsgs();
	writeSumActiveInstances();
	
	var msg = getMessage(msgid);
	var data = JSON.parse(msg['data']);
	
	// set the instance
	storage.set("instanceid", msg['instanceid']);
	storage.set("instanceProcessID", getProcessIDforInstance(msg['instanceid']));
	// get the data
	storage.set("instancedata", loadInstanceData(storage.get("instanceid")));
	// get the graph
	storage.set("instancegraph", loadInstanceGraph(storage.get("instanceid")));
	storage.set("userid", getUserID(storage.get("loggedin_user")));
	
	document.getElementById("welcome").style.display = "none";
	document.getElementById('ausfuehrung').style.display = 'block';
	document.getElementById("graph").style.display = "none";
	document.getElementById('instance_from_process').innerHTML = "Instance of process: " + getProcessName(storage.get("instanceProcessID"));
	
	var insert = "<tr><td align=\"center\">Startknoten w&auml;hlen</td><td align=\"center\">";
	insert += "<table class=\"data\" width=\"60%\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:40%\">Subjekt</th><th style=\"width:60%\">Knoten</th></tr></thead><tbody>";
	
	var g = storage.get("instancegraph");
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

	addHistoryMessage(storage.get("instancedata"), storage.get("userid"), msg, data, false);
	saveInstanceData(storage.get("instanceid"), storage.get("instancedata")); // speichern

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