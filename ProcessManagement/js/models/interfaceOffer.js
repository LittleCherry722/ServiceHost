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
  "model",
  "underscore",
  "models/interfaceImplementation"
], function (ko, Model, _, Implementation) {

  var InterfaceOffer = Model("InterfaceOffer", { remotePath: "repo/offers" });
  window.InterfaceOffer = InterfaceOffer;

  InterfaceOffer.attrs({
    name: "string",
    creator: "string",
    description: "string",
    processId: "integer",
    graph: {
      type: "json",
      lazy: false
    }
  });

  InterfaceOffer.hasMany( "interfaceImplementations" );
  InterfaceOffer.belongsTo( "process" );

  InterfaceOffer.nameAlreadyTaken = function (name) {
    return !!InterfaceOffer.all().filter(function (i) {
      return i.name() == name;
    }).length;
  }

  InterfaceOffer.fromProcess = function(process, creator, description) {
    var options = {
      creator: creator,
      description: description,
      name: process.name(),
      processId: process.id(),
      graph: process.graph()
    };

    return (new InterfaceOffer(options));
  }

	InterfaceOffer.include({
		initialize: function( data ) {
			var self = this;

			this.graphObject = ko.computed({
				deferEvaluation: true,
				read: function() {
					if ( !self.attributesLoaded() ) {
						self.loadAttributes( { async: false } );
					} else {
					}
					return self.graph().definition;
				},
				write: function( graphObject ) {
					var graph = _.clone( self.graph() );
					if ( !graph ) {
						graph = {};
					}

					if ( typeof graphObject === "string" ) {
						graph.definition = JSON.parse( graphObject );
					} else {
						graph.definition = graphObject;
					}

					self.graph( graph );
				}
			});

      this.interfaceSubjects = ko.computed({
				deferEvaluation: true,
				read: function() {
					var t,
          subjects = [];

					_( self.graphObject().process ).each(function( s ) {
            t = s.subjectType ? s.subjectType : s.type;
            if (t === "external" && s.externalType === "interface") {
              console.log(s.id.replace(/ß/, '\\u00d'));
              var imps = Implementation.findByFixedSubjectId('"' + s.id.replace(/ß/, '\\u00df') + '"');
              subjects.push({id: s.id, name: s.name, impCount: imps.length, imps: imps});
            }
					});

					return subjects;
				}
      });

      this.isImplemented = ko.computed({
				deferEvaluation: true,
				read: function() {
          var isubs = self.interfaceSubjects()
					return isubs.every(function(e) { console.log(e); return e.impCount > 0 });
				}
      });
		},

    getTemplate: function(sid) {
      var self = this,
          og   = self.graph(),
          ng   = JSON.parse(JSON.stringify(og.definition)),
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
            makeTau(n, s)
            return n;
          };

      // Make current subject a normal subj. and all others Interface subjects
      ps.forEach(function(s) {
        if (s.id === sid) {
          s.subjectType = "single";
          s.externalType = "";
        } else {
          s.subjectType = "external";
          s.externalType = "interface";
        }
      });

      // anonymize messages.
      var messages = []
      ps.forEach(function (s) {
        if (s.id == sid) {
          s.macros.forEach(function(m) {
            m.edges.forEach(function(e) {
              if( e.text ) {
                messages.push(e.text)
              }
            })
          })
        }
      });
      messages = _(messages).uniq()
      _(ng.messages).each(function(v, k) {
        if (!_(messages).contains(k)) {
          console.log("anonymizing meessage: " + k);
          ng.messages[k] = "Anonymized"
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
          })
        });
        return hasEdge? p : null;
      })).compact();

      // set all send or receive to / from deleted nodes to tau
      var remainingSubjects = ng.process.map(function(p) { return p.id })
      ps.forEach(function(s) {
        s.macros.forEach(function(m) {
          m.edges.forEach(function(e) {
            if (e.target && !_(remainingSubjects).contains(e.target.id)) {
              makeTauID(e.start, s)
              e.target = "";
              e.text = "tau";
            }
          })
        });
      });

      // set all non send / receive nodes and edges to tau
      ps.forEach(function(s) {
        s.macros.forEach(function(m) {
          m.nodes.forEach(function(n) {
            if (n.type !== "send" && n.type !== "receive") {
              makeTau(n, s);
            }
          })
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

      ps.forEach(function(s) {
        if ( s.id === sid) return;
        s.macros.forEach(function(m) {
          m.edges.forEach(function(e) {
            if (! e.target) {
              e.text = "tau";
            }
          })
        });
      });
      var interfaceSubjects = remainingSubjects.filter(function(id) { return sid !== id });

      return {
        definition: ng,
        id: og.id,
        offerId: self.id(),
        fixedSubjectId: sid,
        interfaceSubjects: interfaceSubjects
      };
    }
	});

  return InterfaceOffer;
});
