define([
	"knockout",
	"app",
	"notify",
	"models/process",
	"underscore"
	// "tk_graph"
], function( ko, App, Notify, Process, _ ) {
	var ViewModel = function() {
		var self = this;

		// The current process Name
		this.processName = ko.observable("");

		this.currentProcess = currentProcess;

		this.assignedRoleText = ko.computed(function() {
			return currentProcess().isCase() ? "Assigned User" : "Assigned Role"
		});

		this.availableProcesses = Process.all;

		this.availableSubjects = availableSubjects;
		this.availableChannels = availableChannels;
		this.availableMacros   = availableMacros;

		this.currentSubject = currentSubject;
	}

	var availableSubjects = ko.observableArray([]);
	var availableChannels = ko.observableArray([]);
	var availableMacros   = ko.observableArray([]);

	var currentSubject = ko.observable();
	var currentChannel = ko.observable();

	availableSubjects.subscribe(function( subjects ) {
		setTimeout(function() {
			$("#slctSbj").trigger("liszt:updated");
		}, 1);
	});

	currentSubject.subscribe(function( subject ) {
		console.log(subject)

		if ( _.isEmpty( subject ) ) {
			return;
		}
		
		console.log("going to internal behavior")

		gv_graph.selectedSubject = null;
		gf_clickedCVnode( subject );
		onChangeViewBV();
	});

	var updateListOfSubjects = function() {
		var subject,
			subjects = [{}];
		console.log("list of subjects")

		_( gv_graph.subjects ).each(function( value, key ) {
			subject = {
				subjectID: key,
				subjectText: value.getText(),
				subject: value
			}

			subjects.push( subject );
		})

		availableSubjects( subjects );
	}

	/*
	 * The current Process.

	 * Create a new Process (but do not save it yet) and let every other
	 * observable (name, isCase etc.) reference this process.
	 * That way everything is updated automatically.
	 *
	 * Example: processName = currentProcess().name()
	 */
	var currentProcess = ko.observable( new Process() );

	// Subscribe to the change of the current Process.
	// Enables us to load new processes without reloading the entire viewmodel.
	//
	// Clears the graph, and loads the new graph for the new process.
	currentProcess.subscribe(function( process ) {
		gv_graph.clearGraph( true );
		
		// If the process already has an associated graph load it.
		// Otherwise load an empty graph from the process (or case).
		if ( process.graph() ) {
			gf_loadGraph( process.graph().graphString(), undefined );

			// TODO
			// var graph = JSON.parse(graphAsJson);
			// self.chargeVM.load(graph);

		} else {
			loadNewProcess( process );
		}

		// Make Tab 2 (Subject interaction view) the active Tab.
		selectTab( 2 );

		// Notify the user that the process has successfully been loaded
		Notify.info("Information", "Process \""+ process.name() +"\" successfully loaded.");
	});

	// method to load an empty process.
	// A process is "empty", if it has no associated graph.
	var loadNewProcess = function( process ) {

		// If the process is a case, create a new case with our current user as
		// subject provider. Otherwise just create an empty graph.
		if ( process.isCase() ) {
			gf_createCase( App.currentUser().name() );
		} else {
			gv_graph.loadFromJSON("{}");
		}
	}

	// Initialize listeners. These are either bound to the DOM (for click events
	// etc), or listeners for the graph library.
	var initializeListeners = function() {

		// Make internal settings screens toggle-able
		$("#rightMenuTrigger").click(function() {
			if ($("#RightMenuDiv").is(":visible")) {
				$(this).closest("fieldset").addClass('hidden');
				$("#rightMenuTrigger").html("Show").addClass("show");
			} else {
				$(this).closest("fieldset").removeClass('hidden');
				$("#rightMenuTrigger").html("Hide")
			}
		});

		// TODO no idea what this is about. anyone?
		$("#internalRadioMenu :input").bind("change", function() {
			if ($("#ge_edge_type_timeout").is(":checked")) {
				$("#timeoutdiv").show();
			} else {
				$("#timeoutdiv").hide();
			}
		});

		// TODO no idea what this is about. anyone?
		$("#ge_cv_id").on( "change", function() {
			if ( !$( "#ge_cv_id" ).val() ) {
				$( "#AssignRoleWarning" ).show();
			} else {
				$( "#AssignRoleWarning" ).hide();
			}
		});


		// Initialize our chosen selects for subjects and channels.
		$("#slctSbj").chosen();
		$("#slctChan").chosen();

		// When a selectable tab is clicked, mark the tab as selected, update the
		// list of subjects and channels.
		// See "selectTab" for more Information,
		$(".switch input[id^='tab']").on( "click", selectTab )

		// Tab2, "Subject Interaction View" clicked.
		// let the graph not we changed views and update the list of subjects and
		// channels.
		$("#tab2").on( "click", function() {
			gv_graph.changeView('cv');

			// Update our chosen selects.
			updateListOfChannels();
		});

		// Tab2, "Charge View" clicked.
		// let the graph not we changed views and update the list of subjects.
		$("#tab3").on( "click", function() {
			gv_graph.selectedNode = null;
		});

		$.subscribeOnce("gf_changeViewBV", onChangeViewBV);
		$.subscribeOnce("tk_communication/updateListOfSubjects", updateListOfSubjects);
	}

	// Method called when the graph view is changed to internal view.
	// Needs to be a separate function so we can potentially unsubscribe it
	// easily e.g. when the view is unloaded.
	var onChangeViewBV = function() {
		selectTab( 1 );
		gf_clickedCVbehavior();
	}

	// Select a certain tab. Can either be directly attacthed to the click
	// listener (or anything else) of a secific tab, or invoked with the number
	// of the tab (id = "tab" + number) as first parameter.
	var selectTab = function( tabIndex ) {
		var tabID;

		// if the first parameter is no number, we assume this method has been
		// called by an event handler. We now need to extract the tabIndex from the
		// ID of the tab (current this object = tab node) that issued the event.
		if ( typeof tabIndex !== "number" ) {
			tabID = this.getAttribute("id");
			// set tabIndex to be the integer value of the last character of the ID
			// string. (to base 10).
			tabIndex = parseInt( tabID.substr( tabID.length - 1 ), 10 );
		}

		// Hide the graph zoom Buttons n charge view. Show it in all other views.
		if ( tabIndex === 3 ) {
			$( ".zoombutton" ).hide();
		} else {
			$( ".zoombutton" ).show();
		}

		// Mark only the clicked tab as active, all other as inactive, hide all
		// current tab contents and only selectively show the tab content of the
		// currently clicked tab.
		$( ".tab_content" ).addClass( "hide" );
		$( ".switch input" ).removeClass( "active" );
		$( "#tab" + tabIndex ).addClass( "active" );
		$( "#tab" + tabIndex + "_content" ).removeClass( "hide" );
		$( "#instance_tab" + tabIndex + "_content" ).removeClass( "hide" );

		updateListOfSubjects();
	}

	// Initialize our View.
	// Includes loading the template and creating the viewModel
	// to be applied to the template.
	var initialize = function( processID, callback ) {
		var viewModel = new ViewModel();

		App.loadTemplate( "process", viewModel, null, function() {

			// Load all sub templates. They are:
			// Subject interaction view (tab 2)
			// Internal view (tab1)
			// Charge View (tab 3
			App.loadTemplates([
				[ "process/subject", "tab2_content" ],
				[ "process/internal", "tab1_content" ]
			], viewModel, function() {

				// After all templates have been loaded and applied successfully,
				// set the current process and initialize the view Listeners.
				currentProcess( Process.find( processID ) );
				initializeListeners();
			});

			// Execute the callback if any was given.
			if ( typeof callback === "function" ) {
				callback.call( this );
			}
		});
	}

	// This function gets called when another view is loaded.
	// At the moment, just unsubscribe all listeners we have set up that are not
	// bound to the DOM (and therefore do not get unsubscribed automatically).
	//
	// We should use it in the future to stop the view from unloading when
	// unsaved changes are detected.
	//
	// Must return true, otherwise the view will not be unloaded.
	var unload = function() {
		$.subscribeOnce("gf_changeViewBV", onChangeViewBV);

		// return true so the view actually gets unloaded.
		return true;
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize,
		currentProcess: currentProcess,
		unload: unload
	}
});
