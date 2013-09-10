define([
	"knockout",
	"models/process",
	"text!../../templates/menu.html",
	"jquery.ui"
	// "jquery"
], function( ko, Process, menuTemplate ) {

	var viewModel = function() {
		// Where do we actually need this? Remove it?
		this.visible = ko.observable({
			home : true,
			process : true,
			save : false,
			saveAs : false,
			messages : true,
			execution : true
		});

		this.allProcesses = Process.all;
	}

	var expandListOfProcesses = function() {
		$('#processListLink').trigger( "click" )
	}
	
	// Initialize our menu.
	// Write the template content in our menuNode and
	// apply all bindings.
	var initialize = function() {
		// Insert the menu template in the page
		menuNode = document.getElementById('left_menu')
		menuNode.innerHTML = menuTemplate;
		ko.applyBindings(new viewModel(), menuNode)

		// Apply custom menu bar behavior
		setupMenu();
	}

	var setupMenu = function() {
		// Set up the main Menu accordion
		jQuery("#main_menu").accordion({
			autoHeight : false
		});

		// Make vanilla links in accordions possible
		jQuery("#main_menu a.link").live( 'click', function() {
			window.location = this.getAttribute('href');
		});

		jQuery("#calendar").datepicker({
			nextText : "&raquo;",
			prevText : "&laquo;"
		});

		jQuery("#hide_menu, #show_menu").click(function() {
			jQuery( "#left_menu, #show_menu" ).toggle();
			jQuery( "body" ).toggleClass("no-menu");
		});
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize,
		expandListOfProcesses: expandListOfProcesses
	}
});

