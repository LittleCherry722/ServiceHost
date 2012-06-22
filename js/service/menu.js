SBPM.Service.Menu = {
	afterRender : function() {
		$("#main_menu").accordion({
			collapsible : true,
			autoHeight : false
		});
		$("#calendar").datepicker({
			nextText : "&raquo;",
			prevText : "&laquo;"
		});

		$("#datepicker").datepicker({
			nextText : "&raquo;",
			prevText : "&laquo;",
			showAnim : "slideDown"
		});

		$("#dialog").dialog({
			autoOpen : false,
			modal : true,
			draggable : false,
			resiable : false,
			buttons : [{
				text : "Continue",
				click : function() {
				}
			}, {
				text : "Cancel",
				click : function() {
					$(this).dialog("close");
				}
			}]
		});
		$("a#newUser").fancybox({
			'padding' : '0px',
			'scrolling' : 'no',
			'width' : '30',
			'height' : '28',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 != false)
					newUser(fancyreturn1);
			}
		});

		$("a#administration").fancybox({
			'padding' : '0',
			'scrolling' : 'no',
			'width' : '80',
			'height' : '50',
			'autoScale' : false,
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {

			}
		});

		$("a#saveAs").fancybox({
			'padding' : '0px',
			'scrolling' : 'no',
			'width' : '50',
			'height' : '40',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 != false)
					GraphSpeichernAls(fancyreturn1);
			}
		});

		$("a#newGroup").fancybox({
			'padding' : '0px',
			'scrolling' : 'no',
			'width' : '30',
			'height' : '28',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 != false)
					newGroup(fancyreturn1);
			}
		});

		$("a#newProcess").fancybox({
			'padding' : '0px',
			'scrolling' : 'no',
			'width' : '30',
			'height' : '28',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 != false) {
					newProcess(fancyreturn1);
					$("#tab2").trigger('click');
				}

			}
		});

		$("a#loadProcess").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 != false)
					ProzessLaden(fancyreturn1);
			}
		});

		$("a#deleteUser").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
			}
		});

		$("a#userTOgroup").fancybox({
			'padding' : '0px',
			'scrolling' : 'no',
			'width' : '30',
			'height' : '37',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 != false)
					user_to_group(fancyreturn1, fancyreturn2);
			}
		});

		$("a#responsibleForloggedinUser").fancybox({
			'padding' : '0px',
			'scrolling' : 'no',
			'width' : '30',
			'height' : '37',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 != false)
					addResponsible(fancyreturn2, fancyreturn1);
			}
		});

		$("a#deleteUserFromGroup").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
			}
		});

		$("a#deleteGroup").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				setSubjectIDs();
			}
		});

		$("a#YesNo").fancybox({
			'padding' : '0px',
			'scrolling' : 'no',
			'width' : '30',
			'height' : '20',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
			}
		});

		$("a#newInstance").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 != false)
					newInstance(fancyreturn1);
			}
		});

		$("a#newMSG").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 != false)
					resumeInstanceMessage(fancyreturn1)
			}
		});

		$("a#MSGInbox").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 != false) {
					drawHistory(loadInstanceData(fancyreturn1));
					document.getElementById("welcome").style.display = "none";
					document.getElementById('ausfuehrung').style.display = 'block';
					document.getElementById("graph").style.display = "none";
					document.getElementById("abortInstanceButton").style.display = "none";
				}
			}
		});

		$("a#MSGOutbox").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 != false) {
					drawHistory(loadInstanceData(fancyreturn1));
					document.getElementById("welcome").style.display = "none";
					document.getElementById('ausfuehrung').style.display = 'block';
					document.getElementById("graph").style.display = "none";
					document.getElementById("abortInstanceButton").style.display = "none";
				}
			}
		});

		$("a#runningInstances").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				writeSumActiveInstances();
				if(fancyreturn1 != false)
					resumeInstance(fancyreturn1);
			}
		});

		$("a#history").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 != false) {
					drawHistory(loadInstanceData(fancyreturn1));
					document.getElementById("welcome").style.display = "none";
					document.getElementById('ausfuehrung').style.display = 'block';
					document.getElementById("graph").style.display = "none";
					document.getElementById("abortInstanceButton").style.display = "none";
				}
			}
		});

		$("a#deleteProcess").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '40',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
			}
		});

		$("a#overviewProcesses").fancybox({
			'padding' : '0px',
			'scrolling' : 'auto',
			'width' : '60',
			'height' : '50',
			'transitionIn' : 'elastic',
			'transitionOut' : 'elastic',
			'type' : 'iframe',
			'overlayColor' : '#333333',
			'modal' : true,
			'overlayOpacity' : '0.6',
			'onClosed' : function() {
				if(fancyreturn1 && fancyreturn1 != false)
					ProzessLaden(fancyreturn1);
			}
		});
	},
}