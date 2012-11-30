define([
	"knockout",
	"text!../../templates/menu.html"
], function( ko, menuTemplate ) {

	var viewModel = function() {
		// all knockout bindings go here
	}
	
	var initialize = function() {
		menuNode = document.getElementById('left_menu')
		menuNode.innerHTML = menuTemplate;
		ko.applyBindings(new viewModel(), menuNode)
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});

