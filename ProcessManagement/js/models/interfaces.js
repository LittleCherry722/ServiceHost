/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
define([
       "knockout",
       "model",
       "underscore",
       "models/process",
       "models/user",
       "models/processInstance",
       "notify"
], function( ko, Model, _, Process, User, ProcessInstances, Notify ) {

  Interfaces = Model( "Interfaces", {remotePath: 'processinstance/action'} );

  Actions.attrs({
    userID: "integer",
    processInstanceID: "integer",
    stateID: "integer",
    stateText: "string",
    stateType: "string",
    actionData: "jsonArray",
    relatedSubject: "string",
    subjectID: "string",
    data: "json",
    messageContent: "string"});

    Actions.all = ko.observableArray();

    Actions.include({
      // Initialize is a special method defined as an instance method.  If any
      // method named "initializer" is given, it will be called upon object
      // creation (when calling new model()) with the context of the model.
      // That is, "this" refers to the model itself.
      // This makes it possible to define defaults for attributes etc.

      initialize: function( data ) {

        var self = this;

        this.user = ko.computed(function() {
          var u = null;
          _.each(User.all(), function(element) {
            if (element.id() === self.userID()) {
              u = element;
            }
          });
          return u;
        });

        // this.processInstanceID = ko.computed(function() { return self.processInstanceID(); });
        // this.stateID = ko.computed(function() { return self.stateID(); });
        // this.stateText = ko.computed(function() { return self.stateText(); });
        // this.stateType = ko.computed(function() { return self.stateType(); });
        this.processInstanceID = ko.observable(self.processInstanceID());
        this.stateType = ko.observable(self.stateType());
        this.stateID = ko.observable(self.stateID());
        this.stateText = ko.observable(self.stateText());


        this.instanceDetailsDivId = ko.computed(function() {
          return "instanceDetails_" + self.processInstanceID() + "_" + self.subjectID();
        });
        this.instanceTableId = ko.computed(function() {
          return "instance_" + self.processInstanceID() + "_" + self.subjectID();
        });

        this.actionData = ko.computed(function() {
          var ad = self.actionData();
          if (ad) {
            _.each(ad, function(a) {
              if (a.messages) {
                _.each(a.messages, function(msg) {
                  _.each(User.all(), function(u) {
                    if (u.id() == msg.userID) {
                      msg.user = u;
                    }
                  });
                });
              }
              a.data = data;
              a.messageText = ko.observable();
              a.selectedUsers = ko.observableArray();
            });
          }
          return ad;
        });

        if (this.actionData()) {
          this.actionData().data = data;
        }


        this.relatedSubject = ko.computed(function() { return self.relatedSubject(); });
        this.subjectID = ko.computed(function() { return self.subjectID(); });



        this.selectedUsers = ko.observableArray();

        this.selectedUsersMax = ko.computed(function() {
          var max = 0;
          if (self.actionData()) {
            _.each(self.actionData(), function (element) {
              if (element.targetUsersData) max = element.targetUsersData.max;
            });
          }
          return max;
        });

        this.selectedUsersMin = ko.computed(function() {
          var min = 0;
          if (self.actionData()) {
            _.each(self.actionData(), function (element) {
              if (element.targetUsersData) min = element.targetUsersData.min;
            });
          }
          return min;
        });

        this.selectUsers = ko.computed(function () {
          var u = [];
          if (self.actionData()) {
            _.each(self.actionData(), function (element) {
              if (element.executeAble && element.targetUsersData) {
                _.each(element.targetUsersData.targetUsers, function (el) {
                  for (var i in User.all()) {
                    if (el === User.all()[i].id()) {
                      u.push(User.all()[i]);
                    }
                  }
                });
              }
            });
          }
          return u;
        });

        this.actionTitle = ko.computed(function() {
          if (self.stateText() != "") return self.stateText();

          var title = "";
          var titleExecutable = "";
          var actionData = self.actionData();

          _.each(actionData, function(element) {
            if (title.length>0) title += " / ";
            title += element.text;

            if (element.executeAble) {
              if (titleExecutable.length>0) titleExecutable += " / ";
              titleExecutable += element.text;
            }
          });
          return titleExecutable!= "" ? titleExecutable : title;
        });

        this.process = ko.computed(function() {
          var processId = null;
          _.each(ProcessInstances.all(), function(element) {
            if (element.id() === self.processInstanceID()) {
              processId = element.processId();
            }
          });



          var p = null;
          _.each(Process.all(), function(element) {
            if (element.id() === processId) {
              p = element;
            }
          });

          return p;
        });

        this.hasActions = ko.computed(function() {
          if (self.actionData()) {
            return self.actionData().length > 0
          }
          return false;
        });

        this.executable = ko.computed(function() {
          var executable = false;
          _.each(self.actionData(), function(element) {
            if (element.executeAble) executable = true;
          } );

          return executable;
        })


      },

      action: function() {
        data = this.data;

        id = data.processInstanceID;
        data.actionData = this;

        delete data.actionData.data;


        data = JSON.stringify(data);
        $.ajax({
          url: '/processinstance/' + id,
          type: "PUT",
          data: data,
          async: true,
          dataType: "json",
          contentType: "application/json; charset=UTF-8",
          success: function(data, textStatus, jqXHR) {
            Actions.fetch();
            Notify.info("Done", "Action successfully sent.");

          },
          error : function(jqXHR, textStatus, error) {
            Notify.error( "Error", "Unable to send action. Please try again." );
          }
        });

      },

      send: function() {
        if (        this.data.actionData[0].targetUsersData.min > this.data.actionData[0].selectedUsers().length
            ||  this.data.actionData[0].targetUsersData.max < this.data.actionData[0].selectedUsers().length) {
              var errorMsg = "Please select the correct amount of users. <br/>";

              errorMsg += "minimum: " + this.data.actionData[0].targetUsersData.min + "<br/>";
              errorMsg += "maximum: " + this.data.actionData[0].targetUsersData.max;
              Notify.error( "Error", errorMsg );
              return;
            }


            data = this.data;
            id = data.processInstanceID;
            data.actionData = this;

            delete data.actionData.data;


            data.actionData.messageContent = data.actionData.messageText();
            delete data.actionData.messageText;


            if( data.actionData.messageContent === undefined || data.actionData.messageContent == "" ) {
              data.actionData.messageContent = "[empty message]";
            }

            var selUsers = data.actionData.selectedUsers().map(function(u) { return u.id();})

            data.actionData.targetUsersData.targetUsers = selUsers;
            delete data.actionData.selectedUsers;


            data = JSON.stringify( data );
            $.ajax({
              url : '/processinstance/' + id,
              type : "PUT",
              data: data,
              async : true, // defaults to false
              dataType : "json",
              contentType : "application/json; charset=UTF-8",
              success : function(data, textStatus, jqXHR) {
                Actions.fetch();
                Notify.info("Done.", "Action successfully sent.");
              },
              error : function(jqXHR, textStatus, error) {
                Notify.error( "Error", "Unable to send action. Please try again." );
              }
            });
      }
    });

    return Actions;
});
