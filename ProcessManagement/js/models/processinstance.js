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
	ProcessInstance = Model( "ProcessInstance" );

	ProcessInstance.belongsTo( "process" );

	ProcessInstance.attrs({
		processId: "integer",
		graph: {
			type: "string",
			defaults: "{}",
			lazy: true
		},
		history: {
			type: "json",
			defaults: "{}",
			lazy: true
		},
		actions: {
			type: "json",
			defaults: [],
			lazy: true
		}
	});

	ProcessInstance.include({
		initialize: function() {
			var self = this;

			this.instanceName = ko.computed(function() {
				return "Instance #" + self.id();
			});
		}
	});

	return ProcessInstance;
});
