define([
	"underscore",
	"knockout"
], function( _, ko ) {

	/***************************************************************************
	 * The initialization function
	 ***************************************************************************/

	var Storage = function( Model, ajaxOptions ) {
		var query;

		if ( typeof ajaxOptions === "undefined" ) {
			ajaxOptions = {}
		}

		// some general ajax options. These are neede later on when performing the
		// requests.
		_( ajaxOptions ).defaults({
			scalaBackend: true,
			modelPath: "scala/" + Model.className.toLowerCase(),
			methods: {
				destroy: "DELETE",
				create: "POST",
				save: "PUT",
				list: "GET",
				get: "GET"
			}
		});

		// Setup prototype callback so we do not have to check for their existence every time.
		_( [ "Save", "Create", "Destroy" ] ).each(function( event ) {
			Model.prototype[ "before" + event ] = function() { return true; }
			Model.prototype[ "after" + event ] = function()  { return true; }
		});


			/*
			 * Save the current Record.
			 * Saves only this model to the database, no related structures.
			 * Sabes the initial set of attributes to the databse.
			 * Only saves the object if it passes validation.
			 *
			 * It may be possible (but uncommon) that the the server alters the
			 * models attributes.
			 */
		Model.prototype.save = abstractMethod( function( options, callback ) {
			var saveFn;

			if ( !this.beforeCreate.call( this ) ) {
				return
			}

			// If this is a new record that has not yet been saved, create a new
			// record. Otherwise, just save it.
			if ( this.isNewRecord ) {
				saveFn = createNew;
			} else {
				saveFn = saveExisting;
			}

			// execute the correct save function and execute the callback aftewards.
			// Also execute the afterSave Callback and mark the model as not changed.
			saveFn.call( this, options, function() {
				this.afterDestroy.call( this );
				callback.call( this );
				this.hasChanged( false );
			});
		});

		// DStringestroy method
		Model.prototype.destroy = abstractMethod( function( options, callback ) {
			if ( !this.beforeDestroy.call( this ) ) {
				return
			}

			var destroyFn;
			if ( this.isNewRecord ) {
				destroyFn = destroyInMemory;
			} else {
				destroyFn = destroyPersisted;
			}

			// execute the correct destroy function and execute the callback aftewards.
			// Also execute the afterDestroy Callback.
			destroyFn.call( this, options, function() {
				this.afterDestroy.call( this );
				callback.call( this );
			});
		});

		Model.prototype.loadAttributes = abstractMethod(function( options, callback ) {
			var data,
					model = this;

			// if ( model.attributesLoaded() ) {
			//   callback.call( model, null );
			//   return;
			// }

			ajax = {
				cache: false,
				url: ajaxOptions.modelPath + "/" + model.id(),
				type: ajaxOptions.methods.get,
				async: options.async,
				dataType: "json",
				contentType: "application/json; charset=UTF-8",
				success: function( data, textStatus, jqXHR ) {

					// Override all local attributes with attributes supplied by the Server
					_( Model.attrs() ).each(function( attrOptions, attrName ) {
						if ( _( data ).has( attrName ) ) {
							model[ attrName ]( attrOptions.fromJSON( data[ attrName ] ));
						}
					});
				},
				error: function( jqXHR, textStatus, error ) {
					// Some error handling maybe?
				},
				complete: function( jqXHR, textStatus ) {
					callback.call( model );
				}
			};
			$.ajax( ajax );

		});

		var destroyInMemory = function( options, callback ) {

			// Mark the model as destroyed and remove it from the list of
			// instances.
			Model.all.remove( this )
			this.isDestroyed = true;
			callback.call( this );
		}

		// issues a destroy request for the current model to the server.
		// options are options to be passed to the jquery ajax method.
		// Current model is passed as the "this" object
		var destroyPersisted = function( options, callback ) {
			var ajax,
					model = this;

			ajax = {
				cache: false,
				url: ajaxOptions.modelPath + "/" + model.id(),
				type: ajaxOptions.methods.destroy,
				async: options.async,
				dataType: "json",
				contentType: "application/json; charset=UTF-8",
				data: "{}",
				success: function( data, textStatus, jqXHR ) {

					// Mark the model as destroyed and remove it from the list of
					// instances
					model.isDestroyed = true;
					Model.all.remove( this )
				},
				error: function( jqXHR, textStatus, error ) {
					// Some error handling maybe?
				},
				complete: function( jqXHR, textStatus ) {
					callback.call( model );
				}
			}
			$.ajax( ajax );
		}

		// These Methods are never called by the user, so we do not need any of the
		// options, callback sanitization stuff.
		var createNew = function( options, callback ) {
			var ajax,
					model = this;

			if ( !model.beforeCreate.call( this ) ) {
				return
			}

			ajax = {
				cache: false,
				url: ajaxOptions.modelPath,
				type: ajaxOptions.methods.create,
				async: options.async,
				data: model.toJSONString(),
				dataType: "json",
				contentType: "application/json; charset=UTF-8",
				success: function( data, textStatus, jqXHR ) {

					// Override all local attributes with attributes supplied by the Server
					_( Model.attrs() ).each(function( attrOptions, attrName ) {
						if ( _( data ).has( attrName ) ) {
							model[ attrName ]( attrOptions.fromJSON( data[ attrName ] ));
						}
					});


					model.isNewRecord = false;
					if ( ! _( Model.all() ).contains( model ) ) {
						Model.all.push( model );
					}
				},
				error: function( jqXHR, textStatus, error ) {
					// Some error handling maybe?
				},
				complete: function( jqXHR, textStatus ) {
					model.afterCreate.call( this );
					callback.call( model );
				}
			}
			$.ajax( ajax );
		}

		var saveExisting = function( options, callback ) {
			var ajax,
					model = this;

			ajax = {
				cache: false,
				url: ajaxOptions.modelPath + "/" + model.id(),
				type: ajaxOptions.methods.save,
				async: options.async,
				data: model.toJSONString(),
				dataType: "json",
				contentType: "application/json; charset=UTF-8",
				success: function( data, textStatus, jqXHR ) {

					// Override all local attributes with attributes supplied by the Server
					_( Model.attrs() ).each(function( attrOptions, attrName ) {
						if ( _( data ).has( attrName ) ) {
							model[ attrName ]( attrOptions.fromJSON( data[ attrName ] ));
						}
					});
				},
				error: function( jqXHR, textStatus, error ) {
					// Some error handling maybe?
				},
				complete: function( jqXHR, textStatus ) {
					callback.call( model );
				}
			}
			$.ajax( ajax );
			
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
		Model.fetch = abstractMethod( function( options, callback ) {
			var newInstance, ajax;

			Model.all.removeAll();

			ajax = {
				url: ajaxOptions.modelPath,
				type: ajaxOptions.methods.list,
				async: options.async,
				dataType: "json",
				contentType: "application/json; charset=UTF-8",
				success: function( data, textStatus, jqXHR ) {

					// If previous statement was excuted successfully, create new
					// instance of our model
					_( data ).each(function( resultJSON ) {
						var newInstance = new Model( data );

						// Mark this Record as not new (and therefore as already persisted)
						newInstance = new Model( resultJSON );
						newInstance.isBeingInitialized = true;
						newInstance.isNewRecord = false;
						newInstance.hasChanged( false );
						newInstance.isBeingInitialized = false;

						// Append the new model to our collection of Models.
						// Every observer will be notified about this event.
						if ( ! _( Model.all() ).contains( newInstance ) ) {
							Model.all.push( newInstance );
						}
					});
				},
				error: function( jqXHR, textStatus, error ) {
					// Some error handling maybe?
				},
				complete: function( jqXHR, textStatus ) {
					callback.call( Model );
				}
				
			};
			$.ajax( ajax );
		});
	}


	var abstractMethod = function( fn ) {
		return function( options, callback ) {
			if ( typeof options === "function" ) {
				callback = options;
				options = {}
			}

			if ( typeof callback !== "function" ) {
				callback = function() {};
			}

			if ( !options ) { options = {}; }

			_( options ).defaults({
				async: true
			})

			fn.call( this, options, callback )
		}
	}

	// Everything in this object will be the public API
	return Storage;
});


