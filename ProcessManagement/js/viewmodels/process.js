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
	"models/interface"
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

		window.existingInterfaces = this.existingInterfaces = Interface.all;

		// Needed for saving the business Interface
		this.newBusinessInterface = newBusinessInterface;
		this.newBusinessInterface().name("");
		this.newBusinessInterface().creator(App.currentUser().name());

		this.selectedInterface = ko.observable();
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

		this.selectedInterfaceDescription = ko.computed(function() {
			if ( self.selectedInterface() ) {
				return self.selectedInterface().description();
			} else {
				return "";
			}
		});

		this.selectBusinessInterface = function() {
			var id = this.id()
			self.selectedInterface( Interface.find( id ) );
		}

		this.noInterfaceSelected = ko.computed(function() {
			return !self.selectedInterface();
		});

		this.resetInterfaceSelection = function() {
			self.selectedInterface( null );
		}

		this.availableInterfaces = ko.computed(function() {
			if ( currentProcess().isNewRecord ){
				return [];
			}
			return _.chain(currentProcess().graph().definition.process).filter(function(subj) {
				return subj.type == "external" && subj.externalType == "interface"
			}).map(function( subj ) {
					return {
						id: subj.id,
						name: subj.name
					}
				}).value();
		});

		window.interfaceReplacementSubject = this.interfaceReplacementSubject = ko.observable("");

		this.loadBusinessInterface = function() {
			if ( !self.selectedInterface() ) return;

			var newGraph = JSON.parse(JSON.stringify(currentProcess().graph()));

			if ( self.interfaceReplacementSubject() ) {
				console.log(newGraph)
				newGraph.definition.process = _(newGraph.definition.process).map(function( subject ) {
					if (subject.id == self.interfaceReplacementSubject()) {
						return self.selectedInterface().graph();
					} else {
						return subject;
					}
				});
				console.log(newGraph)
			} else {
				newGraph.definition.process.push( self.selectedInterface().graph() )
			}

			loadGraph( newGraph );
		}

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
			return currentProcess().isCase() ? "Assigned User" : "Assigned Role"
		});

		this.saveBusinessInterface = function() {
      this.newBusinessInterface()
        .graph(currentProcess().associatedGraph(this.interfaceReplacementSubject()));
      this.newBusinessInterface().processId(currentProcess().id())
      this.newBusinessInterface().subjectId(this.interfaceReplacementSubject())

			this.newBusinessInterface().save({}, {
				success: function() {
					Notify.info("Success", "Business Interface '" +
						this.currentProcess().name() + "' has successfully been made public.");

					this.newBusinessInterface(new Interface({
						name: "",
						creator: App.currentUser().name()
					}));
				},
				error: function() {
					// TODO: real error handling
					console.log("Something bad happened..");
				}
			});
		}

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
		this.availableConversations = availableConversations;
		this.availableMacros   = availableMacros;

		// The currently selected subject and conversation (in chosen)
		this.currentSubject = currentSubject;
		this.currentSubjectName = ko.computed({
			deferEvaluation: true,
			read: function() {
				var subject =  _( availableSubjects() ).find(function( element ) {
					return element['subjectId'] == currentSubject();
				});
				if ( subject ) {
					return subject['subjectText']
				}
			}
		});
		this.currentConversation = currentConversation;
		this.currentMacro   = currentMacro;

		// should certain elements of the right form be visible?
		this.isEdgeSelected        = isEdgeSelected;
		this.isNodeSelected        = isNodeSelected;
		this.isShowRoleWarning     = isShowRoleWarning;
		this.isShowEdgeInformation = isShowEdgeInformation;

		// Save process methods
		this.saveCurrentProcess = saveCurrentGraph;
		this.saveCurrentProcessAs = function() {
			saveCurrentGraphAs( newProcessName() );
		}

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
			$.fancybox.close();
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
			setGraph( currentProcess() )
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
		}

		this.goToRoutings = function() {
			Router.goTo("/processes/"+currentProcess().id()+"/routing");
		}

		// Subscribe to all graph events we need to listen to.
		subscribeAll()
	}


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
	var isEdgeSelected        = ko.observable( false );
	var isNodeSelected        = ko.observable( false );
	var isShowRoleWarning     = ko.observable( true );
	var isShowEdgeInformation = ko.observable( false );

	// ko.observables for similar named attributes of the viewmodel.
	// Reference it outside the viewmodel so we don't have to declare every
	// function inside the viewmodel to get a reference to these objects.
	var availableSubjects      = ko.observableArray([]);
	var availableConversations = ko.observableArray([]);
	var availableMacros        = ko.observableArray([]);

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
			setGraph( currentProcess() )

			subject = subject.replace(/___/, " ");
			var gv_subject = gv_graph.subjects[subject]
			if ( gv_subject && ( !gv_subject.isExternal() || gv_subject.externalType == "interface" )) {
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
				Router.goTo( Router.modelPath( currentProcess() ) + "/me" )
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
	})

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
				Notify.info("Success", "Process '" + currentProcess().name() + "' has successfully been saved.");
				Router.setHasUnsavedChanges(false);
			},
			error: function( textStatus, error ) {
				Notify.error("Error", "Process '" + currentProcess().name() + "' could not be saved.");
			}
		});
	}

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
	}

	// Saves the currently displayed graph to the database.
	var saveCurrentGraph = function( name ) {
		saveGraph( currentProcess() );
	}

	// Saves a duplicate of the current Graph under a given Name.
	// Duplicates the Process and Graph and changes the Name of the
	// duplicated Process.
	// After saving, load the newly created Process and Graph.
	var saveCurrentGraphAs = function( name ) {
		var process = currentProcess().duplicate();

		if ( name ) {
			process.name( name );
		}

		process.save(function() {
			Router.goTo( process );
		});

	}


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

		selectTab( 2 )
	}

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


	/***************************************************************************
	 * Setup methods for DOM and tk_graph Listeners
	 ***************************************************************************/

	var initializeDOM = function() {
		// Initialize our chosen selects for subjects and conversations.
		$( "#slctSbj" ).chosen();
		$( "#slctCon" ).chosen();
		$( "#slctMacro" ).chosen();

		// fancybox
		$('#exportGraphButton').fancybox();
		$('#importGraphButton').fancybox();
	}


	// Initialize listeners. These are either bound to the DOM (for click events
	// etc.), or listeners for the graph library.
	var initializeListeners = function() {

		// Make internal settings screens toggle-able
		$( ".processSettingsTrigger" ).live( "click", function() {
			if ($(this).parent().next().is( ":visible" )) {
				$(this).closest( "fieldset" ).addClass( "hidden" );
				$(this).html( "Show" ).addClass( "show" );
			} else {
				$(this).closest( "fieldset" ).removeClass( "hidden" );
				$(this).html( "Hide" )
			}
		});

		// Show or hide the role warning. Show it when no Role has been selected
		// (empty val), otherwise hide it.
		$( "#ge_cv_id" ).live( "change", showOrHideRoleWarning);

		$( '#internalClearBehavior' ).live( "click", function() {
			Dialog.yesNo( 'Warning', 'Do you really want to clear the behavior?', function(){
				gv_graph.clearGraph();
				parent.$.fancybox.close();
			});
		});

		var updateSubjectIds = "#UpdateSubjectButton, #DeleteSubjectButton, #AddSubjectButton";
		$(updateSubjectIds).live( "click", function() {
			Router.setHasUnsavedChanges(true);
			updateListOfSubjects();
		});

		$('#importGraphButtonAction').click(function(){
			Router.setHasUnsavedChanges(true);
		});

		var changeNodeButtonIds = "#CreateNodeButton, #InsertSendNodeButton, #InsertReceiveButton, #InsertActionNodeButton, #internalClearBehavior, #UpdateEdgeButton, #DeleteEdgeButton";
		$(changeNodeButtonIds).live( "click", function() {
			Router.setHasUnsavedChanges(true);
		});

		// When a selectable tab is clicked, mark the tab as selected, update the
		// list of subjects and conversations.
		// See "selectTab" for more Information,
		$( ".switch .btn[id^='tab']" ).live( "click", selectTab )

		// Save Process buttons behavior
		$( "#saveProcessAsButton" ).live( "click", function() {
			$('#newProcessName').val( currentProcess().name() ).trigger('change');
			setTimeout(function() {
				$('#newProcessName').focus().select();
			}, 150);
		});

		// tool tips
		$('.tooltip-enabled *[title]').tooltip();

		$("#tab3").live( "click", function() {

			gv_graph.selectedNode = null;
		});
	}

    /**
     * Checks if there are no subject. In case of that, the help-box will be shown until the user adds a subject
     */
	var tryShowSubjectHelpBox = function(){
        if(gv_graph.getSubjectNames().length == 0) {
            $('#process-subject-help').removeClass('invisible');

            var svgInterval = setInterval(function(){
                var svg = $('#processContent svg'),
                    checkHideSubjectHelp = function () {
                        if(gv_graph.getSubjectNames().length > 0) {
                            $('#process-subject-help').addClass('invisible');
                            svg.off('DOMSubtreeModified');
                        }
                    };
                if(svg.length > 0){
                    svg.on('DOMSubtreeModified', checkHideSubjectHelp);
                    clearInterval(svgInterval);
                    checkHideSubjectHelp();
                }
            }, 200);
        }
	}

	var subscriptions = [];

	var subscribeAll = function() {
		subscriptions = [
			$.subscribeOnce( "tk_graph/updateListOfSubjects", updateListOfSubjects ),
			$.subscribeOnce( "tk_graph/updateListOfMacros", updateListOfMacros ),
			$.subscribeOnce( "tk_graph/changeViewHook", viewChanged ),
			$.subscribeOnce( "tk_graph/changeViewBV", loadBehaviorView ),
			$.subscribeOnce( "tk_graph/subjectDblClickedInternal", currentSubject),
			$.subscribeOnce( "tk_graph/edgeClickedHook", showEdgeFields ),
			$.subscribeOnce( "tk_graph/nodeClickedHook", showNodeFields ),
			$.subscribeOnce( "tk_graph/subjectClickedHook", showOrHideRoleWarning ),
			$.subscribeOnce( "tk_graph/subjectDblClickedExternal", goToExternalProcess)
		]
	}

	// Unsubscribe from all subscriptions that we subscribed to on
	// initialization.
	var unsubscribeAll = function() {
		_( subscriptions ).each(function( element, list ) {
			$.unsubscribe( element );
		});
	}


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
			if ( value.isExternal() && !value.externalType == "Interface" ) {
				return;
			}

			// create the subject object
			subject = {
				subjectId: key,
				subjectText: value.getText(),
				subject: value
			}

			subjects.push( subject );
		})

		availableSubjects( subjects );
	}

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
			}
			macros.push( macro );
		});

		availableMacros( macros );
	}

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
			}

			conversations.push( conversation );
		})

		availableConversations( conversations );
	}

	var showEdgeFields = function() {
		setVisibleExclusive( isEdgeSelected );
	}
	var showNodeFields = function() {
		setVisibleExclusive( isNodeSelected );
	}



	var updateMenuDropdowns = function() {
		updateListOfSubjects();
		updateListOfConversations();
		updateListOfMacros();

		currentMacro("##main##");
		currentConversation("##all##");
	}

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
	}

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
	}

	var goToExternalProcess = function( process ) {
    console.log(process)
		Router.goTo( Process.find( process ) )
	}

	// Compute whether to show or hide the role warning.
	// Is based upon the selected role in subject settings.
	var showOrHideRoleWarning = function() {
		setTimeout(function() {
			isShowRoleWarning( !$( "#ge_cv_id" ).val() );
		}, 10)
	}

	// Method called when the graph view is changed to internal view.
	// Needs to be a separate function so we can potentially unsubscribe it
	// easily e.g. when the view is unloaded.
	var loadBehaviorView = function( subject ) {
		selectTab( 1 );
		gv_graph.selectedSubject = null;
		gf_clickedCVbehavior();
	}

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
		$( "#switch .btn" ).removeClass( "active" );
		$( "#tab" + tabIndex ).addClass( "active" );
		$( "#tab" + tabIndex + "_content" ).removeClass( "hide" );
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
	}


	/***************************************************************************
	 * Initialization and unload methods
	 ***************************************************************************/

	var initialized = false;

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
				loadProcessByIds( processId, subjectId )

				if ( !initialized ) {
					initialized = true;
					initializeListeners();
				}
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
	}

	// This function gets called when another view is loaded.
	// At the moment, just unsubscribe all listeners we have set up that are not
	// bound to the DOM (and therefore do not get unsubscribed automatically).
	//
	// Must return true, otherwise the view will not be unloaded.
	var unload = function() {
		unsubscribeAll();
        gv_interactionsEnabled = false;
		return true;
	}

	var showHelp = function() {
		$('#main').chardinJs('start');
	}

	// Everything in this object will be the public API
	return {
		init: initialize,
		loadProcessByIds: loadProcessByIds,
		showHelp: showHelp,
		unload: unload
	}
});
