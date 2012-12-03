define([
	"underscore",
	"knockout",
	"router"
], function( _, ko, Router ) {
	var _id = 1;

	var Model = function( modelName, attributes ) {

		// Define our Base model
		var Result = function( data ) {

			// give every model a default and unique id.
			// TODO Implement a sane id system.
			this.id = _id++;

			// Set the class Name. Needed for routes, to display the model (toString
			// method coming soon) etc.
			this.className = modelName;

			// Initialize with an empty set of validators.
			this.validators = {};

			// Validates the model.
			// Iterates over the list of Validators defined (if any) and execute each of
			// them.
			// If one validator returns false, exit early and mark the model as invalid.
			// If no validator retrurns false we assume this model to be valid and
			// therefore return true.
			this.isValid = ko.computed(function() {
				var validator;
				for ( validator in this.validators ) {
					if ( !validator() ) {
						return false
					}
				}
				return true;
			}.bind( this ));

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

			// call our initializer method if defined.
			if ( typeof this.initialize === "function" ) {
				this.initialize.call(this, data);
			}
		}

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

		// Return our newly defined and extended class
		return Result;
	}
	
	// Everything in this object will be the public API
	return Model
});
