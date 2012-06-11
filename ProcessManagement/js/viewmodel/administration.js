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
    
    self.goToTab = function(tab) { 

        self.subsites()[tab].init();

        self.tab(tab);
        
        console.log(tab);
        
    }
       
    self.preloadTemplates = function(){
        for(var svm in self.subsites()){
            $.get({url: 'include/administration/'+self.subsites()[svm].name.toLowerCase()+'.tmpl',
                   success: function(template){
                        $('body').prepend(template);
                   }
                  });
        }
    };
 
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
    
    self.goToTab("General");
    // self.preloadTemplates();
};
 
 
var SubViewModel = function(name, template){

    var self = this;

    self.name = name;
    self.template = template;
    self.data = ko.observable("");
    
    self.init = function(){
        console.log("loading model.");
        self.loadModel();
    }

}


var GeneralViewModel = function(){
        
    var self = this;
       
    self.loadModel = function(){
        self.data({name : "jens", attribut : "test"});
        Utilities.unimplError("loadModel");
    }
    
    self.save = function(){
        Utilities.unimplError("save");
    }

    SubViewModel.call(self, "General", "generalTempl");

}


var UserViewModel = function(){
       
    var self = this;
    
    self.options = ko.observableArray();
    
    self.loadModel = function(){
        self.data({users : UserService.getAll()});
        
        console.log(self.data());
        
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
    
    SubViewModel.call(self, "Users", "usersTempl");
    
}

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

    SubViewModel.call(self, "Roles", "rolesTempl");
    
}

var DebugViewModel = function(){

    var self = this;
    
    self.loadModel = function(){
        Utilities.unimplError("load model.");
    }

    self.save = function(){
        return false;
    }

    SubViewModel.call(self, "Debug", "debugTempl");
    
}

var VM = new ViewModel();

console.log("Applying bindings.");
ko.applyBindings(VM);