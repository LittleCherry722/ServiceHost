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
	Role = Model( "Role" );

	Role.attrs({
		name: "string",
		isActive: {
			type: "boolean",
			defaults: true
		}
	});

	Role.hasMany( "groups", { through: "groupsRoles" } );

	Role.include({
		// Initialize is a special method defined as an instance method.  If any
		// method named "initializer" is given, it will be called upon object
		// creation (when calling new model()) with the context of the model.
		// That is, "this" refers to the model itself.
		// This makes it possible to define defaults for attributes etc.
		initialize: function( data ) {
			this.groupIds = ko.observable();
			this.groupIdsReset = function() {
				var groupIds = _( this.groups() ).map(function( group ) {
					return group.id();
				});
				this.groupIds( groupIds );
			}
		},

		validators: {
			hasUniqueName: function() {
				var self = this;
				var results = Role.findByName( this.name() ).filter(function( result ) {
					return result != self;
				});
				if ( results.length > 0 ) {
					return "Roles must have an unique name.";
				}
			},
			nameNotNull: function() {
				if ( this.name().length < 3 ) {
					return "Name must be at least 3 characters long."
				}
			}
		},

		afterSave: function() {
			if ( !this.validate() ) {
				return;
			}

			var groupsNow, oldGroupIds, newGroupIds, toBePushedIds, toBeDeletedIds,

			groupsOld = this.groups();
			newGroupIds = this.groupIds();

			oldGroupIds = _( groupsOld ).map(function( group ) {
				return group.id();
			});

			toBePushedIds = _.difference( newGroupIds, oldGroupIds );
			toBeDeletedIds = _.difference( oldGroupIds, newGroupIds );

			_( toBePushedIds ).each(function( toBePushedId ) {
				groupsOld.push( Group.find( toBePushedId ) );
			})

			_( toBeDeletedIds ).each(function( toBeDeletedId ) {
				groupsOld.remove( Group.find( toBeDeletedId ) );
			})
		},

		beforeCreate: function() {
			this.id(-1);
		}
	});

	return Role;
});

