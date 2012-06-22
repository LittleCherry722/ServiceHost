SBPM.Service.Process = {
	afterRender : function(){

$("#slctSbj").chosen();

	},
	
	
	showSubjectView : function() {
		SBPM.VM.activeProcessViewIndex(0);
	},
	showInternalView : function() {
		SBPM.VM.activeProcessViewIndex(1);
	},
	showChargeView : function() {
		SBPM.VM.activeProcessViewIndex(2);
	}
	
}
 