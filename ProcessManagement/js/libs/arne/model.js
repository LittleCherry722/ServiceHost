define([
	"underscore",
	"knockout",
	"router",
	"require"
	// "jquery"
], function( _, ko, Router, requier ) {
	var jsonAssign = function(attribute, value) {
		var castValue;

		if ( value === "false" ) {
			castValue = false;
		} else if ( value === "true" ) {
			castValue = true;
		} else if ( value === "null" ) {
			castValue = null;
		} else if ( value === "undefined" ) {
			castValue = undefined;
		} else if ( parseInt( value, 10 ) == value ) {
			castValue = parseInt( value, 10 );
		} else {
			castValue = value;
		}

		attribute(castValue);
		return castValue;
	}

	// Our Model cunstructor function. Returns another constructor function.
	var Model = function( modelName, attrs ) {
		if ( !attrs ) {
			attrs = [];
		}

		// Let every model have an "id" attribute
		if ( !_( attrs ).contains( "id" ) ) {
			attrs.push("id");
		}

		var modelPath = "db/" + modelName.toLowerCase();

		var instances = ko.observableArray([]);

		// Define our Base model
		var Result = function( data ) {

			this.isNewRecord = true;

			// Set the class Name. Needed for routes, to display the model (toString
			// method coming soon) etc.
			this.className = modelName;

			// Initialize every attriubute as a KnockOut observable
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
				_( attrs ).each(function( attribute ) {
					json[attribute] = this[attribute]();
				}.bind( this ));

				return json;
			}

			/*
			 * Save the current Record.
			 * Saves only this model to the database, no related structures.
			 * Sabes the initial set of attributes to the databse.
			 * Only saves the object if it passes validation.
			 *
			 * It may be possible (but uncommon) that the the server alters the
			 * models attributes.
			 */
			this.save = function( callback ) {

				// If this is a new record that has not yet been saved, create a new
				// record. Otherwise, just save it.
				if ( this.isNewRecord ) {
					Result._createFromExisting( this, callback );
				} else {
					Result._saveExisting( this, callback );
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
						jsonAssign(this[ attribute ], data[ attribute ]);
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
			var newResult = new Result( JSONObject );

			// If the server supplies an ID, set the ID of our current model.
			if ( JSONObject['id'] ) {
				newResult.id( parseInt( JSONObject['id'], 10 ) );
			}

			// Mark this Record as not new (and therefore as already persisted)
			newResult.isNewRecord = false;

			return newResult;
		}


		/*
		 *  DB interaction behavior.
		 */

		Result._createFromExisting = function( model, callback ) {
			var newResult, JSONObject, attribute, data,
				self = this;

			data = model.toJSON();
			data.action = "create";
			
			$.ajax({
				url: modelPath + ".php",
				data: data,
				cache: false,
				type: "POST",
				success: function( JSONString ) {
					JSONObject = $.parseJSON( JSONString );

					// Override all local attributes with attributes supplied by the Server
					_( attrs ).each(function( attribute ) {
						if ( _( JSONObject ).has( attribute ) ) {
							jsonAssign(model[ attribute ], JSONObject[ attribute ]);
						}
					});

					// Mark this model as persisted (not new)
					model.isNewRecord = false;
					instances.push(model);

					// If a callback was given, call it and set "this" inside the
					// callback function to our model instance
					if ( typeof callback === "function" ) {
						callback.call( model, null );
					}
				},
				error: function( error ) {
					if ( console && typeof console.log === "function" ) {
						console.log( error )
					}

					// If a callback was given, call it and set "this" inside the
					// callback function to our model instance
					if ( typeof callback === "function" ) {
						callback.call( model, error );
					}
				}
			});
		}

		Result.fetch = function( callback ) {
			var data, newResult, JSONObject;

			instances.removeAll();

			data = { action: "all" }
			
			$.ajax({
				url: modelPath + ".php",
				data: data,
				cache: false,
				type: "POST",
				success: function( JSONString ) {
					// Try to parse JSON String, if sucessfull continue, otherwise return
					// early.
					try {
						JSONObject = $.parseJSON( JSONString );
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

					if( typeof callback === "function" ) {
						callback.call(this);
					}
				},
				error: function( error ) {
					if ( console && typeof console.log === "function" ) {
						console.log( error );
						if( typeof callback === "function" ) {
							callback.call(this);
						}
					}
				}
			});
		}

		Result.find = function( processID ) {
			processID = parseInt( processID, 10 );
			var foundInInstances = _( Result.all() ).filter(function( process ) {
				return process.id() === processID;
			});

			if ( foundInInstances.length > 0 ) {
				return foundInInstances[0]
			}

			// we have not found anything :(
			return undefined;
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

		/**
		 * Set up a belongs to association.
		 * This assumes, that the models to be associated have a foreign key called
		 * like the model name + "ID".
		 *
		 * Example: Given the models Author and Article. An article belongsTo
		 * author (Article.belongsTo( "author" )), the Article model needs and
		 * attribute called "authorID".
		 *
		 * Creates a method on the Model called like the association.
		 * For example: Article.belongsTo( "author" ) makes the method
		 * Article.author() available.
		 */
		Result.belongsTo = function( models ) {
			var foreignKey;

			// Also accept plain strings, not just an array of strings.
			if ( typeof models === "string" ) {
				models = [ models ];
			}

			// Iterate over every model, load it and set up the association method.
			_( models ).each(function( modelName ) {
				foreignKey = modelName.toLowerCase() + "ID";

				require( [ "models/" + modelName ], function( model ) {
					// setup of the method.
					Result.prototype[ modelName ] = function() {
						return model.find( this[ foreignKey ]() );
					}
				})
			});
		}

		// Return our newly defined object.
		return Result;
	}
	
	// Everything in this object will be the public API.
	return Model
});
