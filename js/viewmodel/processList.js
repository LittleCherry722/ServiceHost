var ViewModel = function(){
    
    var self = this;
    
    self.processes = ko.observableArray();
    
    self.init = function(){
        self.processes(SBPM.Service.Process.getAllProcesses());
        
        console.log(self.processes());
    }
    
    self.load = function(process){
        parent.SBPM.VM.processVM.showProcess(process);
        self.close();
    }
    
    self.remove = function(process){
        SBPM.Service.Process.deleteProcess(process);
    }
    
    self.close = function(){
        parent.$.fancybox.close();
    }
    
    self.init();
}
