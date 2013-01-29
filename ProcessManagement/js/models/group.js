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
		}
	});

	Group.hasMany( "roles", { through: "groupsRoles" } );

	return Group;
});
