var ViewModel = function() {
	var self = this;

	self.init = function(callback) {
		console.log("newInstance init");
		
console.log(self.processes);
console.log(self.processArray);

	console.log(self.processTable());
	
		//callback();
	}

	self.checkProcess = function(id) {
		if(parent.SBPM.Service.Process.isExecutbale(id).length < 1) {
			alert("true " + id)
			parent.fancyreturn1 = parent.SBPM.Service.Process.getProcessName(id);
			parent.$.fancybox.close();
		} else {
			alert('false ' + id);
			parent.SBPM.Notification.Warning("Warning","The followig element/s is/are not jet supported: "+parent.SBPM.Service.Process.isExecutbale(processes["+i+"]).toString()+ ". Process can\'t be executed."); 
					
		}
	}

	self.Process = function(id) {
		var self = this;
		self.id = id;
		self.name = parent.SBPM.Service.Process.getProcessName(id);
		self.isExexutable = parent.SBPM.Service.Process.isExecutbale(id);
		self.start = function(){self.checkProcess(id);}

	}
	self.processes = parent.SBPM.Service.Process.getAllProcessesIDs();
	self.processArray = self.processes.map(function(element) {
		return new self.Process(element);

	});
self.processTable = ko.observableArray(self.processArray);


}