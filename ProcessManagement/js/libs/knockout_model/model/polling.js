define([
    "underscore"
], function( _ ) {
    var pollingRescources = [],
        pollingUrl = "/changes",
        lastUpdate;

    var initialize = function( Resource ) {
        Resource.enablePolling = enablePolling;
    }

    var getTime = function () {
        return Math.floor((new Date().getTime()) / 1000);
    }

    var waitingTime = function() {
        var now = getTime();
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
            t: getTime() - lastUpdate
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
            lastUpdate = getTime()
        }


//    var prefix = changesReceived ? "Some " : "No "
//    console.log(prefix + "Changes Received. Next poll in " + waitingTime() + " seconds.");
    }

    lastUpdate = getTime()
    initialize.poll = poll;

    return initialize;
});
