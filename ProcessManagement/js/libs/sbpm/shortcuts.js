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

// update
key('u', function() {
	gv_graph.updateNode();
	v_graph.updateEdge();
});

// delete
key('d', function() {
	gv_graph.deleteNode();
	gv_graph.deleteEdge();
});

//add node
key('a', function() {
	gv_graph.createNode();
});

//connect node
key('c', function() {
	gv_graph.connectNodes();
});

//Insert new sendnode
key('1', function() {
	gf_callMacro('newSendNode');
});

//Insert new receivenode
key('2', function() {
	gf_callMacro('newReceiveNode');
});


//Insert new actionnode
key('3', function() {
	gf_callMacro('newActionNode');
});

//moving through graph with arrow keys
key('up', function() {
	gf_getNodePrevious();
});

key('down', function() {
	gf_getNodeNext();
});

key('left', function() {
	gf_getNodeLeft();
});

key('right', function() {
	gf_getNodeRight();
});


//prevent scrolling on the SVG
//var keys = [];
window.addEventListener("keydown",
    function(e){
       // keys[e.keyCode] = true;
        if(!(document.activeElement.tagName === "TEXTAREA" || document.activeElement.tagName === "INPUT" || document.activeElement.tagName === "SELECT")) {
        switch(e.keyCode){
            case 37: case 39: case 38:  case 40: // Arrow keys
            case 32: e.preventDefault(); break; // Space
            default: break; // do not block other keys
        }
        }
    },
false);
window.addEventListener('keyup',
    function(e){
    //    keys[e.keyCode] = false;
    },
false);