/*

   jQuery pub/sub plugin by Peter Higgins (dante@dojotoolkit.org)

   Loosely based on Dojo publish/subscribe API, limited in scope. Rewritten blindly.

   Original is (c) Dojo Foundation 2004-2010. Released under either AFL or new BSD, see:
   http://dojofoundation.org/license for more information.


   subscribers, subscribeOnce are additions by Arne Link
   function factory added by David Kalnischkies
   (c) 2012, S-BPM Groupware

*/

(function (factory) {
    if ( typeof define === 'function' && define.amd ) {
        // AMD. Register as an anonymous module.
        define(['jquery'], factory);
    } else if (typeof exports === 'object') {
        // Node/CommonJS style for Browserify
        module.exports = factory;
    } else {
        // Browser globals
        factory(jQuery);
    }
}(function (d) {
	// the topic/subscription hash
	var cache = {};

	d.publish = function(/* String */topic, /* Array? */args){
		// summary:
		//		Publish some data on a named topic.
		// topic: String
		//		The channel to publish on
		// args: Array?
		//		The data to publish. Each array item is converted into an ordered
		//		arguments on the subscribed functions.
		//
		// example:
		//		Publish stuff on '/some/topic'. Anything subscribed will be called
		//		with a function signature like: function(a,b,c){ ... }
		//
		//	|		$.publish("/some/topic", ["a","b","c"]);
		cache[topic] && d.each(cache[topic], function(){
			if ( !d.isArray(args) ) {
				args = [args]
			}
			this.apply(d, args || []);
		});
	};

	d.subscribe = function(/* String */topic, /* Function */callback){
		// summary:
		//		Register a callback on a named topic.
		// topic: String
		//		The channel to subscribe to
		// callback: Function
		//		The handler event. Anytime something is $.publish'ed on a
		//		subscribed channel, the callback will be called with the
		//		published array as ordered arguments.
		//
		// returns: Array
		//		A handle which can be used to unsubscribe this particular subscription.
		//
		// example:
		//	|	$.subscribe("/some/topic", function(a, b, c){ /* handle data */ });
		//
		if(!cache[topic]){
			cache[topic] = [];
		}
		cache[topic].push(callback);
		return [topic, callback]; // Array
	};

	d.unsubscribe = function(/* Array */handle){
		// summary:
		//		Disconnect a subscribed function for a topic.
		// handle: Array
		//		The return value from a $.subscribe call.
		// example:
		//	|	var handle = $.subscribe("/something", function(){});
		//	|	$.unsubscribe(handle);

		var t = handle[0];
		cache[t] && d.each(cache[t], function(idx){
			if(this == handle[1]){
				cache[t].splice(idx, 1);
			}
		});
	};

	// List Subscribers
	d.subscribers = function(/* String */topic) {
		l = [];
		cache[topic] && d.each(cache[topic], function(idx) {
			l.push(this);
		});
		return l;
	};

	d.subscribeOnce = function(/* String */topic, /* Function */callback){
		// summary:
		//              Register a callback on a named topic.
		// topic: String
		//              The channel to subscribe to
		// callback: Function
		//              The handler event. Anytime something is $.publish'ed on a
		//              subscribed channel, the callback will be called with the
		//              published array as ordered arguments.
		//
		//      Only subscribes if callback is not already subscribed
		//
		// returns: Array
		//              A handle which can be used to unsubscribe this particular subscription.
		//
		// example:
		//      |       $.subscribe("/some/topic", function(a, b, c){ /* handle data */ });
		//
		if(d.subscribers(topic).indexOf(callback) !== -1) {
			return [topic, callback]; // Array
		}
		if(!cache[topic]){
			cache[topic] = [];
		}
		cache[topic].push(callback);
		return [topic, callback]; // Array
	};
}));
