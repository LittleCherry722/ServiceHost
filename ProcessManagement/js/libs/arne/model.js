define([
	"underscore",
	"knockout",
	"router",
	"jquery"
], function( _, ko, Router, $ ) {

	// Our Model cunstructor function. Returns another constructor function.
	var Model = function( modelName, attrs ) {
		if ( !attrs ) {
			attrs = [];
		}

		var dbPath = "db/";

		var modelPath = dbPath + modelName.toLowerCase();

		var _id = 1;

		var instances = ko.observableArray([]);

		// Define our Base model
		var Result = function( data ) {

			// Set the ID of the model to (initially) undefined.
			// The model receives the ID from the database.
			// Therefore the ID will be available as soon as the model has been saved
			// or for all records that have been loaded from the server.
			this.id = undefined;

			this.isNewRecord = true;

			// Set the class Name. Needed for routes, to display the model (toString
			// method coming soon) etc.
			this.className = modelName;

			for (var i = 0; i < attrs.length; i++) {
				this[ attrs[i] ] = ko.observable();
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

				newErrors = [];

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

			this.toJSON = function() {

			}

			// Save the current Record.
			// Saves only this model to the database, no related structures.
			// Sabes the initial set of attributes to the databse.
			// Only saves the object if it passes validation.
			//
			// It may be possible (but uncommon) that the the server alters the
			// models attributes.
			this.save = function() {

				// If this is a new record that has not yet been saved, create a new
				// record. Otherwise, just save it.
				if ( this.isNewRecord ) {
					Result.create( this.toJSON );
				} else {
					Result._saveExisting( this );
				}
			}

			// this is the default "init" method.
			// If a data object was supplied upon initialization,
			// loop over every attribute and assign it to the attribute of our model.
			// Only assign it if our model has an attribute with this name of course.
			//
			if ( data && typeof data === "object" ) {
				_( attrs ).each(function( attribute ) {
					if ( data[ attribute ] !== undefined ) {
						this[ attribute ]( data[ attribute ] );
					}
				}.bind( this ));
			}

			// call our initializer method if defined.
			// This can override any of the default initializations done on the lines
			// above
			if ( typeof this.initialize === "function" ) {
				this.initialize.call(this, data);
			}

		}

		// Set the className as an static attribute to our newly created model.
		Result.className = modelName;


		// Create a new Result object (an instance of model subclass)
		// from a JSON Object.
		var newFromJSON = function(JSONObject) {
			var newResult;

			newResult = new Result( resultJSON );

			// If the server supplies an ID, set the ID of our current model.
			if ( resultJSON['id'] ) {
				newResult.id = parseInt( resultJSON['id'], 10 );
			}

			// Mark this Record as not new (and therefore as already persisted)
			newResult.isNewRecord = false;

			return newResult;
		}


		/*
		 *  DB interaction bavor
		 */

		Result.fetch = function( callback ) {
			var data, newResult, JSONObject;

			instances.removeAll();

			data = { action: "all" }
			
			$.ajax({
				url: dbPath + modelPath + ".php",
				data: data,
				cache: false,
				type: "POST",
				success: function( JSONString ) {
					// Try to parse JSON String, if sucessfull continue, otherwise return
					// early.
					try {
						JSONObject = jQuery.parseJSON( JSONString );
					} catch( error ) {
						console.error( "Service: Error parsing JSON: " + JSONString );
						console.error( "Error: " + error )

						// We do not want to do anything else if we encoutnered an error
						return;
					}

					// If previous statement was excuted successfully, create new
					// instance of our model
					_( JSONObject ).each(function( resultJSON ) {
						newResult = newFromJSON( resultJSON );

						// Append the new model to our collection of Models.
						// Every observer will be notified about this event.
						instances.push( newResult );
					});
				},
				error: function( error ) {
					if ( console && typeof console.log === "function" ) {
						console.log( error )
					}
				}
			});
		}

		/*
		 * end DB interaction bavior.
		 */

		Result.all = instances;


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

		// Return our newly defined object.
		return Result;
	}
	
	// Everything in this object will be the public API.
	return Model
});
