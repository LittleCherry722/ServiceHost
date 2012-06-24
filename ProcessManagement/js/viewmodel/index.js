var ViewModel = function() {

	var self = this;

	self.user = ko.observable();

	self.menuVM = new MenuViewModel();
	self.headerVM = new HeaderViewModel();
	self.processVM = new ProcessViewModel();
	self.homeVM = new HomeViewModel();

	self.init = function() {
		console.log("init vms");

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
		$("#main_menu").accordion({
			collapsible : true,
			autoHeight : false
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
	
}


var ProcessViewModel = function() {

	var self = this;
	
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
		
		$("#tab1").click(function() {
            console.log("tab1 clicked");
            
            $(this).parent().parent().find("td input").removeClass("active");
            $(this).addClass("active");
            $(".tab_content").addClass("hide");
            $("#tab1_content").removeClass("hide");
            gf_clickedCVbehavior();
            updateListOfSubjects();
            // load internal behavior
        });
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
        
	}
	
	self.showProcess = function(processName){
		self.subjectVM.showView();
		SBPM.Service.Process.loadProcess(processName);
	}
	
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
		// showverantwortliche();
		// gv_graph.selectedNode = null;
		// updateListOfSubjects();

	}
	
}
