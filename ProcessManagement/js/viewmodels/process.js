define([
	"knockout",
	"app",
	"notify",
	"dialog",
	"models/process",
	"underscore",
	"router"
	// "tk_graph"
], function( ko, App, Notify, Dialog, Process, _, Router ) {
	var ViewModel = function() {
		var self = this;

		// The current process Name
		this.processName = ko.observable("");

		this.currentProcess = currentProcess;

		this.assignedRoleText = ko.computed(function() {
			return currentProcess().isCase() ? "Assigned User" : "Assigned Role"
		});

		// List of available Processes (observable array)
		// Needed for the list of related Processes
		this.availableProcesses = Process.all;

		// Available Subjects, channels and macros for display in chosen selects
		// at the top of the internal / interaction view
		this.availableSubjects = availableSubjects;
		this.availableChannels = availableChannels;
		this.availableMacros   = availableMacros;

		// The currently selected subject and channel (in chosen)
		this.currentSubject = currentSubject;
		this.currentChannel = currentChannel;

		// should certain elements of the right form be visible?
		this.isEdgeSelected        = isEdgeSelected;
		this.isNodeSelected        = isNodeSelected;
		this.isShowRoleWarning     = isShowRoleWarning;
		this.isShowEdgeInformation = isShowEdgeInformation;
	}

	// ko.observables for similiar named attributes of the viewmodel.
	// Reference it outside the viewmodel so we dont have to declare every
	// function inside the viewmodel to get a reference to these objects.
	var availableSubjects = ko.observableArray([]);
	var availableChannels = ko.observableArray([]);
	var availableMacros   = ko.observableArray([]);

	// Currently selected subejct and channel (in chosen)
	var currentSubject = ko.observable();
	var currentChannel = ko.observable();

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

	// Do basicly the same for the list of channels (see above)
	availableSubjects.subscribe(function( channels ) {
		setTimeout(function() {
			$("#slctChan").trigger("liszt:updated");
		}, 1);
	});

	// When a subject is clicked in chosen, go to the internal behavior of the
	// subject.
	currentSubject.subscribe(function( subject ) {

		// Do not do anything if an empty subject is selected.
		// Happens every time chosen updates itself with a new list of available
		// subjects.
		if ( _.isEmpty( subject ) ) {
			return;
		}

		// let the graph know we want to go to the internal view of a subejct.
		gv_graph.selectedSubject = null;
		gf_clickedCVnode( subject );
		loadBehaviorView( subject );
	});

	// Function to trigger the update of the list of subjects.
	// More or less a workaround for the graph library not supporting
	// knockout observables or knockout not supporting ES5 style getter / setter
	// methods.
	var updateListOfSubjects = function() {
		var subject,
			subjects = [{}];

		// Iterate over every subject available in the graph and buld a nice
		// JS object from it. Than push this subject to the list of subjects.
		_( gv_graph.subjects ).each(function( value, key ) {

			// we dont want external subjects in the list of (local) subjects
			if ( value.isExternal() ) {
				return;
			}

			// create the subejct object
			subject = {
				subjectID: key,
				subjectText: value.getText(),
				subject: value
			}

			subjects.push( subject );
		})

		availableSubjects( subjects );
	}


	var updateListOfChannels = function() {
		var channel,
			channels = [{}];

		// Iterate over every channel available in the graph and buld a nice
		// JS object from it. Than push this channel to the list of channels.
		_( gf_getChannels() ).each(function( value, key ) {
			channel = {
				channelID: key,
				text: value
			}

			channels.push( channel );
		})

		availableChannels( channels );
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

			selectTab( 2 )

		} else {
			// If the process is a case, create a new case with our current user as
			// subject provider. Otherwise just create an empty graph.
			if ( process.isCase() ) {
				gf_createCase( App.currentUser().name() );
				selectTab( 1 );
			} else {
				gv_graph.loadFromJSON("{}");
				selectTab( 2 );
			}
			var graph = new Graph();
			process.graph( graph );

			graph.save();
			process.save();
		}

		// viewChanged()

		// Notify the user that the process has successfully been loaded
		Notify.info( "Information", "Process \""+ process.name() +"\" successfully loaded." );
	});


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

		// Show or hide the role warning. Show it when no Role has been selected
		// (empty val), otherwise hide it.
		$("#ge_cv_id").on( "change", showOrHideRoleWarning);

		$('#internalClearBehavior').on('click', function() {
			Dialog.yesNo( 'Warning', 'Do you really want to clear the behavior?', function(){
				gv_graph.clearGraph();
				parent.$.fancybox.close();
			});
		})

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
			selectTab( 2 );
			gv_graph.changeView('cv');
		});

		// Tab2, "Charge View" clicked.
		// let the graph not we changed views and update the list of subjects.
		$("#tab3").on( "click", function() {
			gv_graph.selectedNode = null;
		});

		$.subscribeOnce( "tk_communication/updateListOfSubjects", updateListOfSubjects );
		$.subscribeOnce( "tk_communication/changeViewHook", viewChanged );
		$.subscribeOnce( "gf_changeViewBV", loadBehaviorView );
		$.subscribeOnce( "gf_subjectDblClickedHook", currentSubject);
		$.subscribeOnce( "gf_edgeClickedHook", showEdgeFields );
		$.subscribeOnce( "gf_nodeClickedHook", showNodeFields );
		$.subscribeOnce( "gf_subjectClickedHook", showOrHideRoleWarning );
		$.subscribeOnce( "subjectDblClickedExternal", goToExternalProcess);
	}

	// Various observables to controll whether or not to show certain
	// formfields in the internal view.
	var isEdgeSelected        = ko.observable( false );
	var isNodeSelected        = ko.observable( false );
	var isShowRoleWarning     = ko.observable( true );
	var isShowEdgeInformation = ko.observable( false );

	var showEdgeFields = function() {
		setVisibleExclusive( isEdgeSelected );
	}
	var showNodeFields = function() {
		setVisibleExclusive( isNodeSelected );
	}

	// Is called whenenver the view changes from internal to external or vice
	// versa. The view argument can be either "cv" for the subject interaction
	// view or "bv" for the behavior aka internal view.
	var viewChanged = function( view ) {

		// start with every view beeing invisible since we should not have anything selected
		setVisibleExclusive();

		if ( view === "cv" ) {
			currentSubject( undefined );
		}

		// Always update the chosen selects since we don't know where we are going.
		updateListOfSubjects();
		updateListOfChannels();
	}

	// Sets the fields for the internal view to all be hidden exept
	// for the observer passed in as function.
	//
	// So for example if every field execpt the Edge Fields should be hidden,
	// invoke it like: setVisibleExclusive( isEdgeSelected ).
	//
	// this marks the observer as false and therfore hides the fields.
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

	var goToExternalProcess = function( sub ) {
		Router.goTo( Process.findByName( sub ) )
	}

	// Compute whether to show or hide the role warning.
	// Is based upon the selected role in subject settings.
	var showOrHideRoleWarning = function() {
		isShowRoleWarning( !$( "#ge_cv_id" ).val() );
	}

	// Method called when the graph view is changed to internal view.
	// Needs to be a separate function so we can potentially unsubscribe it
	// easily e.g. when the view is unloaded.
	var loadBehaviorView = function( subject ) {
		selectTab( 1 );
		gv_graph.selectedSubject = null;
		gf_clickedCVbehavior();
		updateListOfSubjects();
		updateListOfChannels();
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
		$( "#switch input" ).removeClass( "active" );
		$( "#tab" + tabIndex ).addClass( "active" );
		$( "#tab" + tabIndex + "_content" ).removeClass( "hide" );
		$( "#instance_tab" + tabIndex + "_content" ).removeClass( "hide" );

		updateListOfSubjects();
		updateListOfChannels();
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

				// Execute the callback if any was given.
				// When this is called, everyting should be loaded and ready to go.
				if ( typeof callback === "function" ) {
					callback.call( this );
					updateListOfSubjects();
					updateListOfChannels();
				}
			});
		});
	}

	// Unsubscreibe from all subscriptions thet we subscribed to on
	// initialization.
	var unsubscribeAll = function() {
		$.unsubscribe( "tk_communication/updateListOfSubjects", updateListOfSubjects );
		$.unsubscribe( "tk_communication/changeViewHook", viewChanged );
		$.unsubscribe( "gf_changeViewBV", loadBehaviorView );
		$.unsubscribe( "gf_subjectDblClickedHook", currentSubject);
		$.unsubscribe( "gf_edgeClickedHook", showEdgeFields );
		$.unsubscribe( "gf_nodeClickedHook", showNodeFields );
		$.unsubscribe( "gf_subjectClickedHook", showOrHideRoleWarning );
		$.unsubscribe( "subjectDblClickedExternal", goToExternalProcess);
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
		unsubscribeAll();

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
