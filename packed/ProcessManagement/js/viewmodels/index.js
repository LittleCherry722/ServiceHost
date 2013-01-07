var ViewModel = function() {

  var self = this;

  var subViewModels = {
    processVM : new ProcessViewModel(),
    executionVM : new ExecutionViewModel(),
    homeVM : new HomeViewModel()
  };

  self.init = function(callback) {
    self.user = ko.observable();

    self.menuVM = new MenuViewModel();
    self.headerVM = new HeaderViewModel();
    self.contentVM = ko.observable();

    self.goToPage("home");

    callback();
  }

  self.goToPage = function(page){

    console.log("ViewModel: goToPage("+page+")");

    switch(page) {
      case "process":
        self.contentVM(subViewModels.processVM);
      break;
      case "execution":
        self.contentVM(subViewModels.executionVM);
      break;
      default:
        self.contentVM(subViewModels.homeVM);
      break;
    }

    self.contentVM().init();

    return self.contentVM();
  }
}

var ProcessViewModel = function() {

  var self = this;

  self.processName = ko.observable();
  self.isProcess = ko.observable();

  self.name = "processView";
  self.label = "Process";
  self.processStamp = "";

  self.subjectVM = new SubjectViewModel();
  self.internalVM = new InternalViewModel();
  self.chargeVM = new ChargeViewModel();

  self.init = function() {

    SBPM.VM.menuVM.visible({
      home : true,
      process : true,
      save : true,
      saveAs : true,
      messages : true,
      execution : true
    });

    self.subjectVM.init();
    self.internalVM.init();
    self.chargeVM.init();

    console.log("ProcessViewModel: initialized.");
  }

  self.processViews = [self.subjectVM, self.internalVM, self.chargeVM];


  self.activeViewIndex = ko.observable(0);

  self.activeView = function() {
    return self.processViews[self.activeViewIndex()];
  };

  self.showProcess = function(processName, showInformation, processState, isProcess) {


    self.processName(processName);
    SBPM.VM.contentVM().subjectVM.processName(processName); //TODO Let subjectVM know by it self.
    console.log("ProcessViewModel: showProcess called. processName="+self.processName());

    try{

      gv_graph.clearGraph(true);

      var processId = SBPM.Service.Process.getProcessID(self.processName());

      self.subjectVM.showView();

      gv_graph.clearGraph();

      /* processId:
       *  0    -> new process
       *  1..n -> old process
       */
      if(processId > 0){
        if (isProcess === null || isProcess === undefined) {
          isProcess = SBPM.Service.Process.getIsProcess(self.processName());
        }
        self.isProcess(isProcess);
        var graphAsJson = (self.processStamp == "") ? SBPM.Service.Process.loadGraph(processId) : loadGraphHistory(processId,self.processStamp);

        if(self.isProcess()) {
          gf_loadGraph(graphAsJson, processState);
        } else {
          gf_loadCase(graphAsJson, processState);
        }

        // TODO always  throws errors.
        var graph = JSON.parse(graphAsJson);
        self.chargeVM.load(graph);


        var myOptions = getProcessStamps(processId);
        var mySelect = $('#timestamps');
        mySelect.empty();
        mySelect.unbind();

        $.each(myOptions, function(text, val) {
          mySelect.append(
            $('<option></option>').val(val).html(val)
          );
        });

        $("#timestamps option").last().attr('selected', 'selected');
        $("#timestamps option[value='" + self.processStamp + "']").attr('selected', 'selected');
        $("#timestamps").change(function() {
          self.processStamp = $("#timestamps").val();
          self.showProcess(processName);
        });

        console.log(graph);
      } else {
        if (isProcess === true) {
          console.log("load empty graph");
          gv_graph.loadFromJSON("{}");
        } else {
          console.log("loading empty case");
          var user = SBPM.Storage.get("user")
          var username = (user === undefined) ? "no user" : user.name;
          gf_createCase(username);
        }
      }


      // TODO replace this DEPRECATED CALLS!
      setSubjectIDs();
      $("#tab2").addClass("active");
      if (processState !== undefined && processState !== null) {
        console.log(processState);
        if(processState.behavior !== null) {
          $("#tab2").removeClass("active");
        }
      }
      // TODO END

      if(showInformation) SBPM.Notification.Info("Information", "Process \""+processName+"\" successfully loaded.");

    }catch(e){
      console.error("Could not load process \"" + processName + "\". Error Message: " + e.message);
      console.error(e.stack);
      SBPM.Notification.Error("Error", "Could not load process \""+processName+"\".");
    }

    updateListOfSubjects();
    return processId;
  }

  self.save = function(name, forceOverwrite, saveAs){
    // try to set another default name
    var name = name || self.processName();

    // TODO do not convert twice (in lib and here)

    var graph = JSON.parse(gv_graph.saveToJSON());

    console.log("ProcessViewModel: saving "+name+".");

    // add responsibilities and routings to graph
    $.extend(graph, ko.mapping.toJS(self.chargeVM.data, {
      'ignore' :  ["subjectProvidersForRole"],
    }));

    var graphAsJSON = JSON.stringify(graph);

    var startSubjects = [];

    for (var subject in gv_graph.subjects)
      startSubjects.push(SBPM.Service.Role.getByName(subject));

    var startSubjectsAsJSON = JSON.stringify(startSubjects);

    if(SBPM.Service.Process.saveProcess(graphAsJSON, startSubjectsAsJSON, name, forceOverwrite, saveAs, self.isProcess())){

      $.publish("/process/change");
      var processState = gf_getProcessState();

      gv_graph.clearGraph(true);
      parent.processStamp = ""

      SBPM.VM.contentVM().showProcess(name, false, processState);

      SBPM.Notification.Info("Information", "Process successfully saved.");
    }else
      SBPM.Notification.Error("Error", "Saving Process failed.");
  }
}

var SubjectViewModel = function() {

  var self = this;

  self.name = "subjectView";
  self.label = "Subject-Interaction-View";
  self.userOrRole = ko.observable();
self.processName = ko.observable("");
  self.init = function() {

  }

  self.showView = function() {
    SBPM.VM.contentVM().activeViewIndex(0);

    var activeProcess = SBPM.VM.contentVM().processName();
    var isProcess = SBPM.Service.Process.getIsProcess(activeProcess);
    if( !isProcess ){
      self.userOrRole("Assigned-User:");
    } else {
      self.userOrRole("Assigned-Role:");
    }
  }

  self.afterRender = function() {

  }

  self.availableProcesses = ko.observableArray(SBPM.Service.Process.getAllProcesses());
  


	self.updateProcessList = ko.computed(function() {
		filterArray = SBPM.Service.Process.getAllProcesses();

		self.availableProcesses(filterArray.filter(function(element) {
			return (element.name != self.processName());
		}))
	}); 

  	
  
  
console.log("self.availableProcesses = ko.observableArray(SBPM.Service.Process.getAllProcesses());")

}
var InternalViewModel = function() {

  var self = this;

  self.name = "internalView";
  self.label = "Internal-Behavior-View";

  self.init = function() {
  }

  self.showView = function() {
    SBPM.VM.contentVM().activeViewIndex(1);
  }

}
var ChargeViewModel = function() {

  var self = this;

  self.name = "chargeView";
  self.label = "Person in charge";

  var Routing = function(messagesTypes, fromSubject, fromSubjectprovider, messageType, toSubject, toSubjectprovider){
    var self = this;

    var _private = {
      messageTypes : messagesTypes
    };

    self.fromSubject = ko.observable(fromSubject);
    self.fromSubjectprovider = fromSubjectprovider;
    self.messageType = ko.observable(messageType);
    self.toSubject = ko.observable(toSubject);
    self.toSubjectprovider = toSubjectprovider;

    /**
     * returns a unique list of sender's subjectNames
     */
    self.fromSubjects = ko.computed(function() {
      return $.unique(_private.messageTypes
                      .map(function(element){
                        return element.sender;
                      }));
    });

    /**
     * returns a list of messageTypes for a sender
     */
    self.availableMessageTypes = ko.computed(function() {
      return _private.messageTypes
      .filter(function(element){
        return (element.sender == self.fromSubject());
      })
      .map(function(element){
        return element.messageType.replace("<br />", " ");
      });
    });

    /**
     * returns a list of receiver's subjectNames for a messageType
     */
    self.toSubjects = ko.computed(function() {
      return _private.messageTypes
      .filter(function(element){
        return (element.messageType == self.messageType());
      })
      .map(function(element){
        return element.receiver;
      });
    });

    /**
     * reset messageType and toSubject when fromSubjects is being changed
     */
    self.fromSubject.subscribe(function() {
      self.messageType(undefined);
    });

    /**
     * reset toSubject when messageType is being changed
     */
    self.messageType.subscribe(function() {
      self.toSubject(undefined);
    });

  }

  var Responsibility = function(subjectProvidersForRole, role, subjectProvider){
    var self = this;

    self.subjectProvidersForRole = subjectProvidersForRole;
    self.role = role;
    self.subjectProvider = subjectProvider;
  }

  self.data = ko.observable({
    responsibilities : ko.observableArray([]),  // {role, subjectProvider}
    routings : ko.observableArray([]),          // {fromSubject, fromSubjectprovider, messageType, toSubject, toSubjectprovider}
  });

  self.lists = {
    subjectProviders : ko.observableArray([]),
    roles : {}
  };

  self.init = function() {
    // load lists needed for drop down menus
    self.lists.subjectProviders(SBPM.Service.User.getAll().map(function(user){ return user.name; }));

    console.log("ChargeViewModel: initialized.");
  }

  self.load = function(graph){

    var messageTypes = gf_getMessageTypes();

    var rolesAndUsers = SBPM.Service.Role.getAllRolesAndUsers();

    // if responsibilities arent set yet
    if(!graph.responsibilities || graph.responsibilities.length < 1){

      // initialize them
      $.each(rolesAndUsers, function(role, users){
        self.data().responsibilities.push(new Responsibility(users, role));
      });

    }else{

      // otherwise just map the exisiting values
      self.data().responsibilities(graph.responsibilities.map(function(data){
        return new Responsibility(rolesAndUsers[data.role], data.role, data.subjectProvider);
      }));

    }

    graph.routings = graph.routings || [];

    self.data().routings(graph.routings.map(function(data){
      return new Routing(messageTypes, data.fromSubject, data.fromSubjectprovider, data.messageType, data.toSubject, data.toSubjectprovider)
    }));

    console.log("ChargeViewModel: Responsibilities and Routings loaded.");

  }

  self.addRouting = function(){
    self.data().routings.push(new Routing(gf_getMessageTypes()));
  }

  self.removeRouting = function(element){
    self.data().routings.remove(element);
  }

  self.showView = function() {
    SBPM.VM.contentVM().activeViewIndex(2);
  }

}

var ExecutionViewModel = function() {

  var self = this;

  self.name = "executionView";
  self.lable = "Execution";

  self.courseVM = new CourseViewModel();
  self.instanceVM = new InstanceViewModel();

  self.init = function(instanceName) {

    SBPM.VM.menuVM.visible({
      home : true,
      process : true,
      save : false,
      saveAs : false,
      messages : true,
      execution : true
    });

    // TODO load instance

    self.courseVM.init();
    self.instanceVM.init();
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
    SBPM.VM.contentVM().activeViewIndex(0);

    if(SBPM.Storage.get("inctanceStepNodeID")==null ||SBPM.Storage.get("inctanceStepSubjectID")==null ){
      SBPM.VM.contentVM().drawHistory(loadInstanceData(SBPM.Storage.get("instanceid")));
    }
    else{
      SBPM.VM.contentVM().selectNextNode(SBPM.Storage.get("inctanceStepSubjectID"),SBPM.Storage.get("inctanceStepNodeID"));
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
    SBPM.VM.contentVM().activeViewIndex(1);
  }
  self.activateTab = function() {
    $("#instance_tab1").removeClass("active");
    $("#instance_tab2").addClass("active");
  }
}

