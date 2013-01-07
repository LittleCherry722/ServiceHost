define([
	"knockout",
	"app",
	"notify",
	"dialog",
	"models/process",
	"underscore"
	// "tk_graph"
], function( ko, App, Notify, Dialog, Process, _ ) {
	var ViewModel = function() {

		var self = this;

		self.processes = Process.all;

		self.back = function() {
			history.back()
		}


		self.remove = function( process ) {
			Dialog.yesNo( 'Warning', "Do you really want to delete this Process?", function(){
				destroyProcess( process )
				parent.$.fancybox.close();
			});
		}
	}

	var destroyProcess = function( process ) {
		process.destroy(function( error ) {
			if ( error ) {
				Notify.error( "Error", "Deleting the process failed." );
			} else {
				Notify.info( "Success", "Process " + this.name() + " has successfully been deleted" );
			}
		});
	}

	var initialize = function() {
		var viewModel = new ViewModel();

		App.loadTemplate( "processList", viewModel, null, function() {
			// TODO do we need to do anything?
		});
	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});

