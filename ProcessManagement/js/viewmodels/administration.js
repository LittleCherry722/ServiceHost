define([
	"knockout",
	"app",
	"underscore"
], function( ko, App, _ ) {

	var ViewModel = function() {

		this.tabs = tabs;

		this.currentTab = currentTab;

		this.save = function() {
			var success = true;

			for(var site in this.subsites()) {
				// save all tabs
				success = success && this.subsites()[site].save();
			}

			// and re-init the current tab
			this.subsite().init();

			if(success) {
				SBPM.Notification.Info("Information", "The administration has been saved.");
			} else {
				SBPM.Notification.Error("Error", "An Error occured while saving the administration.");
			}
		}
	}

	var tabs = ['General', 'Users', 'Roles', 'Groups', 'Debug'];

	var currentTab = ko.observable("Users");

	currentTab.subscribe(function( newTab ) {
		App.loadSubView( "administration/" + newTab.toLowerCase() );
	});


	var initialize = function( subSite ) {
		var viewModel;

		viewModel = new ViewModel();

		App.loadTemplate( "administration", viewModel, null, function() {
			currentTab( subSite || tabs[0] )
		});

	}
	
	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


/**
 * extends SubViewModel
 */
// var UserViewModel = function() {

//   var self = this;

//   self.options = ko.observableArray();

//   self.loadModel = function() {

//     if(self.initialized)
//       return;
//     self.data.removeAll();// TODO Remove for better binding

//     var groups = SBPM.Service.Group.getAll();
//     self.options.removeAll();

//     var groupOption = function(name, id) {
//       this.groupName = name;
//       this.groupID = id;
//     }
//     for(var i in groups) {
//       self.options.push(new groupOption(groups[i].name, groups[i].ID));
//     }


//     var transform = SBPM.Service.User.getAllUsersAndGroups();


//     for(var i in transform) {

//       self.data.push({
//         'userName' : transform[i].userName,
//         'userID' : transform[i].userID,
//         'groupID' : ko.observableArray(transform[i].groupID),
//         'userActive': ko.observable(self.isTrue(transform[i].userActive))
				

//       });
			
			
			
//     }



//     self.initialized = true;
		

		
		
//   }

//     self.isTrue = function(value){
//         if(value == 1){
//         return true;
//       }else{
//         return false;
//       }
//     }

//   self.showDetails = function(user) {

//   }

//   self.remove = function(user) {
//     self.data.remove(user);
//     SBPM.Service.User.remove(user.userID);
//   }

//   self.create = function() {

//     self.data.push({
//       userName : "",
//       groupID : ko.observableArray(),
//       userID : 'Will be assigned \n after save',
//       userActive: ko.observable(true)
//     });

//     $(".scrollable input.inline").last().focus();
//   }

//   self.save = function() {
// var toSaveData = new Array();

//     for(var i in self.data()) {

			
//       if(ko.isObservable(self.data()[i].groupID)){
//       self.data()[i].groupID = self.data()[i].groupID();
//       }

			
//       if(ko.isObservable(self.data()[i].userActive)){
//         if(self.data()[i].userActive()){
//           self.data()[i].userActive = 1
//         }else{
//           self.data()[i].userActive = 0
//         }

//       }

			

//       if(! self.data()[i].userName == ""){
//         toSaveData.push(self.data()[i]);

//       }
	
//     }
//     self.data(toSaveData);
		


		
//     SBPM.Service.User.saveAll(toSaveData);
		
//     //ko.mapping.fromJS(SBPM.Service.User.saveAll(toSaveData), self.data);
//     self.loadModel();
//     self.initialized = false;

//     return true;
//   }

//   SubViewModel.call(self, "Users");

// }
/**
 * extends SubViewModel
 */
// var RoleViewModel = function() {

//   var self = this;
//   self.options = ko.observableArray();

//   self.loadModel = function() {

//     if(self.initialized)
//       return;
			
	
//     self.data.removeAll();// TODO Remove for better binding
		
//     var groups = SBPM.Service.Group.getAll();
//     self.options.removeAll();

//     var groupOption = function(name, id) {
//       this.groupName = name;
//       this.groupID = id;
//     }
//     for(var i in groups) {
//       self.options.push(new groupOption(groups[i].name, groups[i].ID));
//     }

//     var transform = SBPM.Service.Role.getAllRolesAndGroups()
//     for(var i in transform) {
//       self.data.push({
//         'roleName' : transform[i].roleName,
//         'roleID' : i,
//         'groupID' : ko.observableArray(transform[i].groupID)
//       });
//     }
//     self.initialized=true;
//   }

//   self.remove = function(role) {
//     self.data.remove(role);
//     SBPM.Service.Role.remove(role.roleID);
//   }

//   self.create = function() {
//     self.data.push({
//       roleName : "",
//       roleID : 'Will be assigned \n after save',
//       groupID : ko.observableArray()
//     });
//     $(".scrollable input.inline").last().focus()

//   }

//   self.save = function() {
// var toSaveData = new Array();
//     for(var i in self.data()) {

//       if(ko.isObservable(self.data()[i].groupID))
//       self.data()[i].groupID = self.data()[i].groupID();

//       if(! self.data()[i].roleName == "")
//         toSaveData.push(self.data()[i]);

//     }
//     self.data(toSaveData);
		

		
//     SBPM.Service.Role.saveAll(toSaveData);
//     self.loadModel();
		
				
//     // ko.mapping.fromJS(SBPM.Service.Role.saveAll(toSaveData), self.data);

//     self.initialized = false;

//     return true;
//   }

//   SubViewModel.call(self, "Roles");

// }
/**
 * extends SubViewModel
 */
// var GroupViewModel = function() {
//   var self = this;

//   self.options = ko.observableArray();

//   self.loadModel = function() {
//     if(self.initialized)
//       return;
//     self.data.removeAll();// TODO Remove for better binding
		
//     var roles = SBPM.Service.Role.getAll();
//     self.options.removeAll();

//     var roleOption = function(name, id) {
//       this.rolesName = name;
//       this.rolesID = id;
//     }
//     for(var i in roles) {
//       self.options.push(new roleOption(roles[i].name, roles[i].ID));
//     }

//     var transform = SBPM.Service.Group.getAll()

//     for(var i in transform) {
//       self.data.push({
//         'groupName' : transform[i].name,
//         'groupID' : transform[i].ID
//       });
//     }

//     //ko.mapping.fromJS(SBPM.Service.Group.getallgroupsandroles(), self.data);

//     self.initialized = true;
//   }

//   self.save = function() {
//     var toSaveData = new Array();
		
//     for(var i in self.data()) {
//       if(! self.data()[i].groupName == "")
//         toSaveData.push(self.data()[i]);
//     }
//      self.data(toSaveData);
		

		
//     SBPM.Service.Group.saveAll(toSaveData);
//     self.loadModel();
//     self.initialized = false;
			
//     return true;
//   }

//   self.create = function() {
//     self.data.push({
//       groupName : "",
//       groupID : 'Will be assigned \n after save',
//     });
//     $(".scrollable input.inline").last().focus()

//   }

//   self.remove = function(group) {
//     self.data.remove(group);
//     SBPM.Service.Group.remove(group.groupID);
//   }

//   SubViewModel.call(self, "Groups");
// }

