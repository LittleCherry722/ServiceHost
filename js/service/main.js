SBPM.Service.Main = {
	afterRender : function(){
alert("main");
console.log($(".chzn-select"));
	},
	showHomeView : function(){
		SBPM.VM.activeMainViewIndex(0);
	},
	showProcessView : function(){
		SBPM.VM.activeMainViewIndex(1);
		SBPM.VM.activeProcessViewIndex(0); // In ProcessView, start with SubjectView.
	}
	
	
}