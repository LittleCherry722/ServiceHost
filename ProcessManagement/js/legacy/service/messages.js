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
