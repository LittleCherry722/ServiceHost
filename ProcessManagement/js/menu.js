function mEdgeClicked() {
	$("#NodeFunctions").hide();
	$("#EdgeFunctions").show();

}

function mNodeClicked() {
	$("#NodeFunctions").show();
	$("#EdgeFunctions").hide();
}

function mSubjectClicked() {

}

function mViewChanged(typeofView) {
	if (typeofView == "bv") {
		$("#subjectMenu").hide();
		$("#NodeFunctions").hide();
		$("#EdgeFunctions").hide();
	} else {
		$("#subjectMenu").show();
	}

}