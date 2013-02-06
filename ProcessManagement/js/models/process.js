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
		graph: {
			type: "string",
			defaults: "{}",
			lazy: true
		}
	});

	Process.hasMany( "processInstances" );

	Process.include({

		// Initialize is a special method defined as an instance method.  If any
		// method named "initializer" is given, it will be called upon object
		// creation (when calling new model()) with the context of the model.
		// That is, "this" refers to the model itself.
		// This makes it possible to define defaults for attributes etc.
		initialize: function( data ) {
			var self = this;

			this.tableSubjects = []
			this.tableMessages = [];
			this.isCreatedFromTable = false;

			this.menuName = ko.computed(function() {
				if ( self.isCase() ) {
					return "[C] " + self.name();
				} else {
					return "[P] " + self.name();
				}
			});

			Process.lazyComputed( this, 'graphObject', {
				read: function() {
					return $.parseJSON( self.graph() );
				},
				write: function( graphObject ) {
					var graph = JSON.stringify( graphObject );
					self.graph( graph );
				}
			});

			Process.lazyComputed( this, 'subjects', function() {
				var subjects = {};

				_( self.graphObject().process ).each(function( element ) {
					subjects[ element['id'] ] = element['name'];
				});

				return subjects;
			});

			Process.lazyComputed( this, 'subjectIds', function() {
				var subjects = [];

				_( self.graphObject().process ).each(function( element ) {
					subjects.push( element['id'] );
				});

				return subjects;
			});

			Process.lazyComputed( this, "routings", {
				read: function() {
					if ( self.graphObject() && self.graphObject().routings ) {
						return self.graphObject().routings;
					} else {
						return [];
					}
				},
				write: function( routings ) {
					if ( !routings ) {
						routings = [];
					}
					var graphObject = self.graphObject();
					graphObject.routings = routings;
					self.graphObject( graphObject );
				}
			});
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
		// var json,
		//   data = {
		//     name: name,
		//     action: "getid"
		//   }
		// $.ajax({
		//   url: 'db/process.php',
		//   data: data,
		//   cache: false,
		//   type: "POST",
		//   async: false,
		//   success: function( data ) {
		//     json = JSON.parse( data );
		//   }
		// });
		// if ((json["code"] == "added") || (json["code"] == "ok")) {
		//   return json["id"] > 0;
		// } else {
		//   return false;
		// }
		return false;
	};

	return Process;
});
