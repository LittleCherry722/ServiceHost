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
	Process = Model( "Process" );
	
	Process.attrs({
		name: "string",
		isCase: "boolean",
		graphID: "integer"
	});

	Process.belongsTo( "graph" );
	Process.hasMany( "graphs" );

	// Process.extend({);

	Process.include({
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
			this.subjects = []
			this.messages = [];
			this.isCreatedFromTable = false;
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

	Process.nameAlreadyTaken = function( name ) {
		var json,
			data = {
				name: name,
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

	return Process;
});