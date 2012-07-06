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
        self.goToTab("Users");
        
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
        for(var site in self.subsites())
             // save all tabs
             self.subsites()[site].save();
        
        
         // and re-init the current tab
         self.subsite().init();

        SBPM.Notification.Info("Information", "The administration has been saved.");  
    }
    
    self.initChosen = function(elements){
        $(elements).find('.chzn-select').chosen();
    }
    
    self.init();
};
 
 
var SubViewModel = function(name){

    // thats the extending class' context
    var self = this;

    self.name = name;
    self.template = name.toLowerCase();
    self.data = ko.mapping.fromJS([]);
    
    self.init = function(){
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
    
    var initialized = false;
    
    self.options = ko.observableArray();
    
    self.loadModel = function(){
        
        if(initialized)
            return;
        
        ko.mapping.fromJS(SBPM.Service.User.getAll(), self.data);
        
        var roles = SBPM.Service.Role.getAll();
        self.options.removeAll();
        for(var i in roles)
            self.options.push(roles[i].name);
        $('.chzn-select').trigger("liszt:updated");

        key('c', function() {
            self.createUser();
        });
        
        var qtipStyle = "ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow";
        var qtipPositionAt = 'right top';
        var qtipPositionMy = 'left bottom';
        
        $('#usersBtn').focus();
        $('#usersBtn').qtip({
            content : {
                text : 'Create new role\n: Press "c"'
            },
            position : {
                at : qtipPositionAt,
                my : qtipPositionMy,
                viewport : $(window),
                adjust : {
                    method : 'mouse',
                    x : 0,
                    y : 0
                }
            },
            style : {
                classes : qtipStyle
            }
        });
        
        initialized = true;
    }
    
    self.showDetails = function(user){
        
    }
    
    self.deleteUser = function(user){
        if(SBPM.Service.User.remove(user.id()))
             self.data.remove(user);
    }
    
    self.createUser = function(){
        var data = self.data();
        data.push({id: 0, name: "", roles: "", active: 1});
        self.data(data);
        $(".scrollable input.inline").last().focus()
    }
    
    self.save = function(){

        for(var i in self.data())
            if(self.data()[i].name == "")
                self.data.remove(self.data()[i]);   
        
        var data = SBPM.Service.User.saveAll(ko.toJS(self.data()));
        
        ko.mapping.fromJS(data, self.data);
        
        initialized = false;
    }
    
    SubViewModel.call(self, "Users");
    
}

/**
 * extends SubViewModel 
 */
var RoleViewModel = function(){
    
    var self = this;
    
    var initialized = false;
    
    self.loadModel = function(){
        
        if(initialized)
            return;
        
        ko.mapping.fromJS(SBPM.Service.Role.getAll(), self.data);
        
        key('c', function() {
            self.createUser();
        });
        
        var qtipStyle = "ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow";
        var qtipPositionAt = 'right top';
        var qtipPositionMy = 'left bottom';
        
        $('#rolesBtn').qtip({
            content : {
                text : 'Create new role\n: Press "c"'
            },
            position : {
                at : qtipPositionAt,
                my : qtipPositionMy,
                viewport : $(window),
                adjust : {
                    method : 'mouse',
                    x : 0,
                    y : 0
                }
            },
            style : {
                classes : qtipStyle
            }
        });
        
        initialized = true;
    }
    
    self.deleteRole = function(role){
        if(SBPM.Service.Role.remove(role.ID))
            self.data.remove(role);
    }
    
    self.createRole = function(){ // TODO why push by itself doesnt work?
        var data = self.data();
        data.push({ID: 0, name: "", active: 1});
        self.data(data);
        $(".scrollable input.inline").last().focus()

    }
    
    self.save = function(){
        
        for(var i in self.data())
            if(self.data()[i].name == "")
                self.data.remove(self.data()[i]);

        self.data(SBPM.Service.Role.saveAll(ko.toJS(self.data())));
        //ko.mapping.fromJS(SBPM.Service.Role.saveAll(self.data()), self.data);

        initialized = false;
    }

    SubViewModel.call(self, "Roles");
    
}

/**
 * extends SubViewModel 
 */
var DebugViewModel = function(){

    var self = this;
    
    self.loadModel = function(){
    }

    self.createUsers = function(){
        console.log("createUsers");
        if(SBPM.Service.Debug.createUsers())
            parent.location.reload();
        else
            SBPM.Notification.Error("Information", "Creating test case failed.");
    }
    
    self.clearDatabase = function(){
        console.log("clearDatabase");
        if(SBPM.Service.Debug.clearDatabase())
            parent.location.reload();
        else
            SBPM.Notification.Error("Information", "Creating test case failed.");        
    }

    self.createProcess1 = function(){
        console.log("createProcess1");
        if(SBPM.Service.Debug.createProcess("applicationforleave")){
            parent.location.reload();
        } else
            SBPM.Notification.Error("Information", "Creating test case failed."); 
    }

    self.save = function(){
        return false;
    }

    SubViewModel.call(self, "Debug");
    
}
