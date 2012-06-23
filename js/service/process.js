SBPM.Service.Process = {
	afterRender : function() {

		$("#slctSbj").chosen();
	},

	showSubjectView : function() {
		SBPM.VM.activeMainViewIndex(1);
		SBPM.VM.activeProcessViewIndex(0);

	},
	showInternalView : function() {
		SBPM.VM.activeMainViewIndex(1);
		SBPM.VM.activeProcessViewIndex(1);
	},
	showChargeView : function() {
		SBPM.VM.activeMainViewIndex(1);
		SBPM.VM.activeProcessViewIndex(2);
	},

	displayProcess : function(processName) {
		this.showSubjectView();
		gv_graph.loadFromJSON(loadGraph(getProcessID(processName)));
		gv_graph.init();
	},

	subjectAfterRender : function() {
		gv_graph.init();
		gf_paperChangeView("cv");
					updateListOfSubjects();
	},
	internalAfterRender : function() {
gf_clickedCVbehavior();
			updateListOfSubjects();
	},
	chargeAfterRender : function() {
showverantwortliche();
			gv_graph.selectedNode = null;
			updateListOfSubjects();

	},
}
