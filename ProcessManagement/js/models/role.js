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
	Role = Model( "Role", [ "name", "isActive" ] );

	Role.hasMany( "groups", { through: "groupsRoles" } );

	// Group.extend({);

	Role.include({
		// Initialize is a special method defined as an instance method.  If any
		// method named "initializer" is given, it will be called upon object
		// creation (when calling new model()) with the context of the model.
		// That is, "this" refers to the model itself.
		// This makes it possible to define defaults for attributes etc.
		initialize: function( data ) {
			var self = this;

			if ( !data ) {
				data = {};
			}

			// Set some defaults for the data object (used as a hash)
			_( data ).defaults({
				name: "",
				isActive: true
			});

			this.name( data.name );
			this.isActive( data.isActive );

			this.groupIDs = ko.observable();
			this.groupIDsReset = function() {
				var groupIDs = _( this.groups() ).map(function( group ) {
					return group.id();
				});
				this.groupIDs( groupIDs );
			}
		},

		beforeSave: function() {
			var groupsNow, oldGroupIDs, newGroupIDs, toBePushedIDs, toBeDeletedIDs,

			groupsOld = this.groups();
			newGroupIDs = this.groupIDs();
			
			oldGroupIDs = _( groupsOld ).map(function( group ) {
				return group.id();
			});

			toBePushedIDs = _.difference( newGroupIDs, oldGroupIDs );
			toBeDeletedIDs = _.difference( oldGroupIDs, newGroupIDs );

			_( toBePushedIDs ).each(function( toBePushedID ) {
				groupsOld.push( Group.find( toBePushedID ) );
			})

			_( toBeDeletedIDs ).each(function( toBeDeletedID ) {
				groupsOld.remove( Group.find( toBeDeletedID ) );
			})
		}
		
		// Custom validator object. Validators are (like the initialize function)
		// special in a sense that this object will be iterated over when the
		// "validate" method is executed.
		// validators: { }
	});
	
	return Role;
});

