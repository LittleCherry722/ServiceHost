var ViewModel = function() {
    var self = this;
    self.processName = ko.observable("");

    self.init = function(callback){
        callback();
    }

    self.saveAsCheck = function() {
        var process = self.processName();
        console.log("saveAsCheck " + process);

        if (!process || process.length < 1) {
            SBPM.Notification.Warning('Warning', 'Please enter a name for the process!');
            return;
        }

        console.log("saveAsProcess: " + process);

        var result = SBPM.Service.Process.saveProcess(process, false, true);

        if (result) {

            if (result['code'] == "duplicated") {

                SBPM.Dialog.YesNo('Warning', 'Process\' name already exists. Do you want to overwrite the process?', function() {

                    // overwrite the existing process
                    SBPM.Service.Process.saveProcess(process, true, true);

                    // reload recent processes
                    parent.SBPM.VM.menuVM.init();

                    // close the layer
                    self.close();

                });

            } else {

                // set the new name
                self.processName(process);

                // reload recent processes
                parent.SBPM.VM.menuVM.init();

                // close the layer
                self.close();

                SBPM.Notification.Info("Information", "Process successfully created.");

            }

            // load the new process
            parent.SBPM.VM.processVM.processName(process);

        } else
            SBPM.Notification.Error("Error", "Could not create process.");

    }

    self.close = function() {
        parent.$.fancybox.close();
    }

    console.log("ViewModel for saveAsProcess initialized.");
}

