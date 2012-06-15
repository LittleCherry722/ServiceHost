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
    self.data = ko.observable("");
    
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
        self.data({name : "jens", attribut : "test"});
        Utilities.unimplError("loadModel");
    }
    
    self.save = function(){
        Utilities.unimplError("save");
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
        self.data({users : UserService.getAll()});

        if(self.options().length < 1){
            var roles = RoleService.getAll();
            
            for(var i in roles)
                self.options.push(roles[i].name);
        }
    }
    
    self.showDetails = function(user){
        
    }
    
    self.deleteUser = function(user){
        
        UserService.del(user.name);   
        
        self.data().users.remove(user);
        
    }
    
    self.save = function(){
        self.data({users : UserService.saveAll(self.data)});
        
        $("#freeow").freeow("Users", "Changes have been saved.", {
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
        self.data({roles : RoleService.getAll()});
    }
    
    self.showDetails = function(role){
        
    }
    
    self.deleteRole = function(role){
        
        RoleService.del(role.name);   
        
        self.data().roles.remove(role);
        
    }
    
    self.save = function(){
        self.data({roles: RoleService.saveAll(self.data)});
        
        $("#freeow").freeow("Roles", "Changes have been saved.", {
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
    
    self.loadModel = function(){
        // no model until now
    }

    self.save = function(){
        return false;
    }

    SubViewModel.call(self, "Debug");
    
}

var VM = new ViewModel();

ko.applyBindings(VM);
