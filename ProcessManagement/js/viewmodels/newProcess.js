define([
	"knockout",
	"models/process",
	"app"
], function( ko, Process, App ) {
	var ViewModel = function() {
		var self = this;

		// The current process Name
		this.processName = ko.observable( currentProcess().name() );

		// Is it a Process or a Case?
		this.isCase = ko.observable( currentProcess().isCase() );

		// Returns the Process Type if called without arguments.
		// Possibilities: "case" wenn isCase() == true, "process" otherwise.
		//
		// If called with an argument (used as setter), writes isCase() dependant
		// on the given argument. Sets isCase to true if newValue === "case", false
		// otherwise.
		this.processType = ko.computed({
			read: function() {
				return self.isCase() ? "case" : "process";
			},
			write: function( newValue ) {
				if ( newValue === "case" ) {
					self.isCase( true );
				} else {
					self.isCase( false );
				}
			}
		});

		// Does this Process already exist?
		this.processExists = ko.observable( Process.exists( this.processName() ) );

		// is this a valid process?
		this.processValid = ko.observable( currentProcess().isValid() );

		// Should a table be used (we NEED another name for this) for creating the
		// Process?
		this.displayTable = ko.observable( false );


		this.createProcess = createProcess;
	}

	// The current Process.
	// Create a new Process (but do not save it yet) and let every other
	// observable (name, isCase etc.) reference this process.
	// That way everything is updated automatically.
	//
	// Example: processName = currentProcess().name()
	var currentProcess = ko.observable( new Process() );

	// Creates the Process
	var createProcess = function() {

	}

	// Initialize our View.
	// Includes loading the template and creating the viewModel
	// to be applied to the template.
	var initialize = function() {
		var viewModel = new ViewModel();
		App.loadTemplate( "newProcess", viewModel, null, function() {
			App.loadTemplate( "newProcess/quickView", viewModel, "quickTable" )
		});
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


// var ViewModel = function() {
//   var self = this;

//   self.init = function(callback) {
//     console.log("newProcess init");
//     self.quickVM = new QuickViewModel();

//     self.quickVM.init();


//     //disable TableCreate if create Case
//     self.updateTableCreate = ko.computed(function(){
//       if(!self.isProcess())
//         self.quickVM.displayTable(false);
//       return null
//     });

//     callback();
//   }



//   self.processName = ko.observable("");

//   self.processExist = ko.computed(function() {
//     return SBPM.Service.Process.processExists(self.processName());

//   });
//   self.caseOrProcess = ko.observable("isProcess");
	
//   //Save in Database
//   self.isProcess = ko.computed(function(){
//     return self.caseOrProcess() == "isProcess"
//   });


//   self.createCheck = function() {
//     var process = self.processName();
//     console.log("createCheck " + process);

//     if(!process || process.length < 1) {
//       SBPM.Notification.Warning('Warning', 'Please enter a name for the process!');
//       return;
//     }

//     SBPM.Service.Process.deleteProcess(process);
//     self.goToProcess(process);
//   }

//   self.goToProcess = function(process) {
//     processVM = parent.SBPM.VM.goToPage("process");
//     processVM.showProcess(process, null, null, self.isProcess());
//     processVM.isProcess(self.isProcess());

//     if(self.quickVM.displayTable()) {
//       self.quickVM.createProcessFromTable();
//     }

//     // update list of recent processes
//     parent.$.publish("/process/change");

//     // close layer
//     self.close();
//   }

//   self.close = function() {
//     parent.$.fancybox.close();
//   }

//   self.tableCheck = function() {
//     self.quickVM.changeDisplay();
//   }

//   console.log("ViewModel for newProcess initialized.");
// }
// var QuickViewModel = function() {

//   var self = this;
//   self.name = "quickView";
//   self.init = function() {
//     console.log("Quick init");
//   }

//   self.displayTable = ko.observable(false);


//   self.fancyboxSize = ko.computed(function() {
//     if(self.displayTable()) {
//       parent.$('#fancybox-content').width('995px');
//       parent.$('#fancybox-content').height('300px');
//       parent.$.fancybox.center();
//     } else {
//       parent.$('#fancybox-content').width('211px');
//       parent.$('#fancybox-content').height('300px');
//       parent.$.fancybox.center();
//     }

//     return null
//   })

//   self.changeDisplay = function() {
//     if(self.displayTable())
//       self.displayTable(false);
//     else
//       self.displayTable(true);
//   }
//   //Used as a class.
//   self.Subject = function(name1) {
//     var self = this;
//     self.name = ko.observable(name1);

//   }

//   self.removeSubject = function(subject) {
//     self.subjectList.remove(subject);
//   }

//   self.addSubject = function() {
//     self.subjectList.push(new self.Subject(""));
//   }
//   //Contains all Subjects.
//   self.subjectList = ko.observableArray([new self.Subject("Subject 1"), new self.Subject("Subject 2")]);

//   //Used as a class.
//   self.Message = function(s1, message, s2) {
//     var self = this;
//     self.message = message;
//     self.sender = s1;
//     self.receiver = s2;

//   }

//   self.removeMessage = function(message) {
//     self.messageList.remove(message);
//   }

//   self.addMessage = function() {
//     self.messageList.push(new self.Message("", "", ""));
//   }
//   //Contains all Messages.
//   self.messageList = ko.observableArray([new self.Message("", "File", ""), new self.Message("", "Answer", "")]);

//   self.noMessage = function(mesOb) {
//     var bool = true;

//     if(mesOb.message != null && mesOb.message.replace(" ", "") != "" && mesOb.sender != null && mesOb.sender.replace(" ", "") != "" && mesOb.receiver != null && mesOb.receiver.replace(" ", "") != "")
//       bool = false;
//     return bool;
//   }
//   //Checks if message is complete.
//   self.completeMessage = function(mesOb) {
//     var bool = false;

//     if(mesOb.message != null && mesOb.message.replace(" ", "") != "" && mesOb.sender != null && mesOb.sender.name().replace(" ", "") != "" && mesOb.receiver != null && mesOb.receiver.name().replace(" ", "") != "")
//       bool = true;
//     return bool;
//   }
//   //Returns an array to be used in SBPM.Service.Process.createProcessFromTable.
//   self.cleanMessages = function() {
//     var array = new Array();
//     for( i = self.messageList().length - 1; i >= 0; i--) {
//       if(self.completeMessage(self.messageList()[i]))
//         array.push(self.messageList()[i]);
//     }
//     for( i = array.length - 1; i >= 0; i--) {
//       array[i].sender = array[i].sender.name().toLowerCase();
//       array[i].receiver = array[i].receiver.name().toLowerCase();
//     }
//     for( i = array.length - 1; i >= 0; i--) {
//       array[i] = {
//         message : array[i].message,
//         sender : array[i].sender,
//         receiver : array[i].receiver
//       };

//     }

//     return array;
//   }
//   //Returns an array to be used in SBPM.Service.Process.createProcessFromTable.
//   self.cleanSubjects = function() {
//     var array = new Array();
//     for( i = self.subjectList().length - 1; i >= 0; i--) {
//       if(self.subjectList()[i].name().replace(" ", "") != "" && self.subjectList()[i].name() != null)
//         array[i] = self.subjectList()[i].name();

//     }
//     return array;
//   }

//   self.createProcessFromTable = function() {
//     var sub = self.cleanSubjects();
//     //console.log(sub);

//     var mes = self.cleanMessages();
//     //console.log(mes);

//     parent.SBPM.Service.Process.createProcessFromTable(sub, mes);
//   }
// }

