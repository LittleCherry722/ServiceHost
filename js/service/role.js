SBPM.Service.Role = {
    _default : {
        endpoint : "groups.php"
    },
    query : function(param, defaultvalue, callback){
        return SBPM.DB.syncQuery(this._default.endpoint, param, defaultvalue, callback);
    },
    /**
     *get all roles 
     */
    getAll : function() {
        return this.query({
            "action" : "getall"
            }, {}, function(json) {
            if (json["code"] == "ok")
                return json["groups"];
        });
    },
    /**
     * 
     * @param {Object} roleId
     */
    getById : function(roleId) {
        Utilities.unimplError(arguments.callee.name);
    },
    /**
     * 
     * @param {Object} roleName
     */
    getByName : function(roleName) {
        Utilities.unimplError(arguments.callee.name);
    },
    /**
     * 
     * @param {Object} role
     */
    remove : function(roleId) {
        return this.query({
            "groupid" : roleId,
            "action" : "remove"
        }, false, SBPM.DB.defaultRemoveReturn);
    },
    /**
     * 
     * @param {Object} roles
     */
    saveAll : function(roles) {
        return this.query({
            "action" : "save",
            "groups" : roles
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["groups"];
        });
    },
    
    getRolesByUser : function(name){
    	return this.query({
    		//To Do
    		
    }
    )
    
    
    
}
}