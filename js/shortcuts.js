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

