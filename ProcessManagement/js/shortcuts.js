/**
 * @author André Röder
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

//Insert new sendnode
key('2', function() {
	gf_callMacro('newReceiveNode');
});


//Insert new sendnode
key('3', function() {
	gf_callMacro('newActionNode');
});
