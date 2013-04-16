define([
	"knockout",
	"app",
	"underscore",
	"models/user",
	"models/message"

], 
function( ko, App, _, User, Message ) {

	var ViewModel = function() {
		var self = this;
		
		self.users = User.all;
		self.fromUserId = ko.observable();
		self.toUserId = ko.observable();
		self.title = ko.observable();
		self.content = ko.observable();
		self.userFilter = ko.observable();
		self.messages = ko.observableArray();
		
		self.send = function() {
			if (self.fromUserId() && self.toUserId()) {
				self.messages.push(new Message({
					"fromUserId" : self.fromUserId(),
					"toUserId" : self.toUserId(),
					"title" : self.title(),
					"content" : self.content()
				}));
				self.fromUserId(undefined);
				self.toUserId(undefined);
				self.title(undefined);
				self.content(undefined);
			}
		}
		self.reset = function() {
			self.fromUserId(undefined);
			self.toUserId(undefined);
			self.title(undefined);
			self.content(undefined);
			self.messages.removeAll();
		}
		self.userNameById = function(id) {
			return User.find(id).name()
		}

		self.filteredMessages = ko.computed(function() {
			var filter = self.userFilter();
			var messages = viewModel.messages;
			if (!filter) {
				return self.messages();
			} else {
				return ko.utils.arrayFilter(self.messages(), function(item) {
					return item.fromUserId() === filter || item.toUserId() === filter;
				});
			}
		}); 


	}
	var initialize = function() {
		var viewModel = new ViewModel();
		window.mView = viewModel;
		App.loadTemplate("messages", viewModel);


	}
	// Everything in this object will be the public API
	return {
		init : initialize
	}
});

