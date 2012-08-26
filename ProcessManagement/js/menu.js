function mEdgeClicked() {
	$("#NodeFunctions").hide();
	$("#EdgeFunctions").show();
	$("#ge_edge_type_condition").change();
	console.log("Edge clicked fired");

}

function mNodeClicked() {
	$("#NodeFunctions").show();
	$("#EdgeFunctions").hide();
}

function mSubjectClicked(id) {
	if ($("#ge_cv_id").val() == null || $("#ge_cv_id").val() == "") {

		$("#AssignRoleWarning").show();
	} else {
		$("#AssignRoleWarning").hide();
	}

}

function mViewChanged(typeofView) {
	if (typeofView == "bv") {
		$("#subjectMenu").hide();
		$("#NodeFunctions").hide();
		$("#EdgeFunctions").hide();
	} else {
		$("#subjectMenu").show();
	}
	$("#zoominbutton").show();
	$("#zoomoutbutton").show();
	$("#reset-button").show();
}

function mDisplayEdge(edge, type) {
	gf_guiDisplayEdge(edge, type);
	$("#ge_edge_type_condition").change();
	console.log("Edge display fired");
}
