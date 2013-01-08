define([
	"underscore",
	"knockout",
	"router",
	"require",
	"async",
  "model/associations",
  "model/attributes"
], function( _, ko, Router, require, async, Associations, Attributes ) {
	var models = [];

	// Our Model cunstructor function. Returns another constructor function.
	var Model = function( modelName ) {

		var modelPath = "db/" + modelName.toLowerCase();

		// All currently available instances this Model are stored in this
		// observableArray.
		var instances = ko.observableArray([]);

		// Define our Base model
		var Result = function( data ) {
			this.isBeingInitialized = true;

			if ( !data ) {
				data = {};
			}

			// needed only in rare cases, but invaluable there.
			var self = this;

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
				_( Result.attrs() ).each(function( attrOptions, attrName ) {
					if ( typeof this[ attrName ]() === "undefined" ) {
						json[ attrName ] = "";
					} else {
						json[ attrName ] = this[ attrName ]();
					}
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
			this.save = function( options, callback ) {

				// Allow the callback to be on first position if no options are given.
				if ( typeof options === "function" ) {
					callback = options;
					options = {}
				}

				if ( !options ) { options = {}; }

				_( options ).defaults({
					async: true
				});

				// Initiate the beforeSave callback if it was defined
				if ( typeof this.beforeSave === "function" ) {
					this.beforeSave.call( this );
				}

				// If this is a new record that has not yet been saved, create a new
				// record. Otherwise, just save it.
				if ( this.isNewRecord ) {
					Result._createFromExisting( this, options, callback );
				} else {
					Result._saveExisting( this, options, callback );
				}

				this.hasChanged( false );
			}

			// Delete a specific record from the database
			this.destroy = function( options, callback ) {
				var JSONObject, data,
					error = false,
					model = this;

				// Allow the callback to be on first position if no options are given.
				if ( typeof options === "function" ) {
					callback = options;
					options = {}
				}

				if ( this.isNewRecord ) {
					if ( _( instances() ).contains( this ) ) {
						instances.remove( this )
					}

					if ( typeof callback === "function" ) {
						callback();
					}
					return;
				}

				if ( !options ) { options = {}; }

				_( options ).defaults({
					async: true
				});

				data = {
					action: "destroy"
				}
				_( Result.ids() ).each(function( id ) {
					data[ id ] = this[ id ]();
				}, this);

				$.ajax({
					url: modelPath + ".php",
					data: data,
					cache: false,
					async: options.async,
					type: "POST",
					headers: {
						debug: true
					},
					success: function( JSONString ) {
						JSONObject = $.parseJSON( JSONString );

						// We successfully removed the model from the DB if
						// we get back the code "removed"
						if ( JSONObject["code"] === "removed" ) {

							// Mark the model as destroyed and remove it from the list of
							// models
							model.isDestroyed = true;
							instances.remove(model);
						} else {

							// set the error message
							error = "Could not delete " + model.className + ": ";
						}

						// If a callback was given, call it and set "this" inside the
						// callback function to our model instance
						if ( typeof callback === "function" ) {
							callback.call( model, error );
						}
					},
					error: function( error ) {
						if ( console && typeof console.error === "function" ) {
							console.error( error )
						}

						// If a callback was given, call it and set "this" inside the
						// callback function to our model instance
						if ( typeof callback === "function" ) {
							callback.call( model, error );
						}
					}
				});
			}

			this.loadAttributes = function( options, callback ) {
				var JSONObject, data,
					error = false,
					instance = this;

				// Allow the callback to be on first position if no options are given.
				if ( typeof options === "function" ) {
					callback = options;
					options = {}
				}

				if ( instance.attributesLoaded() ) {
					if ( typeof callback === "function" ) {
						callback.call( instance, null );
					}
					return;
				}

				if ( !options ) { options = {}; }

				_( options ).defaults({
					async: true
				});

				data = {
					action: "get"
				}
				_( Result.ids() ).each(function( id ) {
					data[ id ] = instance[ id ]();
				});

				$.ajax({
					url: modelPath + ".php",
					data: data,
					cache: false,
					async: options.async,
					type: "POST",
					headers: {
						debug: true
					},
					success: function( JSONString ) {
						JSONObject = $.parseJSON( JSONString );

						// Override all local attributes with attributes supplied by the Server
						_( Result.attrs() ).each(function( attrOptions, attrName ) {
							if ( _( JSONObject ).has( attrName ) ) {
								instance[ attrName ]( attrOptions.fromJSON( JSONObject[ attrName ] ));
							}
						});

						// If a callback was given, call it and set "this" inside the
						// callback function to our model instance
						if ( typeof callback === "function" ) {
							callback.call( instance, null );
						}
					},
					error: function( error ) {
						if ( console && typeof console.error === "function" ) {
							console.error( error )
						}

						// If a callback was given, call it and set "this" inside the
						// callback function to our model instance
						if ( typeof callback === "function" ) {
							callback.call( instance, error );
						}
					}
				});

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

		Result._initializers = [];

		Attributes( Result );
		Associations( Result );

		// Create a new Result object (an instance of model subclass)
		// from a JSON Object.
		var newFromJSON = function(JSONObject) {
			var newResult = new Result( JSONObject );

			// Mark this Record as not new (and therefore as already persisted)
			newResult.isNewRecord = false;

			return newResult;
		}

		Result.build = function( data ) {
			var result;

			result = new Result( data );
			instances.push( result );
			
			return result;
		}

		/*
		 *  DB interaction behavior.
		 */
		Result._createFromExisting = function( model, options, callback ) {
			var newResult, JSONObject, attribute, data,
				self = this;

			data = model.toJSON();
			data.action = "create";
			
			$.ajax({
				url: modelPath + ".php",
				data: data,
				cache: false,
				async: options.async,
				type: "POST",
				headers: {
					debug: true
				},
				success: function( JSONString ) {
					JSONObject = $.parseJSON( JSONString );

					// Override all local attributes with attributes supplied by the Server
					_( Result.attrs() ).each(function( attrOptions, attrName ) {
						if ( _( JSONObject ).has( attrName ) ) {
							model[ attrName ]( attrOptions.fromJSON( JSONObject[ attrName ] ));
						}
					});

					// Mark this model as persisted (not new)
					model.isNewRecord = false;
					model.hasChanged( false );
					if ( ! _( instances() ).contains( model ) ) {
						instances.push( model );
					}

					// If a callback was given, call it and set "this" inside the
					// callback function to our model instance
					if ( typeof callback === "function" ) {
						callback.call( model, null );
					}
				},
				error: function( error ) {
					if ( console && typeof console.error === "function" ) {
						console.error( error )
					}

					// If a callback was given, call it and set "this" inside the
					// callback function to our model instance
					if ( typeof callback === "function" ) {
						callback.call( model, error );
					}
				}
			});
		}

		// Saves an existing record to the database.
		Result._saveExisting = function( model, options, callback ) {
			var newResult, JSONObject, attribute, data,
				self = this;

			if ( ! model.hasChanged() ) {
				if ( typeof callback === "function" ) {
					callback.call( model );
				}
				return;
			}

			data = model.toJSON();
			data.action = "save";
			
			$.ajax({
				url: modelPath + ".php",
				data: data,
				cache: false,
				async: options.async,
				type: "POST",
					headers: {
						debug: true
					},
				success: function( JSONString ) {
					JSONObject = $.parseJSON( JSONString );

					// Override all local attributes with attributes supplied by the Server
					_( Result.attrs() ).each(function( attrOptions, attrName ) {
						if ( _( JSONObject ).has( attrName ) ) {
							model[ attrName ]( attrOptions.fromJSON( JSONObject[ attrName ] ));
						}
					});

					// If a callback was given, call it and set "this" inside the
					// callback function to our model instance
					if ( typeof callback === "function" ) {
						callback.call( model, null );
					}
				},
				error: function( error ) {
					if ( console && typeof console.error === "function" ) {
						console.error( error )
					}

					// If a callback was given, call it and set "this" inside the
					// callback function to our model instance
					if ( typeof callback === "function" ) {
						callback.call( model, error );
					}
				}
			});
		}

		// Fetch a list of all model instances from the Server.
		// (At the moment) fetches the entire object.
		// TODO Only get a list of objects and then upon first load, get all the
		// details needed. This WILL BE NEEDED to be able to scale reasonably.
		// Options hash can be omitted and callback can be the only argument.
		//
		// Options descriptions:
		//	async: Whether the query should be performed asynchronously or not.
		//	It is strongly recommended to perform async requests and only wait
		//	for it to finish if really necessary.
		//
		// Options defaults are:
		//	{
		//		async: true
		//	}
		//
		Result.fetch = function( options, callback ) {
			var data, newResult, JSONObject;

			instances.removeAll();

			if ( typeof options === "function" ) {
				callback = options;
				options = {}
			}

			if ( !options ) {
				options = {};
			}

			_( options ).defaults({
				async: true
			});

			data = { action: "all" }
			
			$.ajax({
				url: modelPath + ".php",
				data: data,
				async: options.async,
				cache: false,
				type: "POST",
				headers: {
					debug: true
				},
				success: function( JSONString ) {
					// Try to parse JSON String, if sucessfull continue, otherwise return
					// early.
					try {
						JSONObject = $.parseJSON( JSONString );
					} catch( error ) {
						if ( console && typeof console.error === "function" ) {
							console.error( "Service: Error parsing JSON: " + JSONString );
							console.error( "Error: " + error );
						}

						// We do not want to do anything else if we encoutnered an error
						return;
					}

					// If previous statement was excuted successfully, create new
					// instance of our model
					_( JSONObject ).each(function( resultJSON ) {
						newResult = newFromJSON( resultJSON );
						newResult.isBeingInitialized = true;
						newResult.hasChanged( false );
						newResult.isBeingInitialized = false;

						// Append the new model to our collection of Models.
						// Every observer will be notified about this event.
						if ( ! _( instances() ).contains( newResult ) ) {
							instances.push( newResult );
						}
					});

					if( typeof callback === "function" ) {
						callback.call(this);
					}
				},
				error: function( error ) {
					if ( console && typeof console.error === "function" ) {
						console.error( error );
					}
					if( typeof callback === "function" ) {
						callback.call(this, error);
					}
				}
			});
		}

		// Get one model instance by id
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

		Result.destroy = function( process, callback ) {
			process.destroy( callback );
		}

		/*
		 * end DB interaction bavior.
		 */

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

		// Return our newly defined object.
		return Result;
	}

	// Fetch all resources of all models
	Model.fetchAll = function( callback ) {
		async.map( models, function( model, cb ) {
			model.fetch( cb );
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
