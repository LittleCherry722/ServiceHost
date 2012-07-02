var ViewModel = function() {
	var self = this;
	self.processName = ko.observable("");

	self.createCheck = function() {
		var process = self.processName();
		console.log("createCheck " + process);

		if(SBPM.Service.Process.processExists(process) == false) {
			console.log("exists not");

			SBPM.Service.Process.newProcess(process);

			self.close();
			parent.SBPM.VM.processVM.showProcess(process);
            parent.SBPM.VM.menuVM.init();
		} else {
			console.log("may exists");
			if(SBPM.Service.Process.processExists(process) == true) {
				console.log("exists");
				SBPM.Dialog.YesNo('Warning', 'Process already exists. Do you want to overwrite it?', function() {
					SBPM.Service.Process.deleteProcess(process);
					SBPM.Service.Process.newProcess(process);
					self.close();
					parent.SBPM.VM.processVM.showProcess(process);

				});
			} else {
				console.log("empty");
				SBPM.Dialog.Notice('Empty', 'Please enter a name for the process!');
			}
		}
	}

	self.close = function() {
		parent.$.fancybox.close();
	}

	console.log("ViewModel for newProcess initialized.");
}

