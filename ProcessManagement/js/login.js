var LoginViewModel = function() {
window.Lview = this;

self = this;
self.name = ko.observable("");
self.pass = ko.observable("");

self.login = function(){
	console.log("login: "+ self.name() +" "+self.pass());
	
	http://localhost:8080/user/login
	
}
	
}

ko.applyBindings(new LoginViewModel());


