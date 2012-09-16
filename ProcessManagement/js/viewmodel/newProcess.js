var ViewModel = function() {
	var self = this;

	self.init = function(callback) {
		console.log("newProcess init");
		self.quickVM = new QuickViewModle();

		self.quickVM.init();

		callback();
	}

	self.processName = ko.observable("");

	self.createCheck = function() {
		var process = self.processName();
		console.log("createCheck " + process);

		if(!process || process.length < 1) {
			SBPM.Notification.Warning('Warning', 'Please enter a name for the process!');
			return;
		}

		// if process name does not exist
		if(!SBPM.Service.Process.processExists(process)) {

            parent.SBPM.VM.goToPage("process").showProcess(process);

			if(self.quickVM.displayTable()) {
				self.quickVM.createProcessFromTable();
			}

			// update list of recent processes
			parent.SBPM.VM.menuVM.init();

			// close layer
			self.close();

		} else {// otherwise ask the user to keep the given name anyhow

			SBPM.Dialog.YesNo('Warning', 'Process\' name already exists. Do you want to proceed with the given name?', function() {// yes

				// close the newProcess layer
				self.close();

			});

		}
	}

	self.close = function() {
		parent.$.fancybox.close();
	}

	self.tableCheck = function() {
		self.quickVM.changeDisplay();
	}

	console.log("ViewModel for newProcess initialized.");
}

var QuickViewModle = function() {

	var self = this;
	self.name = "quickView";
	self.init = function() {
		console.log("Quick init");
	}

	self.displayTable = ko.observable(false);

	self.fancyboxSize = ko.computed(function() {
		
		
		if(self.displayTable()) {
			parent.$('#fancybox-content').width('995px');
			parent.$('#fancybox-content').height('300px');
			parent.$.fancybox.center();
		} else {
			parent.$('#fancybox-content').width('211px');
			parent.$('#fancybox-content').height('300px');
			parent.$.fancybox.center();
		}

		return null
	})

	self.changeDisplay = function() {
		if(self.displayTable())
			self.displayTable(false);
		else
			self.displayTable(true);
	}
	
	//Used as a class. 
	self.Subject = function(name1) {
		var self = this;
		self.name = ko.observable(name1);

	}

	self.removeSubject = function(subject) {
		self.subjectList.remove(subject);
	}

	self.addSubject = function() {
		self.subjectList.push(new self.Subject(""));
	}

	//Contains all Subjects.
	self.subjectList = ko.observableArray([new self.Subject("Subject 1"), new self.Subject("Subject 2")]);

	//Used as a class. 
	self.Message = function(s1, message, s2) {
		var self = this;
		self.message = message;
		self.sender = s1;
		self.receiver = s2;

	}

	self.removeMessage = function(message) {
		self.messageList.remove(message);
	}

	self.addMessage = function() {
		self.messageList.push(new self.Message("", "", ""));
	}


	//Contains all Messages.
	self.messageList = ko.observableArray([new self.Message("", "File", ""), new self.Message("", "Answer", "")]);

	
	self.noMessage = function(mesOb) {
		var bool = true;

		if(mesOb.message != null && mesOb.message.replace(" ", "") != "" && mesOb.sender != null && mesOb.sender.replace(" ", "") != "" && mesOb.receiver != null && mesOb.receiver.replace(" ", "") != "")
			bool = false;
		return bool;
	}
	//Checks if message is complete.
	self.completeMessage = function(mesOb) {
		var bool = false;

		if(mesOb.message != null && mesOb.message.replace(" ", "") != "" && mesOb.sender != null && mesOb.sender.name().replace(" ", "") != "" && mesOb.receiver != null && mesOb.receiver.name().replace(" ", "") != "")
			bool = true;
		return bool;
	}

	//Returns an array to be used in SBPM.Service.Process.createProcessFromTable.
	self.cleanMessages = function() {
		var array = new Array();
		for( i = self.messageList().length - 1; i >= 0; i--) {
			if(self.completeMessage(self.messageList()[i]))
				array.push(self.messageList()[i]);
		}
		for( i = array.length - 1; i >= 0; i--) {
			array[i].sender = array[i].sender.name().toLowerCase();
			array[i].receiver = array[i].receiver.name().toLowerCase();
		}
		for( i = array.length - 1; i >= 0; i--) {
			array[i] = {
				message : array[i].message,
				sender : array[i].sender,
				receiver : array[i].receiver
			};

		}

		return array;
	}
	//Returns an array to be used in SBPM.Service.Process.createProcessFromTable.
	self.cleanSubjects = function() {
		var array = new Array();
		for( i = self.subjectList().length - 1; i >= 0; i--) {
			if(self.subjectList()[i].name().replace(" ", "") != "" && self.subjectList()[i].name() != null)
				array[i] = self.subjectList()[i].name();

		}
		return array;
	}

	self.createProcessFromTable = function() {
		var sub = self.cleanSubjects();
		//console.log(sub);

		var mes = self.cleanMessages();
		//console.log(mes);

		parent.SBPM.Service.Process.createProcessFromTable(sub, mes);
	}
}

