define([
	"knockout",
	"app",
	"notify",
	"dialog",
	"models/process",
	"models/processInstance",
	"underscore",
	"router",
	"moment",
	"select2",
	"jquery.ui",
], function( ko, App, Notify, Dialog, Process, ProcessInstance, _, Router, moment, select2 ) {
	var ViewModel = function() {
		var self = this;
		self.processes = processlist;
		this.availableProcesses = ko.observableArray(Process.all());
		this.selectedStart = selectedStart;
		this.selectedEnd = selectedEnd;
		this.selectedProcess = selectedProcess;

		self.back = function() {
			history.back()
		}


		self.remove = function( process ) {
			Dialog.yesNo( 'Warning', "Do you really want to delete this Process?", function(){
				destroyProcess( process )
				parent.$.fancybox.close();
			});
		}
		
		self.removeInstance = function( processInstance ) {
			Dialog.yesNo( 'Warning', "Do you really want to delete this Processinstance?", function(){
				destroyProcessInstance( processInstance )
				parent.$.fancybox.close();
			});
		}

		self.newInstance = function() {
			var process = Process.find( $("input[name='processId']").val()) ;
			$('#processNameModal').modal('hide');
            instance = new ProcessInstance( {
				processId: process.id(),
				name: $("input[name='instancename']").val(),
				owner: App.currentUser().id(),
				graph: process.graph()
			});
		
			instance.save(null, {
				success: function() {
					Actions.fetch();
				},
				error: function() {
					Notify.error( "Error", 'Unable to create a new instance of "' + process.name() + '" process.'  );
				}
			});
		}
		this.showProcessNameModal = function() {
			var process = this;
            if (!process.startAble()) {
                Notify.warning("Not possible", "This process can only be started by external partners.");
            }
            else {
                $("input[name='processId']").val(process.id());
                $("input[name='instancename']").val(process.name() +' ' + moment().format('YYYY-MM-DD HH:mm'));
                $("#processNameModal").modal();
            }
		}
	}
	var selectedStart = ko.observable();
	var selectedEnd = ko.observable();
	var selectedProcess = ko.observable("");
	
	var processlist = ko.observableArray();		
	var updateProcesslist = ko.computed(function() {
		processlist.removeAll();
		$.each( Process.all(), function ( i, value ) {
			var filter = false;
			if((selectedEnd() ||selectedStart()) && (typeof value.processInstances !== "function" || value.processInstances().length<1)) {
				filter = true;
			}
                        if (typeof value.processInstances === "function") {
                            $.each( value.processInstances(), function ( i, valueis ) {
                                    if(selectedStart() && parseInt(moment(selectedStart()).format("X")) >= parseInt(moment(valueis.startedAt().date).format('X'))){
                                            filter = true;
                                    }
                                    if(selectedEnd() && parseInt(moment(selectedEnd()).format("X"))<= parseInt(moment(valueis.startedAt().date).format('X'))){
                                            filter = true;
                                    }
                            });
                        }
                        
			if (selectedProcess() && selectedProcess() != value.id() ) {
				filter = true;
			}
			if(filter==false) {
				processlist.push(value);
			}
		});
	});
	

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
	
	var destroyProcessInstance = function( processInstance ) {
		processInstance.destroy(null, {
			success: function( textStatus ) {
				Notify.info( "Success", "Processinstance " + this.name() + " has successfully been deleted" );
			},
			error :function( textStatus, error ) {
				Notify.error( "Error", "Deleting the processinstance failed." );
			}
		});
	}



	var initialize = function() {
		var viewModel = new ViewModel();

		App.loadTemplate( "processList", viewModel, null, function() {
			$( "#from" ).datepicker({
				defaultDate: "+1w",
				changeMonth: true,
				numberOfMonths: 3,
				onClose: function( selectedDate ) {
					$( "#to" ).datepicker( "option", "minDate", selectedDate );
				}
			});
			$( "#to" ).datepicker({
				defaultDate: "+1w",
				changeMonth: true,
				numberOfMonths: 3,
				onClose: function( selectedDate ) {
					$( "#from" ).datepicker( "option", "maxDate", selectedDate );
				}
			});	
			$("#ui-datepicker-div").wrap('<div id="dashboard_datepicker" />');
			$(".sel").prepend('<option/>').val(function(){return $('[selected]',this).val() ;});
			var select2 = $(".sel").select2( {
		        width: "copy",
		        allowClear: true,
		        dropdownAutoWidth: "true"
	        });
	        $(".sel").on("change", function(e) { 
				viewModel.selectedProcess(e.val);
			});
			$(document).on('propertychange change keyup input paste', 'input.data_field', function(){
    			var io = $(this).val().length ? 1 : 0 ;
    			$(this).next('.icon_clear').stop().fadeTo(300,io);
			}).on('click', '.icon_clear', function() {
    			$(this).delay(300).fadeTo(300,0).prev('input').val('').change();
			});
		});
	};
	
	// Everything in this object will be the public API
	return {
		init: initialize
	};
});

