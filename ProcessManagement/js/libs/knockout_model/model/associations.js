define([
	"underscore",
	"knockout"
], function( _, ko ) {

	var Association = function( Result ) {

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
		 *		checks the authorId on this Model (or to be more specific calls the
		 *		"findByAuthorId" method) and returns an array of articles.
		 *	Aquarium.hasMany("octopi", { : foreignModelName: "octopus" })
		 */
		var hasMany = [],
				belongsTo = [];
		Result.hasMany = function( foreignModelPluralName, options ) {
			if ( arguments.length === 0 ) {
				return hasMany;
			}

			var foreignKey, keyToSet, foreignModelName, requireArray;

			if ( !options ) { options = {}; }

			// set the default foreign Model name. Is just the pluralName minus the
			// last character (the s in most cases) which leaves us with the singular
			// form of the name... most of the times at least.
			foreignModelName = foreignModelPluralName.slice(0, foreignModelPluralName.length - 1)

			// Actually set the options defaults.
			_( options ).defaults({
				foreignModelName: foreignModelName,
				foreignKey: toAttributeName( Result.className, "Id" ),
				foreignSearchMethod: "findBy" + Result.className + "Id",
				through: undefined
			});

			hasMany.push( options );

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
					Result._initializers.push(function ( model, data  ) {
						model[ foreignModelPluralName ] =
							createHasManyThroughAccessors.call( model, ForeignModel, IntermediateModel, options );
					});
				} else {
					Result._initializers.push(function( model, data  ) {
						model[ foreignModelPluralName ] =
							createHasManyAccessors.call( model, ForeignModel, options );
					});
				}
			});
		}


		/**
		 * Set up a belongs to association.
		 * This assumes, that the models to be associated have a foreign key called
		 * like the model name + "Id".
		 *
		 * Example: Given the models Author and Article. An article belongsTo
		 * author (Article.belongsTo( "author" )), the Article model needs and
		 * attribute called "authorId".
		 *
		 * Creates a method on the Model called like the association.
		 * For example: Article.belongsTo( "author" ) makes the method
		 * Article.author() available.
		 */
		Result.belongsTo = function( modelName, options ) {
			if ( arguments.length === 0 ) {
				return belongsTo;
			}
			var foreignKey, keyToSet;

			if ( !options ) { options = {}; }

			_( options ).defaults({
				modelName: modelName,
				foreignKey: modelName + "Id"
			});

			belongsTo.push( options );

			require( [ "models/" + options.modelName ], function( model ) {

				// setup of the method.
				// Creates a new method that is called like the modelName (for example
				// "author") that optionally accepts a model as attribute.
				Result.prototype[ modelName ] = function( foreignModel ) {
					if ( foreignModel ) {
						keyToSet = toAttributeName( this.className.toLowerCase(), "Id" );
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
	}

	// setup of the method. This creates a new method that is called like
	// the modelPluralName, for example "articles".
	var createHasManyAccessors = function( ForeignModel, options ) {
		var self = this;
		return ko.computed({
			deferEvaluation: true,
			read: function() {
				var results, _push;

				results = ForeignModel[ options.foreignSearchMethod ]( self.id() );

				// we want to be able to easily add or remove models to our relation.
				// This is done by overriding the push method of the array we return
				// and add a handy little remove method.
				_push = results.push;

				// Push a new object to the array of results and save the pushed model.
				// Sets the foreign key of the foreign model to the id of our current model.
				// If no Id could be found (probably because the record has not yet been
				// persisted), save the record first.
				// Also saves the foreign model after assigning the foreign key.
				// Does so asynchronously if a callback is given or blocking if
				// the callback is ommited.
				results.push = function( item, callback ) {

					// Return if the item already exists in this relation
					if ( _( results ).contains( item ) ) {
						return;
					}

					// call the native push method.
					_push.call( results, item );

					// Save the record if it has not yet been saved
					if ( !self.id() || self.isNewRecord ) {
						self.save({ async: false });
					}

					// Set the forign key of the item to be added to our current Id.
					item[ options.foreignKey ]( self.id() )

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
					if ( !self.id() || self.isNewRecord ) {
						self.save({ async: false });
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
		});
	}

	// This is where things get complicated.
	// Setting up a has many association through a foreign model in order
	// to get a many to many relation working.
	var createHasManyThroughAccessors = function( ForeignModel, IntermediateModel, options ) {
		var model = this;

		return ko.computed({
			owner: model,
			deferEvaluation: true,
			read: function(arrayOfForeignModels) {
				var _push, intermediateResults, results, existingRelations,
					intermediateForeignKey, intermediateModelObject, toBeDeletedIntermediate,
					intermediateIndex,
					self = this;

				// First get all intermediate model instances whose foreignKey equal
				// the Id of our current model.
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
				// That is create a new intermediate model with the Ids of our current
				// model and the foreign model.
				// But only, if no intermediate Model with this exact relation already
				// exists. We can leverage our existing intermediate results for this.
				results.push = function( item, callback ) {
					intermediateForeignKey = toAttributeName( item.className, "Id" );

					if ( !item.id() || item.isNewRecord ) {
						item.save({ async: false });
					}

					// Array of existing relations that, in the end, match the Id of our
					// current model and the Id of the foreignModel to be added to the
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
					intermediateForeignKey = toAttributeName( item.className, "Id" );

					// Array of existing relations that, in the end, match the Id of our
					// current model and the Id of the foreignModel to be added to the
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
		});
	}

	// Transforms an String to an attribute name.
	// lowecases the first letter, does not change the case of any other
	// Letter and appends the 2nd argument to the string.
	//
	// For Example: toAttributeName( "Process", "Id" ) // => "processId"
	var toAttributeName = function( string, append ) {
		if ( !append || typeof append !== "string" ) {
			append = "";
		}

		return string[0].toLowerCase() + string.slice(1, string.length) + append;
	}

	// Everything in this object will be the public API
	return Association;
} );
