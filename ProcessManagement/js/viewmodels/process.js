define([
    "knockout",
    "app",
    "notify",
    "dialog",
    "models/process",
    "underscore",
    "router",
    "async",
    "models/user",
    "models/role",
    "models/interface",
    "knockout.chosen",
], function( ko, App, Notify, Dialog, Process, _, Router, async, User, Role, Interface ) {

    // The main viewmodel. Every observable defined inside can be used by the
    // view. Lets keep it clean and define functions and other helper variables
    // outside this viewmodel so it is immediately apparent which functions /
    // observables are really used by the view.
    var ViewModel = function() {
        var self = this;

        this.currentProcess = currentProcess;

        // The history of the graph. Used to let the user switch between different
        // revisions of a graph for the current process.
        // TODO: Not available in the new backend
        // this.graphHistory = graphHistory;

        // Needed for saving a process under a different name
        this.newProcessName = newProcessName;

        // Validation errors for saving a process under a different name
        this.processNameError = ko.computed(function() {
            if ( Process.nameAlreadyTaken( newProcessName() ) ) {
                return "Process name '" + newProcessName() + "' is not available.";
            } else {
                return "";
            }
        });

        window.existingInterfaces = this.existingInterfaces = ko.computed(function() {
		        return ko.utils.arrayFilter(Interface.all(), function(interf) {
			          return interf.interfaceType() == "interface";
		        });
	      });

        window.existingBlackboxes = this.existingBlackboxes = ko.computed(function(){
		        return ko.utils.arrayFilter(Interface.all(), function(x){
			          return x.interfaceType() == "blackboxcontent";
		        });
	      });

        // Needed for saving the business Interface
        this.newBusinessInterface = newBusinessInterface;
        this.newBusinessInterface().name("");
        this.newBusinessInterface().creator(App.currentUser().name());

        this.selectedInterface = window.selectedInterface = ko.observable();
        this.selectedInterfaceName = ko.computed(function() {
            if ( self.selectedInterface() ) {
                return self.selectedInterface().name();
            } else {
                return "";
            }
        });

        this.selectedInterfaceCreator = ko.computed(function() {
            if ( self.selectedInterface() ) {
                return self.selectedInterface().creator();
            } else {
                return "";
            }
        });

        this.selectedInterfaceId = ko.computed(function() {
            if ( self.selectedInterface() ) {
                return self.selectedInterface().id();
            } else {
                return -1;
            }
        });

        this.selectedInterfaceImplSubjects = ko.computed(function() {
            window.selectedInterface = self.selectedInterface();
            if ( self.selectedInterface() ) {
                return self.selectedInterface().implementedInterfaceSubjects();
            } else {
                return [];
            }
        });

        this.selectedInterfaceDescription = ko.computed(function() {
            if ( self.selectedInterface() ) {
                return self.selectedInterface().description();
            } else {
                return "";
            }
        });

        this.selectBusinessInterface = function() {
            var id = this.id();
            self.selectedInterface( Interface.find( id ) );
        };

        this.resetInterfaceSelection = function() {
            self.selectedInterface( null );
        };

        this.availableInterfaces = ko.computed(function() {
            if ( currentProcess().isNewRecord ){
                return [];
            }
            return _.chain(currentProcess().graph().definition.process).filter(function(subj) {
                return subj.type == "external" && subj.externalType == "interface"; // TODO: externalType == "blackbox" ?
            }).map(function( subj ) {
                return {
                    id: subj.id,
                    name: subj.name
                };
            }).value();
        });

        this.updatePublishInterface = function() {
            self.currentProcess().publishInterface( !self.currentProcess().publishInterface() );
        };

        this.interfaceInsertionSubject = ko.observable("");
        this.interfaceInsertionStrategy = ko.observable("insert");

        this.interfaceReplacementSubject = ko.observable("");

        this.loadBusinessInterface = function() {

            if ( !self.selectedInterface() ) return;
            if ( self.interfaceInsertionStrategy() === "insert" ) {
                var template = self.selectedInterface().getTemplate(self.interfaceInsertionSubject());
                currentProcess().insertTemplate(template);
            } else /*conditions...*/ {
                // TODO: implement...
                throw("inserting as new process is not yet implemented.");
            }

            loadGraph( currentProcess().graph() );
        };

        this.newBusinessInterfaceName = newBusinessInterface().name;
        this.newBusinessInterfaceAuthor = newBusinessInterface().creator;

        // Validation errors for saving a process under a different name
        this.businessInterfaceNameError = ko.computed(function() {
            if ( Interface.nameAlreadyTaken( newBusinessInterface().name() ) ) {
                return "Interface name '" + newBusinessInterface().name() + "' is not available.";
            } else {
                return "";
            }
        });

        // The text used for the currently assigned "role".
        // Is dependant on whether the process is a case or not.
        this.assignedRoleText = ko.computed(function() {
            return currentProcess().isCase() ? "Assigned User" : "Assigned Role";
        });

        this.serviceName = ko.observable("");
        this.serviceAuthor = ko.observable("");
        this.serviceSubject = ko.observable("");
        this.exportService = function () {
            var process = currentProcess().duplicate();
            process.interfaceId(undefined);

            var service = {
                version: 1,
                name: self.serviceName(),
                author: self.serviceAuthor(),
                subjectId: self.serviceSubject(),
                process: process
            };

            var exportString = JSON.stringify(service);
            var blob = new Blob([exportString], {type: "application/json;charset=" + document.characterSet} );
            window.saveAs( blob, "service_export_" + this.serviceName() + '.json' );
        };

        this.rolesOrUsers = ko.computed(function() {
            if ( currentProcess().isCase() ) {
                return User.all();
            } else {
                return Role.all();
            }
        });

        // List of available Processes (observable array)
        // Needed for the list of related Processes
        this.availableProcesses = Process.all;

        // Available Subjects, conversations and macros for display in chosen selects
        // at the top of the internal / interaction view
        this.availableSubjects = availableSubjects;
        this.mergeSubjects = mergeSubjects;
        this.mergeSubject = ko.observable(null);

        this.selectedSubjectType = selectedSubjectType;
        this.selectedSubjectId = selectedSubjectId;
        this.selectedSubjectText = selectedSubjectText;
        this.selectedSubjectRole = selectedSubjectRole;
        this.selectedInputPool = selectedInputPool;
        this.selectedBlackboxName = selectedBlackboxName;
        this.selectedRelatedProcess = selectedRelatedProcess;
        this.selectedRelatedSubject = selectedRelatedSubject;
        this.selectedExternalType = selectedExternalType;
        this.selectedSubjectComment = selectedSubjectComment;

        this.availableConversations = availableConversations;
        this.availableMacros   = availableMacros;

        /*
         * function to merge multiple subjects into one subject. Inserts old
         * macros into the new subject. This includes nodes and edges. Merges
         * macros if macros with the same name are present in both subjects.
         * TODO: Maybe only do this for the main macro?
         * Also adjusts edge relations for the new node count and sets new
         * target IDs and exchange origin/target IDs for edges coming from send
         * or receive nodes.
         * Also inserts variables from the old subject into the new merge
         * subject and adjusts variable ids in nodes / edges accordingly
         */
        this.mergeSubjectHandler = function() {
            // get graph object
            var graph = gv_graph.save();
            // index of the fromSubject in the process array
            var i = 0;
            var toSubjectId = self.mergeSubject();
            var fromSubjectId = self.selectedSubjectId();
            //  subjectMapping :: Map[SubjectId -> Subject]
            var subjectMapping = graph.process.reduce(function(mem, subject) {
                mem[subject.id] = subject;
                return mem;
            }, {});
            // copy the fromSubject to avoid modifying the nodes etc.
            var fromSub = JSON.parse(JSON.stringify(_(graph.process).find(function(s) {
                i += 1;
                return s.id == fromSubjectId;
            })));
            var toSub = _(graph.process).find(function(s) {
                return s.id == toSubjectId;
            });
            // create a object / hash / map from the macro id to the macro
            // object for easy access later on
            //  toSubMacros :: Map[MacroId -> Macro]
            var toSubMacros = toSub.macros.reduce(function(mem, val) {
                mem[val.id] = val;
                return mem;
            }, {});

            // add fromSubject to list of merged subjects
            toSub.mergedSubjects.push({
              id: fromSubjectId,
              name: fromSub.name
            });
            // set start subject
            toSub.startSubject = toSub.startSubject || fromSub.startSubject;

            // insert fromSub variables into the toSub variables and rename the
            // IDs to match the regular variable naming and avoid conflicts.
            // Also generate a mapping from old variable name to new varialbe
            // name for easier renaming when inserting the new edges.
            var variablesMapping = { "": "" };
            _(fromSub.variables).each(function(val, key) {
                var varKey = "v" + toSub.variables.length + 1;
                toSub.variables[varKey] = val;
                variablesMapping[key] = varKey;
            });
            // update variablesCounter to math the new count
            toSub.variableCounter += fromSub.variables.length;

            // for every subject that is not the target or from subject, modify
            // the target object of edges that belong to send or receive nodes
            // pointing to the fromSubject, to point to the toSubject and set
            // the exchange target IDs
            _(graph.process).filter(function(subject) {
                return !(subject.id === fromSubjectId || subject.id === toSubjectId);
            }).forEach(function(subject) {
                subject.macros.forEach(function(macro) {
                    macro.edges.forEach(function(edge) {
                        var nodeType = macro.nodes[edge.start].nodeType;
                        var target = edge.target;
                        if((nodeType === "send" || nodeType == "receive") &&
                           target && target.id === fromSubjectId) {
                            // only set exchangeTargetId if it is not already set.
                            if (!target.exchangeTargetId) {
                                target.exchangeTargetId = fromSubjectId;
                            }
                            target.id = toSubjectId;
                        }
                    });
                });
            });

            // INSERT MACROS FROM OLD SUBJECT TO NEW SUBJECT
            var macros = _(fromSub.macros).map(function(macro) {
                var nodeMapping = {};  // reset nodeapping for this macro
                // find macro in toSub if it exists
                var toSubMacro = _(toSub.macros).find(function(m) {
                    return m.id === macro.id;
                });
                // add process macro to the toSubject if a macro with this ID
                // does not exist. Also save a reference in the toSubMacros
                // object
                if (toSubMacro) {
                    toSubMacros[macro.id] = toSubMacro;
                } else {
                    toSub.macros.push(macro);
                    toSubMacros[macro.id] = macro;
                }
                // push new nodes to the toSubject macro object.
                // also adjust node IDs (index in the macros array). Also save
                // nodeMapping for this node, as the edges refer to nodes by
                // their index.
                macro.nodes.forEach(function(node) {
                    nodeMapping[node.id] = toSubMacros[macro.id].nodes.length + node.id;
                    node.id = nodeMapping[node.id];
                    toSubMacros[macro.id].nodes.push(node);
                });
                // for every edge, exchange start and end id with the new ids
                // obtained from the nodeMapping. Also set exchangeOriginId if
                // apropriate.
                macro.edges.forEach(function(edge) {
                    // if edge has a target and start node is a send node,
                    // set the exchangeOriginId, so the origin subject ID can be
                    // exchanged when sending messages. This is necessary to not
                    // confuse the target subject when receiving the message.
                    if (edge.target) {
                        if (macro.nodes[edge.start].type === "send") {
                            // only set origin id if it is not already set which
                            // could happen when merging multiple subjects.
                            if (!edge.target.exchangeOriginId) {
                                edge.target.exchangeOriginId = fromSub.id;
                            }
                        }
                    }
                    // Update variable ID
                    edge.variable = variablesMapping[edge.variable];
                    if (edge.target) {
                        edge.target.variable = variablesMapping[edge.target.variable];
                    }
                    edge.start = nodeMapping[edge.start];
                    edge.end = nodeMapping[edge.end];
                    toSubMacros[macro.id].edges.push(edge);
                });
            });

            // Basic bookkeeping. Adjust macro counter to (potentially) new
            // macro count
            toSub.macroCounter = toSub.macros.length;

            // remove the fromSub from the process graph
            graph.process.splice(i-1, 1);
            // load new graph in the view
            console.log(graph);
            window.oldGraph = gv_graph.save();
            window.newGraph = graph;
            loadGraph({ definition: graph });
        };

        // The currently selected subject and conversation (in chosen)
        this.currentSubject = currentSubject;
        this.currentSubjectName = ko.computed({
            deferEvaluation: true,
            read: function() {
                var subject =  _( availableSubjects() ).find(function( element ) {
                    return element['subjectId'] == currentSubject();
                });
                if ( subject ) {
                    return subject['subjectText'];
                } else return null;
            }
        });
        this.currentConversation = currentConversation;
        this.currentMacro   = currentMacro;

        // should certain elements of the right form be visible?
        this.isEdgeSelected = isEdgeSelected;
        this.isNodeSelected = isNodeSelected;
        this.selectedNodeHasPositionOffset = selectedNodeHasPositionOffset;
        this.selectedEdgeHasLabelPositionOffset = selectedEdgeHasLabelPositionOffset;
        this.isShowRoleWarning = isShowRoleWarning;
        this.isShowEdgeInformation = isShowEdgeInformation;

        // Save process methods
        this.saveCurrentProcess = saveCurrentGraph;
        this.saveCurrentProcessAs = function() {
            saveCurrentGraphAs( newProcessName() );
        };

        //Import and export the graph.

        this.exportGraph = function() {
            var graph = currentProcess().graphString();
            // graph = graph.replace(/"role":"[^"]+/g, "\"role\":\"");
            graph = graph.replace(/"routings":[^\]]+/g, "\"routings\":[");
            this.graphText(graph);

        };

        this.graphText = ko.observable("");

        this.importGraph = function() {
            currentProcess().graphString(this.graphText());
            loadGraph(currentProcess().graph());
        };

        this.uploadGraphDataClicked = function() {
            $('#graph-import-fileupload').click();
        };

        this.readUploadGraphData = function() {
            var file, reader,
            that = this,
            files = $('#graph-import-fileupload')[0].files;
            if( undefined !== files ) {
                file = files.item(0);
                reader = new FileReader();
                reader.onload = function(e){
                    that.graphText(e.target.result);
                };
                reader.readAsText(file);
            }
        };

        this.saveGraphDataClicked = function() {
            var blob = new Blob( [this.graphText()], {type: "application/json;charset=" + document.characterSet} );
            window.saveAs( blob, currentProcess().name() + '.json' );
        };

        this.clearGraphText = function() {
            this.graphText('');
        };

        this.goToRoot = function() {
            setGraph( currentProcess() );
            Router.goTo( currentProcess() );
        };

        this.resetProcess = function() {
            if ( confirm("Are you sure you want to reset this process to the last saved version? Doing so will reload the page and you will loose all unsaved changes.") ) {
                Router.setHasUnsavedChanges(false);
                var subject = gv_graph.selectedSubject;
                currentProcess().graphReset();
                loadGraph( currentProcess().graph() );
                currentSubject( subject );
                if ( subject ) {
                    gv_graph.selectedSubject = null;
                    gf_clickedCVnode( subject );
                    loadBehaviorView( subject );
                }
            }
        };

        this.goToRoutings = function() {
            Router.goTo("/processes/"+currentProcess().id()+"/routing");
        };

        // misc

        this.resetManualPositionOffsetNode = resetManualPositionOffsetNode;
        this.resetManualPositionOffsetEdgeLabel = resetManualPositionOffsetEdgeLabel;

        // Subscribe to all graph events we need to listen to.
        subscribeAll();
    };


    /***************************************************************************
     * Variable definitions for the viewmodel.
     * Current Subject, list of conversations, etc.
     **************************************************************************/

    /*
     *
     * The current Process.

     * Create a new Process (but do not save it yet) and let every other
     * observable (name, isCase etc.) reference this process.
     * That way everything is updated automatically.
     *
     * Example: processName = currentProcess().name()
     */
    var currentProcess = ko.observable( new Process() );

    var newProcessName = ko.observable("");

    var newBusinessInterface = ko.observable(new Interface());

    // Currently selected subject and conversation (in chosen)
    var currentSubject = ko.observable();
    var currentConversation = ko.observable();
    var currentMacro   = ko.observable();


    // Various observables to control whether or not to show certain
    // form fields in the internal view.
    var isEdgeSelected = ko.observable( false );
    var isNodeSelected = ko.observable( false );
    var selectedNodeHasPositionOffset = ko.observable(false);
    var selectedEdgeHasLabelPositionOffset = ko.observable(false);
    var isShowRoleWarning = ko.observable( true );
    var isShowEdgeInformation = ko.observable( false );

    // ko.observables for similar named attributes of the viewmodel.
    // Reference it outside the viewmodel so we don't have to declare every
    // function inside the viewmodel to get a reference to these objects.
    var availableSubjects = ko.observableArray([]);
    var availableConversations = ko.observableArray([]);
    var availableMacros = ko.observableArray([]);

    var selectedSubjectId = ko.observable(null);
    var selectedSubjectType = ko.observableArray([]);
    var selectedSubjectText = ko.observable(null);
    var selectedSubjectRole = ko.observable(null);
    var selectedInputPool = ko.observable(null);
    var selectedBlackboxName = ko.observable(null);
    var selectedRelatedProcess = ko.observable(null);
    var selectedRelatedSubject = ko.observable(null);
    var selectedExternalType = ko.observable(null);
    var selectedSubjectComment = ko.observable(null);

    var mergeSubjects = ko.computed(function() {
        var selSubId = selectedSubjectId();
        var res = _(availableSubjects()).filter(function(s) {
            return s.subjectId !== selSubId && s.subject.getType() === "single";
        });
        return res;
    });
    window.mergeSubjects = mergeSubjects;
    window.availableSubjects = availableSubjects;

    /***************************************************************************
     * Subscriptions to our observables. Used
     * For example to load the current Graph.
     ***************************************************************************/

    // On change of the available Subjects, let chosen know we updated the list
    // so it can do its magic and rebuild.
    // We need to do it in a timed (out) function because knockout needs some
    // time to rebuild the selects. 1ms is enough since we only need to wait for
    // the render process to become available again. As soon as this happens we
    // know knockout is done.
    availableSubjects.subscribe(function( subjects ) {
        setTimeout(function() {
            $("#slctSbj").trigger("liszt:updated");
        }, 1);
    });

    // Do basicly the same for the list of conversations (see above)
    availableConversations.subscribe(function( conversations ) {
        setTimeout(function() {
            $("#slctChan").trigger("liszt:updated");
        }, 1);
    });

    // Do basically the same for the list of macros (see above)
    availableMacros.subscribe(function( conversations ) {
        setTimeout(function() {
            $("#slctMacro").trigger("liszt:updated");
        }, 1);
    });


    // When a subject is clicked in chosen, go to the internal behavior of the
    // subject.
    currentSubject.subscribe(function( subject ) {
        var newRoute;

        // Do not do anything if an empty subject is selected.
        // Happens every time chosen updates itself with a new list of available
        // subjects.
        if ( subject ) {
            setGraph( currentProcess() );

            subject = decodeURIComponent(subject);
            var gv_subject = gv_graph.subjects[subject];
            if ( gv_subject && gv_subject.hasInternalBehavior()) {
                if ( !Router.goTo([ Router.modelPath( currentProcess() ), subject ]) ) {
                    // let the graph know we want to go to the internal view of a subject.
                    gv_graph.selectedSubject = null;
                    gf_clickedCVnode( subject );
                    loadBehaviorView( subject );
                }
            }
        }
    });

    // Subscribe to the change of the current Process.
    // Enables us to load new processes without reloading the entire viewmodel.
    //
    // Clears the graph, and loads the new graph for the new process.
    currentProcess.subscribe(function( process ) {
        var graph, isNewRecord;

        // If there is no graph associated to the process so far, it is probably a new
        // process that has not yet been loaded. In this case create a new Graph
        // and act on it dependant on whether it is a process, case or has been
        // created from a table input.
        if ( !process.graph() ) {

            // If it has been created from a table input, let the graph library do
            // the heavy lifting and create the graph.
            // If it is a case, just tell the graph lib. to create a new case with
            // default nodes already setup.
            // Otherwise just load an empty process graph..
            if ( process.isCreatedFromTable ) {
                // Clear the graph canvas
                gv_graph.clearGraph( true );
                gf_createFromTable( process.subjects, process.messages );
            } else if ( process.isCase() ) {
                // Clear the graph canvas
                gv_graph.clearGraph( true );
                gf_createCase( App.currentUser().name() );
                Router.goTo( Router.modelPath( currentProcess() ) + "/me" );
            } else {
                loadGraph( graph );
            }

            // in any case, save the graph.
            saveGraph( process );
        } else {
            // Graph already exists. Just load it.
            loadGraph( process.graph() );
        }
    });

    currentConversation.subscribe(function( conversation ) {
        if ( !conversation || !gf_getConversations()[ conversation ] ) {
            return;
        }
        gf_selectConversation( conversation );
    });

    currentMacro.subscribe(function( macro ) {
        if ( !macro || !gf_getMacros()[ macro ] ) {
            return;
        }
        gf_selectMacro( macro );
    });


    /***************************************************************************
     * Graph and Process helper functions
     ***************************************************************************/

    // Save the graph to the database.
    var saveGraph = function( process ) {

        // Load all changes into the process model.
        setGraph( process );
        process.isNewRecord = false;

        // Something is not right with lazy attributes... Need to set it twice -.-
        process.save(null, {
            success: function( textStatus ) {
                if (process.publishInterface()) {
                    console.log("Fetching new interfaces");
                    Interface.fetch();
                }
                Notify.info("Success", "Process '" + currentProcess().name() + "' has successfully been saved.");
                Router.setHasUnsavedChanges(false);
            },
            error: function( textStatus, error ) {
                Notify.error("Error", "Process '" + currentProcess().name() + "' could not be saved.\nFehler: " + error);
            }
        });
    };

    var setGraph = function( process ) {
        var routings;

        // save the routings attribute of the graph in a local variable because
        // it would be overwritten by setting the graph to the current
        // graph that is displayed via the gv_graph.saveToJSON() method.
        routings = process.routings();

        process.graphObject( gv_graph.saveToJSON() );
        if ( routings ) {
            process.routings( routings );
        }
    };

    // Saves the currently displayed graph to the database.
    var saveCurrentGraph = function( name ) {
        saveGraph( currentProcess() );
    };

    // Saves a duplicate of the current Graph under a given Name.
    // Duplicates the Process and Graph and changes the Name of the
    // duplicated Process.
    // After saving, load the newly created Process and Graph.
    var saveCurrentGraphAs = function( name ) {
        var process = currentProcess().duplicate();

        if ( name ) {
            process.name( name );
        }

        process.interfaceId(undefined);
        process.save(function() {
            Router.goTo( process );
        });

    };

    // Basic graph loading.
    // Just load the graph from a JSON String and display it.
    // no saving needed.
    var loadGraph = function( graph ) {
        // Clear the graph canvas
        gv_graph.clearGraph( true );
        if ( graph && graph.definition ) {
            gf_loadGraph( JSON.stringify( graph.definition ), undefined );
        } else {
            gf_loadGraph( "{}", undefined );
        }

        // TODO
        // var graph = JSON.parse(graphAsJson);
        // self.chargeVM.load(graph);

        selectTab( 2 );
    };

    // Loads a Process given the Id of the process.
    var loadProcessByIds = function( processId, subjectId, callback ) {

        if ( currentProcess().id() !== processId ) {
            currentProcess( Process.find( processId ) );
        }

        currentSubject( subjectId );

        if ( typeof callback === "function" ) {
            callback.call( this );
        }
    }

    var resetManualPositionOffsetNode = function(){
        gf_addManualPositionOffset(null, gv_graph.getSelectedNode(), 'node');
    }

    var resetManualPositionOffsetEdgeLabel = function(){
        gf_addManualPositionOffset(null, gv_graph.getSelectedEdge(), 'edgeLabel');
    }


    /***************************************************************************
     * Setup methods for DOM and tk_graph Listeners
     ***************************************************************************/

    var initializeDOM = function() {
        // Initialize our chosen selects for subjects and conversations.
        [ '#slctSbj', '#slctCon', '#slctMacro' ].forEach(function(id) {
            $(id).chosen({ 'disable_search_threshold': 10 });
        });

        $('.panel-heading').append('<span class="panel-show-hide pull-right">hide</span>');

        // Make internal settings screens toggle-able
        $('.panel-show-hide').click(function() {
            var panelbody = $(this).parent().next();
            panelbody.toggle();
            if (panelbody.is(":visible"))
                $(this).html("hide")
            else
                $(this).html("show");
        });
    }


    // Initialize listeners. These are either bound to the DOM (for click events
    // etc.), or listeners for the graph library.
    var initializeListeners = function() {
        // Show or hide the role warning. Show it when no Role has been selected
        // (empty val), otherwise hide it.
        $('#ge_cv_id').click(showOrHideRoleWarning);

        $('#internalClearBehavior').click(function() {
            Dialog.yesNo( 'Warning', 'Do you really want to clear the behavior?', function(){
                gv_graph.clearGraph();
                parent.$.fancybox.close();
            });
        });

        $('#UpdateSubjectButton, #DeleteSubjectButton, #AddSubjectButton').click(function() {
            Router.setHasUnsavedChanges(true);
            updateListOfSubjects();
        });

        $('#importGraphButtonAction').click(function(){
            Router.setHasUnsavedChanges(true);
        });

        var changeNodeButtonIds = "#CreateNodeButton, #InsertSendNodeButton, #InsertReceiveButton, #InsertActionNodeButton, #internalClearBehavior, #UpdateEdgeButton, #DeleteEdgeButton";
        $(changeNodeButtonIds).click(function() {
            Router.setHasUnsavedChanges(true);
        });

        // When a selectable tab is clicked, mark the tab as selected, update the
        // list of subjects and conversations.
        // See "selectTab" for more Information,
        $( ".switch .btn[id^='tab']" ).click(selectTab);

        // Save Process buttons behavior
        $( "#saveProcessAsButton" ).click(function() {
            $('#newProcessName').val( currentProcess().name() ).trigger('change');
            setTimeout(function() {
                $('#newProcessName').focus().select();
            }, 150);
        });

        // tool tips
        $('[data-toggle="tooltip"]').tooltip();

        $("#tab3").click(function() {
            gv_graph.selectedNode = null;
        });
    };

    /**
     * Checks if there are no subject. In case of that, the help-box will be shown until the user adds a subject
     */
    var tryShowSubjectHelpBox = function(){
        if(!gv_graph.getSubjectNames().length) {
            $('#process-subject-help').removeClass('invisible');
            var subscription = $.subscribe(gv_topics.general.subjects, function(data){
                if(data.action === 'add') {
                    $.unsubscribe(subscription);
                    $('#process-subject-help').addClass('invisible');
                }
            });
        }
    };

    var subscriptions = [];

    var subscribeAll = function() {
        subscriptions = [
            $.subscribeOnce( "tk_graph/updateListOfSubjects", updateListOfSubjects ),
            $.subscribeOnce( "tk_graph/displaySubjectHook", loadSelectedSubject ),
            $.subscribeOnce( "tk_graph/readSubjectHook", saveSelectedSubject ),
            $.subscribeOnce( "tk_graph/updateListOfMacros", updateListOfMacros ),
            $.subscribeOnce( "tk_graph/changeViewHook", viewChanged ),
            $.subscribeOnce( "tk_graph/changeViewBV", loadBehaviorView ),
            $.subscribeOnce( "tk_graph/subjectDblClickedInternal", currentSubject),
            $.subscribeOnce( "tk_graph/edgeClickedHook", showEdgeFields ),
            $.subscribeOnce( "tk_graph/nodeClickedHook", showNodeFields ),
            $.subscribeOnce( "tk_graph/subjectClickedHook", showOrHideRoleWarning ),
            $.subscribeOnce( "tk_graph/subjectDblClickedExternal", goToExternalProcess)
        ];
    };

    // Unsubscribe from all subscriptions that we subscribed to on
    // initialization.
    var unsubscribeAll = function() {
        _( subscriptions ).each(function( element, list ) {
            $.unsubscribe( element );
        });
    };


    /***************************************************************************
     * Directly view related methods
     ***************************************************************************/

    // Function to trigger the update of the list of subjects.
    // More or less a workaround for the graph library not supporting
    // knockout observables or knockout not supporting ES5 style getter / setter
    // methods.
    var updateListOfSubjects = function() {
        var subject,
        subjects = [];

        // Iterate over every subject available in the graph and build a nice
        // JS object from it. Than push this subject to the list of subjects.
        _( gv_graph.subjects ).each(function( value, key ) {

            // we don't want external subjects in the list of (local) subjects
            if ( !value.hasInternalBehavior()) {
                return;
            }

            // create the subject object
            subject = {
                subjectId: key,
                subjectText: value.getText(),
                subject: value
            };

            subjects.push( subject );
        });

        availableSubjects( subjects );
    };

    var loadSelectedSubject = function (subj) {
        selectedSubjectId(subj.id);
        selectedSubjectType([]);
        if (subj.isMulti()) selectedSubjectType.push("multi");
        if (subj.isExternal()) selectedSubjectType.push("external");
        if (subj.isStartSubject()) selectedSubjectType.push("start");
        selectedSubjectText(subj.getText());
        selectedSubjectRole(subj.getRole());
        selectedInputPool(subj.getInputPool());
        selectedBlackboxName(subj.getBlackboxname());
        selectedRelatedProcess(subj.getRelatedProcess());
        selectedRelatedSubject(subj.getRelatedSubject());
        selectedSubjectComment(subj.getComment());
        selectedExternalType(subj.getExternalType());
    };
    var saveSelectedSubject = function (subj) {
        subj.id = selectedSubjectId();
        if (_.contains(selectedSubjectType(), 'multi')) {
            if (_.contains(selectedSubjectType(), 'external'))
                subj.setType('multiexternal');
            else
                subj.setType('multi');
        } else {
            if (_.contains(selectedSubjectType(), 'external'))
                subj.setType('external');
            else
                subj.setType('single');
        }
        subj.setStartSubject(_.contains(selectedSubjectType(), 'start'));
        subj.setText(selectedSubjectText());
        subj.setRole(selectedSubjectRole());
        subj.setInputPool(selectedInputPool());
        subj.setBlackboxname(selectedBlackboxName());
        subj.setRelatedProcess(selectedRelatedProcess());
        subj.setRelatedSubject(selectedRelatedSubject());
        subj.setComment(selectedSubjectComment());
        subj.setExternalType(selectedExternalType());
    };

    var updateListOfMacros = function() {
        var macro,
        macros = [{}];

        _.chain( gf_getMacros() ).pairs().each(function( arr ) {
            if ( arr[0] === "length" ) {
                return
            }
            macro = {
                id: arr[0],
                value: arr[1]
            };
            macros.push( macro );
        });

        availableMacros( macros );
    };

    // Updates the list of conversations.
    var updateListOfConversations = function() {
        var conversation,
        conversations = [{}];

        // Iterate over every conversation available in the graph and build a nice
        // JS object from it. Than push this conversation to the list of conversations.
        _( gf_getConversations() ).each(function( value, key ) {
            conversation = {
                conversationId: key,
                text: value
            };

            conversations.push( conversation );
        });

        availableConversations( conversations );
    };

    var showEdgeFields = function() {
        setVisibleExclusive( isEdgeSelected );

        setTimeout(function () {
            var hasOffset = false,
            offset = gf_getManualPositionOffset(gv_graph.getSelectedEdge(), 'edgeLabel');
            if (offset && 'dx' in offset && 'dy' in offset) {
                hasOffset = offset.dx !== 0 || offset.dy !== 0;
            }
            selectedEdgeHasLabelPositionOffset(isEdgeSelected() && hasOffset);
        }, 60);
    };
    var showNodeFields = function() {
        setVisibleExclusive( isNodeSelected );

        setTimeout(function () {
            var hasOffset = false,
            offset = gf_getManualPositionOffset(gv_graph.getSelectedNode(), 'node');
            if (offset && 'dx' in offset && 'dy' in offset) {
                hasOffset = offset.dx !== 0 || offset.dy !== 0;
            }
            selectedNodeHasPositionOffset(isNodeSelected() && hasOffset);
        }, 60);
    };


    var updateMenuDropdowns = function() {
        updateListOfSubjects();
        updateListOfConversations();
        updateListOfMacros();

        currentMacro("##main##");
        currentConversation("##all##");
    };

    // Is called whenever the view changes from internal to external or vice
    // versa. The view argument can be either "cv" for the subject interaction
    // view or "bv" for the behavior aka internal view.
    var viewChanged = function( view ) {

        // start with every view being invisible since we should not have anything selected
        setVisibleExclusive();

        if ( view === "cv" ) {
            currentSubject( undefined );
        }

        // Always update the chosen selects since we don't know where we are going.
        updateMenuDropdowns();
    };

    // Sets the fields for the internal view to all be hidden except
    // for the observer passed in as function.
    //
    // So for example if every field except the Edge Fields should be hidden,
    // invoke it like: setVisibleExclusive( isEdgeSelected ).
    //
    // this marks the observer as false and therefore hides the fields.
    //
    // Can also be invoked without any arguments. Then everything will be hidden.
    var setVisibleExclusive = function( fn ) {
        isEdgeSelected( false );
        isNodeSelected( false );
        isShowEdgeInformation( false );

        if ( typeof fn === "function" ) {
            fn( true );
        }
    };

    var goToExternalProcess = function( process ) {
        console.log(process);
        Router.goTo( Process.find( process ) );
    };

    // Compute whether to show or hide the role warning.
    // Is based upon the selected role in subject settings.
    var showOrHideRoleWarning = function() {
        setTimeout(function() {
            isShowRoleWarning( !$( "#ge_cv_id" ).val() );
        }, 10);
    };

    // Method called when the graph view is changed to internal view.
    // Needs to be a separate function so we can potentially unsubscribe it
    // easily e.g. when the view is unloaded.
    var loadBehaviorView = function( subject ) {
        selectTab( 1 );
        gv_graph.selectedSubject = null;
        gf_clickedCVbehavior();
    };

    // Select a certain tab. Can either be directly attached to the click
    // listener (or anything else) of a specific tab, or invoked with the number
    // of the tab (id = "tab" + number) as first parameter.
    var selectTab = function( tabIndex ) {
        var tabId;

        // if the first parameter is no number, we assume this method has been
        // called by an event handler. We now need to extract the tabIndex from the
        // Id of the tab (current this object = tab node) that issued the event.
        if ( typeof tabIndex !== "number" ) {
            tabId = this.getAttribute("id");
            // set tabIndex to be the integer value of the last character of the Id
            // string. (to base 10).
            tabIndex = parseInt( tabId.substr( tabId.length - 1 ), 10 );
        }

        // Mark only the clicked tab as active, all other as inactive, hide all
        // current tab contents and only selectively show the tab content of the
        // currently clicked tab.
        $( ".tab_content" ).addClass( "hide" );
        $( "#switch" ).removeClass( "active" );
        [ 1, 2, 3 ].forEach(function(i) {
            $( "#tab" + i + "_content" ).removeClass( "active" );
        });
        $( "#tab" + tabIndex + "_content" ).addClass( "active" );
        $( "#instance_tab" + tabIndex + "_content" ).removeClass( "hide" );
        $( "#slctMacroDropDown" ).hide();

        // Hide the graph zoom Buttons and charge view. Show it in all other views.
        if ( tabIndex === 3 ) {
            $( ".zoombutton" ).hide();
        } else if( tabIndex === 2 ) {
            gv_graph.changeView('cv');
        } else {
            $( ".zoombutton" ).show();
            $( "#slctMacroDropDown" ).show();
        }

        updateMenuDropdowns();
    };


    /***************************************************************************
     * Initialization and unload methods
     ***************************************************************************/

    // Initialize our View.
    // Includes loading the template and creating the viewModel
    // to be applied to the template.
    var initialize = function( processId, subjectId, callback ) {
        var viewModel = new ViewModel();
        window.pView = viewModel;
        gv_interactionsEnabled = true;
        App.loadTemplate( "process", viewModel, null, function() {

            // Load all sub templates. They are:
            // Subject interaction view (tab 2)
            // Internal view (tab 1)
            // Routing View (tab 3)
            App.loadTemplates([
                [ "process/subject", "tab2_content" ],
                [ "process/internal", "tab1_content" ]
            ], viewModel, function() {

                // After all templates have been loaded and applied successfully,
                // set the current process and initialize the view Listeners.
                loadProcessByIds( processId, subjectId );

                initializeListeners();
                tryShowSubjectHelpBox();

                initializeDOM();

                // Execute the callback if any was given.
                // When this is called, everyting should be loaded and ready to go.
                if ( typeof callback === "function" ) {
                    callback.call( this );
                    updateMenuDropdowns();
                }
            });
        });
    };

    // This function gets called when another view is loaded.
    // At the moment, just unsubscribe all listeners we have set up that are not
    // bound to the DOM (and therefore do not get unsubscribed automatically).
    //
    // Must return true, otherwise the view will not be unloaded.
    var unload = function() {
        unsubscribeAll();
        gv_interactionsEnabled = false;
        return true;
    };

    // Everything in this object will be the public API
    return {
        init: initialize,
        loadProcessByIds: loadProcessByIds,
        unload: unload
    };
});
