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
					self.displayTable( false );
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

		// Event handler for process Creation
		this.createProcess = createProcess;

		/*
		 *	Everything related to the table view goes here
		 */

		// A list ob all Subjects used in the table process creation form.
		this.subjectList = ko.observableArray([
			new Subject( "Subject 1" ),
			new Subject( "Subject 2" )
		]);

		/**
		 *	Removes a subject from the list of subjects.
		 *	@param {Subject} subject the subject to be removed.
		 */
		this.removeSubject = function( subject ) {
			this.subjectList.remove( subject );
		}

		/**
		 *	Adds an empty subject to the list of subjects.
		 */
		this.addSubject = function() {
			this.subjectList.push( new Subject() );
		}


		/**
		 *	Removes a message from the list of messages.
		 *	@param {Message} message the subject to be removed.
		 */
		this.removeMessage = function( message ) {
			this.messageList.remove( message );
		}

		/**
		 *	Adds a subject to the list of subjects.
		 */
		this.addMessage = function() {
			this.messageList.push( new Message() );
		}

		//Contains all Messages.
		this.messageList = ko.observableArray([
			new Message("", "File", ""),
			new Message("", "Answer", "")
		]);
	}

	/**
	 *	Local Subject object. Is only needed in the context of this ViewModel,
	 *	therefore no need to make it public and create its own file.
	 *
	 *	To be used as an object ( var subject = new Subject("my name"); )
	 *
	 *	@param {String} name the name of the subject
	 */
	var Subject = function( name ) {

		// Make empty string the default for the subject name
		if ( !name ) name = "";
		this.name = ko.observable( name )
	};

	/**
	 *	Local Message object. Is only needed in the context of this ViewModel,
	 *	therefore no need to make it public and create its own file.
	 *
	 *	To be used as an object:
	 *		var message = new Message("Subject 1", "Answer", "Subject 2");
	 *
	 *	@param {String} sender the Sender of this message
	 *	@param {String} message the message name / content
	 *	@param {String} receiver the receiver of this message
	 */
	var Message = function( sender, message, receiver ) {

		// Make empty string the default for every argument.
		if ( !sender ) sender = "";
		if ( !message ) message = "";
		if ( !receiver ) receiver = "";

		this.sender = ko.observable( sender);
		this.message = ko.observable( message );
		this.receiver = ko.observable( receiver );
	};

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
		console.log(viewModel);
		App.loadTemplate( "newProcess", viewModel, null, function() {
			App.loadTemplate( "newProcess/quickView", viewModel, "quick_table" )
		});
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});






var ViewModel = function() {
	var self = this;

	self.processExist = ko.computed(function() {
		return SBPM.Service.Process.processExists(self.processName());

	});
	
	self.createCheck = function() {
		var process = self.processName();
		console.log("createCheck " + process);

		if(!process || process.length < 1) {
			SBPM.Notification.Warning('Warning', 'Please enter a name for the process!');
			return;
		}

		SBPM.Service.Process.deleteProcess(process);
		self.goToProcess(process);
	}

	self.goToProcess = function(process) {
		processVM = parent.SBPM.VM.goToPage("process");
		processVM.showProcess(process, null, null, self.isProcess());
		processVM.isProcess(self.isProcess());

		if(self.quickVM.displayTable()) {
			self.quickVM.createProcessFromTable();
		}

		// update list of recent processes
		parent.$.publish("/process/change");

		// close layer
		self.close();
	}
}

var QuickViewModel = function() {
	var self = this;


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

