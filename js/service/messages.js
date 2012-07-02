SBPM.Service.Message = {
    _default : {
        endpoint : "messages.php"
    },
    query : function(param, defaultvalue, callback){
        return SBPM.DB.syncQuery(this._default.endpoint, param, defaultvalue, callback);
    },
    countNewMessages : function(userId){
        return this.query({
            "action" : "count",
            "userid" : userId
            }, {}, function(json) {
            if (json["code"] == "ok")
                return json["count"] ? json["count"] : 0;
        });
    }
}
