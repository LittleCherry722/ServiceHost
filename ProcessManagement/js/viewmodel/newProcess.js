var ViewModel = function() {
	var self = this;

	self.init = function(){
	console.log("newProcess init");	
	self.quickVM = new QuickViewModle();
	
	self.quickVM.init();
}
	self.init();
	
	self.processName = ko.observable("");

	self.createCheck = function() {
		var process = self.processName();
		console.log("createCheck " + process);

        if(!process || process.length < 1){
            SBPM.Notification.Warning('Warning', 'Please enter a name for the process!');
            return;
        }

        // if process name does not exist
		if(!SBPM.Service.Process.processExists(process)) {
		    
			// load a new process
			parent.SBPM.VM.processVM.showProcess(process);
			
			if(self.quickVM.displayTable()){
				self.quickVM.createProcessFromTable(self.quickVM.subjects(),self.quickVM.messages());
		
			}
			
			
			// update list of recent processes
            parent.SBPM.VM.menuVM.init();
            
            // close layer
            self.close();
            
		} else { // otherwise ask the user to keep the given name anyhow
		    
			SBPM.Dialog.YesNo('Warning', 'Process\' name already exists. Do you want to proceed with the given name?', 
			function() { // yes
			    
			    // close the newProcess layer
				self.close();
				
			});

		}
	}

	self.close = function() {
		parent.$.fancybox.close();
	}





	console.log("ViewModel for newProcess initialized.");
	}
	





var QuickViewModle = function(){

var self=this;
self.name = "quickView";
self.init = function(){
	console.log("Quick init");	
}

	self.displayTable = ko.observable(false);
	
	self.changeDisplay = function() {
		if(self.displayTable())
		self.displayTable(false);
		else
		self.displayTable(true);
	}
	
self.subjectS1 = ko.observable();
self.subjectS2 = ko.observable();
self.subjectS3 = ko.observable();
self.subjectS4 = ko.observable();
self.subjectS5 = ko.observable();
	
self.subjects =ko.observableArray();

	self.addSubject = function(sub) {
if(sub != null && sub != "" && self.subjects.indexOf(sub)<0) self.subjects.push(sub);
		}

self.cleanSubjects = function(){

	for(i=self.subjects().length-1;i>=0;i--){
	if(self.subjects()[i] != self.subjectS1() && self.subjects()[i] != self.subjectS2() && self.subjects()[i] != self.subjectS3() && self.subjects()[i] != self.subjectS4() && self.subjects()[i] != self.subjectS5()) self.subjects.remove(self.subjects()[i]);
}
}

	self.subjectsList = ko.computed(function() {

self.addSubject(self.subjectS1());
self.addSubject(self.subjectS2());
self.addSubject(self.subjectS3());
self.addSubject(self.subjectS4());
self.addSubject(self.subjectS5());

self.cleanSubjects();
		
		console.log(self.subjects());
		return null;
	}); 

	self.m1=ko.observable();
	self.m2=ko.observable();
	self.m3=ko.observable();
	self.m4=ko.observable();
	self.m5=ko.observable();
	self.m6=ko.observable();
	self.m7=ko.observable();
	self.m8=ko.observable();
	self.m9=ko.observable();
	self.m10=ko.observable();
	
	
	self.m1s1=ko.observable();
self.m1s2=ko.observable();
self.m2s1=ko.observable();
self.m2s2=ko.observable();
self.m3s1=ko.observable();
self.m3s2=ko.observable();
self.m4s1=ko.observable();
self.m4s2=ko.observable();
self.m5s1=ko.observable();
self.m5s2=ko.observable();
self.m6s1=ko.observable();
self.m6s2=ko.observable();
self.m7s1=ko.observable();
self.m7s2=ko.observable();
self.m8s1=ko.observable();
self.m8s2=ko.observable();
self.m9s1=ko.observable();
self.m9s2=ko.observable();
self.m10s1=ko.observable();
self.m10s2=ko.observable();
	

self.messages=ko.observableArray();

self.noMessage = function(mesOb){
	var bool = true;
	
	if(mesOb.message != null && mesOb.message.replace(" ", "")  != "" && mesOb.sender != null && mesOb.sender.replace(" ", "")  != "" && mesOb.receiver != null && mesOb.receiver.replace(" ", "")  != "" ) bool=false;
return bool;
}

self.cleanMessages= function(){
	for(i=self.messages().length-1;i>=0;i--){
		if(self.noMessage(self.messages()[i])) self.messages.remove(self.messages()[i]);
	}
}


self.messageList = ko.computed(function() {

self.messages([{message : self.m1(),sender : self.m1s1(),receiver : self.m1s2()},{message : self.m2(),sender:self.m2s1(),receiver : self.m2s2()},{message : self.m3(),sender:self.m3s1(),receiver : self.m3s2()},{message : self.m4(),sender:self.m4s1(),receiver : self.m4s2()},{message : self.m5(),sender:self.m5s1(),receiver : self.m5s2()},{message : self.m6(),sender:self.m6s1(),receiver : self.m6s2()},{message : self.m7(),sender:self.m7s1(),receiver : self.m7s2()},{message : self.m8(),sender:self.m8s1(),receiver : self.m8s2()},{message : self.m9(),sender:self.m9s1(),receiver : self.m9s2()},{message : self.m10(),sender:self.m10s1(),receiver : self.m10s2()}]);

self.cleanMessages();

	console.log(self.messages());
	
	return null;
});

self.createProcessFromTable = function(sub,mes){
	
	parent.SBPM.Service.Process.createProcessFromTable(sub,mes);
}	


		
	

	self.readSubjects = function() {
		console.log("read");
		for( m = 1; m <= 10; m++) {
			for( s = 1; s <= 2; s++) {
				var name = "m" + m + "s" + s;
				var sel = document.getElementById(name).options;
				sel.length = 0;

				var opt = document.createElement("option");
				opt.text = "";
				opt.value = "";
				opt.id = name + "_00000.0";
				sel.add(opt);

				for( si = 1; si <= 5; si++) {
					var subj = document.getElementById("s" + si).value;
					if(subj != "") {
						opt = document.createElement("option");
						opt.text = subj;
						opt.value = subj.toLowerCase();
						opt.id = name + "_" + subj.toLowerCase();
						sel.add(opt);
					}
				}
			}
		}
	}

	self.createProcessFromTable2 = function() {
		var subjs = [];

		for( s = 1; s <= 5; s++) {
			var val = document.getElementById("s" + s).value;
			if(val.replace(" ", "") != "") {
				subjs[subjs.length] = val;
			}
		}

		var msgs = [];

		for( m = 1; m <= 10; m++) {
			var msg = document.getElementById("m" + m).value;
			var s1 = document.getElementById("m" + m + "s1").value;
			var s2 = document.getElementById("m" + m + "s2").value;

			if(msg.replace(" ", "") != "" && s1 != "" && s2 != "") {
				msgs[msgs.length] = {
					message : msg,
					sender : s1,
					receiver : s2
				};
			}
		}

		gf_createFromTable(subjs, msgs);
        updateListOfSubjects();
	}

	
	
}
	

