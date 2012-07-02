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
			SBPM.Dialog.YesNo('Warning', 'Do you really want to save this process?', function() {
				SBPM.Service.Process.saveProcess();
				parent.$.fancybox.close();
			});
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
				if(fancyreturn1 != false)
					newInstance(fancyreturn1);
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
				if(fancyreturn1 != false)
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
				if(fancyreturn1 != false) {
					drawHistory(loadInstanceData(fancyreturn1));
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
				if(fancyreturn1 != false) {
					drawHistory(loadInstanceData(fancyreturn1));
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
				if(fancyreturn1 != false)
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
				if(fancyreturn1 != false) {
					drawHistory(loadInstanceData(fancyreturn1));
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
		if(SBPM.Storage.get("user")) {
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
	self.objectOfSubjects = ko.observable(gv_graph.subjects);
	//console.log(self.objectOfSubjects());
	self.arrayOfSubjects = ko.observableArray();
	self.compSubjects = ko.computed(function() {
		for(subject in self.objectOfSubjects()) {
			//console.log(subject);
			self.arrayOfSubjects.push(subject);
		}
		return self.arrayOfSubjects();

	});
	self.name = "processView";
	self.label = "Process";

	self.subjectVM = new SubjectViewModel();
	self.internalVM = new InternalViewModel();
	self.chargeVM = new chargeViewModel();

	self.init = function() {
		self.subjectVM.init();
		self.internalVM.init();
		self.chargeVM.init();
		//console.log(gv_graph.subjects);
		//console.log(self.arrayOfSubjects);

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
		self.subjectVM.showView();
		self.processName(processName);
		SBPM.Service.Process.loadProcess(processName);
		$("#tab2").addClass("active");
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
		$("#graph_cv_outer").css("width", window.innerWidth - 175 - 235);
		$("#graph_cv_outer").css("height", window.innerHeight - 125);
		$(window).resize(function() {
			$("#graph_cv_outer").css("width", window.innerWidth - 175 - 235);
			$("#graph_cv_outer").css("height", window.innerHeight - 125);
		});
		// gv_graph.init();
		// gf_paperChangeView("cv");
		// updateListOfSubjects();
		// gv_graph.draw();
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
		$("#graph_bv_outer").css("width", window.innerWidth - 190 - 245);
		$("#graph_bv_outer").css("height", window.innerHeight - 170);

		$(window).resize(function() {
			$("#graph_bv_outer").css("width", window.innerWidth - 190 - 245);
			$("#graph_bv_outer").css("height", window.innerHeight - 170);
		});

		// gf_clickedCVbehavior();
		// updateListOfSubjects();
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
				if(fancyreturn1 != false)
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
	};
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
		SBPM.VM.executionVM.activeViewIndex(0);
	}
	self.activateTab= function() {
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
	}
	self.showView = function() {
		SBPM.VM.executionVM.activeViewIndex(1);
	}
		self.activateTab= function() {
		$("#instance_tab1").removeClass("active");
		$("#instance_tab2").addClass("active");
	}
}

