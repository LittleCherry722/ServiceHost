define([
	"knockout",
	"model",
	"underscore",
	"moment"
], function( ko, Model, _, moment ) {

	// Our main model that will be returned at the end of the function.
	//
	// Process is responsivle for everything associated with processes directly.
	//
	// For example: Getting a list of all processes, savin a process,
	// validating the current process etc.
	Graph = Model( "Graph" );

	Graph.attrs({
		graphString: {
			type: "string",
			defaults: "{}",
			lazy: true
		},
		date: "string",
		processID: "integer"
	});

	Graph.ids([ "id" ]);

	Graph.belongsTo( "process" )

	Graph.include({
		beforeSave: function() {
			this.date( moment().format( "YYYY-MM-DD HH:mm:ss" ) );
		}
	});

	return Graph;
});
