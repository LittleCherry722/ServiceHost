define([
  "knockout",
  "app",
  "underscore",
  "models/actions",
  "models/processInstance",
  "models/process",
  "notify",
  "moment"
], function( ko, App, _, Actions, ProcessInstance, Process, Notify, moment ) {

  var ViewModel = function() {
    var self = this;
    this.actions = actionsList;
    this.processes = Process.all;
    // Filter
    this.selectedUser = selectedUser;
    this.selectedProcess = selectedProcess;
    this.selectedStatetype = selectedStatetype;
    this.selectedStart = selectedStart;
    this.selectedEnd = selectedEnd;
    this.showGraph = showGraph;
    this.hasActions = hasActions;

    this.googleDriveData = ko.observable();
    this.refreshGoogleDriveData = function() {
      $.ajax({
        cache: false,
        dataType: "json",
        type: "GET",
        url: "../googledrive/get_files?id=" + App.currentUser().id(),
        success: function( data, textStatus, jqXHR ) {
          self.googleDriveData( data.items );
        },
        error: function( jqXHR, textStatus, error ) {
          Notify.error("Error", "There has been an Error retrieving the file list." +
                       "Please make sure you have the appropriate permissions.");
        }
      });
    };
    this.selectFile = function() {
      //console.log("call");
      $('#googleDriveModal').modal('hide');
      parent.currentSelectedFile( this );
    };

  };

  /* Filter Start */
  var selectedUser = ko.observable();
  var selectedProcess = ko.observable();
  var selectedStatetype = ko.observable();
  var selectedStart = ko.observable();
  var selectedEnd = ko.observable();

  var actionsList = ko.computed(function() {
    var actions = _.chain( Actions.all()).map(function( action ) {
      var processStarted = parseInt(moment(action.processStarted).format('X'), 10);
      if (selectedUser() && selectedUser() !== action.userID()) {
        return null;
      }
      if (selectedProcess() && selectedProcess() !== action.process().name() ) {
        return null;
      }
      if (selectedStatetype() && selectedStatetype() !== action.stateType()) {
        return null;
      }
      if (selectedStart() && parseInt(selectedStart(), 10) >= processStarted) {
        return null;
      }
      if (selectedEnd() && parseInt(selectedEnd(), 10) <= processStarted) {
        return null;
      }
      return action;
    }).compact().value();

    return actions;
  });

  var hasActions = ko.computed(function () {
    return !!actionsList.length;
  });

  var showGraph = function(action){

    setTimeout(function(){
      var table = $( '#' + action.instanceTableId()),
        node = 0,
        graphContainer = $('#graph_bv_outer'),
        graphModal = $('#graphModal'),
        processInstance, currentState, process;

      // fetch process instance
      _.each( ProcessInstance.all(), function (element) {
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
    }, 100);
  };

  var initialize = function() {
    var viewModel = new ViewModel();

    App.loadTemplate( "home/actions", viewModel, "executionContent", function() {
      $('.show-graph').fancybox({
        scrolling: 'no',
        transitionIn: 'none',
        transitionOut: 'none'
      });
    });
    if ( !Actions.all().length ) {
      Actions.fetch();
    }
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
