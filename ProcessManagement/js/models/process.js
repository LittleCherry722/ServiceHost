define([
	"knockout",
	"router"
], function( ko, Router ) {

	// Our main model that will be returned at the end of the function.
	//
	// Process is responsivle for everything associated with processes directly.
	//
	// For example: Getting a list of all processes, savin a process,
	// validating the current process etc.
	Process = function(name, isCase) {
		this.isCase = ko.observable( !!isCase );
		this.name = ko.observable( name );
	}

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

	// Define some validators.
	// These will be called by the valid method to check whether a given
	// process is valid or not.
	//
	// This object should be a set of methods that return true if what they
	// validate is valid and false if it does not pass validation.
	Process.prototype.validators = {

		// Does this Process already exist?
		exists: function() {
			return Process.exists(this.name());
		},

		// Does this process have a valid name?
		isNameInvalid: function() {
			return this.name().length > 0;
		}
	}

	// Validates the current process.
	// Iterates over the list of Validators defined above and execute each of
	// them.
	// If one validator returns false, exit early and mark the model as invalid.
	// If no validator retrurns false we assume this model to be valid and
	// therefore return true.
	Process.prototype.isValid = ko.computed(function() {
		var validator;
		for ( validator in this.validators ) {
			if ( !validator() ) {
				return false
			}
		}
		return true;
	}.bind( this ));

	/**
	 *	Return the url for this process.
	 *	URL might be dependant on the current Router being used.
	 *
	 *	Calls Router.processPath(process) internally.
	 *
	 *	@return {String} the path to this process.
	 */
	Process.prototype.url = function() {
		return Router.processPath(this);
	}

	/**
	 *	Returns a list of all Processes currently available.
	 *
	 *	@return {ko.observableArray<Process>} the Array of Processes
	 */
	Process.all = function() {
		processes = ko.observableArray([ new Process( "test Process" ) ]);
		return processes();
	}

	/**
	 *	Checks whether a Process with the given Name already exists.
	 *
	 */
	Process.exists = function(name) {
		return getProcessID(name) > 0
	}
	
	return Process;
});
