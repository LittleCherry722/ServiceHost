var ViewModel = function() {

	var self = this;

	self.init = function() {
		$("#dialog").dialog({
			autoOpen : false,
			modal : true,
			draggable : false,
			resiable : false,
			buttons : [{
				text : "Continue",
				click : function() {
				}
			}, {
				text : "Cancel",
				click : function() {
					$(this).dialog("close");
				}
			}]
		});

		self.user = ko.observable();
		self.activeViewIndex = ko.observable(0);

		self.menuVM = new MenuViewModel();
		self.headerVM = new HeaderViewModel();
		self.processVM = new ProcessViewModel();
		self.homeVM = new HomeViewModel();
		self.executionVM = new ExecutionViewModel();

		self.menuVM.init();
		self.headerVM.init();
		self.homeVM.init();
		self.processVM.init();
		self.executionVM.init();

	}

	self.init();
	self.mainViews = [self.homeVM, self.processVM, self.executionVM];

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

	self.afterRender = function() {

		$("#main_menu").accordion({
			collapsible : true,
			autoHeight : false
		});

		$("#calendar").datepicker({
			nextText : "&raquo;",
			prevText : "&laquo;"
		});

		$("a#save").click(function() {
		    
		    if(SBPM.Service.Process.saveProcess()){
		      // reload recent processes
		      self.init();
		      
		      SBPM.Notification.Info("Information", "Process successfully created.");
		    }else
		      SBPM.Notification.Info("Error", "Could not create process.");

		});

		$("#hide_menu").click(function() {
			$("#left_menu").hide();
			$("#show_menu").show();
			$("body").addClass("nobg");
			$("#content").css("marginLeft", 35);
		});

		$("#show_menu").click(function() {
			$("#left_menu").show();
			$(this).hide();
			$("body").removeClass("nobg");
			$("#content").css("marginLeft", 245);
		});

		$("a#saveAs").fancybox({
			'padding' : '0px',
			'scrolling' : 'no',
			'width' : '50',
			'height' : '40',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
			}
		});

		$("a#newProcess").fancybox({
			'padding' : '0px',
			'scrolling' : 'no',
			'width' : '30',
			'height' : '28',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
			}
		});

		$("a#newInstance").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if (fancyreturn1 != false)
					SBPM.VM.executionVM.newInstance(fancyreturn1);
			}
		});

		$("a#newMSG").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if (fancyreturn1 != false)
					resumeInstanceMessage(fancyreturn1)
			}
		});

		$("a#MSGInbox").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if (fancyreturn1 != false) {
					SBPM.VM.executionVM.drawHistory(loadInstanceData(fancyreturn1));
					document.getElementById("welcome").style.display = "none";
					document.getElementById('ausfuehrung').style.display = 'block';
					document.getElementById("graph").style.display = "none";
					document.getElementById("abortInstanceButton").style.display = "none";
				}
			}
		});

		$("a#MSGOutbox").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if (fancyreturn1 != false) {
					SBPM.VM.executionVM.drawHistory(loadInstanceData(fancyreturn1));
					document.getElementById("welcome").style.display = "none";
					document.getElementById('ausfuehrung').style.display = 'block';
					document.getElementById("graph").style.display = "none";
					document.getElementById("abortInstanceButton").style.display = "none";
				}
			}
		});

		$("a#runningInstances").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				writeSumActiveInstances();
				if (fancyreturn1 != false)
					resumeInstance(fancyreturn1);
			}
		});

		$("a#history").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if (fancyreturn1 != false) {
					SBPM.VM.executionVM.drawHistory(loadInstanceData(fancyreturn1));
					document.getElementById("welcome").style.display = "none";
					document.getElementById('ausfuehrung').style.display = 'block';
					document.getElementById("graph").style.display = "none";
					document.getElementById("abortInstanceButton").style.display = "none";
				}
			}
		});

		$("a#processList").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6'
		});

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


	self.afterRender = function() {

		$("a#administration").fancybox({
			'padding' : '0',
			'scrolling' : 'no',
			'width' : '80',
			'height' : '50',
			'autoScale' : false,
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'enableEscapeButton' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {

			}
		});

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
/**	self.objectOfSubjects = ko.observable(gv_graph.subjects);
	self.arrayOfSubjects = ko.observableArray();
	self.compSubjects = ko.computed(function() {
		for (subject in self.objectOfSubjects()) {
			self.arrayOfSubjects.push(subject);
		}
		return self.arrayOfSubjects();

	});
	
	*/
	self.name = "processView";
	self.label = "Process";

	self.subjectVM = new SubjectViewModel();
	self.internalVM = new InternalViewModel();
	self.chargeVM = new chargeViewModel();

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

	self.afterRender = function() {
		$("#slctSbj").chosen();

		$("#tab2").click(function() {
			console.log("tab2 clicked");

			$(this).parent().parent().find("td input").removeClass("active");
			$(this).addClass("active");
			$(".tab_content").addClass("hide");
			$("#tab2_content").removeClass("hide");
			gv_graph.changeView('cv');
			updateListOfSubjects();
		});

		$("#tab3").click(function() {
			console.log("tab3 clicked");

			$(this).parent().parent().find("td input").removeClass("active");
			$(this).addClass("active");
			$(".tab_content").addClass("hide");
			$("#tab3_content").removeClass("hide");

			gv_graph.selectedNode = null;
			updateListOfSubjects();
		});

		$("input#help-button").fancybox({
			'padding' : '0px',
			'scrolling' : 'no',
			'height' : '60',
			'width' : '40',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
			}
		});

	}

	self.showProcess = function(processName) {
	    
	    try{
            
            gv_graph.clearGraph();
            
            var processId = SBPM.Service.Process.getProcessID(processName);
            
            self.subjectVM.showView();
            
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
            showverantwortliche();
            setSubjectIDs(); 
            $("#tab2").addClass("active");
            // TODO END
            
            SBPM.Notification.Info("Information", "Process \""+processName+"\" successfully loaded.");
    	        
	    }catch(e){
	        
	        SBPM.Notification.Error("Error", "Could not load process \""+processName+"\".");
	        
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
		//resize canvas to fit into screen
		$("#graph_cv_outer").css("width", window.innerWidth - 170 - 245);
		$("#graph_cv_outer").css("height", window.innerHeight - 145);
		$("#show_menu").click(function() {
			$(window).trigger('resize');
		});
		$("#hide_menu").click(function() {
			$(window).trigger('resize');
		});
		$(window).resize(function() {
			if ($("#show_menu").css("display") == "none") {
				$("#graph_cv_outer").css("width", window.innerWidth - 170 - 245);
				$("#graph_cv_outer").css("height", window.innerHeight - 145);
			} else {
				$("#graph_cv_outer").css("width", window.innerWidth - 195);
				$("#graph_cv_outer").css("height", window.innerHeight - 185);
		}
		});

		// gv_graph.init();
		// gf_paperChangeView("cv");
		// updateListOfSubjects();
		// gv_graph.draw();
		var qtipStyle = "ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow";
		var qtipPositionAt = 'right top';
		var qtipPositionMy = 'left bottom';
		$('#AddSubjectButton').qtip({
			content : {
				text : 'Macro\n: Press "A"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
		$('#UpdateSubjectButton').qtip({
			content : {
				text : 'Macro\n: Press "U"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
		$('#DeleteSubjectButton').qtip({
			content : {
				text : 'Macro\n: Press "D"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
		$('#DeleteSubjectButton').qtip({
			content : {
				text : 'Macro\n: Press "D"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
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

	self.afterRender = function() {

		//resize canvas to fit into screen
		$("#graph_bv_outer").css("width", window.innerWidth - 170 - 245);
		$("#graph_bv_outer").css("height", window.innerHeight - 145);
		$("#show_menu").click(function() {
			$(window).trigger('resize');
		});
		$("#hide_menu").click(function() {
			$(window).trigger('resize');
		});
		$(window).resize(function() {
			if ($("#show_menu").css("display") == "none") {
				$("#graph_bv_outer").css("width", window.innerWidth - 170 - 245);
				$("#graph_bv_outer").css("height", window.innerHeight - 145);
			} else {
				$("#graph_bv_outer").css("width", window.innerWidth - 195);
				$("#graph_bv_outer").css("height", window.innerHeight - 185);
			}
		});

        // TODO WORKAROUND - Why isnt it working in internalView.html???
        $('#ge_type2').html('<option value="R" id="ge_type2_R">receive</option><option value="S" id="ge_type2_S">send</option><option value="action" id="ge_type2_action">action</option><option value="end" id="ge_type2_end">end</option>');

		// gf_clickedCVbehavior();
		// updateListOfSubjects();
		var qtipStyle = "ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow";
		var qtipPositionAt = 'right top';
		var qtipPositionMy = 'left bottom';
		$('#CreateNodeButton').qtip({
			content : {
				text : 'Macro\n: Press "A"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
		$('#InsertSendNodeButton').qtip({
			content : {
				text : 'Macro\n: Press "1"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
		$('#InsertReceiveButton').qtip({
			content : {
				text : 'Macro\n: Press "2"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
		$('#InsertActionNodeButton').qtip({
			content : {
				text : 'Macro\n: Press "3"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
		$('#UpdateNodeButton').qtip({
			content : {
				text : 'Macro\n: Press "U"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
		$('#DeleteNodeButton').qtip({
			content : {
				text : 'Macro\n: Press "D"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
		$('#ConnectNodeButton').qtip({
			content : {
				text : 'Macro\n: Press "C"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
		$('#UpdateEdgeButton').qtip({
			content : {
				text : 'Macro\n: Press "U"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
		$('#DeleteEdgeButton').qtip({
			content : {
				text : 'Macro\n: Press "D"'
			},
			position : {
				at : qtipPositionAt,
				my : qtipPositionMy,
				viewport : $(window),
				adjust : {
					method : 'mouse',
					x : 0,
					y : 0
				}
			},
			style : {
				classes : qtipStyle
			}
		});
	}
}
var chargeViewModel = function() {

	var self = this;

	self.name = "chargeView";
	self.label = "Person in charge";

	self.init = function() {

	}

	self.showView = function() {
		SBPM.VM.activeViewIndex(1);
		SBPM.VM.processVM.activeViewIndex(2);
	}

	self.afterRender = function() {
		$("a#responsibleForloggedinUser").fancybox({
			'padding' : '0px',
			'scrolling' : 'no',
			'width' : '30',
			'height' : '37',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if (fancyreturn1 != false)
					addResponsible(fancyreturn2, fancyreturn1);
			}
		});

		// showverantwortliche();
		// gv_graph.selectedNode = null;
		// updateListOfSubjects();

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
	self.afterRender = function() {
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
	self.afterRender = function() {
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
	self.afterRender = function() {
		
		gf_showInternalBehavior(SBPM.Service.Process.loadGraph(getProcessIDforInstance(SBPM.Storage.get("userid"))),
		 getGroupName(getGroupIDforResponsibleUser(SBPM.Storage.get("userid"),getProcessIDforInstance(SBPM.Storage.get("instanceid"))).groups[0]).toLowerCase(),
		  SBPM.Storage.get("instancedata")[SBPM.Storage.get("userid")].history[SBPM.Storage.get("instancedata")[SBPM.Storage.get("userid")].history.length-1].nodeid);
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

