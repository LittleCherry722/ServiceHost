/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

define([
  "underscore"
], function( _ ) {

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



    // Setup prototype callback so we do not have to check for their
    // existence every time.
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
    Model.prototype.save = abstractMethod( function( options, callbacks ) {
      var saveFn, complete;

      if ( this.beforeSave.call( this ) === false ) {
        callbacks.success.call( this, "" );
        callbacks.complete.call( this, "" );
        return
      }

      if( !this.validate() ) {
        callbacks.error.call( this, "", "Did not pass validation." );
        callbacks.complete.call( this, "" );
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
      complete = callbacks.complete;
      callbacks.complete = function( textStatus ) {
        this.afterSave.call( this );
        complete.call( this, textStatus );
        this.hasChanged( false );
      }

      saveFn.call( this, options, callbacks );
    });

    Model.prototype.refresh = function( options, callbacks ) {
      this.attributesLoaded( false );
      this.loadAttributes( options, callbacks );
    };

    Model.prototype.applyData = abstractMethod(function( input, callbacks ) {
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

      callbacks.complete.call( this );
      callbacks.success.call( this );
    });

    // DStringestroy method
    Model.prototype.destroy = abstractMethod( function( options, callbacks ) {
      if ( this.beforeDestroy.call( this ) === false ) {
        callbacks.error.call( this, "Deletion canceled" );
        callbacks.complete.call( this, "" );
        return
      }

      var destroyFn, complete;
      if ( this.isNewRecord ) {
        destroyFn = destroyInMemory;
      } else {
        destroyFn = destroyPersisted;
      }

      // execute the correct destroy function and execute the callback aftewards.
      // Also execute the afterDestroy Callback.
      complete = callbacks.complete;
      callbacks.complete = function( textStatus ) {
        this.afterDestroy.call( this );
        complete.call( this, textStatus );
      }

      destroyFn.call( this, options, callbacks);
    });

    Model.prototype.loadAttributes = abstractMethod(function( options, callbacks ) {
      var data, ajax,
          model = this;

      if ( model.attributesLoaded() ) {
        callbacks.success.call( model, "" );
        callbacks.complete.call( model, "" );
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
          callbacks.success.call( model, textStatus );
        },
        error: function( jqXHR, textStatus, error ) {
          callbacks.error.call( model, textStatus, error );
        },
        complete: function( jqXHR, textStatus ) {
          callbacks.complete.call( model, textStatus );
        }
      };
      $.ajax( ajax );
    });

    var destroyInMemory = function( options, callbacks ) {

      // Mark the model as destroyed and remove it from the list of
      // instances.
      Model.all.remove( this )
      this.isDestroyed = true;
      callbacks.success.call( this );
      callbacks.complete.call( this );
    }

    // issues a destroy request for the current model to the server.
    // options are options to be passed to the jquery ajax method.
    // Current model is passed as the "this" object
    var destroyPersisted = function( options, callbacks ) {
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

          callbacks.success.call( model, textStatus );
        },
        error: function( jqXHR, textStatus, error ) {
          callbacks.error.call( model, textStatus, error );
        },
        complete: function( jqXHR, textStatus ) {
          callbacks.complete.call( model, textStatus );
        }
      }
      $.ajax( ajax );
    }

    // These Methods are never called by the user, so we do not need any of the
    // options, callback sanitization stuff.
    var createNew = function( options, callbacks ) {
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

          callbacks.success.call( model, textStatus );
        },
        error: function( jqXHR, textStatus, error ) {
          callbacks.error.call( model, textStatus, error );
        },

          complete: function( jqXHR, textStatus ) {
          model.afterCreate.call( this );
          callbacks.complete.call( model, textStatus );
        }
      }
      $.ajax( ajax );
    }

    var saveExisting = function( options, callbacks ) {
      var ajax, url,
          model = this;

      if ( ! model.hasChanged() ) {
        callbacks.success.call( model, "" );
        callbacks.complete.call( model, "" );
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
          callbacks.success.call( model, textStatus );
        },
        error: function( jqXHR, textStatus, error ) {
          callbacks.error.call( model, textStatus, error );
        },
        complete: function( jqXHR, textStatus ) {
          callbacks.complete.call( model, textStatus );
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
    //  async: Whether the query should be performed asynchronously or not.
    //  It is strongly recommended to perform async requests and only wait
    //  for it to finish if really necessary.
    //
    // Options defaults are:
    //  {
    //    async: true
    //  }
    Model.fetch = abstractMethod( function( options, callbacks ) {
      var ajax;

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
            if ( Model.find(resultJSON.id) ) {
                // TODO update instance instead
              return;
            }

            var newInstance = new Model( resultJSON );

            // Mark this Record as not new (and therefore as already persisted)
            newInstance.isBeingInitialized = true;
            newInstance.isNewRecord = false;
            newInstance.hasChanged( false );
            newInstance.isBeingInitialized = false;

            // Append the new model to our collection of Models.
            // Every observer will be notified about this event.
            Model.all.push( newInstance );

          });
          if ( _(data).every(function(e) { return e.id }) ) {
            var modelIds, newIds, removedIds;
            modelIds = Model.all().map(function( e ) {
              return e.id();
            });
            newIds = data.map(function( e ) {
              return e.id;
            });
            removedIds = _.difference( modelIds, newIds );
            Model.all.remove(function( e ) {
              return _(removedIds).contains( e.id() )
            });
          }
          callbacks.success.call( Model, textStatus  );
        },
        error: function( jqXHR, textStatus, error ) {
          callbacks.error.call( Model, textStatus, error );
        },
        complete: function( jqXHR, textStatus ) {
          callbacks.complete.call( Model, textStatus );
        }
      };
      $.ajax( ajax );
    });

  }

  var abstractMethod = function( fn ) {
    return function( options, callbacks ) {
      if ( typeof options === "function" ) {
        callbacks = options;
        options = {}
      }

      if ( typeof callbacks === "function" ) {
        callbacks = {
          complete: callbacks
        }
      } else if ( typeof callbacks !== "object" ) {
        callbacks = {}
      }

      _( callbacks ).defaults({
        success: function() {},
        error: function() {},
        complete: function() {}
      })

      if ( !options ) { options = {}; }

      _( options ).defaults({
        async: true
      })

      fn.call( this, options, callbacks );
    }
  }

  var PathBuilder = function( Model ) {
    var regularModelPath, relationModelPath,
        isIntermediateModel,
        pathPrefix = "/";

    isIntermediateModel = function() {
      if ( Model.belongsTo().length === Model.ids().length && Model.ids().length > 1 ) {
        _( Model.belongsTo() ).each(function( o ) {
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
        return pathPrefix + Model.remotePath;
      } else {
        return pathPrefix + Model.remotePath + "/" + instance.id();
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
