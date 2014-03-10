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
    "underscore"
], function( ko, Model, _ ) {

    // Our main model that will be returned at the end of the function.
    //
    // Process is responsivle for everything associated with processes directly.
    //
    // For example: Getting a list of all processes, savin a process,
    // validating the current process etc.
    Process = Model( "Process" );

    Process.attrs({
        name: "string",
        isCase: "boolean",
        processInstanceId: "integer",
        startAble: "boolean",
        graph: {
            type: "json",
            defaults: {
                routings: [],
                definition: {
                    conversationCounter: 1,
                    conversations: {},
                    messageCounter: 0,
                    messages: {},
                    nodeCounter: 0,
                    process: []
                }
            },
            lazy: true
        }
    });

    Process.enablePolling();

    Process.hasMany( "processInstances" );

    Process.include({

        // Initialize is a special method defined as an instance method.  If any
        // method named "initializer" is given, it will be called upon object
        // creation (when calling new model()) with the context of the model.
        // That is, "this" refers to the model itself.
        // This makes it possible to define defaults for attributes etc.
        initialize: function( data ) {
            var self = this;

            this.tableSubjects = []
            this.tableMessages = [];
            this.isCreatedFromTable = false;

            this.menuName = ko.computed(function() {
                if ( self.isCase() ) {
                    return "[C] " + self.name();
                } else {
                    return "[P] " + self.name();
                }
            });

            this.instanceCount = ko.computed({
                deferEvaluation: true,
                read: function() {
                    return self.processInstances().length
                }
            });

            this.hasInstances = ko.computed({
                deferEvaluation: true,
                read: function() {
                    return self.instanceCount() > 0;
                }
            });

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

            this.subjects = ko.computed({
                deferEvaluation: true,
                read: function() {
                    var subjects = {};

                    _( self.graphObject().process ).each(function( element ) {
                        subjects[ element['id'] ] = element['name'];
                    });

                    return subjects;
                }
            });

            this.associatedGraph = function( subjectId ) {
                return self.graphObject().process.filter(function( s ) {
                    return s.macros.some(function( m ) {
                        return m.edges.some(function( e ) {
                            return e.target && e.target.id === subjectId;
                        });
                    });
                })[0];
            };

            this.graphForSubject = function( subjectId ) {
                return self.graphObject().process.filter(function( subject ) {
                    return subject.id === subjectId;
                })[0];
            };

            this.subjectsArray = ko.computed({
                deferEvaluation: true,
                read: function() {
                    var subjects = [];

                    _( self.graphObject().process ).each(function( element ) {
                        subjects.push( [ element['id'], element['name'] ] );
                    });

                    return subjects;
                }
            });

            this.subjectIds = ko.computed({
                deferEvaluation: true,
                read: function() {
                    var subjects = [];

                    _( self.graphObject().process ).each(function( element ) {
                        subjects.push( element['id'] );
                    });

                    return subjects;
                }
            });

            this.routings = ko.computed({
                deferEvaluation: true,
                read: function() {
                    if ( self.graph() && self.graph().routings ) {
                        return self.graph().routings;
                    } else {
                        return [];
                    }
                },
                write: function( routings ) {
                    if ( !routings ) {
                        routings = [];
                    }
                    var graph = _.clone( self.graph() );
                    graph.routings = routings;
                    self.graph( graph );
                }
            });

            this.graphString = ko.computed({
                deferEvaluation: true,
                read: function() {
                    if ( self.graphObject() ) {
                        return JSON.stringify( self.graphObject() );
                    } else {
                        return {};
                    }
                },
                write: function( graphString ) {
                    var graph = self.graph();
                    graph.definition = JSON.parse( graphString );
                    self.graph( graph );
                }
            });
        },

        // Custom validator object. Validators are (like the initialize function)
        // special in a sense that this object will be iterated over when the
        // "validate" method is executed.
        validators: {
            // Does this Process already exist?
            exists: function() {
                if ( Process.nameAlreadyTaken( this.name() ) ) {
                    return "Process already exists! Please choose a different name.";
                }
            },

            // Does this process have a valid name?
            isNameInvalid: function() {
                if ( this.name().length < 2 ) {
                    return "Process name is Invalid. Process name must have at least two characters.";
                }
            }
        }
    });

    Process.nameAlreadyTaken = function( name ) {
        // var json,
        //   data = {
        //     name: name,
        //     action: "getid"
        //   }
        // $.ajax({
        //   url: 'db/process.php',
        //   data: data,
        //   cache: false,
        //   type: "POST",
        //   async: false,
        //   success: function( data ) {
        //     json = JSON.parse( data );
        //   }
        // });
        // if ((json["code"] == "added") || (json["code"] == "ok")) {
        //   return json["id"] > 0;
        // } else {
        //   return false;
        // }
        return false;
    };

    return Process;
});
