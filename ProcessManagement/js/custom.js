var fancyreturn1, fancyreturn2 = false;

$(document).ready(function() {

$(function() {
	$("#main_menu").accordion({collapsible:true,autoHeight:false});

	$("#calendar").datepicker({nextText:"&raquo;",prevText:"&laquo;"});
	
	$("#datepicker").datepicker({nextText:"&raquo;",prevText:"&laquo;",showAnim:"slideDown"});
	
	$("#dialog").dialog({ autoOpen: false, modal: true, draggable: false, resiable: false, buttons: [
	    {
	        text: "Continue",
	        click: function() { }
	    },
	    {
	        text: "Cancel",
	        click: function() { $(this).dialog("close"); }
	    }
	] });
	
	$("#tab1").click(function(){
		$(this).parent().parent().find("td input").removeClass("active");
		$(this).addClass("active");
		$(".tab_content").addClass("hide");
		$("#tab1_content").removeClass("hide");
		});
	$("#tab2").click(function(){
		$(this).parent().parent().find("td input").removeClass("active");
		$(this).addClass("active");
		$(".tab_content").addClass("hide");
		$("#tab2_content").removeClass("hide");
		gv_graph.changeView('cv');
		});
	$("#tab3").click(function(){
		$(this).parent().parent().find("td input").removeClass("active");
		$(this).addClass("active");
		$(".tab_content").addClass("hide");
		$("#tab3_content").removeClass("hide");
		});
	
	
	$("#instance_tab1").click(function(){
		$(this).parent().parent().find("td input").removeClass("active");
		$(this).addClass("active");
		$(".tab_content").addClass("hide");
		$("#instance_tab1_content").removeClass("hide");
		});
	$("#instance_tab2").click(function(){
		$(this).parent().parent().find("td input").removeClass("active");
		$(this).addClass("active");
		$(".tab_content").addClass("hide");
		$("#instance_tab2_content").removeClass("hide");
		});
	
	
	$("#hide_menu").click(function(){
		$("#left_menu").hide();
		$("#show_menu").show();$("body").addClass("nobg");
		$("#content").css("marginLeft",35);
	});

	$("#show_menu").click(function(){
		$("#left_menu").show();
		$(this).hide();
		$("body").removeClass("nobg");
		$("#content").css("marginLeft",245);
	});
	

	$("a#newUser").fancybox({
		'padding': '0px',
		'scrolling': 'no',
		'width': '30',
        'height': '28',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {if(fancyreturn1 != false) newUser(fancyreturn1);}
    });
	
	$("a#newGroup").fancybox({
		'padding': '0px',
		'scrolling': 'no',
		'width': '30',
        'height': '28',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {if(fancyreturn1 != false) newGroup(fancyreturn1);}
    });

	$("a#newProcess").fancybox({
		'padding': '0px',
		'scrolling': 'no',
		'width': '30',
        'height': '28',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {if(fancyreturn1 != false) newProcess(fancyreturn1);}
    });
	
	$("a#loadProcess").fancybox({
		'padding': '0px',
		'scrolling': 'auto',
		'width': '40',
        'height': '50',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {if(fancyreturn1 != false) ProzessLaden(fancyreturn1);}
    });
	
	$("a#deleteUser").fancybox({
		'padding': '0px',
		'scrolling': 'auto',
		'width': '40',
        'height': '50',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {}
    });
	
	$("a#userTOgroup").fancybox({
		'padding': '0px',
		'scrolling': 'no',
		'width': '30',
        'height': '37',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {if(fancyreturn1 != false) user_to_group(fancyreturn1, fancyreturn2);}
    });
	
	$("a#responsibleForloggedinUser").fancybox({
		'padding': '0px',
		'scrolling': 'no',
		'width': '30',
        'height': '37',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {if(fancyreturn1 != false) addResponsible(fancyreturn2, fancyreturn1);}
    });
	
	
	$("a#deleteUserFromGroup").fancybox({
		'padding': '0px',
		'scrolling': 'auto',
		'width': '40',
        'height': '50',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {}
    });
	
	$("a#deleteGroup").fancybox({
		'padding': '0px',
		'scrolling': 'auto',
		'width': '40',
        'height': '50',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {setSubjectIDs();}
    });
	
	$("a#YesNo").fancybox({
		'padding': '0px',
		'scrolling': 'no',
		'width': '30',
        'height': '20',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {}
    });
	
	$("a#newInstance").fancybox({
		'padding': '0px',
		'scrolling': 'auto',
		'width': '40',
        'height': '50',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {if(fancyreturn1 != false) newInstance(fancyreturn1);}
    });	
	
	$("a#newMSG").fancybox({
		'padding': '0px',
		'scrolling': 'auto',
		'width': '40',
        'height': '50',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {if(fancyreturn1 != false) resumeInstanceMessage(fancyreturn1)}
    });	
	
	$("a#MSGInbox").fancybox({
		'padding': '0px',
		'scrolling': 'auto',
		'width': '40',
        'height': '50',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {if(fancyreturn1 != false) {drawHistory(loadInstanceData(fancyreturn1)); 
				document.getElementById("welcome").style.display = "none"; 
				document.getElementById('ausfuehrung').style.display = 'block';
				document.getElementById("graph").style.display = "none";
				document.getElementById("abortInstanceButton").style.display = "none";}}
    });	
	
	$("a#MSGOutbox").fancybox({
		'padding': '0px',
		'scrolling': 'auto',
		'width': '40',
        'height': '50',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {if(fancyreturn1 != false) {drawHistory(loadInstanceData(fancyreturn1)); 
				document.getElementById("welcome").style.display = "none"; 
				document.getElementById('ausfuehrung').style.display = 'block';
				document.getElementById("graph").style.display = "none";
				document.getElementById("abortInstanceButton").style.display = "none";}}
    });	
	
	$("a#runningInstances").fancybox({
		'padding': '0px',
		'scrolling': 'auto',
		'width': '40',
        'height': '50',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {writeSumActiveInstances(); if(fancyreturn1 != false) resumeInstance(fancyreturn1);}
    });	
	
	$("a#history").fancybox({
		'padding': '0px',
		'scrolling': 'auto',
		'width': '40',
        'height': '50',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {if(fancyreturn1 != false) {drawHistory(loadInstanceData(fancyreturn1)); 
				document.getElementById("welcome").style.display = "none"; 
				document.getElementById('ausfuehrung').style.display = 'block';
				document.getElementById("graph").style.display = "none";
				document.getElementById("abortInstanceButton").style.display = "none";}}
    });
	
	$("a#deleteProcess").fancybox({
		'padding': '0px',
		'scrolling': 'auto',
		'width': '40',
        'height': '50',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {}
    });
    
    $("a#overviewProcesses").fancybox({
		'padding': '0px',
		'scrolling': 'auto',
		'width': '60',
        'height': '50',
        'transitionIn': 'elastic',
        'transitionOut': 'elastic',
        'type': 'iframe',
        'overlayColor'  : '#333333',
		'modal': true,
		'overlayOpacity': '0.6',
		'onClosed' : function() {
			if(fancyreturn1 && fancyreturn1 != false) ProzessLaden(fancyreturn1);
		}
    });
	
	
});

});

function callFancyBox(my_href) {
var j1 = document.getElementById("hiddenclicker");
j1.href = my_href;
$('#hiddenclicker').trigger('click');
}

function showtab1 ()
{
$("#tab2").removeClass("active");
$("#tab1").addClass("active");
$(".tab_content").addClass("hide");
$("#tab1_content").removeClass("hide");

$('#graph_bv_outer').scrollTo( {left: '50%', top: '0px'}, 0);
}

function shownothing() {
$("#tab1").removeClass("active");
$("#tab2").removeClass("active");
$("#tab3").removeClass("active");
$("#tab1_content").addClass("hide");
$("#tab2_content").addClass("hide");
$("#tab3_content").addClass("hide");
}

//resize canvas to fit into screen
$(function() {
	$("#graph_bv_outer").css("height", window.innerHeight - 526);
	
	$(window).resize(function() {
		$("#graph_bv_outer").css("height", window.innerHeight - 526);
	});
});
