define ([
	"knockout",
	"app",
	"underscore",
	"models/user",
	"models/userMessage", 
        "notify"
],
	function (ko, App, _, User, UserMessage, notify) {
		var ViewModel = function () {
			var self = this;

			self.users = User.all;
			self.fromUser = ko.observable ();
			self.toUser = ko.observable ();
			self.title = ko.observable ();
			self.content = ko.observable ();

			self.userFilter = ko.observable ();
			self.messages = ko.observableArray ();
			self.currentTab = ko.observable ();


			self.currentTab.subscribe(function(){
				$('.message').addClass('hide');
			});
                        
                        
			self.send = function () {
				if (self.toUser() && self.title() && self.content()) {
					var message = new UserMessage ({
						"toUser": self.toUser (),
						"title": self.title (),
						"content": self.content ()
					});
					message.id(self.messages().length);
					message.save();
					self.messages.unshift (message);
					self.fromUser (undefined);
					self.toUser (undefined);
					self.title (undefined);
					self.content (undefined);
				}
                                else {
                                    notify.warning("Error", "Please fill out all input fields.");
                                }
			};

			self.reset = function () {
				self.fromUser (undefined);
				self.toUser (undefined);
				self.title (undefined);
				self.content (undefined);
				self.messages.removeAll ();
			};

			self.userNameById = function (id) {
				return User.find(id).name()
			};

			self.selectInbox = ko.computed (function () {
				var filter = self.userFilter ();
				var messages = self.messages;
				if (!filter) {
					return self.messages ();
				} else {
					return ko.utils.arrayFilter (self.messages (), function (item) {
						return item.toUser () === filter;
					});
				}
			});

			self.openMessage = function (message) {
				var messageContainer = $('#' + self.messageContainerId(message.id())),
					targetElement = $('.message', messageContainer);

				targetElement.toggleClass('hide');
				$('.message').not(targetElement).addClass('hide');
			};

			self.replyMessage = function (message) {
				self.title("Re: " + message.title());
				self.fromUser(message.toUser());
				self.toUser(message.fromUser());

				var reply = "\n\n";
				reply += "On " + message.formattedDate() + ", " + self.userNameById(message.fromUser()) +" wrote:\n";
				reply += message.content().replace(/^(.*)$/mg, '> $1');
				self.content(reply);

				return true;		// follow the actual link behind <a>
			};

			self.messageContainerId = function(messageId) {
				return 'id_message_' + messageId;
			};
		};

		var viewModel;

		var setView = function (tab) {
			if ('string' === typeof tab) {
				viewModel.currentTab (tab);
			} else {
				viewModel.currentTab ('messagesOverview');
			}
		};

		var initialize = function (tab) {
			viewModel = new ViewModel ();
			window.mView = viewModel;
			App.loadTemplate ("messages", viewModel, undefined, function () {
				setView (tab);
			});
		};


		return {
			init: initialize,
			setView: setView
		}
	});