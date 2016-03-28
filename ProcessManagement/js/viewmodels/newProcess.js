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

		currentProcess( new Process({ isCase: false, name: "" }) );

		// The current process Name
		this.processName = ko.computed({
			read: function() {
				return currentProcess().name();
			},
			write: function( name ) {
				currentProcess().name( name );
			}
		});

		// Validate the model on every change
		this.processName.subscribe(function() {
			currentProcess().validate();
		});

		// Is it a Process or a Case?
		this.isCase = ko.computed({
			read: function() {
				return currentProcess().isCase();
			},
			write: function( bool ){
				currentProcess().isCase( bool );
			}
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
		this.processValid = ko.computed(function() {
			return currentProcess().isValid;
		});

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
  };

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

    currentProcess().subjectMap({});
    currentProcess().messageMap({});

		currentProcess().save(null, {
			success: function() {
				Router.goTo( currentProcess() );
			},
			error: function() {
        Notify.error("Error", "Saving the process failed.");
			}
		});
  };

	// Initialize our View.
	// Includes loading the template and creating the viewModel
	// to be applied to the template.
	var initialize = function() {
		var viewModel = new ViewModel();

		window.currentProcess = currentProcess;

		App.loadTemplate( "newProcess", viewModel, null, function() {
			App.loadTemplate( "newProcess/quickView", viewModel, "quick_table" );

		});
  };

	// Everything in this object will be the public API
	return {
		init: initialize
  };
});

