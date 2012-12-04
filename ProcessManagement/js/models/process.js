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
		name: ko.observable(""),
		isCase: ko.observable(false),

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
			this.name(data.name);
			this.isCase(data.isCase);
		},
		
		// Custom validator object. Validators are (like the initialize function)
		// special in a sense that this object will be iterated over when the
		// "validate" method is executed.
		validators: {
			// Does this Process already exist?
			exists: function() {
				if ( Process.exists( this.name() ) ) {
					return "Process already exists! Please choose a different name.";
				}
			},

			// Does this process have a valid name?
			isNameInvalid: function() {
				if ( this.name().length < 2 ) {
					return "Process name is Invalid. Process name must have at least two characters.";
				}
			}
		}
	});

	// Javascript... Define these functions here so we can call them.
	// They get overwritten before execution anyway.
	Process.exists = Process.all = function(){};


	/**
	 *	Returns a list of all Processes currently available.
	 *	TODO should not really be here...
	 *
	 *	@return {ko.observableArray<Process>} the Array of Processes
	 */
	Process.all = function() {
		this.all = ko.observableArray([ new Process( { name: "test Process" } ) ]);
		return this.all();
	}

	/**
	 *	Checks whether a Process with the given Name already exists.
	 *	TODO should not really be here...
	 */
	Process.exists = function(name) {
		return true;
	}
	
	return Process;
});
