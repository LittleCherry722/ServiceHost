SBPM.Service.User = {
    _default : {
        endpoint : "users.php"
    },
    query : function(param, defaultvalue, callback){
        return SBPM.DB.syncQuery(this._default.endpoint, param, defaultvalue, callback);
    },
    /**
     *  gets all users
     */
    getAll : function() {
        return this.query({
            "action" : "getall"
        }, {}, function(data,json) {           
            if (data["code"] == "ok")
                return data["users"];
        });
    },
    /**
     *  gets a users by a his id
     *
     * @param {int} userId
     */
    getById : function(userId) {
        Utilities.unimplError(arguments.callee.name);
    },
    /**
     *  gets a users by a his id
     *
     * @param {int} userName
     */
    getByName : function(userName) {
        Utilities.unimplError(arguments.callee.name);
    },
    /**
     *  gets a list of users by a role id
     *
     * @param {int} roleId
     */
    getByRoleId : function(roleId) {
        return SBPM.DB.syncQuery("groups.php", {
            "action" : "getallusers",
            "groupid" : roleId
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["users"];
        });
    },
    /**
     * gets a list of roles by a user id
     * 
     * @param {int} userId 
     */
    getRoleByUserId : function(userId) {
        return this.query({
            "action" : "getallgroupsbyuserid",
            "userid" : userId
        }, {}, function(data) {           
            if (data["code"] == "ok")
                return data["groups"];
        });
    },
    /**
     * deletes a user by his ID
     *
     * @param {int} userId
     */
    remove : function(userId) {
        return this.query({
            "action" : "remove",
            "userid" : userId
        }, false, SBPM.DB.defaultRemoveReturn);
    },
    /**
     * saves a list of users
     *
     * @param {array of {id, name, active, roles : [roleId1, ...]}} users
     */
    saveAll : function(users) {
        return this.query({
            "action" : "save",
            "users" : users
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["users"];
        });
    }
}
