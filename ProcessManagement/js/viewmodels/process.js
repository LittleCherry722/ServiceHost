define([
	"knockout",
	"app",
	"notify",
	"router",
	"models/process",
	"async"
	// "tk_graph"
], function( ko, App, Notify, Router, Process, async ) {
	var ViewModel = function() {
		var self = this;

		// The current process Name
		this.processName = ko.observable("");

		this.currentProcess = currentProcess;

		this.assignedRoleText = ko.computed(function() {
			return currentProcess().isCase() ? "Assigned User" : "Assigned Role"
		});

		this.availableProcesses = Process.all;
	}

	var currentView = ko.observable();

	/*
	 * The current Process.

	 * Create a new Process (but do not save it yet) and let every other
	 * observable (name, isCase etc.) reference this process.
	 * That way everything is updated automatically.
	 *
	 * Example: processName = currentProcess().name()
	 */
	var currentProcess = ko.observable( new Process() );

	currentProcess.subscribe(function( process ) {
		gv_graph.clearGraph( true );
		
		if ( process.graph() ) {
			console.log( "loading existing graph" );
			gf_loadGraph( process.graph().graphString(), undefined );

			// var graph = JSON.parse(graphAsJson);
			// self.chargeVM.load(graph);

		} else {
			loadEmptyProcess( process );
		}

		$("#tab2").addClass("active");

		Notify.info("Information", "Process \""+ process.name() +"\" successfully loaded.");
	});

	var loadEmptyProcess = function( process ) {
		if ( process.isCase() ) {
			gf_createCase( App.currentUser().name() );
		} else {
			gv_graph.loadFromJSON("{}");
		}
	}

	var initializeListeners = function() {
		//resize canvas to fit into screen
		$("#graph_bv_outer").css("width", window.innerWidth - 170 - 245);
		$("#graph_bv_outer").css("height", window.innerHeight - 145);
		$("#show_menu").click(function() {
			$(window).trigger('resize');
		});
		$("#hide_menu").click(function() {
			$(window).trigger('resize');
		});
		$(window).resize(function() {
			if ($("#show_menu").css("display") == "none") {
				$("#graph_bv_outer").css("width", window.innerWidth - 170 - 245);
				$("#graph_bv_outer").css("height", window.innerHeight - 145);
			} else {
				$("#graph_bv_outer").css("width", window.innerWidth - 195);
				$("#graph_bv_outer").css("height", window.innerHeight - 185);
			}
		});

		$("#rightMenuTrigger").click(function() {
			if ($("#RightMenuDiv").is(":visible")) {
				$("#RightMenuDiv").hide();
				$("#rightMenuTrigger").html("Show")
			} else {
				$("#RightMenuDiv").show();
				$("#rightMenuTrigger").html("Hide")
			}
		});

		$("#internalRadioMenu :input").bind("change", function() {
			if ($("#ge_edge_type_timeout").is(":checked")) {

				$("#timeoutdiv").show();
			} else {
				$("#timeoutdiv").hide();
			}

		});

		//resize canvas to fit into screen
		$("#graph_bv_outer").css("width", window.innerWidth - 170 - 245);
		$("#graph_bv_outer").css("height", window.innerHeight - 145);
		$("#show_menu").click(function() {
			$(window).trigger('resize');
		});
		$("#hide_menu").click(function() {
			$(window).trigger('resize');
		});
		$(window).resize(function() {
			if ($("#show_menu").css("display") == "none") {
				$("#graph_bv_outer").css("width", window.innerWidth - 170 - 245);
				$("#graph_bv_outer").css("height", window.innerHeight - 145);
			} else {
				$("#graph_bv_outer").css("width", window.innerWidth - 195);
				$("#graph_bv_outer").css("height", window.innerHeight - 185);
			}
		});

		$("#rightMenuTrigger").click(function() {
			if ($("#RightMenuDiv").is(":visible")) {
				$("#RightMenuDiv").hide();
				$("#rightMenuTrigger").html("Show")
			} else {
				$("#RightMenuDiv").show();
				$("#rightMenuTrigger").html("Hide")
			}
		});

		$("#internalRadioMenu :input").bind("change", function() {
			if ($("#ge_edge_type_timeout").is(":checked")) {

				$("#timeoutdiv").show();
			} else {
				$("#timeoutdiv").hide();
			}

		});

		$("#slctSbj").chosen();

		$("#slctChan").chosen();

		$("#tab2").click(function() {
			console.log("tab2 clicked");

			$(this).parent().parent().find("td input").removeClass("active");
			$(this).addClass("active");
			$(".tab_content").addClass("hide");
			$("#tab2_content").removeClass("hide");
			gv_graph.changeView('cv');
			updateListOfSubjects();
			updateListOfChannels();

			SBPM.VM.contentVM().activeViewIndex(0);
		});

		$("#tab3").click(function() {
			console.log("tab3 clicked");

			$(this).parent().parent().find("td input").removeClass("active");
			$(this).addClass("active");
			$(".tab_content").addClass("hide");
			$("#tab3_content").removeClass("hide");

			gv_graph.selectedNode = null;
			updateListOfSubjects();
			$("#zoominbutton").hide();
			$("#zoomoutbutton").hide();
			$("#reset-button").hide();

			SBPM.VM.contentVM().activeViewIndex(2);
		});

		//resize canvas to fit into screen
		$("#graph_cv_outer").css("width", window.innerWidth - 170 - 245);
		$("#graph_cv_outer").css("height", window.innerHeight - 145);
		$("#show_menu").click(function() {
			$(window).trigger('resize');
		});
		$("#hide_menu").click(function() {
			$(window).trigger('resize');
		});
		$(window).resize(function() {
			if ($("#show_menu").css("display") == "none") {
				$("#graph_cv_outer").css("width", window.innerWidth - 170 - 245);
				$("#graph_cv_outer").css("height", window.innerHeight - 145);
			} else {
				$("#graph_cv_outer").css("width", window.innerWidth - 195);
				$("#graph_cv_outer").css("height", window.innerHeight - 185);
			}
		});

		$("#ge_cv_id").bind("change", function() {
			if ($("#ge_cv_id").val() == null || $("#ge_cv_id").val() == "") {

				$("#AssignRoleWarning").show();
			} else {
				$("#AssignRoleWarning").hide();
			}
		});
	}

	// Initialize our View.
	// Includes loading the template and creating the viewModel
	// to be applied to the template.
	var initialize = function( processID, callback ) {
		var viewModel = new ViewModel();

		App.loadTemplate( "process", viewModel, null, function() {

			App.loadTemplates([
				[ "process/subject", "tab2_content" ],
				[ "process/internal", "tab1_content" ]
			], viewModel, function() {
				currentProcess( Process.find( processID ) );
				initializeListeners();
			});

			if ( typeof callback === "function" ) {
				callback.call( this );
			}
		});
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize,
		currentProcess: currentProcess
	}
});
