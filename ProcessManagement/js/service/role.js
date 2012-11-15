SBPM.Service.Role = {
    _default : {
        endpoint : "roles.php"
    },
    query : function(param, defaultvalue, callback) {
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
                return json["roles"];
        });
    },
    /**
     *get all roles and their users
     */
    getAllRolesAndUsers : function() {
        return this.query({
            "action" : "getallrolesandusers"
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
        return this.query({
            "groupid" : roleId,
            "action" : "getbyid"
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["group"];
        });
    },
    /**
     *
     * @param {Object} roleName
     */
    getByName : function(roleName) {
        return this.query({
            "groupname" : roleName,
            "action" : "getbyname"
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["group"];
        });
    },
    /**
     *
     * @param {Object} role
     */
    remove : function(roleId) {
        return this.query({
            "roleid" : roleId,
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

    getRolesByUser : function(name) {
        return this.query({
            //To Do

        })

    }
}