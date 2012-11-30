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
		
	}

	Process.all = function() {
		processes = [ new Process( "test Process" ) ];
		return processes;
	}
	
	return Process;
});
