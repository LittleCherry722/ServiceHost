var LoginViewModel = function() {
window.Lview = this;

self = this;
self.user = ko.observable("");
self.pass = ko.observable("");

self.login = function(){
	console.log("login: "+ self.user() +" "+self.pass());
	
var data = '{"user" : "'+self.user()+'" , "pass" : "'+self.pass()+'"}';
self.pass = ko.observable("");	 
console.log(data);

$.ajax({
url : '/user/login',
type : "POST",
data : data,
async : true, // defaults to false
dataType : "json",
contentType : "application/json; charset=UTF-8",
success : function(data, textStatus, jqXHR) {
	window.location = "http://localhost/ProcessManagement/index.html";

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


