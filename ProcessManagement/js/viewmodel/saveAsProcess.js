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
                    parent.SBPM.VM.save(process, true, true);
                    
                    // close the layer
                    self.close();

                });

            } else {

                SBPM.Notification.Info("Information", "Process successfully created.");

                // close the layer
                self.close();

            }



        } else
            SBPM.Notification.Error("Error", "Could not create process.");

    }

    self.close = function() {
        parent.$.fancybox.close();
    }

    console.log("ViewModel for saveAsProcess initialized.");
}

