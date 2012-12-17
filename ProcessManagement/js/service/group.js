SBPM.Service.Group = {
	    _default : {
        endpoint : "group.php"
    },
	    query : function(param, defaultvalue, callback) {
        return SBPM.DB.syncQuery(this._default.endpoint, param, defaultvalue, callback);
    },
    
    /**
     *get all groups
     */    
        getAll : function() {
        return this.query({
            "action" : "getall"
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["groups"];
        });
    },
	getallgroupsandroles : function() {
        return this.query({
            "action" : "getallgroupsandroles"
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["groups"];
        });
    },
    /**
     *
     * @param {Object} group
     */
    remove : function(groupId) {
        return this.query({
            "groupid" : groupId,
            "action" : "remove"
        }, false, SBPM.DB.defaultRemoveReturn);
    },
        /**
     *
     * @param {Object} groupID
     */
    getName : function(groupId) {
        return this.query({
            "groupid" : groupId,
            "action" : "getname"
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["name"];
        });
    },
    /**
     * saves a list of groups
     *
     * @param {array of {name, ID}} users
     */
    saveAll : function(users) {
       	
        return this.query({
            "action" : "save",
            "groups" : users
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["groups"];
        });
    }
}


