var ViewModel = function() {

    var self = this;

    self.tab = ko.observable("");
    self.tabs = ['General', 'Users', 'Roles', 'Debug'];
    self.subsites = ko.observable({
        'General' : new GeneralViewModel(),
        'Users' : new UserViewModel(),
        'Roles' : new RoleViewModel(),
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
        self.subsite().init();

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

        ko.mapping.fromJS(SBPM.Service.User.getAll(), self.data);

        var roles = SBPM.Service.Role.getAll();
        self.options.removeAll();
        
        for (var i in roles)
            self.options.push(roles[i].name);

        self.initialized = true;
    }

    self.showDetails = function(user) {

    }

    self.remove = function(user) {
        if (user.id == "0" || SBPM.Service.User.remove(user.id()))
            self.data.remove(user);
    }

    self.create = function() {
        var data = self.data();
        data.push({
            id : "0",
            name : "",
            roles : [""],
            active : "1",
            inputpoolsize : "8"
        });
        self.data(data);
        $(".scrollable input.inline").last().focus();
    }

    self.save = function() {

        for (var i in self.data())
            if (self.data()[i].name == "")
                self.data.remove(self.data()[i]);

        var toSaveData = ko.toJS(self.data());

        ko.mapping.fromJS(SBPM.Service.User.saveAll(toSaveData), self.data);

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

    self.loadModel = function() {

        if (self.initialized)
            return;

        ko.mapping.fromJS(SBPM.Service.Role.getAll(), self.data);

        self.initialized = true;
    }

    self.remove = function(role) {
        if (SBPM.Service.Role.remove(role.ID))
            self.data.remove(role);
    }

    self.create = function() {// TODO why push by itself doesnt work?
        var data = self.data();
        data.push({
            ID : 0,
            name : "",
            active : 1
        });
        self.data(data);
        $(".scrollable input.inline").last().focus()

    }

    self.save = function() {

        for (var i in self.data())
        if (self.data()[i].name == "")
            self.data.remove(self.data()[i]);

        var toSaveData = ko.toJS(self.data());

        ko.mapping.fromJS(SBPM.Service.Role.saveAll(toSaveData), self.data);

        self.initialized = false;

        return true;
    }

    SubViewModel.call(self, "Roles");

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
