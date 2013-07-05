define([
  "knockout",
  "app",
  "underscore",
  "models/actions",
], function( ko, App, _, Actions) {

  var ViewModel = function() {
      this.actions = actionsList;
        // Filter
    this.selectedUser = selectedUser;
    this.selectedProcess = selectedProcess;
    this.selectedStatetype = selectedStatetype;
    this.selectedStart = selectedStart;
    this.selectedEnd = selectedEnd;
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
      /*
      if (selectedStart() && parseInt(selectedStart()) > parseInt(moment(value.processStarted).format('X'))) {
        filter = true;
      }
      if (selectedEnd() && parseInt(selectedEnd()) < parseInt(moment(value.processEnd).format('X'))) {
        filter = true;
      }*/
      if(filter==false) {
        actionsList.push(value);
      }
    });
  }

  var initialize = function() {
    //actions(Actions.all.slice(0));

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
