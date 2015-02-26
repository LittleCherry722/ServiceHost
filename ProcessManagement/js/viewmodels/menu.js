define([
	"knockout",
	"models/process",
	"text!../../templates/menu.html",
	"jquery",
	"bootstrap.datepicker"
], function( ko, Process, menuTemplate, $ ) {

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
		$("#calendar").datepicker({
			todayHighlight: true,
			weekStart: 1,
		});

		$('[data-toggle=sidebar]').click(function() {
			var sidebar = $('#sidebar');
			if (sidebar.is(":visible"))
				sidebar.addClass('hidden');
			else
				sidebar.removeClass('hidden');
			$('[data-toggle=sidebar]').toggle();
		});
	}

	// Everything in this object will be the public API
	return {
		init: initialize,
		expandListOfProcesses: expandListOfProcesses
	}
});

