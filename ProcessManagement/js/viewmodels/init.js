// on dom ready load mediator
$(document).ready(function() {

	$.getScript("js/mediator/mediator.js").fail(function(jqxhr, settings, exception) {
		console.log("Error loading root mediator.");
		throw exception;
	}).success(function() {

		$.getScript("js/mediator/" + Utilities.getFilename(true) + ".js").fail(function(jqxhr, settings, exception) {

			console.log("Application: Loading mediator failed. Falling back to standard mediator.");

			// do some minimal viable stuff to initialize an ViewModel without a
			// Mediator (backward-compability)
			SBPM.Mediator = new (function(){

				// Extends IMediator
				IMediator.call(this);

			});

		}).success(function() {

			console.log("Application: Loading mediator succeded. (js/mediator/" + Utilities.getFilename(true) + ".js).");

			// the current Mediator can be found here
			SBPM.Mediator = new Mediator();

		}).complete(function() {

			console.log("Application: Initializing Mediator...");
			SBPM.Mediator.init(SBPM.Mediator.viewListeners);

		});

	});

});
