var ViewModel = function() {

    var self = this;

    self.processes = ko.observableArray();

    self.init = function(callback) {
        self.processes(SBPM.Service.Process.getAllProcesses());
        
        if(callback)
            callback();
    }

    self.load = function(process) {
        parent.SBPM.VM.goToPage("process").showProcess(process);        

        // update list of recent processes
        parent.$.publish("/process/change");

        self.close();

    }

    self.remove = function(process) {
        if (SBPM.Service.Process.deleteProcess(process)) {

            self.init();
            
            // update list of recent processes
            parent.$.publish("/process/change");

        } else
            parent.SBPM.Notification.Error("Error", "Deleting the process failed.");

    }

    self.isLocked = function(processName){
        return (parent.SBPM.VM.contentVM() instanceof parent.ProcessViewModel && parent.SBPM.VM.contentVM().processName() === processName);   
    }

    self.close = function() {
        parent.$.fancybox.close();
    }

}
