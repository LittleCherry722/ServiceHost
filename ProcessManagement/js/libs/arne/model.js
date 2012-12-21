define([
	"underscore",
	"knockout",
	"router",
	"require",
	"async"
	// "jquery"
], function( _, ko, Router, require, async ) {
	var models = [];

	// Some Dynamic typecasting magic. Should *probably* be changed
	// so that attributes have a specific type and we always try to cast to
	// this type, but this have to do for now...
	// Most type related work is done on the backend regardless.
	//
	// Assigns a value to an attribute and tries to find the right type.
	// Basicly checks if a value is a string representation of any known
	// JS keyword like true, false, undefined etc.
	var jsonAssign = function(attribute, value) {
		var castValue, trueValues;

		// Try casting the value
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

		// Assign and return the value.
		attribute( castValue );
		return castValue;
	}

	// Our Model cunstructor function. Returns another constructor function.
	var Model = function( modelName, attrs, ids ) {
		if ( !attrs ) {
			attrs = [];
		}

		if ( !ids ) {
			ids = [ "id" ];
		} else {
			if ( !_(ids).isArray() ) {
				ids = [ ids ];
			}
		}

		_( ids ).each(function( id ) {
			// Let every model have an "id" attribute
			if ( !_( attrs ).contains( id) ) {
				attrs.push(id);
			}
		})

		var modelPath = "db/" + modelName.toLowerCase();

		// All currently available instances this Model are stored in this
		// observableArray.
		var instances = ko.observableArray([]);

		// Creates basic methods that depend on the attributes of a model.
		// Parameters are in order: The model constructor class, the specific
		// instance of the model to which to assign the methods and an array
		// of attributes as string that should be defined.
		// The attributes should always include an "id" attribute.
		var createAttributeObservables = function( M, instance, attrs ) {
			// Initialize every attriubute as a KnockOut observable
			// and create a search mehtod for it.
			for ( var i = 0; i < attrs.length; i++ ) {
				// First we need to set up the CamelCase attiribute name so the
				// method is called findByAttribute and not findByattribute and so on.
				camelCasedAttribute = attrs[i][0].toUpperCase() + attrs[i].slice( 1, attrs[i].length );

				// Setup basic attribute accessors and oldValue accessors.
				// Old values are mostly used internally to reset an attribute to the
				// last saved state or determine if an attribute has changed but can
				// just as well be accessed from the outside.
				instance[ attrs[i] ] = ko.observable();
				instance[ attrs[i] + "Old" ] = ko.observable();

				// Create HasChanged and reset methods.
				createChangedMethod( instance, attrs[i] )

				// Create dynamic finder methods. This allows us to use for example
				// Article.findByTitle("test") and we get back an array of Articles
				// that match this exact title.

				// Create dynamic attribute Finder
				M[ "findBy" + camelCasedAttribute ] = dynamicFinderForAttribute( attrs[i] );
			}
		}

		// Creates hasChanged and Reset methods for every attribute.
		// Given the attribute "name", generates the instance methods
		// "nameHasChanged()" that returns true when the name has changed since the
		// last save and "nameReset()" that resets the name to the value
		// at the last save.
		var createChangedMethod = function( instance, attr ) {
			// Method to reset the changes to a specific attribute
			instance[ attr+ "Reset" ] = function() {
				instance[ attr ]( instance[ attr + "Old" ]() );
			}

			// Method to check whether a specific attribute has changed
			instance[ attr + "HasChanged" ] = ko.computed({
				read: function() {
					return instance[ attr ]() !== instance[ attr + "Old" ]();
				},
				write: function( bool ) {
					if ( bool === false ) {
						instance[ attr + "Old" ]( instance[ attr ]() );
					}
					return false;
				}
			});
		}

		// Creates a dynamic attribute finder for a given attribute.
		// This method consumes a search string
		// (or more general an object) and a set of options.
		// These options can be:
		//	observable: should this method return a ko.computed object or
		//		a plain list of results? Defaults to false (plain list).
		//		If a computed object is wanted, to get to the actual results
		//		the method has to be called:
		//		Article.findById(2, { observable: true })();
		var dynamicFinderForAttribute = function( attribute ) {
			return function( search, options ) {

				// Initialize the options hash and set some default values.
				if ( !options ) {
					options = {}
				}
				_( options ).defaults({
					observable: false
				});

				// If an observable (more generally computed) should be returned,
				// create a new computed (not write enabled, that wold be crazy...)
				// object. Otherwise just filter the list of instances for this
				// specific attribute and return it.
				if ( options.observable ) {
					return ko.computed(function() {
						return _( instances() ).filter(function( model ) {
							return model[ attribute ]() === search;
						});
					});
				} else {
					return _( instances() ).filter(function( model ) {
						return model[ attribute ]() === search;
					});
				}
			}
		}


		// Define our Base model
		var Result = function( data ) {

			// needed only in rare cases, but invaluable there.
			var self = this;

			// The camelCase nam
			var camelCasedAttribute;

			this.isNewRecord = true;
			this.isDestroyed = false;

			// Set the class Name. Needed for routes, to display the model (toString
			// method coming soon) etc.
			this.className = modelName;

			createAttributeObservables( Result, this, attrs );

			// The global hasChanged method that returns true when any attribute of
			// the model was modified since the last save and false otherwise.
			// Has to be a knockout computed since we have to check every attribute
			// and if it has changed.
			//
			// Also allows us to set the hasChanged value, but only to false,
			// essentially working as a global reset method.
			this.hasChanged = ko.computed({

				// Reading has changed: Loop through every attribute and check if the
				// attributeHasChanged mehtod returns true. If so return early,
				// otherwise return false at the end, meaning that nothing has changed.
				read: function() {
					var i = 0,
						length = attrs.length;

					for ( i = 0; i < length; i++ ) {
						if ( self[ attrs[i] + "HasChanged" ]() ) {
							return true;
						}
					}
					return false;
				},

				// Writing is similiar to reading the has changed value, only that we
				// return early if a true value is given since we do not know how to
				// set this. Otherwise delegate the work to the attribute specific
				// hasChanged method that will reset the attribute for us.
				write: function( bool ) {
					var i = 0,
						length = attrs.length;

					if( bool ) { return true; }

					for ( i = 0; i < length; i++ ) {
						self[ attrs[i] + "HasChanged" ]( false );
					}
					return false;
				}
			});

			// The global reset method for our model instance.
			// Resets every attribute of this model.
			this.reset = function() {
				_(attrs).each(function( attr ) {
					this[ attr + "Reset" ]();
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
				_( attrs ).each(function( attribute ) {
					if ( typeof this[attribute]() === "undefined" ) {
						json[attribute] = "";
					} else {
						json[attribute] = this[attribute]();
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
				_( ids ).each(function( id ) {
					data[ id ] = this[ id ]();
				}, this);

				$.ajax({
					url: modelPath + ".php",
					data: data,
					cache: false,
					async: options.async,
					type: "POST",
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

			// Duplicate the current Object. Does NOT do a deep copy, so obejcts,
			// arrays, etc storede inside this copied object will reflect changes
			// made to the original obect, array etc.
			// This can be solved by sinply saving the object as everything is
			// serialized when sent to the server and then parsed and re-written to
			// the object.
			this.duplicate = function() {
				var result = new Result();
				
				_.chain( attrs ).without( "id" ).each(function( attribute ) {
					result[ attribute ]( this[ attribute ]() );
				}.bind( this ));

				return result;
			}


			// this is the default "init" method.
			// If a data object was supplied upon initialization,
			// loop over every attribute and assign it to the attribute of our model.
			// Only assign it if our model has an attribute with this name of course.
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

			// If the server supplies an id, set the id of our current model.
			if ( JSONObject['id'] ) {
				newResult.id( parseInt( JSONObject['id'], 10 ) );
			}

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
				success: function( JSONString ) {
					JSONObject = $.parseJSON( JSONString );

					// Override all local attributes with attributes supplied by the Server
					_( attrs ).each(function( attribute ) {
						if ( _( JSONObject ).has( attribute ) ) {
							jsonAssign(model[ attribute ], JSONObject[ attribute ]);
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
						newResult.hasChanged( false );

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
		Result.belongsTo = function( modelName, options ) {
			var foreignKey, keyToSet;

			if ( !options ) { options = {}; }

			_( options ).defaults({
				foreignKey: modelName + "ID"
			});

			require( [ "models/" + modelName ], function( model ) {

				// setup of the method.
				// Creates a new method that is called like the modelName (for example
				// "author") that optionally accepts a model as attribute.
				Result.prototype[ modelName ] = function( foreignModel ) {
					if ( foreignModel ) {
						keyToSet = toAttributeName( this.className.toLowerCase(), "ID" );
						if ( !foreignModel.id() || foreignModel.isNewRecord ) {
							if ( console && typeof console.error === "function" ) {
								console.error("Foreign Model must be saved bevor it can be assigned. ")
							}
						} else {
							this[ options.foreignKey ]( foreignModel.id() )
						}
					} else {
						return model.find( this[ options.foreignKey ]() );
					}
				}
			});
		}

		/*
		 * Setup a has many association.
		 * Accepts a plural form of the name of the foreign Model and an optional
		 * set of options.
		 * The first argument will be used as the method name for the association
		 * and the options can be used in case the foreign Model cannot be easily
		 * identified from the plural name (irregular pluran or nouns whose plural
		 * form is not created by just appending an "s").
		 *
		 * So for example:
		 *	Author.hasMany("articles")
		 *		This creates an Author.articles() method that loads the Article Model,
		 *		checks the authorID on this Model (or to be more specific calls the
		 *		"findByAuthorID" method) and returns an array of articles.
		 *	Aquarium.hasMany("octopi", { : foreignModelName: "octopus" })
		 */
		Result.hasMany = function( foreignModelPluralName, options ) {
			var foreignKey, keyToSet, foreignModelName, requireArray;

			if ( !options ) { options = {}; }

			// set the default foreign Model name. Is just the pluralName minus the
			// last character (the s in most cases) which leaves us with the singular
			// form of the name... most of the times at least.
			foreignModelName = foreignModelPluralName.slice(0, foreignModelPluralName.length - 1)

			// Actually set the options defaults.
			_( options ).defaults({
				foreignModelName: foreignModelName,
				foreignKey: toAttributeName( this.className, "ID" ),
				foreignSearchMethod: "findBy" + this.className + "ID",
				through: undefined
			});
			
			// Build the array with all paths to our needed models in advance.
			// Needed because we are not certain if we need any intermediate model.
			requireArray = [ "models/" + options.foreignModelName ];
			if ( options.through ) {
				requireArray.push( "models/" + options.through );
			}

			// IntermediateModel will only be available if a "hasMany { through: "..." }"
			// relation has been defined.
			require( requireArray, function( ForeignModel, IntermediateModel ) {
				if ( options.through ) {
					Result.prototype[ foreignModelPluralName ] =
						createHasManyThroughAccessors.call( this, ForeignModel, IntermediateModel, options );
				} else {
					Result.prototype[ foreignModelPluralName ] =
						createHasManyAccessors.call( this, ForeignModel, options );
				}
			});
		}

		// setup of the method. This creates a new method that is called like
		// the modelPluralName, for example "articles".
		var createHasManyAccessors = function( ForeignModel, options ) {
			return function() {
				results = ForeignModel[ options.foreignSearchMethod ]( this.id() );

				// we want to be able to easily add or remove models to our relation.
				// This is done by overriding the push method of the array we return
				// and add a handy little remove method.
				_push = results.push;

				// Push a new object to the array of results and save the pushed model.
				// Sets the foreign key of the foreign model to the id of our current model.
				// If no ID could be found (probably because the record has not yet been
				// persisted), save the record first.
				// Also saves the foreign model after assigning the foreign key.
				// Does so asynchronously if a callback is given or blockingly if
				// the callback is ommited.
				results.push = function( item, callback ) {

					// Return if the item already exists in this relation
					if ( _( results ).contains( item ) ) {
						return;
					}

					// call the native push method.
					_push.call( results, item );

					// Save the record if it has not yet been saved
					if ( !this.id() || this.isNewRecord ) {
						this.save({ async: false });
					}

					// Set the forign key of the item to be added to our current ID.
					item[ options.foreignKey ]( this.id() )

					// If no callback was given, save blockingly, otherwise just
					// supply the callback to the save method.
					if ( !callback ) {
						callback = { async: false }
					}
					item.save( callback );

					return results;
				}

				results.remove = function( item, callback ) {
					// Return if the item does not exist in this relation
					if ( !_( results ).contains( item ) ) {
						return;
					}

					// Reove the model from this relation array
					results.splice( _(results).indexOf( item ), 1 );

					// Save the record if it has not yet been saved.
					if ( !this.id() || this.isNewRecord ) {
						this.save({ async: false });
					}

					// Empty the foreign key of the item to be removed.
					item[ options.foreignKey ]( -1 )

					// If no callback was given, save blockingly, otherwise just
					// supply the callback to the save method.
					if ( !callback ) {
						callback = { async: false }
					}
					item.save( callback );
				}

				// if no foreign models were given, this method is just a getter and
				// return every foreign model matching
				// the request.
				return results
			}
		}

		// This is where things get complicated.
		// Setting up a has many association through a foreign model in order
		// to get a many to many relation working.
		var createHasManyThroughAccessors = function( ForeignModel, IntermediateModel, options ) {
			return function(arrayOfForeignModels) {
				var _push, intermediateResults, results, existingRelations,
					intermediateForeignKey, intermediateModelObject, toBeDeletedIntermediate,
					intermediateIndex,
					self = this;

				// First get all intermediate model instances whose foreignKey equal
				// the ID of our current model.
				intermediateResults = IntermediateModel[ options.foreignSearchMethod ]( self.id() );

				// The results array will hold all final results, that is
				// the foreign models that we want to access.
				results = [];

				// Iterate over every intermediate result and push the result of
				// the "belongsTo" relation accessor that has been setup with the same
				// name as our initial "hasMany X" relation.
				// For example: Author hasMany("articles" { through: "articlesAuthor" })
				// ArticlesAuthor belongsTo( "articles" ).
				_( intermediateResults ).each(function( result ) {
					results.push( result[ options.foreignModelName ]() )
				});

				// we want to be able to easily add or remove models to our relation.
				// This is done by overriding the push method of the array we return
				// and add a handy little remove method.
				_push = results.push;

				// Push a new object to the array of results and save the pushed model -
				// That is create a new intermediate model with the IDs of our current
				// model and the foreign model.
				// But only, if no intermediate Model with this exact relation already
				// exists. We can leverage our existing intermediate results for this.
				results.push = function( item, callback ) {
					intermediateForeignKey = toAttributeName( item.className, "ID" );

					if ( !item.id() || item.isNewRecord ) {
						item.save({ async: false });
					}

					// Array of existing relations that, in the end, match the ID of our
					// current model and the ID of the foreignModel to be added to the
					// relation.
					existingRelations = _( intermediateResults ).filter(function( result ) {
						return result[ intermediateForeignKey ]() === item.id();
					});

					// if no existing relations were found for this constellation of
					// models, add it.
					if ( existingRelations.length === 0 ) {
						_push.call( results, item );

						intermediateModelObject = {};
						intermediateModelObject[ intermediateForeignKey ] = item.id();
						intermediateModelObject[ options.foreignKey ]     = self.id();

						if ( !callback ) {
							callback = { async: false };
						}
						IntermediateModel.build( intermediateModelObject ).save( callback );
					}

					return results;
				}

				// add a remove method to the array for easy deletion of models from
				// an association.
				// Does only delete (from the DB) the intermediate relation, not the
				// model itself.
				results.remove = function( item, callback ) {
					intermediateForeignKey = toAttributeName( item.className, "ID" );

					// Array of existing relations that, in the end, match the ID of our
					// current model and the ID of the foreignModel to be added to the
					// relation.
					existingRelations = _( intermediateResults ).filter(function( result ) {
						return ( result[ intermediateForeignKey ]() === item.id() );
					});

					// If the DB was engineered sanely, we only get at most one result we
					// have to delete now.
					if ( existingRelations.length > 0 ) {
						// Splice the results (js way of deleting array elements in place
						// and returning the deleted element), and destroy the intermediate
						// model in question (delete it from the DB).
						if ( !callback ) {
							callback = { async: false };
						}

						intermediateIndex = _( intermediateResults ).indexOf( existingRelations[0] )
						toBeDeletedIntermediate = intermediateResults.splice( intermediateIndex, 1 )[0]
						toBeDeletedIntermediate.destroy( callback );
					}

					return results;
				}

				return results;
			}
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

	var toAttributeName = function( string, append ) {
		if ( !append || typeof append !== "string" ) {
			append = "";
		}

		return string[0].toLowerCase() + string.slice(1, string.length) + append;
	}

	// Everything in this object will be the public API.
	return Model
});
