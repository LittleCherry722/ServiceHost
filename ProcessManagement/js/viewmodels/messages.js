define ([
	"knockout",
	"app",
	"underscore",
	"models/user",
	"models/userMessage"
],
	function (ko, App, _, User, UserMessage) {
		var ViewModel = function () {
			var self = this;

			self.users = User.all;
			self.fromUserId = ko.observable ();
			self.toUserId = ko.observable ();
			self.title = ko.observable ();
			self.content = ko.observable ();
			self.userFilter = ko.observable ();
			self.messages = ko.observableArray ();
			self.currentTab = ko.observable ();

			self.send = function () {
				if (self.fromUserId () && self.toUserId ()) {
					self.messages.push (new UserMessage ({
						"fromUserId": self.fromUserId (),
						"toUserId": self.toUserId (),
						"title": self.title (),
						"content": self.content ()
					}));
					self.fromUserId (undefined);
					self.toUserId (undefined);
					self.title (undefined);
					self.content (undefined);
				}
			}

			self.reset = function () {
				self.fromUserId (undefined);
				self.toUserId (undefined);
				self.title (undefined);
				self.content (undefined);
				self.messages.removeAll ();
			}

			self.userNameById = function (id) {
				return User.find (id).name ()
			}

			self.selectInbox = ko.computed (function () {
				var filter = self.userFilter ();
				var messages = self.messages;
				if (!filter) {
					return self.messages ();
				} else {
					return ko.utils.arrayFilter (self.messages (), function (item) {
						return item.toUserId () === filter;
					});
				}
			});

			self.openMessage = function (message) {
				var messageContainer = $('#' + self.messageContainerId(message.id())),
					targetElement = $('.message', messageContainer);

				targetElement.toggleClass('hide');
				$('.message').not(targetElement).addClass('hide');
			}

			self.messageContainerId = function(messageId) {
				return 'id_message_' + messageId;
			}
		}

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
		}


		return {
			init: initialize,
			setView: setView
		}
	});