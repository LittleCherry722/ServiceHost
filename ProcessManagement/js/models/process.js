define([
	"knockout",
	"router"
], function( ko, Router ) {
	var Process = function(name, isCase) {
		this.isCase = !!isCase;
		this.name = ko.observable(name);

		this.messageCount = ko.observable(0);
	}

	Process.prototype.url = function() {
		return Router.processPath(this);
	}

	Process.all = function() {
		processes = ko.observableArray([ new Process( "test Process" ) ]);
		return processes();
	}

	Process.exists = function() {

	}
	
	return Process;
});
