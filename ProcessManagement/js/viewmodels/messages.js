define([
	"knockout",
	"app",
	"underscore",
	"models/user",
	"models/message"

], function( ko, App, _, User, Message ) {



	var ViewModel = function() {
		this.users = User.all;
		this.fromUserId = ko.observable(undefined);
		this.toUserId = ko.observable(undefined);
		this.title = ko.observable();
		this.content = ko.observable();
		this.messages = ko.observableArray();		
		this.send = function(){
		console.log("send");	
		console.log(this.fromUserId());
		console.log(this.toUserId());
		this.messages.push(new Message({"formUserId":this.fromUserId(),"toUserId":this.toUserId(),"title":this.title(),"content":this.content()}));
		console.log(this.messages());
		}
		this.reset = function(){
		this.fromUserId(undefined);
		this.toUserId(undefined);
		this.title(undefined);
		this.content(undefined);
		this.messages.removeAll();
		}
	}

	var initialize = function() {
		var viewModel = new ViewModel();
		window.mView = viewModel;
		App.loadTemplate( "messages", viewModel );
		$(".chzn-select").chosen();
	}

	// Everything in this object will be the public API
	return {
		init: initialize
	}
});


