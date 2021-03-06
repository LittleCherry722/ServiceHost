define([
  "knockout",
  "app",
  "underscore",
  "models/user",
  "models/process",
  "models/actions",
  "models/history",
  "models/processInstance",
  "moment",
  "notify",
  "select2",
  "jquery.ui"
], function( ko, App, _, User, Process, Actions, History, ProcessInstance, moment, Notify, select2 ) {

  var ViewModel = function() {
    var self = this;
    // Filter
    this.availableUsers = ko.observableArray(User.all());
    this.availableProcesses = ko.observableArray(Process.all());
    this.availableStatetypes= availableStatetypes;

    this.startableProcesses = ko.observableArray(
      $.grep(Process.all(), function(p) {
        return p.startAble();
      })
    );

    this.selectedUser = selectedUser;
    this.selectedProcess = selectedProcess;
    this.selectedStatetype = selectedStatetype;
    this.selectedStart = selectedStart;
    this.selectedEnd = selectedEnd;

    this.tabs = tabs;
    this.tabDescriptions = tabDescriptions;
    this.currentTab = currentTab;
    this.startProcessId = startProcess;
    this.startProcess = ko.computed(function() {
      return Process.find(self.startProcessId());
    });
    this.actionCount = actionCount;

    this.newInstance = function(formElement) {
      var process = Process.find( $("input[name='processId']").val()),
      instance;
      $('#processNameModal').modal('hide');
      instance = new ProcessInstance( {
        processId: process.id(),
        name: $("input[name='instancename']").val(),
        graph: process.graph()
      });

      instance.save(null, {
        success: function() {
          Actions.fetch();
          History.fetch();
        },
        error: function() {
          Notify.error( "Error", 'Unable to create a new instance of "' + process.name() + '" process.'  );
        }
      });
    };
  };
  currentSubView = ko.observable();
  var availableStatetypes = ko.computed(function() {
    var uniqueStates= [];
    $.each(Actions.all(), function(i, el){
      if($.inArray(el.stateType(), uniqueStates) === -1) uniqueStates.push(el.stateType());
    });
    return uniqueStates;
  });

  var actionCount = ko.computed(function() {
    var count = 0;
    $.each(Actions.all(), function(i, el){
      if(el.executable()) {
        count++;
      }
    });
    return count;
  });

  var tabs = [ 'Actions', 'History' ];
  var tabDescriptions = {
    'Actions': 'Here you can view the execution graph of the current subject internal behavior',
    'History': 'Here you can view the execution history of this process'
  };
  var currentTab = ko.observable();
  var startProcess = ko.observable();

  var setView = function( tab ) {
    currentTab( tab );
  };

  /* Start Filter */
  var selectedUser = ko.observable();
  selectedUser.subscribe(function( User ) {
    if ( currentSubView() ) {
      currentSubView().setUser( User );
    }
  });
  var selectedProcess = ko.observable();
  selectedProcess.subscribe(function( Process) {
    if ( currentSubView() ) {
      currentSubView().setProcess( Process );
    }
  });
  var selectedStatetype = ko.observable();
  selectedStatetype.subscribe(function( Statetype) {
    if ( currentSubView() ) {
      currentSubView().setStatetype( Statetype );
    }
  });
  var selectedStart = ko.observable();
  selectedStart.subscribe(function( Start) {
    if ( currentSubView() ) {
      if(Start) {
        currentSubView().setStart( moment(Start).format("X") );
      } else {
        currentSubView().setStart("");
      }
    }
  });
  var selectedEnd = ko.observable();
  selectedEnd.subscribe(function( End ) {
    if ( currentSubView() ) {
      if(End) {
        currentSubView().setEnd( moment(End).format("X") );
      } else {
        currentSubView().setStart("");
      }
    }
  });
  /* End Filter */

  currentTab.subscribe(function( newTab ) {
    if ( !newTab ) {
      currentTab( tabs[0] );
      return;
    }

    // just load our new viewmodel and call the init method.
    require([ "viewmodels/home/" + newTab.toLowerCase() ], function( viewModel ) {
      unloadSubView();
      currentSubView( viewModel );
      viewModel.init.apply( viewModel, [  ] );

      selectedUser.valueHasMutated();
      selectedProcess.valueHasMutated();
      selectedStatetype.valueHasMutated();
      selectedStart.valueHasMutated();
      selectedEnd.valueHasMutated();
    });

    if ( newTab === tabs[0] ) {
      $("#executionContent").addClass("first-tab-selected");
    } else {
      $("#executionContent").removeClass("first-tab-selected");
    }
  });

  var initialize = function(subSite) {
    var viewModel;
    viewModel = new ViewModel();

    if (!subSite) {
      subSite = tabs[0];
    }

    App.loadTemplate( "home", viewModel, null, function() {
      if ( currentTab() == subSite ) {
        currentTab.valueHasMutated();
      } else {
        currentTab( subSite );
      }


      $( "#from" ).datepicker({
        defaultDate: "+1w",
        changeMonth: true,
        numberOfMonths: 3,
        onClose: function( selectedDate ) {
          $( "#to" ).datepicker( "option", "minDate", selectedDate );
        }
      });
      $( "#to" ).datepicker({
        defaultDate: "+1w",
        changeMonth: true,
        numberOfMonths: 3,
        onClose: function( selectedDate ) {
          $( "#from" ).datepicker( "option", "maxDate", selectedDate );
        }
      });
      $("#ui-datepicker-div").wrap('<div id="dashboard_datepicker" />');
      $(".sel").prepend('<option/>').val(function(){return $('[selected]',this).val() ;})
      var select2 = $(".sel").select2( {
        width: "copy",
        dropdownAutoWidth: "true"

      });

      $(".sel").on("change", function(e) {
        var process = Process.find( e.val ) ;
        $(".sel").select2("val", "");
        $("input[name='processId']").val(e.val);
        $("input[name='instancename']").val(process.name() +' ' + moment().format('YYYY-MM-DD HH:mm'));
        $("#processNameModal").modal();
      });
    });
  };

  var unload = function() {
    unloadSubView();
    return true;
  };

  var unloadSubView = function() {
    if ( currentSubView() && typeof currentSubView().unload === "function" ) {
      currentSubView().unload();
    }
  };

  // Everything in this object will be the public API
  return {
    init: initialize,
    setView: setView,
    unload: unload
  };
});
