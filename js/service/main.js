SBPM.Service.Main = {
	afterRender : function(){
//alert("main");

	},
	showHomeView : function(){
		SBPM.VM.activeMainViewIndex(0);
	},
	showProcessView : function(){
		SBPM.VM.activeMainViewIndex(1);
	}
	
	
}