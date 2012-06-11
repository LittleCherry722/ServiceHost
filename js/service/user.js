var UserService = {
    /**
     *  gets all users
     */
    getAll : function() {
        return DB.syncQuery("users.php", {
            "action" : "getall"
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["users"];
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
     *  gets a list of users by a role id
     *
     * @param {int} roleId
     */
    getByRoleId : function(roleId) {
        return DB.syncQuery("groups.php", {
            "action" : "getallusers",
            "groupid" : roleId
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["users"];
        });
    },
    /**
     * deletes a user by his ID
     *
     * @param {int} userId
     */
    del : function(user) {
        return DB.syncQuery("users.php", {
            "action" : "remove",
            "username" : user
        }, false, DB.defaultRemoveReturn);
    },
    /**
     * saves a list of users
     *
     * @param {array of {id, name, active, roles : [roleId1, ...]}} users
     */
    saveAll : function(users) {
        return syncQuery("users.php", {
            "action" : "save",
            "users" : users
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["users"];
        });
    }
}
