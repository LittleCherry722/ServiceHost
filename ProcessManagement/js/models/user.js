define([
	"knockout"
], function( ko ) {
	var User = function(name) {
		this.name = ko.observable(name);

		this.messageCount = ko.observable(0);
	}

	User.prototype.all = function() {
		return [];
	}
	
	return User;
});
