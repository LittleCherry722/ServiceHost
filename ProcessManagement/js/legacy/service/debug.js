SBPM.Service.Debug = {
    _default : {
        endpoint : "debug.php"
    },
    query : function(param, defaultvalue, callback){
        return SBPM.DB.syncQuery(this._default.endpoint, param, defaultvalue, callback);
    },
    createUsers : function() {
        return this.query({
            "action" : "user",
        }, {}, SBPM.DB.defaultOKReturnBoolean);
    },
    clearDatabase : function() {
        return this.query({
            "action" : "clear",
        }, {}, SBPM.DB.defaultOKReturnBoolean);
    },
    rebuildDatabase : function() {
        return this.query({
            "action" : "rebuild",
        }, {}, SBPM.DB.defaultOKReturnBoolean);
    },
    createProcess : function($process) {
        return this.query({
            "action" : "process",
            "process" : $process
        }, {}, SBPM.DB.defaultOKReturnBoolean);
    },
}
