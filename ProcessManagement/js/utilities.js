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

var Utilities = {
    getFilename : function(noExt){
        var url = window.location.pathname;
        var filename = url.substring(url.lastIndexOf('/')+1);

        if(filename === "")
            return "index";

        if(noExt)
            filename = filename.replace(/\.(html|htm)/, "");

        return filename;
    },
    unimplError : function(callee){
        console.log("Function '"+callee+"' was called which is not implemented yet.");
    }
}

SBPM.Utilities = Utilities;