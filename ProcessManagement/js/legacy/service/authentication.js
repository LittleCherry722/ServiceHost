SBPM.Service.Authentication = {
    _default : {
        endpoint : "auth.php"
    },
    query : function(param, defaultvalue, callback) {
        return SBPM.DB.syncQuery(this._default.endpoint, param, defaultvalue, callback);
    },
    login : function(name, password) {
        return this.query({
            "username" : name,
            "password" : password,
            "action" : "login"
        }, {}, function(json) {
            return json;
        });
    },
    logout : function(userId) {
        Utilities.unimplError(arguments.callee.name);
    }
}

