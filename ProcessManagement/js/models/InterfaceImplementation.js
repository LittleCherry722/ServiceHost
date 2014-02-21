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

  var InterfaceImplementation = Model("InterfaceImplementation", { remotePath: "repo/implementations" });
  window.InterfaceImplementation = InterfaceImplementation;

  InterfaceImplementation.attrs({
    creator: "string",
    description: "string",
    processId: "integer",
    fixedSubjectId: "string",
    interfaceSubjects: {
      type: "json",
      defaults: []
    },
    offerId:   "integer",
    graph: {
      type: "json",
      lazy: false
    }
  });

  InterfaceImplementation.belongsTo( "interfaceOffer" );

  InterfaceImplementation.fromProcess = function(process, creator, description) {
    var options = {
      creator: creator,
      description: description,
      name: process.name(),
      offerId: process.offerId(),
      fixedSubjectId: process.fixedSubjectId(),
      interfaceSubjects: process.interfaceSubjects(),
      graph: process.graph()
    };

    return (new InterfaceImplementation(options));
  }

  InterfaceImplementation.nameAlreadyTaken = function (name) {
    return !!InterfaceImplementation.all().filter(function (i) {
      return i.name() == name;
    }).length;
  }




  // InterfaceImplementation.fetch = function(obj, callback) {
  //   callback.success();
  // }

  // InterfaceImplementation.all = ko.observableArray([
  //   new InterfaceImplementation({ id: 1, name: "Travel Process", creator: "Arne", graph: graph }),
  // ]);

  return InterfaceImplementation;
});
