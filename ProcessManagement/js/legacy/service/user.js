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
    	getAllUsersAndGroups : function() {
        return this.query({
            "action" : "getAllUsersAndGroups"
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
        return SBPM.DB.syncQuery("group.php", {
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
     * deletes a user by his Id
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
     * @param {array of {userId, userName, groupId[]}} users
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
