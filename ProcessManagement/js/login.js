var LoginViewModel = function() {
window.Lview = this;

self = this;
self.user = ko.observable("");
self.pass = ko.observable("");

self.login = function(){
	console.log("login: "+ self.user() +" "+self.pass());
	

	

$.ajax({
url : '/user/login',
type : "POST",
data : '{user: '+self.user()+', pass: '+self.pass()+'}',
async : true, // defaults to false
//dataType : "json",
//contentType : "application/json; charset=UTF-8",
success : function(data, textStatus, jqXHR) {
	console.log("success")

}, error : function(jqXHR, textStatus, error) {
	console.log("Error")
	console.log(error)
}, complete : function(jqXHR, textStatus) {
	console.log("complete")

}
});

	
}
	
}

ko.applyBindings(new LoginViewModel());


