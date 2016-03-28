/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
define([
  "knockout",
  "model"
], function (ko, Model) {

  var Interface = Model("Interface", { remotePath: "repo/interfaces" });
  window.Interface = Interface;

  Interface.attrs({
    interfaceType: "string",
    name: "string",
    description: "string",
    processId: "integer",
    views: {
      type: "json",
      lazy: false
    }
  });

  Interface.belongsTo("process");

  Interface.include({
    initialize: function( data ) {
      var self = this;

      this.viewSubjects = ko.computed({
        deferEvaluation: true,
        read: function() {
          var t,
              subjects = [];

          _( self.views() ).each(function(view, viewId) {
            var s = _(view.graph.process).find(function(s) {
              return s.id == viewId;
            });
            var imps = view.implementations;
            subjects.push({id: s.id, name: s.name, impCount: imps.length, imps: imps});
          });

          return subjects;
        }
      });

      this.isImplemented = ko.computed({
        deferEvaluation: true,
        read: function() {
          var isubs = self.interfaceSubjects();
          return isubs.every(function(e) { return e.impCount > 0; });
        }
      });
    }
  });

  Interface.nameAlreadyTaken = function (name) {
    return !!Interface.all().filter(function (i) {
      return i.name() == name;
    }).length;
  };

  return Interface;
});
