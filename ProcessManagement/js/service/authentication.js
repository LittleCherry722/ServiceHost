var Authenticaion = {
    login : function(name, password) {
        return syncQuery(db_directory + "auth.php", {
            "username" : name,
            "password" : password,
            "action" : "login"
        }, {}, function(json) {
            return json;
        });
    },
    logout : function(userId){
        Utilities.unimplError(arguments.callee.name);
    }
}
