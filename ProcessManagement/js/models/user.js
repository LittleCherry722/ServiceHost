/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
define([
	"knockout",
	"model",
	"underscore",
	"async",
	"notify"
], function( ko, Model, _, async, notify ) {
	// Our main model that will be returned at the end of the function.
	//
	// Process is responsivle for everything associated with processes directly.
	//
	// For example: Getting a list of all processes, savin a process,
	// validating the current process etc.
	User = Model( "User" );

	User.attrs({
		name: "string",
		isActive: {
			type: "boolean",
			defaults: true
		},
		inputPoolSize: {
			type: "integer",
			defaults: 8
		},
		providerMail: {
			type: "jsonArray",
			defaults: []
		}
	});

	User.hasMany( "groups", { through: "groupsUsers" } );

	User.include({
		// Initialize is a special method defined as an instance method.  If any
		// method named "initializer" is given, it will be called upon object
		// creation (when calling new model()) with the context of the model.
		// That is, "this" refers to the model itself.
		// This makes it possible to define defaults for attributes etc.
		initialize: function( data ) {

			// Set some defaults for the data object (used as a hash)
			_( data ).defaults({
				messageCount: 0
			});

			this.messageCount = ko.observable( data.messageCount );
			//this.providerMail = ko.observableArray(data.providerMail);
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
				var results = User.findByName( this.name() ).filter(function( result ) {
					return result != self;
				});
				if ( results.length > 0 ) {
					return "All users must have an unique name.";
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
				modifiedGroups = [];

			groupsOld = this.groups();
			newGroupIds = this.groupIds();

			oldGroupIds = _( groupsOld ).map(function( group ) {
				return group.id();
			});

			toBePushedIds = _.difference( newGroupIds, oldGroupIds );
			toBeDeletedIds = _.difference( oldGroupIds, newGroupIds );

			if ( toBePushedIds.length === 0 && toBeDeletedIds.length === 0 ) {
				return;
			}

			_( toBePushedIds ).each( function( toBePushedId ) {
				modifiedGroups.push({
					handleMethodName: "push",
					instance: Group.find( toBePushedIds )
				});
			});
			_( toBeDeletedIds ).each( function( toBeDeletedId ) {
				modifiedGroups.push({
					handleMethodName: "remove",
					instance: Group.find( toBeDeletedId )
				});
			});

			asyncHandleAssociations( groupsOld, modifiedGroups );
		},

		beforeCreate: function() {
			this.id(-1);
		}
	});

	var asyncHandleAssociations = function( oldModels, modifiedModels ) {
		async.eachLimit( modifiedModels, 5, function( model, callback ) {
			oldModels[ model.handleMethodName ]( model.instance, {
				success: function( textStatus ) {
					callback();
				},
				error: function( textStatus, error ) {
					callback( "error" );
				}
			});
		}, function( error, results ) {
			if ( error ) {
				notify.error( "Error", "Error adding group to user. Not all groups have been added." )
			}
		});
	}

	return User;
});
