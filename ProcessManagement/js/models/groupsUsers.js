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
	GroupsUsers = Model( "GroupsUsers", [ "isActive", "userID", "groupID" ], [ "userID", "groupID" ] );

	GroupsUsers.belongsTo( "user" );
	GroupsUsers.belongsTo( "group" );

	// Group.extend({);

	GroupsUsers.include({
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
				isActive: true
			});

			this.isActive( data.isActive );
		}
		
		// Custom validator object. Validators are (like the initialize function)
		// special in a sense that this object will be iterated over when the
		// "validate" method is executed.
		// validators: { }
	});
	
	return GroupsUsers;
});
