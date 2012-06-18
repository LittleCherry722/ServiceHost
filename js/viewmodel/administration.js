var ViewModel = function() {
    
    var self = this;

    self.tab = ko.observable("");
    self.tabs = ['General', 'Users', 'Roles', 'Debug'];
    self.subsites = ko.observable({
        'General' : new GeneralViewModel(),
        'Users'   : new UserViewModel(), 
        'Roles'   : new RoleViewModel(),
        'Debug'   : new DebugViewModel() 
    });
    
    self.init = function(){
        
        //preselect the general tab
        self.goToTab("General");
        
    }
    
    self.goToTab = function(tab) { 

        if(tab == self.tab())
            return;

        // load the selected tabs model & ui
        self.subsites()[tab].init();

        // set the tab for highlighting
        self.tab(tab);
             
    }

    self.subsite = function(){
        return self.subsites()[self.tab()];
    }
       
    self.close = function(){
        parent.$.fancybox.close();
    }
    
    self.save = function(){
        return self.subsite().save();
    }
    
    self.initChosen = function(elements){
        $(elements).find('.chzn-select').chosen();
    }
    
    self.init();
    console.log("ViewModel for administration initialized.");
};
 
 
var SubViewModel = function(name){

    // thats the extending class' context
    var self = this;

    self.name = name;
    self.template = name.toLowerCase();
    self.data = ko.observableArray();
    
    self.init = function(){
        console.log("loading model.");
        self.loadModel();
    }

}

/**
 * extends SubViewModel 
 */
var GeneralViewModel = function(){
        
    var self = this;
       
    self.loadModel = function(){
        //Utilities.unimplError("loadModel");
    }
    
    self.save = function(){
        //Utilities.unimplError("save");
    }

    SubViewModel.call(self, "General");

}

/**
 * extends SubViewModel 
 */
var UserViewModel = function(){
       
    var self = this;
    
    self.options = ko.observableArray();
    
    self.loadModel = function(){
        ko.mapping.fromJS(SBPM.Service.User.getAll(), self.data);
        
        console.log(SBPM.Service.User.getAll());
        
        if(self.options().length < 1){
            var roles = SBPM.Service.Role.getAll();
            
            for(var i in roles)
                self.options.push(roles[i].name);
        }
    }
    
    self.showDetails = function(user){
        
    }
    
    self.deleteUser = function(user){
        if(SBPM.Service.User.remove(user.id()))
             self.loadModel();
    }
    
    self.createUser = function(){
        var data = self.data();
        data.push({id: 0, name: "", roles: "", active: 1});
        self.data(data);
    }
    
    self.save = function(){

        for(var i in self.data())
            if(self.data()[i].name == "")
                self.data().removeAll(self.data()[i]);   
        
        console.log(self.data());
        
        ko.mapping.fromJS(SBPM.Service.User.saveAll(ko.toJS(self.data())), self.data);
        
        parent.$("#freeow").freeow(self.name, "The current tab has been saved.", {
            classes: [,"ok"],
            autohide: true
        });
    }
    
    SubViewModel.call(self, "Users");
    
}

/**
 * extends SubViewModel 
 */
var RoleViewModel = function(){
    
    var self = this;
    
    self.loadModel = function(){
        self.data(ko.mapping.fromJS(SBPM.Service.Role.getAll()));
    }
    
    self.deleteRole = function(role){
        if(SBPM.Service.Role.remove(role.ID))
            self.loadModel();
    }
    
    self.createRole = function(){ // TODO why push by itself doesnt work?
        var data = self.data();
        data.push({ID: 0, name: "", active: 1});
        self.data(data);
    }
    
    self.save = function(){
        
        for(var i in self.data())
            if(self.data()[i].name == "")
                self.data().removeAll(self.data()[i]);

        self.data(SBPM.Service.Role.saveAll(ko.toJS(self.data())));
        //ko.mapping.fromJS(SBPM.Service.Role.saveAll(self.data()), self.data);
        
        parent.$("#freeow").freeow("Roles", "The current tab has been saved.", {
            classes: [,"ok"],
            autohide: true
        });
    }

    SubViewModel.call(self, "Roles");
    
}

/**
 * extends SubViewModel 
 */
var DebugViewModel = function(){

    var self = this;
    
    self.test = 1;
    
    self.loadModel = function(){
    }

    self.createUsers = function(){
        if(SBPM.Service.Debug.createUsers(name))
            ;
    }

    self.createProcess1 = function(){
        if(SBPM.Service.Debug.createProcess("applicationforleave"))
            ;
    }
    
    self.createProcess2 = function(){
        if(SBPM.Service.Debug.createProcess("asyncmsg"))
            ;
    }

    self.save = function(){
        return false;
    }

    SubViewModel.call(self, "Debug");
    
}

var VM = new ViewModel();

ko.applyBindings(VM);
