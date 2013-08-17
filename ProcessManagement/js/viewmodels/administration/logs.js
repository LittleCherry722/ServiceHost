define([
	"knockout",
	"app",
	"underscore",
	"notify",
	"model"
], function( ko, App, _, Notify, Model ) {

	// TODO Actually do something here
	var ViewModel = function() {

		this.save = function() {
			
		};

		this.dataForUI = function() {
			return [];
		};
                
                this.getLogs = function() {
                    var n = $('#logEntries').val();

                    $.get("/logging/get_logs", {n: n}, function (data) {
                        $('#logMonitor').val(data);
                    });    
                };
	};

	var initialize = function() {
		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "administration/logs", viewModel, "right_content", null );
                
                $.get("/logging/get_logs", {n: 50}, function (data) {
                    $('#logMonitor').val(data);
                });
	};
        


	// Everything in this object will be the public API
	return {
		init: initialize
	};
});

