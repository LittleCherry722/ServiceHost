define([
	"underscore"
], function( _ ) {
  var pollingRescources = [],
  lastUpdate = parseInt( (new Date().getTime())/1000, 10 ),
  pollingUrl = "/changes";

  var initialize = function( Resource ) {
    Resource.enablePolling = enablePolling;
  }


  var waitingTime = function() {
    var now = parseInt( (new Date().getTime())/1000, 10 );
    var s =  now - lastUpdate;
    if (s <    30) return 2;
    if (s <  5*60) return 10;
    if (s < 30*60) return 30;
    return 3*60;
  }

  var enablePolling = function( name, priority ) {
    Resource = this;
    if ( !name ) {
      name = Resource.className.toLowerCase()
    }
    if ( !priority ) {
      priority = 0;
    }
    pollingRescources.push({
      resource: Resource,
      name: name,
      priority: priority
    });
  }

  var updateHandler = {
    inserted: function(data, resource) {
      if ( !resource.find( data.id ) ) {
        var instance = new resource(data);
        instance.isNewRecord = false;
        resource.all.push( instance );
      }
    },
    updated: function(data, resource) {
      instance = resource.find(data.id)
      if ( instance ) {
        instance.applyData( data );
      }
    },
    deleted: function(data, resource) {
      resource.all.remove(function( instance ) {
        return instance.id() === data.id;
      })
    }
  }

  var poll = function() {
    var data = {
      since: lastUpdate
    };
    $.ajax({
      url: pollingUrl,
      data: data,
      dataType: "json",
      success: update,
      complete: function() {
        window.setTimeout(poll, waitingTime() * 1000 );
      }
    })
  }

  var update = function( pollingData ) {
    var changesReceived = false;

    _.chain(pollingRescources).sortBy( "priority" ).reverse().each(function( resourceObj ) {
      if ( !pollingData[resourceObj.name] ) { return }
      _.each(updateHandler, function( action, actionName ) {
        _(pollingData[resourceObj.name][actionName]).each(function( item ) {
          changesReceived = true;
          action(item, resourceObj.resource);
        })
      });
    });

    if (changesReceived) {
      lastUpdate = Math.ceil( (new Date().getTime())/1000 );
    }


//    var prefix = changesReceived ? "Some " : "No "
//    console.log(prefix + "Changes Received. Next poll in " + waitingTime() + " seconds.");
  }

  initialize.poll = poll;

  return initialize;
});
