define([
	"knockout",
	"app",
	"notify",
	"router",
	"models/process",
	"models/message",
	"models/subject"
	// "tk_graph"
], function( ko, App, Notify, Router, Process, Message, Subject ) {
	window.Message = Message;
	window.Subject = Subject;
	var ViewModel = function() {
		var self = this;

		currentProcess( new Process() );

		// The current process Name
		this.processName = ko.observable("");

		// Validate the model on every change
		this.processName.subscribe(function() {
			currentProcess().validate();
		});

		// Is it a Process or a Case?
		this.isCase = ko.observable( currentProcess().isCase() );
		this.isCase.subscribe( function( newValue ) {
			currentProcess().isCase( newValue );
		});

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
					self.displayTable( false );
				} else {
					self.isCase( false );
				}
			}
		});

		// is this a valid process?
		this.processValid = currentProcess().isValid;

		// Should error messages be displayed?
		// We choose to not display error messages if the name text field is empty,
		// but there should be a better way to do it...
		this.showErrors = ko.computed(function() {
			return self.processName() && !currentProcess().isValid();
		});

		// Every error
		this.processErrors = currentProcess().errors;

		// Should a table be used (we NEED another name for this) for creating the
		// Process?
		this.displayTable = ko.observable( false );

		// Event handler for process Creation
		this.createProcess = createProcess;


		/*
		 *	Everything related to the table view goes here
		 */

		// A list ob all Subjects used in the table process creation form.
		this.subjectList = Subject.all;
		this.subjectList([
			new Subject( "Subject 1" ),
			new Subject( "Subject 2" )
		]);

		//Contains all Messages.
		this.messageList = Message.all;
		this.messageList([
			new Message(Subject.all()[0], "File", Subject.all()[1]),
			new Message(Subject.all()[1], "Answer", Subject.all()[0])
		]);

		this.addSubject = Subject.add;
		this.addMessage = Message.add;

		this.removeSubject = Subject.remove;
		this.removeMessage = Message.remove;
	}

	/*
	 * The current Process.
	 * Create a new Process (but do not save it yet) and let every other
	 * observable (name, isCase etc.) reference this process.
	 * That way everything is updated automatically.
	 *
	 * Example: processName = currentProcess().name()
	 */
	var currentProcess = ko.observable();

	// Creates the Process
	var createProcess = function() {
		var graph;

		if ( currentProcess().name().length < 1 ) {
			Notify.warning( 'Warning', 'Please enter a name for the process!' );
			return;
		}

		if ( this.displayTable() ) {
			currentProcess().subjects = Subject.allClean();
			currentProcess().messages = Message.allClean();
			currentProcess().isCreatedFromTable = true;
		}

		currentProcess().save(function() {
			Router.goTo( currentProcess() );
		});
	}

	// Initialize our View.
	// Includes loading the template and creating the viewModel
	// to be applied to the template.
	var initialize = function() {
		var viewModel = new ViewModel();

		// Bind the name of our process to the process name input field
		currentProcess().name = viewModel.processName;

		App.loadTemplate( "newProcess", viewModel, null, function() {
			App.loadTemplate( "newProcess/quickView", viewModel, "quick_table" );

		});
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});




// var ViewModel = function() {
//   var self = this;

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
// }

// var QuickViewModel = function() {
//   var self = this;


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
// }

