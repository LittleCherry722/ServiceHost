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
	"underscore",
	"knockout",
	"router",
	"require",
	"async",
  "model/associations",
  "model/attributes",
  "model/storage"
  // "arne/model/attributes"
	// "jquery"
], function( _, ko, Router, require, async, Associations, Attributes, Storage ) {
	var Model,
			models = [];

	// Our Model cunstructor function. Returns another constructor function.
	Model = function( modelName, ajaxOptions ) {
		var instances;

		// All currently available instances this Model are stored in this
		// observableArray.
		instances = ko.observableArray([]);

		// Define our Base model
		var Result = function( data ) {
			var camelCasedAttribute,
					self = this;

			this.isBeingInitialized = true;

			if ( !data ) {
				data = {};
			}

			this.isNewRecord = true;
			this.isDestroyed = false;

			// Set the class Name. Needed for routes, to display the model (toString
			// method coming soon) etc.
			this.className = modelName;


			// The global reset method for our model instance.
			// Resets every attribute of this model.
			this.reset = function() {
				_( Result.attrs() ).each(function( attrOptions, attrName ) {
					this[ attrName + "Reset" ]();
				}, this);
			}

			// Initialize an empty error object.
			this.errors = ko.observableArray([]);

			this.isValid = ko.observable();
			this.isInvalid = ko.computed(function() {
				return !this.isValid();
			}.bind(this));

			/*
			 * Validates the model.
			 * Iterates over the list of Validators defined (if any) and execute each of
			 * them.
			 * A validator is considered "false" when it returns something of type
			 * "string".  This return value is also used as error message and
			 * appendet to the observable Error Object.
			 */
			this.validate = function() {
				var validator, message, newErrors;

				newErrors = []

				// lets assume there are no errors;
				valid = true;

				// loop through every validator and execute it.
				for ( validator in this.validators ) {
					message = this.validators[validator].call( this );
					if ( message ) {
						// we just found an error. Mark the model as not valid
						valid = false;

						// append error message to a new Error Array. Needed because we do
						// not want to trigger to many updates on our observable error
						// Array
						newErrors.push(message);
					}
				}

				// All all errors not already in the error array to our list of errors.
				_( newErrors ).each(function( element ) {
					if ( this.errors.indexOf( element ) === -1 ) {
						this.errors.push( element );
					}
				}.bind( this ));

				// Remove all errors not in our new error array (but in the old one)
				// from the array of errors.
				this.errors.removeAll( _( this.errors() ).difference( newErrors ) );

				// Return our valid / invalid status and write it to our observable;
				this.isValid( valid );
				return valid;
			};

			/**
			 *	Return the url for this process.
			 *	URL might be dependant on the current Router being used.
			 *
			 *	Calls Router.processPath(process) internally.
			 *
			 *	@return {String} the path to this process.
				 */
			this.url = function() {
				return Router.modelPath(this);
			}


			/**
			 * converts this instance of a model to a JSON string.
			 * The String is formed by looking at the attributes supplied at
			 * instanciating this model type.
			 *
			 * No other Attribute will be serialized.
			 *
			 * @return {Object} the JSON object
			 */
			this.toJSON = function() {
				json = {};
				_( Result.attrs() ).each(function( attrOptions, attrName ) {
					if ( typeof this[ attrName ]() === "undefined" ) {
						if ( attrOptions.lazy && attrOptions.defaults ) {
							json[ attrName ] = attrOptions.defaults;
						} else {
							json[ attrName ] = "";
						}
					} else {
						json[ attrName ] = this[ attrName ]();
					}
				}.bind( this ));

				return json;
			}

			this.toJSONString = function() {
				return JSON.stringify( this.toJSON() );
			}

			// Duplicate the current Object. Does NOT do a deep copy, so obejcts,
			// arrays, etc storede inside this copied object will reflect changes
			// made to the original obect, array etc.
			// This can be solved by sinply saving the object as everything is
			// serialized when sent to the server and then parsed and re-written to
			// the object.
			this.duplicate = function() {
				var result = new Result();

				_.chain( Result.attrs() ).each(function( attrOptions, attrName ) {
					if ( _( Result.ids ).contains( attrName ) ) {
						return;
					}
					result[ attrName ]( this[ attrName ]() );
				}.bind( this ));

				return result;
			}

			_( Result._initializers ).each(function( initializer ) {
				initializer( this, data );
			}, this);

			// call our initializer method if defined.
			// This can override any of the default initializations done on the lines
			// above
			if ( typeof this.initialize === "function" ) {
				this.initialize.call(this, data);
			}

			this.isBeingInitialized = false;
		}

		// Set the className as an static attribute to our newly created model.
		Result.className = modelName;
		Result.prototype.classModel = Result;

		Result._initializers = [];

		Result.build = function( data ) {
			var result;

			result = new Result( data );
			instances.push( result );

			return result;
		}

		// Get one model instance by id
		Result.find = function( processId ) {
			processId = parseInt( processId, 10 );
			var foundInInstances = _( Result.all() ).filter(function( process ) {
				return process.id() === processId;
			});

			if ( foundInInstances.length > 0 ) {
				return foundInInstances[0]
			}

			// we have not found anything :(
			return undefined;
		}

		Result.all = instances;

		// Resets all attributes of all models currently available / known by
		// the instance (Model.all()) array.
		// Deletes every unsaved object from the list of instances and then
		// resets every remaining model instance.
		Result.resetAll = function() {
			var unsavedInstances;

			// filter all instances and only return those who are a newRecord.
			unsavedInstances = _( instances() ).filter(function( instance ) {
				return instance.isNewRecord;
			})

			// No remove all unsaved instances from the array of model instances.
			instances.removeAll( unsavedInstances );

			// And reset all remaining instances.
			_( instances() ).each(function( group ) {
				group.reset();
			});
		}

		// Initialize with an empty set of validators.
		// Validators all have a name for easier identifiaction.
		// Validators that return ANYTHING will be treated as failed and the
		// return value will be added to the errors object.
		Result.prototype.validators = {};

		/**
		 * Extend the class with static methods. All methods defined inside the
		 * given object will overwrite any previously defined methods.
		 *
		 * @param {Object} obj The object which methods are to be included as
		 *	static (class level) methods
		 */
		Result.extend = function(obj) {
			_(Result).extend(obj);
		}

		/**
		 * Extend the class with instance methods. All methods defined inside the
		 * given object will overwrite any previously defined methods.
		 *
		 * @param {Object} obj The object which methods are to be included as
		 *	instance level methods
		 */
		Result.include = function(obj) {
			_(Result.prototype).extend(obj);
		}

		models.push( Result );

		Attributes( Result );
		Associations( Result );
		Storage( Result, ajaxOptions );

		// Return our newly defined object.
		return Result;
	}

	// Fetch all resources of all models
	Model.fetchAll = function( callback ) {
		async.map( models, function( model, cb ) {
			model.fetch( null, {
				error: function( textStatus, error ) { cb( error ); },
				success: function() { cb() }
			});
		}, function( error, results ) {
			if ( error ) {
				if (console && typeof console.error === "function") {
					console.error("Error while fetching ");
				}
			}

			if ( typeof callback === "function" ) {
				callback();
			}
		});
	}

	// Everything in this object will be the public API.
	return Model
});

