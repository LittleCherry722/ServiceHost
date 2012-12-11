
var ViewModel = function() {
  var self = this;

self.init = function(callback) {
    console.log("editProcessSettings init");
    callback();
}

  self.processName = ko.observable(parent.SBPM.VM.contentVM().processName());
  console.log(self.processName);
  console.log(self.processName());

  self.processExist = ko.computed(function() {
    return parent.SBPM.Service.Process.processExists(self.processName());

  });

  self.caseOrProcess = ko.observable("isProcess");

  self.createCheck = function() {
		var process = self.processName();
		console.log("createCheck " + process);

		if(!process || process.length < 1) {
			SBPM.Notification.Warning('Warning', 'Please enter a name for the process!');
			return;
		}

		parent.$.fancybox.close();
		SBPM.Service.Process
	}

  console.log("ViewModel for editProcessSettings initialized.");
}
