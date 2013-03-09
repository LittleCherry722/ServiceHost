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
			var process = this;
			
			instance = new ProcessInstance( {
				processId: process.id(),
				graph: process.graph()
			});

			instance.save(null, {
				success: function() {
					Router.goTo( instance );
				},
				error: function() {
					Notify.error( "Error", 'Unable to create a new instance of "' + process.name() + '" process.'  );
				}
			});
		}
	}

	var destroyProcess = function( process ) {
		process.destroy(null, {
			success: function( textStatus ) {
				Notify.info( "Success", "Process " + this.name() + " has successfully been deleted" );
			},
			error :function( textStatus, error ) {
				Notify.error( "Error", "Deleting the process failed." );
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

