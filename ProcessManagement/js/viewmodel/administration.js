var ViewModel = function() {

    var self = this;

    self.tab = ko.observable("");
    self.tabs = ['General', 'Users', 'Roles','Groups', 'Debug'];
    self.subsites = ko.observable({
        'General' : new GeneralViewModel(),
        'Users' : new UserViewModel(),
        'Roles' : new RoleViewModel(),
        'Groups' : new GroupViewModel(),
        'Debug' : new DebugViewModel()
        
    });

    self.init = function(callback) {

        //preselect the general tab
        self.goToTab("Users");

        console.log("ViewModel: Initialized with tab '" + self.tab() + "'");
        
        callback();
    }

    self.goToTab = function(tab) {

        if (tab == self.tab())
            return;

        // load the selected tabs model & ui
        self.subsites()[tab].init();

        // set the tab for highlighting
        self.tab(tab);

    }

    self.subsite = function() {
        return self.subsites()[self.tab()];
    }

    self.close = function() {
        parent.$.fancybox.close();
    }

    self.save = function() {
        var success = true;
     
        

        for (var site in self.subsites())
            // save all tabs
            success = success && self.subsites()[site].save();

        // and re-init the current tab
        //self.subsite().init();

        if (success)
            SBPM.Notification.Info("Information", "The administration has been saved.");
        else
            SBPM.Notification.Error("Error", "An Error occured while saving the administration.");
           
    }
};

var SubViewModel = function(name) {

    // thats the extending class' context
    var self = this;

    self.name = name;
    self.data = ko.mapping.fromJS([]);
    self.initialized = false;

    self.init = function() {
        self.loadModel();
    }
}
/**
 * extends SubViewModel
 */
var GeneralViewModel = function() {

    var self = this;

    self.loadModel = function() {

        if (self.initialized)
            return;

        ko.mapping.fromJS(SBPM.Service.Configuration.read(), self.data);

        self.initialized = true;
    }

    self.save = function() {
        var success = SBPM.Service.Configuration.write(ko.toJS(self.data()));

        // if(success)
        // SBPM.Storage.set('configuration', ko.toJS(self.data()));

        self.initialized = false;

        return success;
    }

    self.dataForUI = function() {// TODO return actual data
        return [];
    }

    SubViewModel.call(self, "General");

}
/**
 * extends SubViewModel
 */
var UserViewModel = function() {

    var self = this;

    self.options = ko.observableArray();
	
    self.loadModel = function() {

        if (self.initialized)
            return;
            
        var groups = SBPM.Service.Group.getAll();
        self.options.removeAll();
        
        var groupOption = function(name,id){
        	this.groupName= name;
        	this.groupID=id;
        }
        
        for (var i in groups){
            self.options.push(new groupOption(groups[i].name, groups[i].ID));
           }
           
            //console.log(self.options());
		var transform = SBPM.Service.User.getAllUsersAndGroups();
		console.log(self.transform);

		for(var i in transform) {
			console.log(i);
			console.log(transform[i]);
			console.log(transform[i].groupID);
			self.data.push({
				'userName' : transform[i].userName,
				'userID' : transform[i].userID,
				//'groupName' : SBPM.Service.Group.getName(transform[i].groupID),
				'groupID' : ko.observableArray(transform[i].groupID),
				//'inputpoolsize' : transform[i].inputpoolsize
				
			});
		}
        console.log(self.data());
		
        

        self.initialized = true;
    }

    self.showDetails = function(user) {

    }

    self.remove = function(user) {
       SBPM.Service.User.remove(user.userID);
       self.data.remove(user);
    }

    self.create = function() {
        //var data = self.data();
        self.data.push({
            userName : "",
            groupName : undefined,
            groupID : undefined,
            userID : 'Will be assigned \n after save',
            inputpoolsize : "8"
        });
      
        $(".scrollable input.inline").last().focus();
    }

    self.save = function() {

        for (var i in self.data())
            if (self.data()[i].userName == "")
                self.data.remove(self.data()[i]);

        var toSaveData = ko.toJS(self.data());
		
		SBPM.Service.User.saveAll(toSaveData);
		
		console.log("Users");
		console.log(toSaveData);
		
        //ko.mapping.fromJS(SBPM.Service.User.saveAll(toSaveData), self.data);

        self.initialized = false;

        return true;
    }

    SubViewModel.call(self, "Users");

}
/**
 * extends SubViewModel
 */
var RoleViewModel = function() {

    var self = this;
	self.options = ko.observableArray();
	
    self.loadModel = function() {
		if(self.initialized)
			return;



		var groups = SBPM.Service.Group.getAll();
		self.options.removeAll();



        var groupOption = function(name,id){
        	this.groupName= name;
        	this.groupID=id;
        }
        
        for (var i in groups){
            self.options.push(new groupOption(groups[i].name, groups[i].ID));
           }


		var transform = SBPM.Service.Role.getAllRolesAndGroups()
		//console.log(self.transform);
		self.data([]);
		for(var i in transform) {
			//console.log(i);
			//console.log(transform[i]);
			self.data.push({
				'roleName' : transform[i].roleName,
				'roleID' : i,
				//'roles' : transform[i].roleName,
				'groupID' : ko.observableArray(transform[i].groupID)
			});
		}
}

    self.remove = function(role) {
        SBPM.Service.Role.remove(role.ID);
            self.data.remove(role);
    }


	self.create = function() {
		self.data.push({
			roleName : "",
			roleID : 'Will be assigned \n after save',
			groupID : undefined
		});
		$(".scrollable input.inline").last().focus()

	}


    self.save = function() {

        for (var i in self.data())
        if (self.data()[i].name == "")
            self.data.remove(self.data()[i]);

        var toSaveData = ko.toJS(self.data());
console.log("roles");
console.log(toSaveData);

       // ko.mapping.fromJS(SBPM.Service.Role.saveAll(toSaveData), self.data);

        self.initialized = false;

        return true;
    }

    SubViewModel.call(self, "Roles");

}

/**
 * extends SubViewModel
 */
var GroupViewModel = function() {
	var self = this;
	
	self.options = ko.observableArray();
	

	self.loadModel = function() {
		if(self.initialized)
			return;

		var roles = SBPM.Service.Role.getAll();
		self.options.removeAll();



        var roleOption = function(name,id){
        	this.rolesName= name;
        	this.rolesID=id;
        }
        
        for (var i in roles){
            self.options.push(new roleOption(roles[i].name, roles[i].ID));
           }


		var transform = SBPM.Service.Group.getallgroupsandroles()
		//console.log(self.transform);

		for(var i in transform) {
			//console.log(i);
			//console.log(transform[i]);
			self.data.push({
				'groupName' : transform[i].groupName,
				'groupID' : i,
				//'roles' : transform[i].roleName,
				'rolesID' : ko.observableArray(transform[i].roleID)
			});
		}

		//ko.mapping.fromJS(SBPM.Service.Group.getallgroupsandroles(), self.data);



		self.initialized = true;
	}

    
    self.save = function() {
    	for (var i in self.data())
            if (self.data()[i].groupName == "")
                self.data.remove(self.data()[i]);

        var toSaveData = ko.toJS(self.data());
        console.log("groups");
		console.log(toSaveData);
		//SBPM.Service.User.saveAll(toSaveData);
    	
    	
        return true;
    }    
    
    
      self.create = function() {
       self.data.push({
             groupName : "",
             groupID :"",
             rolesID :ko.observableArray()          
        });
        $(".scrollable input.inline").last().focus()

    }  
    
    self.remove = function(group){
    	//console.log(group.groupID);
    	self.data.remove(group);
    	SBPM.Service.Group.remove(group.groupID);
    }
    
    
    
        SubViewModel.call(self, "Groups");
}



/**
 * extends SubViewModel
 */
var DebugViewModel = function() {

    var self = this;

    self.loadModel = function() {
    }

    self.createUsers = function() {
        console.log("createUsers");
        if (SBPM.Service.Debug.createUsers())
            parent.location.reload();
        else
            SBPM.Notification.Error("Error", "Creating test case failed.");
    }

    self.clearDatabase = function() {
        console.log("clearDatabase");
        if (SBPM.Service.Debug.clearDatabase())
            parent.location.reload();
        else
            SBPM.Notification.Error("Error", "Creating test case failed.");
    }

    self.createProcess1 = function() {
        console.log("createProcess1");
        if (SBPM.Service.Debug.createProcess("travelapplication")) {
            parent.location.reload();
        } else
            SBPM.Notification.Error("Error", "Creating test case failed.");
    }

    self.rebuildDatabase = function() {
        console.log("clearDatabase");
        if (SBPM.Service.Debug.rebuildDatabase())
            parent.location.reload();
        else
            SBPM.Notification.Error("Error", "Creating test case failed.");
    }

    self.save = function() {
        return true;
    }

    SubViewModel.call(self, "Debug");

}
