define([
	"underscore",
	"knockout"
], function( _, ko ) {

	/***************************************************************************
	 * The initialization function
	 ***************************************************************************/

	var Attributes = function( Result ) {
		canHaveAttributes( Result );
		canHaveComputedAttributes( Result );
		Result._initializers.push( attributeInitializer( Result ) );
		Result._initializers.push( computedAttributeInitializer( Result ) );
		Result._initializers.push( initializeChangableAttributes( Result ) );
		Result._initializers.push( initializeDynamicFinders( Result ) )
	}

	/***************************************************************************
	 * Initialize attribute setup functions like Model.attrs(), Model.ids, etc.
	 ***************************************************************************/

	var canHaveAttributes = function( Result ) {
		var attrs = {},
			lazyAttributes = [],
			attributeNames = [],
			ids = [ "id" ];

		// Read the attribute Object (no argument given) or add a set of attributes
		// to the attributes Object.
		Result.attrs = function( attributesObject ) {
			if ( !attributesObject ) {
				return attrs;
			}

			_( attributesObject ).defaults({
				id: "integer"
			});
			
			// Allow strings or objects as arguments. Strings will be looked
			// up in our default arguments hash and given only the default values,
			// objects allow for customization of attribute behavior by setting
			// custom default values etc.
			// The "type" attribute of the attribute object must always be set.
			_( attributesObject ).each(function( value, key ) {
				if ( typeof value === "string" ) {
					attrs[ key ] = attributeFromString( value );
				} else if ( typeof value === "object" ) {
					attrs[ key ] = attributeFromObject( value );
				} else {
					throw "Invalid attribute format for key: '" + key +
						"' with value: " + value + " [" + typeof value + "]"
				}

				attributeNames.push( key );

				if ( attrs[ key ].lazy ) {
					lazyAttributes.push( key );
				}
			});
		}

		// Return only the names of all attributes of the current model.
		Result.attributeNames = function() {
			return attributeNames;
		}

		// Return only the names of all attributes of the current model.
		Result.lazyAttributes = function() {
			return lazyAttributes;
		}

		Result.hasLazyAttributes = function() {
			return lazyAttributes.length > 0;
		}

		// Return (no argument) or set (on array as argument) the IDs of the
		// current model.
		Result.ids = function( idsArray ) {
			if ( !idsArray ) {
				return ids;
			}

			ids = idsArray;
		}

		// cheks whether or not the Model has a certain attribute.
		Result.hasAttribute = function( attr ) {
			return attributeNames.indexOf( attr ) !== -1;
		}
	}

	// Convert a JSON value to a string.
	var stringFromJSON = function( value ) {
		return "" + value;
	}

	// Convert a JSON value to an integer value.
	var integerFromJSON = function( value ) {
		return parseInt( value, 10 );
	}

	// Convert a JSON value to a decimal (float) value.
	var decimalFromJSON = function( value ) {
		return parseFloat( value );
	}

	// Convert a JSON value to a boolean value.
	var booleanFromJSON = function( value ) {
		return !!value;
	}

	// Default attributes Object.
	// Defines default type, default value, lazy initialization, JSON converter
	// function etc for every attribute type.
	var attributeDefaults = {
		string: {
			type: "string",
			defaults: "",
			lazy: false,
			fromJSON: stringFromJSON
		},

		integer: {
			type: "integer",
			defaults: 0,
			lazy: false,
			fromJSON: integerFromJSON
		},

		decimal: {
			type: "decimal",
			defaults: 0.0,
			lazy: false,
			fromJSON: decimalFromJSON
		},

		boolean: {
			type: "boolean",
			defaults: false,
			lazy: false,
			fromJSON: booleanFromJSON
		}
	}

	// Get Attribute defaults for a certain type ("string", "integer", etc.).
	var attributeDefaultsFor = function( type ) {

		// Do not allow anything else than numbers and letters as attribute type.
		if ( !type.match(/\w(\w|\d)*/) ) {
			throw "Illegal attribute type for type: " + type;
		}

		// Only accept those attribute types that are defined in our defaults
		// attribute object.
		if ( !attributeDefaults[ type ] ) {
			throw "Illegal attribute type for type: " + type;
		}

		return attributeDefaults[ type ];
	}

	// Convert a string with attribute type ("boolean" for example") to a
	// attrbute object.
	var attributeFromString = function( type ) {
		return attributeDefaultsFor( type );
	}

	// Process an given attribute object so it can be stored in our attributes
	// object array. Sets default values and ensures that an object type has been
	// given.
	var attributeFromObject = function( obj ) {
		if ( !obj.type ) {
			throw "Missing attribute type in attribute object: " + JSON.stringify( obj );
		}

		return _( obj ).defaults( attributeDefaultsFor( obj.type ) );
	}

	/***************************************************************************
	 * Methods for virtual attributes that act like an attribute but do not get
	 * persisted
	 ***************************************************************************/
	var canHaveComputedAttributes = function( Result ) {
		var computedAttrs = {},
			computedLazyAttributes = [];

		// Read the attribute Object (no argument given) or add a set of attributes
		// to the attributes Object.
		Result.computedAttrs = function( attributesObject ) {
			if ( !attributesObject ) {
				return computedAttrs;
			}
			
			// Allow strings or objects as arguments. Strings will be looked
			// up in our default arguments hash and given only the default values,
			// objects allow for customization of attribute behavior by setting
			// custom default values etc.
			// The "type" attribute of the attribute object must always be set.
			_( attributesObject ).each(function( value, key ) {
				if ( typeof value === "function" ) {
					computedAttrs[ key ] = computedAttributeFromFunction( value );
				} else if ( typeof value === "object" ) {
					computedAttrs[ key ] = value;
				} else {
					throw "Invalid computed attribute format for key: '" + key +
						"' with value: " + value + " [" + typeof value + "]"
				}

				if ( computedAttrs[ key ].lazy ) {
					computedLazyAttributes.push( key );
					delete computedAttrs[ key ].lazy;
				}
			});
		}

		// Return only the names of all attributes of the current model.
		Result.computedLazyAttributes = function() {
			return lazyAttributes;
		}

		Result.hasComputedLazyAttributes = function() {
			return lazyAttributes.length > 0;
		}
	}

	// Process an given attribute object so it can be stored in our attributes
	// object array. Sets default values and ensures that an object type has been
	// given.
	var computedAttributeFromFunction = function( func ) {
		return { read: func };
	}

	/***************************************************************************
	 * Methods that add the attributes to each new record.
	 ***************************************************************************/
	//
	// Creates basic methods that depend on the attributes of a model.
	// Parameters are in order: The specific instance of the model to which to
	// assign the methods and an array of attributes as string that should be
	// defined.
	// The attributes should always include an "id" attribute.
	var attributeInitializer = function( Result ) {
		return function( instance, data ) {
			var attrValue;

			if ( Result.hasLazyAttributes() ) {
				instance.attributesLoaded = ko.observable( false );
			} else {
				instance.attributesLoaded = ko.observable( true );
			}

			_( Result.attrs() ).each(function( attrOptions, attrName ) {
				if ( typeof data[ attrName ] !== "undefined" ) {
					attrValue = data[ attrName ];
				} else {
					attrValue = attrOptions.defaults;
				}

				// The attribute value to be assigned, converted to the specific file type of the
				// attribute if necessary.
				attrValue = attrOptions.fromJSON( attrValue );

				// Setup basic attribute accessors and oldValue accessors.
				// Old values are mostly used internally to reset an attribute to the
				// last saved state or determine if an attribute has changed but can
				// just as well be accessed from the outside.
				if ( attrOptions.lazy && !data[ attrName ] ) {
					setupLazyAttribute( instance, attrName );
				} else {
					instance[ attrName ] = regularAttribute( attrValue );
				}
				instance[ attrName + "Old" ] = ko.observable();
			});
		}
	}

	var setupLazyAttribute = function( instance, attrName ) {
		var observable,
			subscribers = [];

		instance[ attrName ] = function( value ) {
			observable = ko.observable( value );

			if ( typeof value === "undefined" ) {
				if ( !instance.isBeingInitialized ) {
					instance[ attrName ] = observable;
					_( subscribers ).each(function( subscriber ) {
						observable.subscribe( subscriber );
					})

					instance.loadAttributes({ async: false });
					instance.attributesLoaded( true );
				}

				return observable();
			} else {
				instance[ attrName ] = observable;
				_( subscribers ).each(function( subscriber ) {
					observable.subscribe( subscriber );
				})

				return undefined;
			}
		}

		instance[ attrName ].subscribe = subscribers.push;
	}

	var regularAttribute = function( attrValue ) {
		return ko.observable( attrValue );
	}


	/***************************************************************************
	 * Methods that add computed attributes to each new record.
	 ***************************************************************************/
	//
	// Creates basic methods that depend on the attributes of a model.
	// Parameters are in order: The specific instance of the model to which to
	// assign the methods and an array of attributes as string that should be
	// defined.
	// The attributes should always include an "id" attribute.
	var computedAttributeInitializer = function( Result ) {
		return function( instance, data ) {
			var attrValue;

			_( Result.computedAttrs() ).each(function( computedBody, attrName ) {

				// Set the owner of the computed observable since users would
				// probably want to access the current instance as "this", rather
				// than the window object
				computedBody.owner = instance;

				// Setup basic attribute accessors and oldValue accessors.
				// Old values are mostly used internally to reset an attribute to the
				// last saved state or determine if an attribute has changed but can
				// just as well be accessed from the outside.
				if ( computedBody.lazy ) {
					setupLazyComputed( instance, attrName, computedBody );
				} else {
					instance[ attrName ] = regularComputed( computedBody );
				}
			});
		}
	}

	var setupLazyComputed = function( instance, name, computedBody ) {
		var computed,
			subscribers = [];

		instance[ name ] = function( value ) {
			computed = ko.computed( computedBody );

			if ( typeof value === "undefined" ) {
				if ( !instance.isBeingInitialized ) {
					instance[ name ] = computed;
					_( subscribers ).each(function( subscriber ) {
						computed.subscribe( subscriber );
					})

					instance.loadAttributes({ async: false });
					instance.attributesLoaded( true );
				}

				return computed();
			} else {
				computed( value )
				instance[ name ] = computed;
				_( subscribers ).each(function( subscriber ) {
					computed.subscribe( subscriber );
				})

				return undefined;
			}
		}

		instance[ attrName ].subscribe = subscribers.push;
	}

	var regularComputed = function( computedBody ) {
		return ko.observable( computedBody );
	}

	/***************************************************************************
	 * Dynamic Attribute finder methods
	 ***************************************************************************/
	
	var initializeDynamicFinders = function( Result ) {
		var camelCasedAttributeName;

		return function( instance, data ) {
			_( Result.attrs() ).each(function( attrOptions, attrName ) {

				// Create dynamic finder methods. This allows us to use for example
				// Article.findByTitle("test") and we get back an array of Articles
				// that match this exact title.
				camelCasedAttributeName = attrName[0].toUpperCase() + attrName.slice( 1, attrName.length );
				// Create dynamic attribute Finder
				Result[ "findBy" + camelCasedAttributeName ] = dynamicFinderForAttribute( Result, instance, attrName );
			});
		}
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
	var dynamicFinderForAttribute = function( Result, instance, attrName ) {
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
					return _( Result.all() ).filter(function( result ) {
						return result[ attrName ]() === search;
					});
				});
			} else {
				return _( Result.all() ).filter(function( result ) {
					return result[ attrName ]() === search;
				});
			}
		}
	}

	/***************************************************************************
	 * Semi-persistance related hasChanged and reset methods
	 ***************************************************************************/

	var initializeChangableAttributes = function( Result ) {
		return function( instance, data ) {
			_( Result.attrs() ).each(function( attrOptions, attrName ) {

				// Create HasChanged and reset methods for each attribute
				instance[ attrName + "HasChanged" ] = attributeHasChangedConstructor( instance, attrName );
				instance[ attrName + "Reset" ] = attributeResetConstructor( instance, attrName );
			});

			// Create instance-level has changed method ( product.hasChanged() ).
			instance.hasChanged = hasChangedConstructor( Result, instance );
			instance.reset = resetConstructor( Result, instance );
		}
	}

	// The global hasChanged method that returns true when any attribute of
	// the model was modified since the last save and false otherwise.
	// Has to be a knockout computed since we have to check every attribute
	// and if it has changed.
	//
	// Also allows us to set the hasChanged value, but only to false,
	// essentially working as a global reset method.
	var hasChangedConstructor = function( Result, instance ) {
		return ko.computed({

			// Reading has changed: Loop through every attribute and check if the
			// attributeHasChanged mehtod returns true. If so return early,
			// otherwise return false at the end, meaning that nothing has changed.
			read: function() {
				_( Result.attrs() ).each(function( attrOptions, attrName ){
					if ( instance[ attrName + "HasChanged" ]() ) {
						return true;
					}
				});
				return false;
			},

			// Writing is similiar to reading the has changed value, only that we
			// return early if a true value is given since we do not know how to
			// set this. Otherwise delegate the work to the attribute specific
			// hasChanged method that will reset the attribute for us.
			write: function( bool ) {
				if( bool ) { return true; }

				_( Result.attrs() ).each(function( attrOptions, attrName ){
					instance[ attrName + "HasChanged" ]( false );
				});
				return false;
			}
		});
	}

	// The global reset method for our model instance.
	// Resets every attribute of the given instance.
	var resetConstructor = function( Result, instance ) {
		return function() {
			_( Result.attrs() ).each(function( attrOptions, attrName ) {
				instance[ attrName + "Reset" ]();
			});
		}
	}

	// Creates hasChanged methods for every attribute.
	// Given the attribute "name", generates the instance methods
	// "nameHasChanged()" that returns true when the name has changed since the
	// last save and "nameReset()" that resets the name to the value
	// at the last save.
	var attributeHasChangedConstructor = function( instance, attrName ) {
		var createComputed = function() {

			// Method to check whether a specific attribute has changed
			return ko.computed({
				read: function() {
					return instance[ attrName ]() !== instance[ attrName + "Old" ]();
				},
				write: function( bool ) {
					if ( bool === false ) {
						instance[ attrName + "Old" ]( instance[ attrName ]() );
					}
					return false;
				}
			});
		}

		return createComputed();
	}


	// Creates the reset method for every attribute.
	// Invoking this function will Reset the attribute values of every
	// attribute to the value in the 'attribute'Old observable.
	var attributeResetConstructor = function( instance, attrName ) {

		// Method to reset the changes to a specific attribute
		return function() {
			instance[ attrName ]( instance[ attrName + "Old" ]() );
		}
	}

	// Everything in this object will be the public API
	return Attributes;
});
