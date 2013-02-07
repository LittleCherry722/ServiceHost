define([
	"knockout",
	"app",
	"notify",
	"dialog",
	"models/process",
	"models/processInstance",
	"underscore",
	"router"
	// "tk_graph"
], function( ko, App, Notify, Dialog, Process, ProcessInstance, _, Router ) {
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

		self.newInstance = function() {
			instance = new ProcessInstance( { processId: this.id() } );
			instance.save( { async: false } );
			console.log("switching")
			Router.goTo( instance );
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

