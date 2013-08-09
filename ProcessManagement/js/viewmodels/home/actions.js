define([
	"knockout",
	"app",
	"underscore",
	"models/actions",
	"models/processInstance"
], function( ko, App, _, Actions, ProcessInstances) {

	var ViewModel = function() {
		this.actions = actionsList;
		this.processes = Process.all;
		// Filter
		this.selectedUser = selectedUser;
		this.selectedProcess = selectedProcess;
		this.selectedStatetype = selectedStatetype;
		this.selectedStart = selectedStart;
		this.selectedEnd = selectedEnd;
		this.showGraph = showGraph;
	};
	var actionsList = ko.observableArray();
	var actions = ko.computed(function() {actionsList(Actions.all().slice(0));});

	/* Filter Start */
	var selectedUser = ko.observable();
	var selectedProcess = ko.observable();
	var selectedStatetype = ko.observable();
	var selectedStart = ko.observable();
	var selectedEnd = ko.observable();
	selectedUser.subscribe(function() { filter(); });
	selectedProcess.subscribe(function() { filter(); });
	selectedStatetype.subscribe(function() { filter(); });
	selectedStart.subscribe(function() { filter(); });
	selectedEnd.subscribe(function() { filter();});

	var filter = function() {
		actionsList.removeAll();
		$.each( Actions.all(), function ( i, value ) {
			var filter = false;
			if (selectedUser() && selectedUser() !== value.userID()) {
				filter = true;
			}
			if (selectedProcess() && selectedProcess() !== value.process().name() ) {
				filter = true;
			}
			if (selectedStatetype() && selectedStatetype() !== value.stateType()) {
				filter = true;
			}
			if (selectedStart() && parseInt(selectedStart()) >= parseInt(moment(value.processStarted).format('X'))) {
		    	filter = true;
			}
			if (selectedEnd() && parseInt(selectedEnd()) <= parseInt(moment(value.processStarted).format('X'))) {
				filter = true;
			}
			if(filter==false) {
				actionsList.push(value);
			}
		});
	};

	var showGraph = function(action){
		var graphId = 'graph_bv_outer',
			table = $( '#' + action.instanceTableId()),
			graphContainerOuter = $('.graph', table),
			node = 0,
			graphContainer, processInstance, currentState, process;

		$('.show-graph').removeClass('invisible');
		$('.show-graph', table).addClass('invisible');

		// create graph containers
		$('#' + graphId).remove();
		graphContainer = $('<div/>').attr('id', graphId).appendTo(graphContainerOuter);

		// fetch process instance
		_.each( ProcessInstances.all(), function (element) {
			if( element.id () === action.processInstanceID() ) {
				processInstance = element;
			}
		});

		// load graph
		gf_loadGraph( JSON.stringify( processInstance.graph().definition ) );
		gv_graph.selectedSubject = null;
		gf_clickedCVnode( action.subjectID() );
		gf_clickedCVbehavior();

		// select active node
		currentState = processInstance.getCurrentState( action.subjectID() );
		process = processInstance.getCurrentProcess( action.subjectID() );

		if( process !== null ) {
			$.each( process.macros[0].nodes, function( i, value ) {
				if ( value.id === currentState ) {
					node = i;
				}
			} );

			if( gv_objects_nodes[node] ){
				gf_deselectNodes();
				gv_objects_nodes[node].select();
			}
		}
	};

	var initialize = function() {
		var viewModel = new ViewModel();

		App.loadTemplate( "home/actions", viewModel, "executionContent", function() { });
		Actions.fetch();
	};

	// Everything in this object will be the public API
	return {
		init: initialize,
		setUser: selectedUser,
		setProcess: selectedProcess,
		setStatetype: selectedStatetype,
		setStart: selectedStart,
		setEnd: selectedEnd
	};
});
