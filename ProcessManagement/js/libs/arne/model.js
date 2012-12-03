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

			// Initialize an empty error object.
			this.errors = ko.observableArray([]);

			// Validates the model.
			// Iterates over the list of Validators defined (if any) and execute each of
			// them.
			// If one validator returns false, exit early and mark the model as invalid.
			// If no validator retrurns false we assume this model to be valid and
			// therefore return true.
			this.isValid = function() {
				var validator, valid, message;

				// reset the error array
				this.errors([]);

				// lets assume there are no errors;
				valid = true;

				// loop through every validator and execute it.
				for ( validator in this.validators ) {
					message = this.validators[validator].call( this );
					if ( message ) {
						// we just found an error. Mark the model as not valid
						valid = false;

						// append error message
						this.errors.push(message);
					}
				}

				// Return our valid / invalid status;
				console.log("valid? " + valid )
				if ( !valid ) {
					console.log(this.errors)
				}
				return valid;
			};

			// convinience negation method
			this.isInvalid = ko.computed(function() {
				return !this.isValid();
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

		// Set the className as an static attribute to our newly created model.
		Result.className = modelName;


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
