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

