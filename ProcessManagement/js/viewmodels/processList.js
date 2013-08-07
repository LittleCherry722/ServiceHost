define([
	"knockout",
	"app",
	"notify",
	"dialog",
	"models/process",
	"models/processInstance",
	"underscore",
	"router",
	"moment"
], function( ko, App, Notify, Dialog, Process, ProcessInstance, _, Router, moment ) {
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
			var process = Process.find( $("input[name='processId']").val()) ;
			$('#processNameModal').modal('hide');
            instance = new ProcessInstance( {
				processId: process.id(),
				name: $("input[name='instancename']").val(),
				graph: process.graph()
			});
		
			instance.save(null, {
				success: function() {
					Actions.fetch();
					History.fetch();
				},
				error: function() {
					Notify.error( "Error", 'Unable to create a new instance of "' + process.name() + '" process.'  );
				}
			});
		}
		this.showProcessNameModal = function() {
			var process = this;
			$("input[name='processId']").val(process.id());
			$("input[name='instancename']").val(process.name() +' ' + moment().format('YYYY-MM-DD HH:mm'));
			$("#processNameModal").modal();
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

