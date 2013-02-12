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
	Group = Model( "Group" );

	Group.attrs({
		name: "string",
		isActive: {
			type: "boolean",
			defaults: true
		}
	});

	Group.include({
		beforeCreate: function() {
			this.id(-1);
		},

		validators: {
			hasUniqueName: function() {
				var self = this;
				var results = Group.findByName( this.name() ).filter(function( result ) {
					return result != self;
				});
				if ( results.length > 0 ) {
					return "all groups must have an unique name.";
				}
			},
			nameNotNull: function() {
				if ( this.name().length < 3 ) {
					return "Name must be at least 3 characters long."
				}
			}
		}
	});

	Group.hasMany( "roles", { through: "groupsRoles" } );

	return Group;
});
