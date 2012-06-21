SBPM.Service.Process = {
	afterRender : function(){
//alert("process");
$("#goSbjView").css("border-width" , "5px");
				$(".chzn-select").chosen();
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
 