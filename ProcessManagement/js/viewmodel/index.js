var ViewModel = function() {
    
    var self = this;
    
    self.user = ko.observable();
    
    self.menuVM = new MenuViewModel();
    self.headerVM = new HeaderViewModel();
    self.contentVM = new ContentViewModel();
    
    self.init = function(){
        console.log("init vms");
           
        console.log(self);

        console.log(self.menuVM);
            
            
        self.menuVM.init();
        self.headerVM.init();
        self.contentVM.init();
        
        $(".chzn-select").chosen();
    }

    self.init();
}

var MenuViewModel = function(){
    
    var self = this;
    
    self.init = function(){
        console.log("init mvm");
    }
    
}

var HeaderViewModel = function(){
    
    var self = this;
    
    self.userName = ko.observable("no user");
    self.messageCount = ko.observable(0);
    
    self.init = function(){
        console.log("init hvm");
    
        if(SBPM.Storage.get("user")){
            self.userName(SBPM.Storage.get("user").name);
            initMessageCheck();
        }
    } 
    
    self.messageCountString = ko.computed(function() {
        return self.messageCount() < 1 ? "no new messages" : self.messageCount()+" new messages ";
    });
    
    function initMessageCheck(){
        console.log(SBPM.Service.Message.countNewMessages(SBPM.Storage.get("user").id));
        
        self.messageCount(
            SBPM.Service.Message.countNewMessages(SBPM.Storage.get("user").id)
        );
        
        setTimeout(initMessageCheck, 1000);
    }
    
}


var ContentViewModel = function(){
    
    var self = this;
    
    self.init = function(){
        
    }
    
}
