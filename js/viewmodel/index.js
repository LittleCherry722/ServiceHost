var ViewModel = function() {

	var self = this;

	self.user = ko.observable();

	self.menuVM = new MenuViewModel();
	self.headerVM = new HeaderViewModel();
	self.processVM = new ProcessViewModel();
	self.homeVM = new HomeViewModel();

	self.init = function() {
		console.log("init vms");

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
        
		self.menuVM.init();
		self.headerVM.init();
		self.homeVM.init();
		self.processVM.init();

	}

	self.init();
	self.mainViews = [
        self.homeVM,
	    self.processVM
    ];

	self.activeViewIndex = ko.observable(0);
	
	self.activeView = function() {
		return self.mainViews[self.activeViewIndex()]
	};
}


var MenuViewModel = function() {

	var self = this;

	self.init = function() {
		console.log("init Menu VM");
	}
	
	self.afterRender = function(){
	    
        $("#main_menu").accordion({
            collapsible : true,
            autoHeight : false
        });
        
        $("#calendar").datepicker({
            nextText : "&raquo;",
            prevText : "&laquo;"
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
                if(fancyreturn1 != false)
                    GraphSpeichernAls(fancyreturn1);
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
                if(fancyreturn1 != false) {
                    SBPM.Service.Process.newProcess(fancyreturn1);
                }

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
            'width' : '60',
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
		console.log("init Header VM");

		if(SBPM.Storage.get("user")) {
			self.userName(SBPM.Storage.get("user").name);
			initMessageCheck();
		}
	}

	self.messageCountString = ko.computed(function() {
		return self.messageCount() < 1 ? "no new messages" : self.messageCount() + " new messages ";
	});

	function initMessageCheck() {
		console.log(SBPM.Service.Message.countNewMessages(SBPM.Storage.get("user").id));

		self.messageCount(SBPM.Service.Message.countNewMessages(SBPM.Storage.get("user").id));

		setTimeout(initMessageCheck, 120000);
	}

    self.afterRender = function(){

        console.log($("a#administration"));

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
		console.log("init Home VM");
	}
	
	self.afterRender = function() {
		console.log("home afterRender");
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
	self.chargeVM = new chargeViewModel();
	
	self.init = function() {
		console.log("init Process VM");

		self.subjectVM.init();
		self.internalVM.init();
		self.chargeVM.init();
	}

	self.processViews = [
        self.subjectVM,
        self.internalVM,
        self.chargeVM
    ];

	self.activeViewIndex = ko.observable(0);
	
	self.activeView = function() {
		return self.processViews[self.activeViewIndex()]
	};
	
	self.afterRender = function() {
		$("#slctSbj").chosen();
		console.log("process afterRender");
		
		console.log($("#tab1"));
		
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
	
	self.showProcess = function(processName){
		self.subjectVM.showView();
		self.processName(processName);
		SBPM.Service.Process.loadProcess(processName);
	}
	self.arrayOfSubjects = ko.observableArray();
}


var SubjectViewModel = function() {

	var self = this;

    self.name = "subjectView";
    self.label = "Subject-Interaction-View";

	self.init = function() {
		console.log("init Subject VM");

	}

	self.showView = function() {
		SBPM.VM.activeViewIndex(1);
		SBPM.VM.processVM.activeViewIndex(0);
	}
	
	self.afterRender = function() {
		console.log("subject afterRender");
		
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
		console.log("init Internal VM");
	}
	
	self.showView = function() {
		SBPM.VM.activeViewIndex(1);
		SBPM.VM.processVM.activeViewIndex(1);
	}
	
	self.afterRender = function() {
		console.log("internal afterRender");
		
        //resize canvas to fit into screen
        $("#graph_bv_outer").css("width", window.innerWidth - 190 - 245);
        $("#graph_bv_outer").css("height", window.innerHeight - 124);
        console.log("asd");
        $(window).resize(function() {
            $("#graph_bv_outer").css("width", window.innerWidth - 190 - 245);
            $("#graph_bv_outer").css("height", window.innerHeight - 124);
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
		console.log("init Charge VM");

	}
	
	self.showView = function() {
		SBPM.VM.activeViewIndex(1);
		SBPM.VM.processVM.activeViewIndex(2);
	}
	
	self.afterRender = function() {
		console.log("charge afterRender");
		
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
