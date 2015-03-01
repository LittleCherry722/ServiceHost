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
    creator: "string",
    name: "string",
    description: "string",
    processId: "integer",
    implementations: {
      type: "json",
      layz: false,
      defaults: []
    },
    graph: {
      type: "json",
      lazy: false
    }
  });

  Interface.belongsTo("process");

  Interface.include({
    initialize: function( data ) {
      var self = this;

      this.filterInterfaceSubjects = function(test) {
        return ko.computed({
          deferEvaluation: true,
          read: function() {
            var t,
                subjects = [];

            _( self.graph().process ).each(function( s ) {
              t = s.subjectType ? s.subjectType : s.type;
              if (t === "external" && (s.externalType === "interface" || s.externalType === "external") && test(s)) {
                var imps = s.implementations;
                subjects.push({id: s.id, name: s.name, impCount: imps.length, imps: imps});
              }
            });

            return subjects;
          }
        });
      };

      this.implementedInterfaceSubjects = self.filterInterfaceSubjects(function(s) {
        return !s.relatedInterfaceSubjects;
      });

      this.freeInterfaceSubjects = self.filterInterfaceSubjects(function(s) {
        return s.relatedInterfaceSubjects;
      });

      this.isImplemented = ko.computed({
        deferEvaluation: true,
        read: function() {
          var isubs = self.interfaceSubjects();
          return isubs.every(function(e) { return e.impCount > 0; });
        }
      });
    },

    getTemplate: function(sid) {
      var self = this,
          og   = self.graph(),
          ng   = JSON.parse(JSON.stringify(og)),
          ps   = ng.process,
          // concatTau = function(n, s, last) {
          //   s.macros.forEach(function(m) {
          //     // adjust remove outgoing edges
          //     m.edges = _(m.edges.map(function(e) {
          //       if (e.start === n.id) {
          //         e.start = last;
          //       } else if (e.end === n.id) {
          //         return null;
          //       }
          //       return e;
          //     })).compact();
          //     // adjust remove node
          //   });
          // },
          makeTau = function(node, s) {
            if (s.id === sid) {
              return;
            }
            node.type = node.nodeType = "tau";
            node.text = "tau";
            s.macros.forEach(function(m) {
              m.edges.forEach(function(e) {
                if (e.start === node.id) {
                  e.target = "";
                }
              });
            });
          },
          makeTauID = function(nid, s) {
            var n;
            s.macros.forEach(function(m) {
              n = _(m.nodes).findWhere({id: nid});
            });
            makeTau(n, s);
            return n;
          };

      // Make current subject a normal subj. and all others Interface subjects
      ps.forEach(function(s) {
        if (s.id === sid) {
          s.subjectType = "Single";
          s.startSubject = false;
          s.externalType = "";
          s.name = s.name + " (me)";
          // s.role = "Please choose role";
          s.relatedInterface = self.id();
          s.relatedSubject = sid;
          s.isImplementation = true;
          s.isImplementation = true;
        } else {
          s.relatedInterface = null;
          s.startSubject = false;
          s.relatedSubject = s.id;
          s.subjectType = "external";
          s.externalType = "interface";
          // s.role = "Please choose role";
        }
      });

      // anonymize messages.
      var messages = [];
      ps.forEach(function (s) {
        if (s.id == sid) {
          s.macros.forEach(function(m) {
            m.edges.forEach(function(e) {
              if( e.text ) {
                messages.push(e.text);
              }
            });
          });
        }
      });
      messages = _(messages).uniq();
      _(ng.messages).each(function(v, k) {
        if (!_(messages).contains(k)) {
          ng.messages[k] = "Anonymized";
        }
      });
      ng.messageCounter = messages.length;

      // remove all subjects that are not conncected to our sel. subject
      ng.process = _(ps.map(function(p) {
        if (p.id === sid) return p;
        var hasEdge = false;
        p.macros.forEach(function(m) {
          m.edges.forEach(function(e) {
            if (e.target && e.target.id === sid) {
              hasEdge = true;
            }
          });
        });
        return hasEdge? p : null;
      })).compact();

      // set all send or receive to / from deleted nodes to tau
      var remainingSubjects = ng.process.map(function(p) { return p.id; });
      ps.forEach(function(s) {
        s.macros.forEach(function(m) {
          m.edges.forEach(function(e) {
            if (e.target && !_(remainingSubjects).contains(e.target.id)) {
              makeTauID(e.start, s);
              e.target = "";
              e.text = "tau";
            }
          });
        });
      });

      // set all non send / receive nodes and edges to tau
      ps.forEach(function(s) {
        s.macros.forEach(function(m) {
          m.nodes.forEach(function(n) {
            if (n.type !== "send" && n.type !== "receive") {
              makeTau(n, s);
            }
          });
        });
      });

      // Concatenate Tau nodes and edges
      // ps.forEach(function(s) {
      //   s.macros.forEach(function(m) {
      //     var lastTau = null;
      //     m.nodes = _(m.nodes.map(function(n) {
      //       if (n.type == "tau") {
      //         if (lastTau) {
      //           concatTau(n, s, lastTau)
      //           return null;
      //         } else {
      //           lastTau = n.id;
      //         }
      //       } else {
      //         lastTau = null;
      //       }
      //       return n;
      //     })).compact();
      //   });
      // });

      // set all edges not related to our subject to tau

      console.log("sid: " + sid);
      ps.forEach(function(s) {
        if ( s.id === sid) return;
        s.macros.forEach(function(m) {
          m.edges.forEach(function(e) {
            if (! e.target) {
              e.text = "tau";
            }
          });
        });
      });
      var interfaceSubjects = remainingSubjects.filter(function(id) { return sid !== id; });

      return {
        definition: ng,
        id: og.id
      };
    }
  });

  Interface.fromProcess = function(process, creator, description) {
    // TODO: interfaceType ??
    var options = {
      creator: creator,
      description: description,
      name: process.name(),
      graph: process.graph()
    };

    return (new Interface(options));
  };

  Interface.nameAlreadyTaken = function (name) {
    return !!Interface.all().filter(function (i) {
      return i.name() == name;
    }).length;
  };

  // var uuidSubjects = function(oldGraph, oldId, newId) {
  //   var graph = JSON.parse(JSON.stringify(oldGraph));
  //   var nameMap = {};
  //   graph.process.forEach(function(s) {
  //     var t = s.subjectType ? s.subjectType : s.type;
  //     if (!(t === "external" && s.externalType === "interface")) {
  //       var newId = s.id.replace(/:.*/, '') + Utilities.newUUID();
  //       nameMap[s.id] = newId;
  //     }
  //   })
  //   graph.process.forEach(function(s) {
  //     if (nameMap[s.id]) {
  //       s.id = nameMap[s.id];
  //     }
  //     (s.macros || []).forEach(function(macro) {
  //       (s.edges || []).forEach(function(e) {
  //         if (nameMap[e.target.id]) {
  //           e.target.id = nameMap[e.target.id];
  //         }
  //       });
  //     })
  //   });
  // }

  return Interface;
});
