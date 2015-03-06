define([
	"knockout",
	"models/process",
	"text!../../templates/menu.html",
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
		$("#calendar").datepicker({
			todayHighlight: true,
			weekStart: 1,
		});

		$('[data-toggle=left_menu]').click(function() {
			var left_menu = $('#left_menu');
			if (left_menu.is(":visible"))
				left_menu.addClass('hidden');
			else
				left_menu.removeClass('hidden');
			$('[data-toggle=left_menu]').toggle();
		});
	}

	// Everything in this object will be the public API
	return {
		init: initialize,
		expandListOfProcesses: expandListOfProcesses
	}
});

