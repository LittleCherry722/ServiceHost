var ViewModel = function() {

	var self = this;

	self.user = ko.observable();

	self.menuVM = new MenuViewModel();
	self.headerVM = new HeaderViewModel();
	self.processVM = new ProcessViewModel();
	self.homeVM = new HomeViewModel();
	self.subjectVM = new SubjectViewModel();
	self.internalVM = new InternalViewModel();
	self.chargeView = new ChargeViewModel();

	self.mainViews = ko.observable([{
		name : "homeView",
		data : self.homeVM,
		word : "Home",
		afterRender : SBPM.Service.Home.afterRender
	}, {
		name : "processView",
		data : self.processVM,
		word : "Process",
		afterRender : SBPM.Service.Process.afterRender
	}]);

	self.activeMainViewIndex = ko.observable(0);
	self.activeMainView = ko.computed(function() {
		return self.mainViews()[self.activeMainViewIndex()]
	});

	self.processViews = ko.observable([{
		name : "subjectView",
		data : self.subjectVM,
		word : "Subject-Interaction-View",
		afterRender : SBPM.Service.Process.subjectAfterRender
	}, {
		name : "internalView",
		data : self.internalVM,
		word : "Internal-Behavior-View",
		afterRender : SBPM.Service.Process.internalAfterRender
	}, {
		name : "chargeView",
		data : self.chargeView,
		word : "Person in charge",
		afterRender : SBPM.Service.Process.chargeAfterRender

	}]);

	self.activeProcessViewIndex = ko.observable(0);
	self.activeProcessView = ko.computed(function() {
		return self.processViews()[self.activeProcessViewIndex()]
	});

	self.init = function() {
		console.log("init vms");

		self.menuVM.init();
		self.headerVM.init();
		self.homeVM.init();
		self.processVM.init();
		self.subjectVM.init();
		self.internalVM.init();
		self.chargeView.init();

	}

	self.init();
}
var MenuViewModel = function() {

	var self = this;

	self.init = function() {
		console.log("init mvm");
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
		console.log("init hvm");

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
var ProcessViewModel = function() {

	var self = this;

	self.init = function() {
		console.log("init Process VM");
		$(".chzn-select").chosen();

	}
}
var HomeViewModel = function() {

	var self = this;

	self.init = function() {
		console.log("init Home VM");
	}
}
var SubjectViewModel = function() {

	var self = this;

	self.init = function() {
		console.log("init Subject VM");

	}
}
var InternalViewModel = function() {

	var self = this;

	self.init = function() {
		console.log("init Internal VM");
	}
}
var ChargeViewModel = function() {

	var self = this;

	self.init = function() {
		console.log("init Charge VM");

	}
}

