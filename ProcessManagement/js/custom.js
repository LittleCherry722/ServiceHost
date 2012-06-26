/*
 * S-BPM Groupware v0.8
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2012 Johannes Decher, Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

var fancyreturn1, fancyreturn2 = false;

function callFancyBox(my_href) {
	var j1 = document.getElementById("hiddenclicker");
	j1.href = my_href;
	$('#hiddenclicker').trigger('click');
}

function showtab1() {
	$("#tab2").removeClass("active");
	$("#tab3").removeClass("active");
	$("#tab1").addClass("active");
	$(".tab_content").addClass("hide");
	$("#tab1_content").removeClass("hide");

	gf_clickedCVbehavior();

	$('#graph_bv_outer').scrollTo({
		left : '50%',
		top : '0px'
	}, 0);
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
	$("#graph_bv_outer").css("width", window.innerWidth - 190 - 245);
	$("#graph_bv_outer").css("height", window.innerHeight - 124);
	$(window).resize(function() {
		$("#graph_bv_outer").css("width", window.innerWidth - 190 - 245);
		$("#graph_bv_outer").css("height", window.innerHeight - 124);
	});
});