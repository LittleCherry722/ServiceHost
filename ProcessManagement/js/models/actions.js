/*
 d* S-BPM Groupware v1.2
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
define(["knockout", "app", "model", "underscore", "models/process", "models/user", "models/processInstance", "notify"], function(ko, App, Model, _, Process, User, ProcessInstances, Notify) {

  Actions = Model("Actions", {
    remotePath : 'processinstance/action'
  });

  Actions.attrs({
    userID : "integer",
    macroID : "string",
    processInstanceID : "integer",
    stateID : "integer",
    stateText : "string",
    stateType : "string",
    actionData : {
      type: "jsonArray",
      lazy: false
    },
    relatedSubject : "string",
    subjectID : "string",
    messageContent : "string",
    currentSelectedFile : "string"
  });

	Actions.belongsTo( "processInstance", { foreignKey: "processInstanceID" } );

  Actions.enablePolling( "action" );

  Actions.all = ko.observableArray();

  Actions.include({
    // Initialize is a special method defined as an instance method.  If any
    // method named "initializer" is given, it will be called upon object
    // creation (when calling new model()) with the context of the model.
    // That is, "this" refers to the model itself.
    // This makes it possible to define defaults for attributes etc.

    initialize : function(data) {
      var self = this;

      this.user = ko.computed({
        read: function() {
          var u = null;
          _.each(User.all(), function(element) {
            if (element.id() === self.userID()) {
              u = element;
            }
          });
          return u;
        },
        deferEvaluation: true
      });

      this.instanceDetailsDivId = ko.computed(function() {
        return "instanceDetails_" + self.processInstanceID() + "_" + self.subjectID() + "_" + self.stateID();
      });
      this.instanceTableId = ko.computed(function() {
        return "instance_" + self.processInstanceID() + "_" + self.subjectID() + "_" + self.stateID();
      });

      this.data = ko.computed(function() {
        var ad = self.actionData();
        User.all();
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
            // a.data = data;
            a.messageText = ko.observable();
            a.selectedUsers = ko.observableArray();
            a.currentSelectedFile = ko.observable();
          });
        }
        return ad;
      });

      // if (this.actionData()) {
      //   self.actionData().data = data;
      // }

      this.hasUsers = ko.computed(function() {
        if (!self.actionData()) {
          return false;
        }
        return !self.actionData().some(function( data ) {
          if (!data.targetUsersData) return false;
          return data.targetUsersData.external;
        })
      });

      this.isSend = ko.computed(function() {
        return self.stateType() === "send";
      });

      this.selectedUsers = ko.observableArray();

      this.selectedUsersMax = ko.computed(function() {
        var max = 0;
        if (self.actionData()) {
          _.each(self.actionData(), function(element) {
            if (element.targetUsersData)
              max = element.targetUsersData.max;
          });
        }
        return max;
      });

      this.selectedUsersMin = ko.computed(function() {
        var min = 0;
        if (self.actionData()) {
          _.each(self.actionData(), function(element) {
            if (element.targetUsersData)
              min = element.targetUsersData.min;
          });
        }
        return min;
      });

      this.selectUsers = ko.computed(function() {
        var u = [];
        if (self.actionData()) {
          _.each(self.actionData(), function(element) {
            if (element.executeAble && element.targetUsersData) {
              _.each(element.targetUsersData.targetUsers, function(el) {
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
        if (self.stateText() !== "") {
          return self.stateText();
        }

        var title = "";
        var titleExecutable = "";
        var actionData = self.actionData();

        _.each(actionData, function(element) {
          if (title.length > 0)
            title += " / ";
          title += element.text;

          if (element.executeAble) {
            if (titleExecutable.length > 0)
              titleExecutable += " / ";
            titleExecutable += element.text;
          }
        });
        return titleExecutable !== "" ? titleExecutable : title;
      });

      this.process = ko.computed(function() {
        if ( self.processInstance() ) {
          return self.processInstance().process();
        } else {
          return undefined;
        }
      });

      this.instanceName = ko.computed(function() {
        if ( self.processInstance() ) {
          return self.processInstance().name();
        } else {
          return "";
        }
      });

      this.hasActions = ko.computed(function() {
        if (self.actionData()) {
          return self.actionData().length > 0;
        }
        return false;
      });

      this.executable = ko.computed(function() {
        var executable = false;
        _.each(self.actionData(), function(element) {
          if (element.executeAble){
            executable = true;
          }
        });

        return executable;
      });

      this.selectFile = function() {
        $('.gdrive-modal').modal('hide');
        self.currentSelectedFile(this);
      };

    },

    action : function( message ) {
      var data, id, actionData;
      data = this.toJSON();
      id = data.processInstanceID;
      actionData = {
        executeAble: message.executeAble,
        messageContent: message.messageText(),
        relatedSubject: message.relatedSubject,
        text: message.text,
        transition: message.transition,
        targetUsersData: message.targetUsersData,
        transitionType: message.transitionType
      }
      data.actionData = actionData;

      data = JSON.stringify(data);
      $.ajax({
        url : '/processinstance/' + id,
        type : "PUT",
        data : data,
        async : true,
        dataType : "json",
        contentType : "application/json; charset=UTF-8",
        success : function(data, textStatus, jqXHR) {
          Actions.fetch();
          Notify.info("Done", "Action successfully sent.");

        },
        error : function(jqXHR, textStatus, error) {
          Notify.error("Error", "Unable to send action. Please try again.");
        }
      });
    },

    send : function( message, obj ) {
      var data, id, actionData,
        selectedUsers = _(this.data()[0].selectedUsers()).compact().length;

      if (this.data()[0].targetUsersData.min > selectedUsers || this.data()[0].targetUsersData.max < selectedUsers) {
        var errorMsg = "Please select the correct amount of users. <br/>";

        errorMsg += "minimum: " + this.data()[0].targetUsersData.min + "<br/>";
        errorMsg += "maximum: " + this.data()[0].targetUsersData.max;
        Notify.error("Error", errorMsg);
        return;
      }

      data = this.toJSON();
      id = data.processInstanceID;
      actionData = {
        executeAble: message.executeAble,
        messageContent: message.messageText(),
        relatedSubject: message.relatedSubject,
        text: message.text,
        transition: message.transition,
        targetUsersData: message.targetUsersData,
        transitionType: message.transitionType
      }
      if (this.currentSelectedFile()) {
        actionData.fileId = this.currentSelectedFile().id;
      }

      var selUsers = message.selectedUsers().map(function(u) {
        return u.id();
      });
      actionData.targetUsersData.targetUsers = selUsers;

      data.actionData = actionData;

      if (!data.actionData.messageContent) {
        data.actionData.messageContent = "[empty message]";
      }

      data = JSON.stringify(data);
      $.ajax({
        url : '/processinstance/' + id,
        type : "PUT",
        data : data,
        async : true, // defaults to false
        dataType : "json",
        contentType : "application/json; charset=UTF-8",
        success : function(data, textStatus, jqXHR) {
          Actions.fetch();
          Notify.info("Done.", "Action successfully sent.");
        },
        error : function(jqXHR, textStatus, error) {
          Notify.error("Error", "Unable to send action. Please try again.");
        }
      });
    }
  });

  return Actions;
});
