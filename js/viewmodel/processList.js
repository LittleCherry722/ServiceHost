var ViewModel = function(){
    
    var self = this;
    
    self.processes = ko.observableArray();
    
    self.init = function(){
        self.processes(SBPM.Service.Process.getAllProcesses());
    }
    
    self.load = function(process){
        parent.SBPM.VM.processVM.showProcess(process);

        self.close();

    }
    
    self.remove = function(process){
        if(SBPM.Service.Process.deleteProcess(process)){
          
            self.init();
        }else
            parent.SBPM.Notification.Error("Error", "Deleting the process failed.");
            
        
    }
    
    self.close = function(){
        parent.$.fancybox.close();
    }
    
    self.init();
}
