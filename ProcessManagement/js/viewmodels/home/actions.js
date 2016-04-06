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
    window.actions = this.actions = actionsList;
    this.processes = Process.all;
    // Filter
    this.selectedUser = selectedUser;
    this.selectedProcess = selectedProcess;
    this.selectedStatetype = selectedStatetype;
    this.selectedStart = selectedStart;
    this.selectedEnd = selectedEnd;
    this.showGraph = showGraph;
    this.hasActions = hasActions;
    this.getSubjectName = getSubjectName;
    this.getMessageName = getMessageName;
    this.getActionText = getActionText;

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
      $('#googleDriveModal').modal('hide');
      parent.currentSelectedFile( this );
    };

  };

  var getSubjectName = function(subjectId, processInstance) {
    var subject = _.find(processInstance.process().subjectsArray(), function(s) {
      return s[0] == subjectId;
    });
    if(subject) {
      return subject[1];
    } else {
      return "";
    }
  };

  var getMessageName = function(messageId, processInstance) {
    var message = _.find(_.pairs(processInstance.process().graph().definition.messages), function(s) {
      return s[0] == messageId;
    });
    if(message) {
      return message[1];
    } else {
      return "";
    }
  };

  var getActionText = function(action) {
    if(action.stateText()) {
      return action.stateText();
    } else {
      var text = action.stateType();
      var processInstance = ProcessInstance.find(action.processInstanceID());
      if('actionData' in action && $.isArray(action.actionData()) && action.actionData().length > 0) {
        var separator = " - ";
        if(action.stateType() === "send") {
          separator = " to ";
        } else if (action.stateType() === "receive") {
          separator = " from ";
        }
        text += ": " + getMessageName(action.actionData()[0].text, processInstance) + separator + getSubjectName(action.actionData()[0].relatedSubject, processInstance);
      }
      return text;
    }
  };


  /* Filter Start */
  var selectedUser = ko.observable();
  var selectedProcess = ko.observable();
  var selectedStatetype = ko.observable();
  var selectedStart = ko.observable();
  var selectedEnd = ko.observable();

  var actionsList = ko.observableArray([]);
  var makeActionsList = function(as) {
    var actions = _.chain( as ).map(function( action ) {
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

    actionsList.removeAll();
    _(actions).each(function(a) {
      actionsList.push(a);
    });
  };
  makeActionsList(Actions.all());
  Actions.all.subscribe(makeActionsList);

  var hasActions = ko.computed(function () {
    return !!actionsList().length;
  });

  var showGraph = function(action){
    $.fancybox({
      scrolling: 'yes',
      transitionIn: 'none',
      transitionOut: 'none',
      href: '#graphModal'
    });

    setTimeout(function(){
      var table = $( '#' + action.instanceTableId()),
          nodes = [],
          graphContainer = $('#graph_bv_outer'),
          graphModal = $('#graphModal'),
          processInstance, currentState, process;

      // fetch process instance
      _.each( ProcessInstance.all(), function (element) {
        if( element.id () === action.processInstanceID() ) {
          processInstance = element;
        }
      });

      gv_cv_paper = null;           // remove reference to any previous cv_paper. When cv_paper would be not null and no cv-container exists, tk_graph crashes
      gf_clearGraph();
      gf_loadGraph( JSON.stringify( processInstance.graph().definition ) );
      gv_graph.selectedSubject = null;
      gf_clickedCVnode( action.subjectID() );
      gf_clickedCVbehavior();
      gv_graph.selectedSubject = action.subjectID();

      // select active node
      currentStates = processInstance.getCurrentStates( action.subjectID() );
      process = processInstance.getCurrentProcess( action.subjectID() );

      if( process !== null ) {
        $.each( process.macros[0].nodes, function( i, value ) {
          if (-1 !== $.inArray(value.id, currentStates)) {
            nodes.push(i);
          }
        } );

        $.each(nodes, function(k, node) {
          if(gv_objects_nodes[node] ){
            gv_objects_nodes[node].select();
          }
        });
      }
    }, 100);
  };


  var initialize = function() {
    var viewModel = new ViewModel();

    App.loadTemplate( "home/actions", viewModel, "executionContent");
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
