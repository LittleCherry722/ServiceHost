define([
	"knockout",
	"model",
	"underscore"
], function( ko, Model, _ ) {

	// Our main model that will be returned at the end of the function.
	//
	// Process is responsivle for everything associated with processes directly.
	//
	// For example: Getting a list of all processes, savin a process,
	// validating the current process etc.
	ProcessInstance = Model( "Process" );

	ProcessInstance.belongsTo( "process" );

	ProcessInstance.attrs({
		processId: "integer"
	});

	ProcessInstance.include({
		initialize: function() {
			var self = this;

			self.instanceName = ko.computed(function() {
				return "Instance " + self.id();
			});
		}
	});

	return ProcessInstance;
});
