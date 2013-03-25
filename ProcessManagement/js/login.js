var LoginViewModel = function() {
	//window.Lview = this;

	self = this;
	self.user = ko.observable("");
	self.pass = ko.observable("");



	self.isBackendAlive = function() {
		$.ajax({
			url : '/isalive',
			type : "GET",
			async : false, // defaults to false
			success : function(data, textStatus, jqXHR) {
				return true
			},
			error : function(jqXHR, textStatus, error) {
				return false
			},
			complete : function(jqXHR, textStatus) {
			}
		});
	}; 





	self.login = function() {
		
		if (!isBackendAlive ){
			alert("Can not reach backend!");
		}
		
		var data = { user: self.user(), pass: self.pass()};
		data = JSON.stringify(data); 
		self.pass("");

		$.ajax({
			url : '/user/login',
			type : "POST",
			data : data,
			async : true, // defaults to false
			dataType : "json",
			contentType : "application/json; charset=UTF-8",
			success : function(data, textStatus, jqXHR) {
				window.location = "./#/";

			},
			error : function(jqXHR, textStatus, error) {
				alert("E-Mail or Password wrong, please try again.");

			},
			complete : function(jqXHR, textStatus) {

			}
		});

	}
}

ko.applyBindings(new LoginViewModel());

