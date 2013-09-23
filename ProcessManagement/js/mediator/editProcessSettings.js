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
var Mediator = function(){

    var self = this;

    // Extends IMediator
    IMediator.call(self);

    // Overwrite initListeners
    self.viewListeners = function(){
    }

    /**
     * used by afterRender in administration.html 
     */
    self.subviewListeners = {
    }

    self.afterRenderUsersRow = function(row){
    }
}

