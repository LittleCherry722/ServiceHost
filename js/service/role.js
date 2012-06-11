var RoleService = {
    getAll : function() {
        return DB.syncQuery("groups.php", {
            "action" : "getall"
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["groups"];
        });
    },
    getById : function(roleId) {

    },
    del : function(role) {
        return syncQuery(db_directory + "users.php", {
            "username" : name,
            "action" : "remove"
        }, false, defaultRemoveReturn);

    },
    saveAll : function(roles) {
        return DB.syncQuery("groups.php", {
            "action" : "save",
            "groups" : roles
        }, {}, function(json) {
            if (json["code"] == "ok")
                return json["groups"];
        });
    }
}
