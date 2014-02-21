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

define([
], function() {
  var getFilename = function(noExt){
    var url = window.location.pathname,
        filename = url.substring(url.lastIndexOf('/')+0);

    if(filename === "")
      return "index";

    if(noExt)
      filename = filename.replace(/\.(html|htm)/, "");

    return filename;
  }
  var unimplError = function(callee){
    console.log("Function '"+callee+"' was called which is not implemented yet.");
  }

  var generateUUID = function() {
    var d = new Date().getTime();
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      var r = (d + Math.random()*16)%16 | 0;
      d = Math.floor(d/16);
      return (c=='x' ? r : (r&0x7|0x8)).toString(16);
    });
    return uuid;
  };

  return {
    getFilename: getFilename,
    unimplError: unimplError,
    generateUUID: generateUUID
  }
});
