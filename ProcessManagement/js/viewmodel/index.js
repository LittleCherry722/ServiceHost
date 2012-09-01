var ViewModel = function() {

	var self = this;

	self.init = function(callback) {
		self.user = ko.observable();
		self.activeViewIndex = ko.observable(0);

		self.menuVM = new MenuViewModel();
		self.headerVM = new HeaderViewModel();
		self.processVM = new ProcessViewModel();
		self.homeVM = new HomeViewModel();
		self.executionVM = new ExecutionViewModel();

        self.mainViews = [self.homeVM, self.processVM, self.executionVM];

		self.menuVM.init();
		self.headerVM.init();
		self.homeVM.init();
		self.processVM.init();
		self.executionVM.init();

        callback();
	}
	
	self.activeView = function() {
		return self.mainViews[self.activeViewIndex()]
	};
}

var MenuViewModel = function() {

	var self = this;

	self.recentProcesses = ko.observableArray();
	self.maxRecent = 5;

	self.init = function() {
		self.recentProcesses(SBPM.Service.Process.getAllProcesses(self.maxRecent));
	}

}
var HeaderViewModel = function() {

	var self = this;

	self.userName = ko.observable("no user");
	self.messageCount = ko.observable(0);

	self.init = function() {

		if (SBPM.Storage.get("user")) {
			console.log("User: " + SBPM.Storage.get("user"));

			self.userName(SBPM.Storage.get("user").name);
			messageCheck();
		}
	}

	self.messageCountString = ko.computed(function() {
		return self.messageCount() < 1 ? "no new messages" : self.messageCount() + " new messages ";
	});

	function messageCheck() {
		self.messageCount(SBPM.Service.Message.countNewMessages(SBPM.Storage.get("user").id));

		setTimeout(messageCheck, 120000);
	}
	
}
var HomeViewModel = function() {

	var self = this;

	self.name = "homeView";
	self.label = "Home";

	self.init = function() {
	}

	self.afterRender = function() {
	}
	self.showView = function() {
		SBPM.VM.activeViewIndex(0);
	}
}
var ProcessViewModel = function() {

	var self = this;

	self.processName = ko.observable();

	self.name = "processView";
	self.label = "Process";

	self.subjectVM = new SubjectViewModel();
	self.internalVM = new InternalViewModel();
	self.chargeVM = new ChargeViewModel();


	self.init = function() {
		self.subjectVM.init();
		self.internalVM.init();
		self.chargeVM.init();
	}

	self.processViews = [self.subjectVM, self.internalVM, self.chargeVM];

	self.activeViewIndex = ko.observable(0);

	self.activeView = function() {
		return self.processViews[self.activeViewIndex()]
	};
	
	self.showProcess = function(processName) {
	    
	    try{
            $("#tab2").click();
           
            gv_graph.clearGraph(true);
            
            var processId = SBPM.Service.Process.getProcessID(processName);
            
            self.subjectVM.showView();
            
            gv_graph.clearGraph();
            
            /* processId:
             *  0    -> new process
             *  1..n -> old process
             */
            if(processId > 0){
            
                var graphAsJson = SBPM.Service.Process.loadGraph(processId);
                
                console.log("load graph: "+graphAsJson);
                
                gv_graph.loadFromJSON(graphAsJson);
            
            }else{
                
                console.log("load empty graph");
                
                gv_graph.loadFromJSON("{}");
                
            }
            
            self.processName(processName);
            
            
            
            // TODO replace this DEPRECATED CALLS!
            setSubjectIDs(); 
            $("#tab2").addClass("active");
            // TODO END
            
            SBPM.Notification.Info("Information", "Process \""+processName+"\" successfully loaded.");
    	        
	    }catch(e){
	        
	        SBPM.Notification.Error("Error", "Could not load process \""+processName+"\".");
	        
	        console.log("ProcessViewModel: Could not load process: "+e);
	        
	    }
	    

		updateListOfSubjects();
		return processId;
	}
}

var SubjectViewModel = function() {

	var self = this;

	self.name = "subjectView";
	self.label = "Subject-Interaction-View";

	self.init = function() {

	}

	self.showView = function() {
		SBPM.VM.activeViewIndex(1);
		SBPM.VM.processVM.activeViewIndex(0);

	}

	self.afterRender = function() {

	}
}
var InternalViewModel = function() {

	var self = this;

	self.name = "internalView";
	self.label = "Internal-Behavior-View";

	self.init = function() {
	}

	self.showView = function() {
		SBPM.VM.activeViewIndex(1);
		SBPM.VM.processVM.activeViewIndex(1);
	}
	
}
var ChargeViewModel = function() {

	var self = this;

	self.name = "chargeView";
	self.label = "Person in charge";

    self.data = {
        responsibilities : ko.observableArray([]),   // {groupName, subjectProvider}
        routings : ko.observableArray([])           // {fromSubject, fromSubjectprovider, messageType, toSubject, toSubjectprovider}
    };

    self.lists = {};

    // responsibilities : userName
    self.unusedSubjectProviders = ko.computed(function() {
        var subjectProviders = [];
        
        self.lists.subjectNames.each(function(){
            var subjectName = $(this);
            self.data.responsibilities.each(function(){
                var responsibility = $(this);
                
                if(subjectName != responsibility.subjectProvider)
                    subjectProviders.push(subjectName);
            });
            
        });
        
        return subjectProviders;
    });

    // responsibilities : fromSubject
    self.fromSubjectNames = ko.computed(function() {
        var fromSubjects = [];
        
        for(var row in self.lists.messageTypes)
            if($.inArray(row.sender, fromSubjects) < 0)
                fromSubjects.push(row.sender);

        return fromSubjects;
    });

    // routings : messageType
    self.availableMessageTypes = ko.computed(function() {
        var messageTypes = [];
        
        for(var row in self.lists.messageTypes)
            if($.inArray(row.sender, self.lists.subjectNames) < 0)
        
        return self.list.messageTypes.map(function(row) { return row.messageType });
    });

    // responsibilities : toSubject
    self.toSubjectNames = ko.computed(function() {
        var toSubjects = [];
        
        for(var row in self.lists.messageTypes)
            if($.inArray(row.receiver, toSubjects) < 0)
                toSubjects.push(row.receiver);

        return toSubjects;
    });

	self.init = function() {

        self.lists.subjects = gf_getSubjects();
        self.lists.subjectNames = gf_getSubjectNames();
        self.lists.subjectProviders = SBPM.Service.User.getAll().map(function(user){ return user.name; });
        self.lists.messageTypes = gf_getMessageTypes(); // {sender, messageType, receiver}

        console.log(self.lists);

        var defaultValue = {
            responsibilities : [{
                groupName : "",
                subjectProvider : ""
            }],
            routings : [{
                fromSubject : "",
                fromSubjectprovider : "",
                messageType : "",
                toSubject : "",
                toSubjectprovider : ""
            }]
        }

        self.data.responsibilities(defaultValue.responsibilities);
        self.data.routings(defaultValue.routings);
          
	}

    self.changeSender = function(element, i){
        // reset messageType & receiver
    }

    self.changeMessageType = function(element, i){
        // reset receiver
    }

    self.addRouting = function(){
        self.data.routings().push({
                fromSubject : "",
                fromSubjectprovider : "",
                messageType : "",
                toSubject : "",
                toSubjectprovider : ""
            });
    }

    self.removeRouting = function(element){
        self.data.routings().remove(element);
    }

	self.showView = function() {
		SBPM.VM.activeViewIndex(1);
		SBPM.VM.processVM.activeViewIndex(2);
	}

}

var ExecutionViewModel = function() {

	var self = this;
	self.name = "executionView";
	self.lable = "Execution";

	self.courseVM = new CourseViewModel();
	self.instanceVM = new InstanceViewModel();

	self.init = function() {

		self.courseVM.init();
		self.instanceVM.init();
	}

	self.showView = function() {
		SBPM.VM.activeViewIndex(2);
	}

	self.executionViews = [self.courseVM, self.instanceVM];

	self.activeViewIndex = ko.observable(0);
	
	self.activeView = function() {
		return self.executionViews[self.activeViewIndex()];
	}

    self.newInstance = function (name){
    	 	SBPM.Storage.set("inctanceStepSubjectID",null);
     		SBPM.Storage.set("inctanceStepNodeID",null);
    		SBPM.Service.Instance.newInstance(name);
    		self.showView();
    	
    		self.drawInstanceState();
    }


	self.drawHistory = function(data) {
	var insert = "";
	if((typeof(data[SBPM.Storage.get("userid")]) != 'undefined') && (typeof(data[SBPM.Storage.get("userid")]['history']) != 'undefined')) {
		for(var i = 0; i < data[SBPM.Storage.get("userid")]['history'].length; i++) {
			if (data[SBPM.Storage.get("userid")].history[i].type == "node"){
				var text = data[SBPM.Storage.get("userid")].history[i].text;
				if(data[SBPM.Storage.get("userid")].history[i].text == "S") text = "Message sent";
				if(data[SBPM.Storage.get("userid")].history[i].text == "R") text = "Wait for messages";
				insert += "<tr><td align=\"center\">"+ (i+1) +"<td align=\"center\">"+ text +"</td></tr>";
			}else if (data[SBPM.Storage.get("userid")].history[i].type == "rcv"){
				//insert += "<tr><td align=\"center\">"+ (i+1) +"<td align=\"center\"> [RVC]MSG !!! </td></tr>"
				insert += "<tr><td align=\"center\">"+ (i+1) +"</td><td align=\"center\">Message received<br><br>";
				insert += "<table class=\"data\" style=\"width:600px\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:30%\">Von</th><th style=\"width:30%\">Typ</th><th style=\"width:40%\">Message</th></tr></thead><tbody>";
				insert += "<tr><td align=\"center\">"+getUserName(data[SBPM.Storage.get("userid")].history[i]['from'])+"</td><td align=\"center\">"+data[SBPM.Storage.get("userid")].history[i]['msgtype']+"</td><td><pre style=\"float:left\">"+data[SBPM.Storage.get("userid")].history[i]['text']+"</pre></td></tr></tbody></table></td></tr>";
			}else if (data[SBPM.Storage.get("userid")].history[i].type == "snd"){
				//insert += "<tr><td align=\"center\">"+ (i+1) +"<td align=\"center\"> [SND]MSG !!! </td></tr>";
				insert += "<tr><td align=\"center\">"+ (i+1) +"</td><td align=\"center\">Message sent<br><br>";
				insert += "<table class=\"data\" style=\"width:600px\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:30%\">An</th><th style=\"width:30%\">Typ</th><th style=\"width:40%\">Message</th></tr></thead><tbody>";
				insert += "<tr><td align=\"center\">"+getUserName(data[SBPM.Storage.get("userid")].history[i]['to'])+"</td><td align=\"center\">"+data[SBPM.Storage.get("userid")].history[i]['msgtype']+"</td><td><pre style=\"float:left\">"+data[SBPM.Storage.get("userid")].history[i]['text']+"</pre></td></tr></tbody></table></td></tr>";
			}
		}
	}
	$('#instance_history').html(insert);
}


self.drawInstanceState = function () {
			var groups = SBPM.Service.User.getRoleByUserId(SBPM.Storage.get("user").id);
/*
	document.getElementById("welcome").style.display = "none";
	document.getElementById('ausfuehrung').style.display = 'block';
	document.getElementById("graph").style.display = "none";
	document.getElementById('instance_from_process').innerHTML = "Instance of process: " + name;
	document.getElementById("abortInstanceButton").style.display = "block";
	*/
	var insert = "<tr><td align=\"center\">Startknoten w&auml;hlen</td><td align=\"center\">";
	insert += "<table class=\"data\" width=\"60%\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:40%\">Subjekt</th><th style=\"width:60%\">Node</th></tr></thead><tbody>";
	for (group in groups){
		
		var groupid = groups[group].id;
		
		var nodes = findStartNodesForGroup(JSON.parse(SBPM.Storage.get("instancegraph")), groupid);
		for (i = 0; i < nodes.length; i++){
							
			insert += "<tr><td align=\"center\">" + getGroupName(groupid) + "</td><td align=\"center\"><input type=\"button\" value=\""+ nodes[i].text +"\" onClick=\"SBPM.VM.executionVM.selectNextNode('"+ groupid +"','"+ nodes[i].id +"');writeSumActiveInstances();\"/></td></tr>";
		}
	}
	insert += "</tbody></table>";
	$('#instance_history').html(insert);
}

self.abortInstance = function (){
deleteInstance(SBPM.Storage.get("instanceid"));
$("#freeow").freeow("Instanz abbrechen", "Instance aborted.", {
	classes: [,"ok"],
	autohide: true
});
writeSumActiveInstances();
location.reload();
}


 self.selectNextNode = function(subjectid, nodeid, msgtext){

	var data = SBPM.Storage.get("instancedata");
	SBPM.VM.executionVM.drawHistory(data);
	
	var node = findNode(JSON.parse(SBPM.Storage.get("instancegraph")), subjectid, nodeid);
	console.log("BEFORE ADD HISTORY");
	addHistory(SBPM.Storage.get("instancedata"), SBPM.Storage.get("userid"),subjectid, node);	// < aktuelle node
	
	// TODO the current node is known here -> highlight it in canvas
	
	saveInstanceData(SBPM.Storage.get("instanceid"), SBPM.Storage.get("instancedata")); // speichern
	var insert = "";
	
	// node anzeigen
	//alert(JSON.stringify(node));
	if (node['type'] == "action") {
		insert += "<tr><td align=\"center\">"+SBPM.Storage.get("instancedata")[SBPM.Storage.get("userid")]['history'].length +"</td><td align=\"center\">"+ node['text'] +"<br><br>";
		insert += "<table class=\"data\" style=\"width:200px\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:100%\">Next</th></tr></thead><tbody id=\"TableContent\"></tbody></table>";
		document.getElementById('instance_history').innerHTML += insert;
	}
	else if (node['type'] == "send"){
		insert += "<tr><td align=\"center\">"+ SBPM.Storage.get("instancedata")[SBPM.Storage.get("userid")]['history'].length +"</td><td align=\"center\"><p><label>"+"Nachricht:" /*"+ data[SBPM.Storage.get("userid")].history[SBPM.Storage.get("instancedata")[SBPM.Storage.get("userid")]['history'].length-1].text +":*/ +"</label><br><textarea id=\"tosend\" style=\"resize:none;height:100px;width:600px\"></textarea><br><br><br>";
		insert += "<form><table class=\"data\" style=\"width:600px\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:30%\">Group</th><th style=\"width:50%\">Person in charge</th><th style=\"width:20%\">Send</th></tr></thead><tbody id=\"TableContent\"></tbody></table>";
		document.getElementById('instance_history').innerHTML += insert;
	}
	else if (node['type'] == "receive") {
		insert += "<tr><td align=\"center\">"+SBPM.Storage.get("instancedata")[SBPM.Storage.get("userid")]['history'].length +"</td><td align=\"center\">Wait for messages:<br><br>";
		insert += "<table class=\"data\" style=\"width:400px\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th style=\"width:50%\">Von (Gruppe)</th><th style=\"width:50%\">Typ</th></tr></thead><tbody id=\"TableContent\"></tbody></table>";
		document.getElementById('instance_history').innerHTML += insert;
	}
	else if (node['type'] == "end"){
		insert += "<tr><td align=\"center\">"+SBPM.Storage.get("instancedata")[SBPM.Storage.get("userid")]['history'].length +"</td><td align=\"center\">"+ node['text'] +"<br><br><b>Instance stopped.</b>";
		document.getElementById('instance_history').innerHTML += insert + "</td></tr>";
		SBPM.Storage.get("instancedata")[SBPM.Storage.get("userid")]['done'] = true;
		saveInstanceData(SBPM.Storage.get("instanceid"), SBPM.Storage.get("instancedata")); // speichern
		document.getElementById("abortInstanceButton").style.display = "none";
		return;
	}
	// nachfolger finden
	var nodeedges = findNodeEdges(JSON.parse(SBPM.Storage.get("instancegraph")), subjectid, node);
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
				receiver = getResponsiblesForUserForGroup(SBPM.Storage.get("userid"), getGroupID(nodeedges[i].target), SBPM.Storage.get("instanceProcessID"));
				if(receiver == "") {
					var users = getallusersforgroup(getGroupID(nodeedges[i].target));
					TableInsert += "<tr><td align=\"center\">"+ nodeedges[i].target + "</td><td align=\"center\"><select id=\"receive_user"+i+"\">";
					for(var x = 0; x < users.length; x++) {
						TableInsert += "<option>"+ getUserName(users[x]) +"</option>";
					}
					TableInsert += "</select></td><td align=\"center\"><input type=\"button\" value=\""+ nodeedges[i].text.replace(/<br>/gi, " ") +"\" onClick=\"if (sendTextMessage('"+ buttonText +"', getUserID(this.form.receive_user"+i+".options[this.form.receive_user"+i+".selectedIndex].value))) SBPM.VM.executionVM.selectNextNode('"+ subjectid +"','"+ nodeedges[i]['end'] +"');\" /></td></tr>"
				} else {
					for(var x = 0; x < receiver.length; x++)
						TableInsert += "<tr><td align=\"center\">"+ nodeedges[i].target + "</td><td align=\"center\">"+ getUserName(receiver[x]) +"</td><td align=\"center\"><input type=\"button\" value=\""+buttonText.replace(/<br>/gi, " ")+"\" onClick=\"if (sendTextMessage('"+ buttonText +"','"+ receiver[x] +"')) SBPM.VM.executionVM.selectNextNode('"+ subjectid +"','"+ nodeedges[i]['end'] +"');\" /></tr>";
				}
				
			}

			else
				TableInsert += "<tr><td align=\"center\"><input type=\"button\" value=\""+ buttonText +"\" onClick=\"SBPM.VM.executionVM.selectNextNode('"+ subjectid +"','"+ nodeedges[i]['end'] +"');\" /></td></tr>";
		}
	}

	document.getElementById('TableContent').innerHTML += TableInsert;
	
}

}

var CourseViewModel = function() {


	var self = this;
	self.name = "courseView";
	self.lable = "Course";

	self.init = function() {

	}

	self.showView = function() {
		SBPM.VM.activeViewIndex(2);
		SBPM.VM.executionVM.activeViewIndex(0);
		
		if(SBPM.Storage.get("inctanceStepNodeID")==null ||SBPM.Storage.get("inctanceStepSubjectID")==null ){
			SBPM.VM.executionVM.drawHistory(loadInstanceData(SBPM.Storage.get("instanceid")));
		}
		else{
			SBPM.VM.executionVM.selectNextNode(SBPM.Storage.get("inctanceStepSubjectID"),SBPM.Storage.get("inctanceStepNodeID"));
		}
		
	}
	self.activateTab = function() {
		$("#instance_tab1").addClass("active");
		$("#instance_tab2").removeClass("active");
	}
	

	
	
}
var InstanceViewModel = function() {
	var self = this;
	self.name = "instanceView";
	self.lable = "Instance";
	self.init = function() {

	}

	self.showView = function() {
		SPBM.VM.activeViewIndex(2);
		SBPM.VM.executionVM.activeViewIndex(1);
	}
	self.activateTab = function() {
		$("#instance_tab1").removeClass("active");
		$("#instance_tab2").addClass("active");
	}
}

