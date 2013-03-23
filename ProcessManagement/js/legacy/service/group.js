/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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


