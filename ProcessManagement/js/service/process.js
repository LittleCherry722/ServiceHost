SBPM.Service.Process = {
    _default : {
        endpoint : "process.php"
    },
    query : function(param, defaultvalue, callback) {
        return SBPM.DB.syncQuery(this._default.endpoint, param, defaultvalue, callback);
    },
    processExists : function(name) {
        return name && name.length > 0 && SBPM.Service.Process.getProcessID(name) > 0;
    },
    saveProcess : function(name, forceOverwrite, saveAs, context) {

        if(!context)
            context = parent;
 
        // try to set another default name
        name = name || context.SBPM.VM.processVM.processName();

        if (!name)
            return false;

        var graphAsJSON = context.gv_graph.saveToJSON();

        var startSubjects = [];

        for (var subject in context.gv_graph.subjects)
            startSubjects.push(SBPM.Service.Role.getByName(subject));

        var startSubjectsAsJSON = JSON.stringify(startSubjects);

        var id;

        if(saveAs){
            // if process already exists
            if(!forceOverwrite && this.processExists(name))
                return {code:"duplicated"}; // return error code
            
            id = this.createProcess(name, forceOverwrite);
        } else
            if(!this.processExists(name))
                id = this.createProcess(name);

        return this.query({
            "processid" : id || this.getProcessID(name),
            "action" : "save",
            "graph" : graphAsJSON,
            "subjects" : startSubjectsAsJSON
        }, false, defaultOKReturnBoolean);
    },
    // create/remove process
    createProcess : function(processname, forceOverwrite) {
        // if the process should be overwritten
        if (forceOverwrite)
            this.deleteProcess(processname);

        return this.query({
            "processname" : processname,
            "action" : "new"
        }, 0, SBPM.DB.defaultIDReturn);
    },
    
    createProcessFromTable : function(subjects,messages){
    			gf_createFromTable(subjects, messages);
        updateListOfSubjects();
    },
    deleteProcess : function(processname) {
        return this.query({
            "processname" : processname,
            "action" : "remove"
        }, false, SBPM.DB.defaultRemoveReturn);
    },
    deleteProcessByID : function(processid) {
        return this.query({
            "processid" : processid,
            "action" : "remove"
        }, false, SBPM.DB.defaultRemoveReturn);
    },
    // get processes
    getProcessName : function(processid) {
        return this.query({
            "processid" : processid,
            "action" : "getname"
        }, "", function(json) {
            if (json["code"] == "ok")
                return json["name"];
        });
    },
    getProcessID : function(processname) {
        return this.query({
            "processname" : processname,
            "action" : "getid"
        }, 0, defaultIDReturn);
    },
    getAllProcesses : function(limit) {
        return this.query({
            "action" : "getallprocesses",
            "limit" : limit
        }, "", function(json) {
            if (json["code"] == "ok")
                return json["processes"];
        });
    },
    getAllProcessesIDs : function(limit) {
        return this.query({
            "action" : "getallprocessesids",
            "limit" : limit
        }, "", function(json) {
            if (json["code"] == "ok")
                return json["ids"];
        });
    },
    getAllStartableProcessIDs : function(userid) {
        return this.query({
            "action" : "getallstartable",
            "userid" : userid
        }, "", function(json) {
            if (json["code"] == "ok")
                return json["ids"];
        });
    },
    // load/save graph
    loadGraph : function(processid) {
        return this.query({
            "processid" : processid,
            "action" : "load"
        }, "", function(json) {
            if (json["code"] == "ok") {
                try {
                    return json["graph"];
                } catch (e) {
                    return "{}";
                }
            } else {
                return "{}";
            }
        });
    },
    saveGraph : function(processid, graphAsJSON, startSubjectsAsJSON) {
        return this.query({
            "processid" : processid,
            "action" : "save",
            "graph" : graphAsJSON,
            "subjects" : startSubjectsAsJSON
        }, false, SBPM.DB.defaultOKReturnBoolean);
    },
}
