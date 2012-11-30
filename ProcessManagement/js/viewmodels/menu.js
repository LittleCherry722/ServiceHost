define([
	"knockout",
	"models/process",
	"jade!../../templates/menu"
], function( ko, Process, menuTemplate ) {

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

		this.allProcesses = allProcesses();
	}

	
	// Initialize our menu.
	// Write the template content in our menuNode and
	// apply all bindings.
	var initialize = function() {
		menuNode = document.getElementById('left_menu')
		menuNode.innerHTML = menuTemplate();
		ko.applyBindings(new viewModel(), menuNode)
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});

