define([
	"knockout",
	"router",
	"model",
	"underscore"
], function( ko, Router, Model, _ ) {

	// Our main model that will be returned at the end of the function.
	//
	// Process is responsivle for everything associated with processes directly.
	//
	// For example: Getting a list of all processes, savin a process,
	// validating the current process etc.
	Process = Model( "Process", [ "name", "isCase" ] );

	Process.extend({

	});

	Process.include({
		// Initialize is a special method defined as an instance method.  If any
		// method named "initializer" is given, it will be called upon object
		// creation (when calling new model()) with the context of the model.
		// That is, "this" refers to the model itself.
		// This makes it possible to define defaults for attributes etc.
		initialize: function( data ) {
			if ( !data ) {
				data = {};
			}
			_(data).defaults({ name: "", isCase: false });
			this.name   = ko.observable(data.name);
			this.isCase = ko.observable(data.isCase);
		},
		
		validators: {
			// Does this Process already exist?
			exists: function() {
				return Process.exists(this.name());
			},

			// Does this process have a valid name?
			isNameInvalid: function() {
				return this.name().length > 0;
			}
		}
	});

	// Javascript... Define these functions here so we can call them.
	// They get overwritten before execution anyway.
	Process.exists = Process.all = function(){};


	// get the ProcessID of a given Process identified by its name.
	// We do not really want this method but it is needed for now because of the
	// old PHP server interface.
	getProcessID = function(name) {
		// TODO actually do something usefull here.
		return 0;
	}

	/**
	 *	Returns a list of all Processes currently available.
	 *
	 *	@return {ko.observableArray<Process>} the Array of Processes
	 */
	Process.all = function() {
		processes = ko.observableArray([ new Process( { name: "test Process" } ) ]);
		return processes();
	}

	/**
	 *	Checks whether a Process with the given Name already exists.
	 */
	Process.exists = function(name) {
		return getProcessID(name) > 0
	}
	
	return Process;
});
