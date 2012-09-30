var ViewModel = function() {
	var self = this;

	self.init = function(callback) {
		console.log("newInstance init");

		callback();
	}

	self.Process = function(id) {
		var self = this;
		self.id = id;
		self.name = parent.SBPM.Service.Process.getProcessName(id);
		self.isExexutable = parent.SBPM.Service.Process.isExecutbale(id);
		self.start = function() {
			if(parent.SBPM.Service.Process.isExecutbale(self.id).length < 1) {
				parent.fancyreturn1 = parent.SBPM.Service.Process.getProcessName(self.id);
				parent.$.fancybox.close();
			} else {
				parent.SBPM.Notification.Warning("Warning", "The followig element/s is/are not jet supported: " + parent.SBPM.Service.Process.isExecutbale(self.id).toString() + ". Process can\'t be executed.");
			}
		}
	}

	self.processes = parent.SBPM.Service.Process.getAllProcessesIDs();
	self.processArray = self.processes.map(function(element) {
		return new self.Process(element);

	});

}