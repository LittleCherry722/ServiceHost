define([
	"underscore",
	"knockout"
], function( _, ko ) {

	/***************************************************************************
	 * The initialization function
	 ***************************************************************************/

	var Storage = function( Model, ajaxOptions ) {
		var pathBuilder = new PathBuilder( Model );

		if ( typeof ajaxOptions === "undefined" ) {
			ajaxOptions = {}
		}
		if ( typeof ajaxOptions.methods === "undefined" ) {
			ajaxOptions.methods = {}
		}

		// some general ajax options. These are neede later on when performing the
		// requests.
		_( ajaxOptions.methods ).defaults({
			destroy: "DELETE",
			create: "POST",
			save: "PUT",
			list: "GET",
			get: "GET"
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

			if ( this.beforeSave.call( this ) === false ) {
				return
			}

			if( !this.validate() ) {
				callback.call( this, "Did not pass validation." );
				return;
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
				this.afterSave.call( this );
				callback.call( this );
				this.hasChanged( false );
			});
		});

		Model.prototype.refresh = function( callback ) {
			this.attributesLoaded( false );
			this.loadAttributes( callback );
		};

		Model.prototype.applyData = abstractMethod(function( input, callback ) {
			var data = input,
				instance = this;

			if ( typeof input === "string" ) {
				data = JSON.parse( input );
			}

			// Override all local attributes with attributes supplied by the Server
			_( Model.attrs() ).each(function( attrOptions, attrName ) {
				if ( _( data ).has( attrName ) ) {
					instance[ attrName ]( attrOptions.fromJSON( data[ attrName ] ));
				}
			});

			callback.call( this );
		});

		// DStringestroy method
		Model.prototype.destroy = abstractMethod( function( options, callback ) {
			if ( this.beforeDestroy.call( this ) === false ) {
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
			var data, ajax,
					model = this;

			if ( model.attributesLoaded() ) {
				callback.call( model, null );
				return;
			}

			ajax = {
				cache: false,
				url: pathBuilder.getPath( model ),
				type: ajaxOptions.methods.get,
				async: options.async,
				dataType: "json",
				contentType: "application/json; charset=UTF-8",
				success: function( data, textStatus, jqXHR ) {

					// Override all local attributes with attributes supplied by the Server
					model.applyData( data );
					model.attributesLoaded( true );
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
				url: pathBuilder.destroyPath( model ),
				type: ajaxOptions.methods.destroy,
				async: options.async,
				dataType: "json",
				contentType: "application/json; charset=UTF-8",
				success: function( data, textStatus, jqXHR ) {

					// Mark the model as destroyed and remove it from the list of
					// instances
					model.isDestroyed = true;
					Model.all.remove( model )
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

			if ( model.beforeCreate.call( this ) === false ) {
				return
			}

			model.attributesLoaded( true );

			ajax = {
				cache: false,
				url: pathBuilder.createPath( model ),
				type: ajaxOptions.methods.create,
				async: options.async,
				data: model.toJSONString(),
				dataType: "json",
				contentType: "application/json; charset=UTF-8",
				success: function( data, textStatus, jqXHR ) {

					// Override all local attributes with attributes supplied by the Server
					model.applyData( data );

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
			var ajax, url,
					model = this;

			if ( ! model.hasChanged() ) {
				callback.call( model );
				return;
			}

			ajax = {
				cache: false,
				url: pathBuilder.savePath( model ),
				type: ajaxOptions.methods.save,
				async: options.async,
				data: model.toJSONString(),
				dataType: "json",
				contentType: "application/json; charset=UTF-8",
				success: function( data, textStatus, jqXHR ) {

					// Override all local attributes with attributes supplied by the Server
					model.applyData( data );
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
				url: pathBuilder.listPath(),
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

			fn.call( this, options, callback );
		}
	}

	var PathBuilder = function( Model ) {
		var regularModelPath, relationModelPath, init,
				isIntermediateModel,
				pathPrefix = "/";

		isIntermediateModel = function() {
			if ( Model.belongsTo().length === Model.ids().length && Model.ids().length > 1 ) {
				_( Model.belongsTo() ).each(function( o, idy ) {
					if ( !_( Model.ids() ).contains( o.foreignKey ) ) {
						return false;
					}
				});
			} else {
				return false;
			}
			return true;
		}

		regularModelPath = function( instance ) {
			if ( typeof instance === "undefined" ) {
				return pathPrefix + Model.className.toLowerCase();
			} else {
				return pathPrefix + Model.className.toLowerCase() + "/" + instance.id();
			}
		}
		relationModelPath = function( instance ) {
			if ( typeof instance === "undefined" ) {
				return pathPrefix + _( Model.belongsTo() ).map(function( o ) {
					return o.modelName;
				}).join("/");
			} else {
				return pathPrefix + _.chain( Model.belongsTo() ).map(function( o ) {
					return [ o.modelName, instance[ o.foreignKey ]() ];
				}).flatten().value().join("/");
			}
		}

		this.listPath = function() {
			return isIntermediateModel() ? relationModelPath() : regularModelPath();
		}
		this.savePath = this.destroyPath = this.getPath = function( instance ) {
			if ( isIntermediateModel() ) {
				return relationModelPath( instance );
			} else {
				return regularModelPath( instance );
			}
		}
		this.createPath = function( instance ) {
			if ( isIntermediateModel() ) {
				return relationModelPath( instance );
			} else {
				return regularModelPath();
			}
		}
	}

	// Everything in this object will be the public API
	return Storage;
});


