define([
	"knockout",
	"model",
	"underscore"
], function( ko, Model, _ ) {

	// Our main model that will be returned at the end of the function.
	//
	// Process is responsivle for everything associated with processes directly.
	//
	// For example: Getting a list of all processes, savin a process,
	// validating the current process etc.
	Process = Model( "Process", [ "name", "isCase", "graphID" ] );

	Process.extend({
		createFromTable: function( subjects, messages, callback ) {
			// updateListOfSubjects();
			callback();
		},

		findByName: function( processName ) {
			return _( Process.all() ).find(function( process ) {
				return process.name() === processName;
			});
		}
	});

	Process.belongsTo([ "graph" ]);
	// Process.hasMany([ "graph" ]);

	Process.include({
		subjects: [],
		messages: [],
		isCreatedFromTable: false,

		menuName: function() {
			if ( this.isCase() ) {
				return "[C] " + this.name();
			} else {
				return "[P] " + this.name();
			}
		},

		// Initialize is a special method defined as an instance method.  If any
		// method named "initializer" is given, it will be called upon object
		// creation (when calling new model()) with the context of the model.
		// That is, "this" refers to the model itself.
		// This makes it possible to define defaults for attributes etc.
		initialize: function( data ) {
			if ( !data ) {
				data = {};
			}

			// Set some defaults for the data object (used as a hash)
			_( data ).defaults({
				name: "",
				isCase: false
			});

			this.name( data.name );
			this.isCase( data.isCase );
		},
		
		// Custom validator object. Validators are (like the initialize function)
		// special in a sense that this object will be iterated over when the
		// "validate" method is executed.
		validators: {
			// Does this Process already exist?
			exists: function() {
				if ( Process.nameAlreadyTaken( this.name() ) ) {
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
	Process.nameAlreadyTaken = function( name ) {
		var json,
			data = {
				processname: name,
				action: "getid"
			}
		$.ajax({
			url: 'db/process.php',
			data: data,
			cache: false,
			type: "POST",
			async: false,
			success: function( data ) {
				json = JSON.parse( data );
			}
		});
		if ((json["code"] == "added") || (json["code"] == "ok")) {
			return json["id"] > 0;
		} else {
			return false;
		}
	};

	/**
	 *	Checks whether a Process with the given Name already exists.
	 *	TODO should not really be here...
	 */
	Process.exists = function(name) {
		return true;
	}

	return Process;
});
