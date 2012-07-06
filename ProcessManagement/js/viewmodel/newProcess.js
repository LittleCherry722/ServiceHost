var ViewModel = function() {
	var self = this;
	self.processName = ko.observable("");

	self.createCheck = function() {
		var process = self.processName();
		console.log("createCheck " + process);

        if(!process || process.length < 1){
            SBPM.Notification.Warning('Warning', 'Please enter a name for the process!');
            return;
        }

        // if process name does not exist
		if(!SBPM.Service.Process.processExists(process)) {
		    
			// load a new process
			parent.SBPM.VM.processVM.showProcess(process);
			
			// update list of recent processes
            parent.SBPM.VM.menuVM.init();
            
            // close layer
            self.close();
            
		} else { // otherwise ask the user to keep the given name anyhow
		    
			SBPM.Dialog.YesNo('Warning', 'Process\' name already exists. Do you want to proceed with the given name?', 
			function() { // yes
			    
			    // close the newProcess layer
				self.close();
				
			});

		}
	}

	self.close = function() {
		parent.$.fancybox.close();
	}

	console.log("ViewModel for newProcess initialized.");
}

