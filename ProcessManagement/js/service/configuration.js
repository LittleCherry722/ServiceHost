SBPM.Service.Configuration = {
    _default : {
        endpoint : "configuration.php"
    },
    query : function(param, defaultvalue, callback){
        return SBPM.DB.syncQuery(this._default.endpoint, param, defaultvalue, callback);
    },
    read : function() {
        return this.query({
            "action" : "read",
        }, {}, function(data,json) {           
            if (data["code"] == "ok")
                return data["configuration"];
        });
    },
    write : function(configuration) {
        return this.query({
            "action" : "write",
            "configuration" : configuration
        }, {}, SBPM.DB.defaultOKReturnBoolean);
    }
}

