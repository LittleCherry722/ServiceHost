define([
	"knockout",
	"jquery",
	"models/process",
	"jade!../../templates/menu",
	"jquery.ui"
], function( ko, $, Process, menuTemplate ) {

	var allProcesses = function() {
		return Process.all();
	}

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

		this.allProcesses = ko.observableArray(allProcesses());
	}

	
	// Initialize our menu.
	// Write the template content in our menuNode and
	// apply all bindings.
	var initialize = function() {
		// Insert the menu template in the page
		menuNode = document.getElementById('left_menu')
		menuNode.innerHTML = menuTemplate();
		ko.applyBindings(new viewModel(), menuNode)

		// Apply custom menu bar behavior
		setupMenu();
	}

	var setupMenu = function() {
		$("#main_menu").accordion({
			autoHeight : false
		});

		$("#calendar").datepicker({
			nextText : "&raquo;",
			prevText : "&laquo;"
		});

		$("#hide_menu, #show_menu").click(function() {
			$( "#left_menu, #show_menu" ).toggle();
			$( "body" ).toggleClass("no-menu");
		});
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});

