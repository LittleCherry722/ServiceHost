/**
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2012 Matthias Schrammek, Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */


/**
 * GCcommunication instance.
 * This is the central access point to the API as all graph modifications are done using the methods of GCcommunication.
 *
 * @type GCCommunication
 */
var gv_graph = new GCcommunication();

/**
 * GCgraphbv instance.
 * This class is responsible for drawing the graph for the internal behavior.
 *
 * @type GCgraphbv
 */
var gv_graph_bv	= new GCgraphbv();

/**
 * GCgraphcv instance.
 * This class is responsible for drawing the graph for the subject interaction view.
 *
 * @type GCgraphcv
 */
var gv_graph_cv	= new GCgraphcv();

/**
 * The tk_graph library provides methods to create macros for adding new nodes to the graph of the behavioral view.
 * These macros are stored in the gv_macros object.
 *
 * @type Object
 */
var gv_macros = {};

/**
 * This variable contains a reference to either the gv_bv_paper or the gv_cv_paper variable.
 * It is used to create new SVG elements for a graph.
 *
 * @type Paper (from RaphaelJS)
 */
var gv_paper = null;

/**
 * This variable contains a canvas created by RaphaelJS.
 * All elements created for the graph of the behavioral view will be displayed here.
 * The canvas is created within the div with the id graph_bv_outer.
 *
 * @type Paper (from RaphaelJS)
 */
var gv_bv_paper = null;

/**
 * This variable contains a canvas created by RaphaelJS.
 * All elements created for the graph of the subject interaction view will be displayed here.
 * The canvas is created within the div with the id graph_cv_outer.
 *
 * @type Paper (from RaphaelJS)
 */
var gv_cv_paper = null;

/**
 * Indicates whether interactions like drag/drop of nodes are enabled or disabled
 *
 * @type boolean
 */
var gv_interactionsEnabled = false;

/**
 * Sets the manual position offset fo a subject or node and redraws the related graph
 *
 * @param {null|{dx: int, dy: int}} offset the position offset or null if the offset should be cleared
 * @param {int|string} id the id of the subject or node for which the manual position offset should be set
 * @param {string} type 'node' or 'edgeLabel'
 */
function gf_addManualPositionOffset(offset, id, type)
{
    var obj = gv_graph.getObjectById(id, type);
    if(obj instanceof GCnode || obj instanceof GCsubject) {
        if('getConnectedEdges' in obj) {    // reset manual position offsets for labels of all connected edges
            var connectedEdges = obj.getConnectedEdges();
            for(var i = 0; i < connectedEdges.length; i++) {
                connectedEdges[i].setManualPositionOffsetLabel(null);
            }
        }
        if(offset) {
            offset.dx += obj.getManualPositionOffset().dx;
            offset.dy += obj.getManualPositionOffset().dy;
        }
        obj.setManualPositionOffset(offset);
    } else if (obj instanceof  GCedge) {
        if(offset) {
            offset.dx += obj.getManualPositionOffsetLabel().dx;
            offset.dy += obj.getManualPositionOffsetLabel().dy;
        }
        obj.setManualPositionOffsetLabel(offset);
    }

    gf_redraw_graph();
}

/**
 * Finds the manual position offset for a node or edge label, depending on the current selected subject (for inner views)
 * @param {int|string} id id of the object
 * @param {string} type 'node' or 'edgeLabel'
 * @return {{dx, dy}|null}
 */
gf_getManualPositionOffset = function(id, type) {
    var obj = gv_graph.getObjectById(id, type);
    if(obj instanceof GCnode || obj instanceof GCsubject) {
        return obj.getManualPositionOffset();
    } else if (obj instanceof  GCedge) {
        return obj.getManualPositionOffsetLabel();
    }
    return null;
};

/**
 * Resets the manual position offsets for an internal subject behavior, the subject graph itself, or for all nodes in
 * the current process
 *
 * @param {string} view either 'all', 'subjects', or 'inner'
 * @param {int} id
 */
function gf_resetManualPositionOffsets(view, id)
{
    var objectsToReset = [],
        subjects = gv_graph.getSubjects(),
        nodes, edges, subjectKey, nodeKey, edgeKey;

    switch(view){
        case 'inner':
            if(gv_graph.getBehavior(id)) {
                nodes = gv_graph.getBehavior(id).getNodes();
                for (nodeKey in nodes) {
                    objectsToReset.push(nodes[nodeKey]);
                }
                edges = gv_graph.getBehavior(id).getEdges();
                for (edgeKey in edges) {
                    objectsToReset.push(edges[edgeKey]);
                }
            }
            break;

        case 'subjects':
            for(subjectKey in subjects) {
                objectsToReset.push(subjects[subjectKey]);
            }
            break;

        case 'all':
            for(subjectKey in subjects) {
                objectsToReset.push(subjects[subjectKey]);
                nodes = gv_graph.getBehavior(subjects[subjectKey]).getNodes();
                for (nodeKey in nodes) {
                    objectsToReset.push(nodes[nodeKey]);
                }
            }
            break;
    }

    for (var i = 0; i < objectsToReset.length; i++) {
        if ('setManualPositionOffset' in objectsToReset[i]) {
            objectsToReset[i].setManualPositionOffset(null);
        } else if ('setManualPositionOffsetLabel' in objectsToReset[i]) {
            objectsToReset[i].setManualPositionOffsetLabel(null);
        }
    }

    gf_redraw_graph();
}

/**
 * Resets the manual position offsets for all nodes of the currently displayed inner process view
 */
function gf_resetManualPositionOffsetsCurrentInner ()
{
    gf_resetManualPositionOffsets('inner', gv_graph.selectedSubject);
}

/**
 * Redraws the current graph while preserving the current view box settings such as zoom and position
 */
function gf_redraw_graph()
{
    var viewBoxBeforeCopy = $.extend({}, gv_currentViewBox),
        graphContainer, scrollTopBefore, scrollLeftBefore, behavior;

    if(null === gv_graph.selectedSubject) {
        graphContainer = $('#' + gv_elements.graphCVouter);
        scrollTopBefore = graphContainer.scrollTop();
        scrollLeftBefore = graphContainer.scrollLeft();
        gv_graph.draw();
    } else {
        behavior = gv_graph.getBehavior(gv_graph.selectedNode);
        graphContainer = $('#'+gv_elements.graphBVouter);
        scrollTopBefore = graphContainer.scrollTop();
        scrollLeftBefore = graphContainer.scrollLeft();
        var macroBefore = behavior.selectedMacro;
        gv_graph.drawBehavior();
        behavior.selectMacro(macroBefore);
    }
    gv_currentViewBox = viewBoxBeforeCopy;
    gv_paper.setViewBox(gv_currentViewBox.x, gv_currentViewBox.y, gv_currentViewBox.width, gv_currentViewBox.height, false);
    graphContainer.scrollTop(scrollTopBefore);
    graphContainer.scrollLeft(scrollLeftBefore);
}

/**
 * Using this method you can insert a node to the graph with the settings stored in gv_macros.
 *
 * @param {String} id The id of the macro.
 * @returns {void}
 */
function gf_callMacro (id)
{
    if (gf_isset(gv_macros[id]))
    {
        // disable redrawing
        gv_noRedraw	= true;

        var gt_macro = gv_macros[id];

        if (gt_macro.connect)
            gv_graph.connectNodes();

        var gt_nodeId = gv_graph.createNode();
        var gt_behavior = gv_graph.getBehavior(gv_graph.selectedSubject);

        if (gt_behavior != null)
        {
            var gt_values	= {text: gt_macro.text, type: gt_macro.type, isStart: gt_macro.isStart};

            gt_behavior.getMacro().selectedNode = gt_nodeId;

            // when another node will be added after this node -> avoid redrawing the internal behavior
            if (!gt_macro.autoEdge)
                gv_noRedraw	= false;

            gt_behavior.updateNode(gt_values);

            if (gt_macro.autoEdge)
            {
                gf_callMacro("newEndNode");
            }
        }

        // reenable drawing
        gv_noRedraw	= false;
    }
}

/**
 * Switches between the behavioral view and the communication view.
 *
 * @see GCcommunication.changeView()
 * @param {String} view The view to load. Possible values are "cv" and "bv".
 * @returns {void}
 */
function gf_changeView (view)
{
    gv_graph.changeView(view);
}

/**
 * Empties a graph.
 * Depending on the shown graph this clears an internal behavior or the complete graph.
 *
 * @see GCcommunication.clearGraph()
 * @param {boolean} When set to true the whole process will be cleared - not matter if the communication or the behavioral view is selected.
 * @returns {void}
 */
function gf_clearGraph (wholeProcess)
{
    if (gf_isset(wholeProcess) && wholeProcess === true)
    {
        gv_graph.changeView("cv");
    }

    gv_graph.clearGraph();
}

/**
 * Deselect all nodes and edges.
 * Call the selectNothing() method of all bv graphs and the cv graph.
 *
 * @returns {void}
 */
function gf_clearSelection ()
{
    gf_deselectEdges();
    gf_deselectNodes();
    gv_graph.selectNothing();

    for (var gt_subjId in gv_graph.subjects)
    {
        gv_graph.subjects[gt_subjId].getBehavior().selectNothing();
    }
}

/**
 * This method is called internally to select an edge within the behavioral view.
 * It is linked to the onClick event of the edge elements of the graph.
 *
 * @see GCcommunication.selectEdge(id)
 * @param {int} edgeId The id of the edge.
 * @returns {void}
 */
function gf_clickedBVedge (edgeId)
{
    gv_graph.selectEdge(edgeId);
}

/**
 * By calling this method a node within the behavioral view is selected.
 * It is linked to the onClick event of the node elements of the graph.
 *
 * @see GCcommunication.selectNode(id)
 * @param {int} nodeId The id of the node.
 * @returns {void}
 */
function gf_clickedBVnode (nodeId)
{
    gv_graph.selectNode(nodeId);
}

/**
 * This method is called onDblClick on a subject element on the graph of the subject interaction view.
 * It loads and displays the behavioral view of the clicked subject.
 * When graphId is either not set or null, the internal behavior of the currently clicked subject is loaded.
 *
 * @see GCcommunication.drawBehavior(id)
 * @param {String} [graphId] The ID of the subject whose internal behavior should be loaded.
 * @returns {void}
 */
function gf_clickedCVbehavior (graphId)
{
    gv_graph.drawBehavior(graphId);

    if (gf_isset(graphId))
    {
        gf_toggleBV();
    }
}

/**
 * The gf_clickedCVnode method is used to select a subject in the communication view.
 * It is linked to the onClick event of the subject elements of the graph.
 *
 * @see GCcommunication.selectNode(id)
 * @param {String} nodeId The ID of the subject.
 * @returns {void}
 */
function gf_clickedCVnode (nodeId)
{
    gv_graph.selectNode(nodeId);
}

/**
 * Activate the connectMode to connect two nodes in an internal behavior.
 *
 * @see GCcommunication.connectNodes()
 * @returns {void}
 */
function gf_connectNodes ()
{
    gv_graph.connectNodes();
}

/**
 * Creates a new case.
 *
 * @see GCcommunication::createCase()
 * @param {String} userName The name of the current user.
 * @returns {void}
 */
function gf_createCase (userName)
{
    gv_graph.createCase(userName);
}

/**
 * Creates a new process from a table containing subjects and messages sent between those subjects.
 *
 * @see GCcommunication.createFromTable()
 * @param {String[]} subjects An array of subject names.
 * @param {Object[]} messages A list of messages containing the attributes "message", "sender" (id of sender subject), "receiver" (id of receiver subject).
 * @returns {void}
 */
function gf_createFromTable (subjects, messages)
{
    gv_graph.createFromTable(subjects, messages);
}

/**
 * This method adds a new macro to the gv_macro array.
 * You can execute the macro by calling gf_callMacro(id).
 *
 * @param {String} id The id of the macro.
 * @param {String} text The label of the node to be inserted.
 * @param {boolean} isStart When set to true the created node will be a start node.
 * @param {String} type The type of the node. Possible values are "send", "receive", "end", "action", "modalsplit", "modaljoin" or any predifined action. (default: "action")
 * @param {boolean} connect When set to true, the inserted node will automatically be connected to the selected node (if one).
 * @param {boolean} autoEdge When set to true, an internal action node will be created and connected to the newly created node.
 * @returns {void}
 */
function gf_createMacro (id, text, isStart, type, connect, autoEdge)
{
    if (!gf_isset(autoEdge) || autoEdge !== true)
        autoEdge = false;

    if (gf_isset(id, type, text, connect) && !gf_isset(this.gv_macros[id]))
    {
        connect = connect === true ? true : false;
        gv_macros[id] = {id: id, type: type, isStart: isStart, text: text, connect: connect, autoEdge: autoEdge};
    }
}

/**
 * Insert a new node into the current graph.
 *
 * @see GCcommunication.createNode()
 * @returns {void}
 */
function gf_createNode ()
{
    gv_graph.createNode();
}

/**
 * This method de- / activates the currently selected edge.
 *
 * @see GCcommunication.deactivateEdge()
 * @deprecated
 * @returns {void}
 */
function gf_deactivateEdge ()
{
    gv_graph.deactivateEdge();
}

/**
 * This method de- / activates the currently selected element.
 *
 * @see GCcommunication.deactivateEdge()
 * @see GCcommunication.deactivateNode()
 * @returns {void}
 */
function gf_deactivateElement ()
{
    gv_graph.deactivateEdge();
    gv_graph.deactivateNode();
}

/**
 * This method de- / activates the currently selected node.
 *
 * @see GCcommunication.deactivateNode()
 * @deprecated
 * @returns {void}
 */
function gf_deactivateNode ()
{
    gv_graph.deactivateNode();
}

/**
 * Deletes the selected element.
 *
 * @see GCcommunication.deleteEdge()
 * @see GCcommunication.deleteNode()
 * @returns {void}
 */
function gf_deleteElement ()
{
    var gt_type	= gf_getSelectedElementType();

    if (gt_type == "node")
    {
        gv_graph.deleteNode();
    }
    else if (gt_type == "edge")
    {
        gv_graph.deleteEdge();
    }
}

/**
 * A simple function doing nothing.
 * Used in the gv_functions to override default functions.
 *
 * @returns {void}
 */
function gf_empty ()
{

}

/**
 * Returns a list of conversations.
 *
 * @returns {Object} List of conversations.
 */
function gf_getConversations ()
{
    var gt_conversations	= {};
    var gt_isCV		= false;

    if (gv_graph.selectedSubject == null)
        gt_isCV	= true;

    // add display conversations
    if (gt_isCV)
        gt_conversations["##conversations##"]	= "display conversations";

    // add all conversations
    gt_conversations["##all##"]	= "All conversations";

    for (var gt_chid in gv_graph.conversations)
    {
        gt_conversations[gt_chid]	= gv_graph.conversations[gt_chid];
    }

    return gt_conversations;
}

/**
 * Returns a list of macros of the current behavior.
 *
 * @returns {Object} List of macros.
 */
function gf_getMacros ()
{
    var gt_macros	= {length: 0};
    var gt_behav	= gv_graph.getBehavior(gv_graph.selectedSubject);

    if (gt_behav != null)
    {
        // add "internal behavior"
        gt_macros["##main##"]	= "internal behavior";
        gt_macros.length++;

        var gt_tmpArray		= [];
        var gt_tmpMacros	= gt_behav.getMacros();
        for (var gt_mid in gt_tmpMacros)
        {
            if (gt_mid != "##main##")
                gt_tmpArray[gt_tmpArray.length]	= {id: gt_mid, text: gt_tmpMacros[gt_mid].name};
        }

        gt_tmpArray.sort(gf_guiSortArrayByText);

        for (var gt_mid in gt_tmpArray)
        {
            gt_macros[gt_tmpArray[gt_mid].id]	= gt_tmpArray[gt_mid].text;
            gt_macros.length++;
        }
    }

    return gt_macros;
}

/**
 * Collects all messages available to the system and returns them as an Array of Objects.
 *
 * @param {String} subjectInfo The information of subjects that will be returned (id, role, name).
 * @returns {Array} Array of Objects {sender, messageType, receiver}.
 */
function gf_getMessageTypes (subjectInfo)
{
    if (!gf_isset(subjectInfo))
        subjectInfo = "name";

    subjectInfo	= subjectInfo.toLowerCase();

    // clear messages
    var gt_messages = [];

    // load messages from behavior
    for (var gt_bi in gv_graph.subjects)
    {
        var gt_subject	= gv_graph.subjects[gt_bi];
        if (!gt_subject.isExternal() || gt_subject.getExternalType() == "interface")
        {
            var gt_behav = gv_graph.getBehavior(gt_bi);
            var gt_edges = gt_behav.getEdges();
            for (var gt_eid in gt_edges)
            {
                var gt_edge					= gt_edges[gt_eid];
                var gt_startNode			= gt_behav.getNode(gt_edge.getStart());
                var gt_endNode				= gt_behav.getNode(gt_edge.getEnd());
                var gt_relatedSubject		= gt_edge.getRelatedSubject();
                var gt_text					= gt_edge.getText();
                var gt_type					= gt_edge.getType();

                if (gt_startNode != null && gt_endNode != null && gt_relatedSubject != null && gt_text != "" && gt_type == "exitcondition")
                {
                    var gt_addMessage	= false;
                    var gt_msgSender	= "";
                    var gt_msgReceiver	= "";

                    if (gf_isset(gv_graph.subjects[gt_relatedSubject]) && gt_startNode.getType() == "send")
                    {
                        gt_addMessage	= true;
                        gt_msgSender	= gt_bi;
                        gt_msgReceiver	= gt_relatedSubject;
                    }

                    if (gf_isset(gv_graph.subjects[gt_relatedSubject]) && gt_startNode.getType() == "receive")
                    {
                        gt_addMessage	= true;
                        gt_msgSender	= gt_relatedSubject;
                        gt_msgReceiver	= gt_bi;
                    }

                    if (gt_addMessage)
                    {
                        gt_text	= gf_isset(gv_graph.messageTypes[gt_text]) ? gv_graph.messageTypes[gt_text] : gt_text;

                        // replace all new line characters by space
                        gt_text	= gf_replaceNewline(gt_text, " ");

                        // get the right information about the subjects
                        if (gf_isset(gv_graph.subjects[gt_msgSender]))
                        {
                            if (subjectInfo == "id")
                                gt_msgSender	= gf_replaceNewline(gv_graph.subjects[gt_msgSender].getId(), " ");
                            else if (subjectInfo == "role")
                                gt_msgSender	= gf_replaceNewline(gv_graph.subjects[gt_msgSender].getRole(), " ");
                            else
                                gt_msgSender	= gf_replaceNewline(gv_graph.subjects[gt_msgSender].getText(), " ");
                        }

                        if (gf_isset(gv_graph.subjects[gt_msgReceiver]))
                        {
                            if (subjectInfo == "id")
                                gt_msgReceiver	= gf_replaceNewline(gv_graph.subjects[gt_msgReceiver].getId(), " ");
                            else if (subjectInfo == "role")
                                gt_msgReceiver	= gf_replaceNewline(gv_graph.subjects[gt_msgReceiver].getRole(), " ");
                            else
                                gt_msgReceiver	= gf_replaceNewline(gv_graph.subjects[gt_msgReceiver].getText(), " ");
                        }

                        var gt_messageFound	= false;
                        for (var gt_mtid in gt_messages)
                        {
                            var gt_msg	= gt_messages[gt_mtid];
                            if (gt_msg.messageType == gt_text && gt_msg.sender == gt_msgSender && gt_msg.receiver == gt_msgReceiver)
                            {
                                gt_messageFound = true;
                                break;
                            }
                        }

                        if (!gt_messageFound)
                            gt_messages[gt_messages.length] = {sender: gt_msgSender, receiver: gt_msgReceiver, messageType: gt_text};
                    }
                }
            }
        }
    }

    return gt_messages;
}

/**
 * Select the left sibbling of the currently selected node.
 * When the current node is the most left sibbling the most right sibbling will be selected.
 *
 * @returns {void}
 */
function gf_getNodeLeft ()
{
    var gt_selectedNode	= gv_graph.getSelectedNode();
    if (gt_selectedNode != null)
    {
        // get the parent node of the current node
        var gt_parent	= gf_getParentNode(gt_selectedNode);

        if (gt_parent != null)
        {
            // get the child nodes of this node's parent node
            var gt_children	= gf_getChildNodes(gt_parent);

            if (gt_children.length > 0)
            {
                // get own ID in array
                var gt_ownID	= 0;
                for (var gt_i = 0; gt_i < gt_children.length; gt_i++)
                {
                    if (gt_children[gt_i] == gt_selectedNode)
                        gt_ownID = gt_i;
                }

                // select the sibbling to the left of the current node
                var gt_nextID	= (gt_ownID < 1) ? gt_children.length - 1 : gt_ownID - 1;
                gf_paperClickNodeB(gt_children[gt_nextID]);
            }
        }
    }
}

/**
 * Select the most left child of the current node.
 * When the current node is an end node the selection will not be changed.
 *
 * @returns {void}
 */
function gf_getNodeNext ()
{
    var gt_selectedNode	= gv_graph.getSelectedNode();
    if (gt_selectedNode != null)
    {
        var gt_children	= gf_getChildNodes(gt_selectedNode);

        // select the most left child of the current node
        if (gt_children.length > 0)
        {
            gf_paperClickNodeB(gt_children[0]);
        }
    }
}

/**
 * Select the parent node of the current node.
 * When the current node is a start node the selection will not be changed.
 *
 * @returns {void}
 */
function gf_getNodePrevious ()
{
    var gt_selectedNode	= gv_graph.getSelectedNode();
    if (gt_selectedNode != null)
    {
        // select parent node
        var gt_parent	= gf_getParentNode(gt_selectedNode);

        if (gt_parent != null)
        {
            gf_paperClickNodeB(gt_parent);
        }
    }
}

/**
 * Select the right sibbling of the currently selected node.
 * When the current node is the most right sibbling the most left sibbling will be selected.
 *
 * @returns {void}
 */
function gf_getNodeRight ()
{
    var gt_selectedNode	= gv_graph.getSelectedNode();
    if (gt_selectedNode != null)
    {
        // get the parent node of the current node
        var gt_parent	= gf_getParentNode(gt_selectedNode);

        if (gt_parent != null)
        {
            // get the child nodes of this node's parent node
            var gt_children	= gf_getChildNodes(gt_parent);

            if (gt_children.length > 0)
            {
                // get own ID in array
                var gt_ownID	= 0;
                for (var gt_i = 0; gt_i < gt_children.length; gt_i++)
                {
                    if (gt_children[gt_i] == gt_selectedNode)
                        gt_ownID = gt_i;
                }

                // select the sibbling to the right of the current node
                var gt_nextID	= (gt_ownID >= gt_children.length - 1) ? 0 : gt_ownID + 1;
                gf_paperClickNodeB(gt_children[gt_nextID]);
            }
        }
    }
}

/**
 * Returns the current state of the process.
 *
 * @returns {Object} Object that contains the selected behavior - or null if process is in communication view - and the currently selected macro - when an internal behavior is selected.
 */
function gf_getProcessState ()
{
    var gt_state	= {behavior: null, macro: null};

    if (gv_graph.selectedSubject != null)
    {
        if (gf_isset(gv_graph.subjects[gv_graph.selectedSubject]))
        {
            gt_state.behavior	= gv_graph.selectedSubject;
            gt_state.macro		= gv_graph.getBehavior(gv_graph.selectedSubject).selectedMacro;
        }
    }

    return gt_state;
}

/**
 * Returns the id of the selected conversation.
 *
 * @see GCcommunication::getSelectedConversation()
 * @returns {String} Id of selected conversation.
 */
function gf_getSelectedConversation ()
{
    return gv_graph.getSelectedConversation();
}

/**
 * Returns the type and the id of the currently selected element as an object.
 * The object contains two attributes, type and id, where id is the id of the element and type is either "subject", "node" or "edge".
 *
 * @returns {Object} Type and ID of selected element or null if no element is selected.
 */
function gf_getSelectedElement ()
{
    var gt_result = {type: "", id: ""};

    if (gv_graph.selectedSubject == null)
    {
        if (gv_graph.selectedNode != null)
        {
            gt_result.type	= "subject";
            gt_result.id	= gv_graph.selectedNode;
        }
    }

    // when a behavior is shown
    else
    {
        if (gf_isset(gv_graph.subjects[gv_graph.selectedSubject]))
        {
            var gt_behav	= gv_graph.getBehavior(gv_graph.selectedSubject);

            if (gt_behav.getMacro().selectedNode != null)
            {
                gt_result.type	= "node";
                gt_result.id	= gt_behav.getMacro().selectedNode;
            }
            if (gt_behav.getMacro().selectedEdge != null)
            {
                gt_result.type	= "edge";
                gt_result.id	= gt_behav.getMacro().selectedEdge;
            }
        }
    }

    if (gt_result.type == "")
        gt_result	= null;

    return gt_result;
}

/**
 * Determines the type of the selected element.
 * Returns "none" when no element is selected.
 *
 * @returns {String} Type of selected element. Either "node", "edge" or "none".
 */
function gf_getSelectedElementType ()
{
    var gt_type	= "none";

    if (gv_graph.selectedSubject == null)
    {
        if (gv_graph.selectedNode != null)
        {
            gt_type = "node";
        }
    }

    // when a behavior is shown
    else
    {
        if (gf_isset(gv_graph.subjects[gv_graph.selectedSubject]))
        {
            var gt_behav	= gv_graph.getBehavior(gv_graph.selectedSubject);

            if (gt_behav.getMacro().selectedNode != null)
            {
                gt_type = "node";
            }
            if (gt_behav.getMacro().selectedEdge != null)
            {
                gt_type = "edge";
            }
        }
    }

    return gt_type;
}

/**
 * Returns the ID of the currently selected macro.
 *
 * @returns {String} The ID of the currently selected macro.
 */
function gf_getSelectedMacro ()
{
    var gt_behav	= gv_graph.getBehavior(gv_graph.selectedSubject);

    if (gt_behav != null)
    {
        return gt_behav.selectedMacro;
    }
    return null;
}

/**
 * Returns the id of the node currently selected depending on the current view.
 *
 * @see GCcommunication.getSelectedNode()
 * @returns {int} The id of the selectedNode of the currently active view or null.
 */
function gf_getSelectedNode ()
{
    return gv_graph.getSelectedNode();
}

/**
 * Returns the IDs of all subjects of the graph.
 *
 * @see GCcommunication.getSubjectIDs()
 * @returns {String[]} An array of all subject IDs.
 */
function gf_getSubjectIDs ()
{
    return gv_graph.getSubjectIDs();
}

/**
 * Returns the names of all subjects of the graph.
 *
 * @see GCcommunication.getSubjectNames()
 * @returns {String[]} An array of all subject names.
 */
function gf_getSubjectNames ()
{
    return gv_graph.getSubjectNames();
}

/**
 * Returns the roles of all subjects of the graph.
 *
 * @see GCcommunication.getSubjectRoles()
 * @returns {String[]} An array of all subject roles.
 */
function gf_getSubjectNames ()
{
    return gv_graph.getSubjectRoles();
}

/**
 * Returns the subjects of the graph.
 *
 * @see GCcommunication.getSubjects()
 * @returns {String[]} An array of all subjects.
 */
function gf_getSubjects ()
{
    return gv_graph.getSubjects();
}

/**
 * Loads a benchmark graph.
 *
 * @returns {void}
 */
function gf_loadBenchmarkGraph ()
{
    gf_clearGraph();

    // Process created by table with 2 Subjects and 2x5 Messages
    gf_loadGraph('{"process":[{"id":"s1","name":"S1","type":"single","deactivated":false,"inputPool":-1,"relatedProcess":null,"relatedSubject":null,"externalType":"external","role":"noRole","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"What to do?","start":true,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":true,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"create msg","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":2,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":4,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":5,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},' +
        '{"id":6,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":7,"text":"","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":8,"text":"","start":false,"end":false,"type":"receive","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":9,"text":"reaction msg 5","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":10,"text":"reaction msg 6","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":11,"text":"reaction msg 7","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":12,"text":"reaction msg 8","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},' +
        '{"id":13,"text":"reaction msg 9","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"send","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":1,"end":0,"text":"cancel","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":1,"end":2,"text":"create msg","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":2,"end":0,"text":"m0","type":"exitcondition","target":{"id":"s2","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":1,"end":3,"text":"create msg","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":3,"end":0,"text":"m1","type":"exitcondition","target":{"id":"s2","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":1,"end":4,"text":"create msg","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":4,"end":0,"text":"m2","type":"exitcondition","target":{"id":"s2","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":1,"end":5,"text":"create msg","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},' +
        '{"start":5,"end":0,"text":"m3","type":"exitcondition","target":{"id":"s2","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":1,"end":6,"text":"create msg","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":6,"end":0,"text":"m4","type":"exitcondition","target":{"id":"s2","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":0,"end":7,"text":"end process","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":0,"end":8,"text":"receive","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":8,"end":0,"text":"cancel","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":8,"end":9,"text":"m5","type":"exitcondition","target":{"id":"s2","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":9,"end":0,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":8,"end":10,"text":"m6","type":"exitcondition","target":{"id":"s2","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":10,"end":0,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},' +
        '{"start":8,"end":11,"text":"m7","type":"exitcondition","target":{"id":"s2","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":11,"end":0,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":8,"end":12,"text":"m8","type":"exitcondition","target":{"id":"s2","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":12,"end":0,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":8,"end":13,"text":"m9","type":"exitcondition","target":{"id":"s2","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":13,"end":0,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""}],"nodeCounter":14}],"macroCounter":0,"variables":{},"variableCounter":0},{"id":"s2","name":"S2","type":"single","deactivated":false,"inputPool":-1,"relatedProcess":null,"relatedSubject":null,"externalType":"external","role":"noRole","comment":"","macros":[{"id":"##main##","name":"internal behavior","nodes":[{"id":0,"text":"What to do?","start":true,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":true,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":1,"text":"create msg","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},' +
        '{"id":2,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":3,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":4,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":5,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":6,"text":"","start":false,"end":false,"type":"send","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":7,"text":"","start":false,"end":true,"type":"end","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":8,"text":"","start":false,"end":false,"type":"receive","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},' +
        '{"id":9,"text":"reaction msg 0","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":10,"text":"reaction msg 1","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":11,"text":"reaction msg 2","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":12,"text":"reaction msg 3","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""},{"id":13,"text":"reaction msg 4","start":false,"end":false,"type":"action","options":{"message":"*","subject":"*","correlationId":"*","conversation":"*","state":""},"deactivated":false,"majorStartNode":false,"conversation":null,"variable":null,"varMan":{"var1":"","var2":"","operation":"and","storevar":""},"macro":"","comment":""}],"edges":[{"start":0,"end":1,"text":"send","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":1,"end":0,"text":"cancel","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":1,"end":2,"text":"create msg","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},' +
        '{"start":2,"end":0,"text":"m5","type":"exitcondition","target":{"id":"s1","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":1,"end":3,"text":"create msg","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":3,"end":0,"text":"m6","type":"exitcondition","target":{"id":"s1","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":1,"end":4,"text":"create msg","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":4,"end":0,"text":"m7","type":"exitcondition","target":{"id":"s1","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":1,"end":5,"text":"create msg","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":5,"end":0,"text":"m8","type":"exitcondition","target":{"id":"s1","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":1,"end":6,"text":"create msg","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":6,"end":0,"text":"m9","type":"exitcondition","target":{"id":"s1","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":0,"end":7,"text":"end process","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},' +
        '{"start":0,"end":8,"text":"receive","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":8,"end":0,"text":"cancel","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":8,"end":9,"text":"m0","type":"exitcondition","target":{"id":"s1","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":9,"end":0,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":8,"end":10,"text":"m1","type":"exitcondition","target":{"id":"s1","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":10,"end":0,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":8,"end":11,"text":"m2","type":"exitcondition","target":{"id":"s1","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":11,"end":0,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":8,"end":12,"text":"m3","type":"exitcondition","target":{"id":"s1","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":12,"end":0,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},' +
        '{"start":8,"end":13,"text":"m4","type":"exitcondition","target":{"id":"s1","min":-1,"max":-1,"createNew":false,"variable":null},"deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""},{"start":13,"end":0,"text":"","type":"exitcondition","target":"","deactivated":false,"optional":false,"priority":1,"manualTimeout":false,"variable":null,"correlationId":"","comment":""}],"nodeCounter":14}],"macroCounter":0,"variables":{},"variableCounter":0}],"messages":{"m0":"Message 1","m1":"Message 2","m2":"Message 3","m3":"Message 4","m4":"Message 5","m5":"Message 6","m6":"Message 7","m7":"Message 8","m8":"Message 9","m9":"Message 10"},"messageCounter":10,"nodeCounter":0,"conversations":{},"conversationCounter":0}');
}

/**
 * Loads a case graph from a given JSON representation stored in the database.
 *
 * @see GCcommunication.loadFromJSON()
 * @param {String} jsonString The JSON representation of a process.
 * @param {Object} state Optional parameter, retrieved from gf_getProcessState()
 * @returns {void}
 */
function gf_loadCase (jsonString, state)
{
    gv_graph.processFlag	= false;

    if (gf_isset(state))
    {
        gf_loadGraph(jsonString, state);
    }
    else
    {
        gv_graph.loadFromJSON(jsonString);
        gf_toggleBV();
        gv_graph.drawBehavior("me");
    }
}

/**
 * Loads a process graph from a given JSON representation stored in the database.
 *
 * @see GCcommunication.loadFromJSON()
 * @param {String} jsonString The JSON representation of a process.
 * @param {Object} state Optional parameter, retrieved from gf_getProcessState()
 * @returns {void}
 */
function gf_loadGraph (jsonString, state)
{
    gv_graph.loadFromJSON(jsonString);

    // if state is given, load the state
    if (gf_isset(state))
    {
        if (gf_isset(state.behavior, state.macro))
        {
            // if behavior is null, load the cv view
            if (state.behavior == null)
            {
                gf_changeView("cv");
            }

            // else load the selected macro
            else
            {
                gv_graph.selectedSubject	= state.behavior;
                gf_changeView("bv");
                gv_graph.selectMacro(state.macro);
            }
        }
    }
}

/**
 * Stop the drag operation and update the position of the current view box.
 *
 * @returns {void}
 */
function gf_paperDragEnd ()
{
    // drag operation is only executed when the shift-key is pressed
    if (event.shiftKey)
    {
        gt_event	= event ? event : window.event;

        // get the current mouse position
        gt_endPosX	= gt_event.pageX ? gt_event.pageX : gt_event.clientX;
        gt_endPosY	= gt_event.pageY ? gt_event.pageY : gt_event.clientY;

        // calculate the ddifference to the original position
        gt_diffX	= (gv_mousePositionStart.x - gt_endPosX) / gv_currentViewBox.zoom;
        gt_diffY	= (gv_mousePositionStart.y - gt_endPosY) / gv_currentViewBox.zoom;

        // update the position of the current view box
        gv_currentViewBox.x	= gv_currentViewBox.x + gt_diffX;
        gv_currentViewBox.y	= gv_currentViewBox.y + gt_diffY;
    }
}

/**
 * Move the canvas corresponding to the mouse move.
 *
 * @returns {void}
 */
function gf_paperDragMove ()
{
    // drag operation is only executed when the shift-key is pressed
    if (event.shiftKey)
    {
        gt_event	= event ? event : window.event;

        // get the current mouse position
        gt_endPosX	= gt_event.pageX ? gt_event.pageX : gt_event.clientX;
        gt_endPosY	= gt_event.pageY ? gt_event.pageY : gt_event.clientY;

        // calculate the ddifference to the original position
        gt_diffX	= (gv_mousePositionStart.x - gt_endPosX) / gv_currentViewBox.zoom;
        gt_diffY	= (gv_mousePositionStart.y - gt_endPosY) / gv_currentViewBox.zoom;

        // update the view box
        gv_paper.setViewBox(gv_currentViewBox.x + gt_diffX, gv_currentViewBox.y + gt_diffY, gv_currentViewBox.width, gv_currentViewBox.height, false);
    }
}

/**
 * Start the drag operation.
 * Backup the current mouse position as a reference for the move.
 *
 * @returns {void}
 */
function gf_paperDragStart ()
{
    // drag operation is only executed when the shift-key is pressed
    if (event.shiftKey)
    {
        gt_event = event ? event : window.event;

        // back up current mouse position
        gv_mousePositionStart.x = gt_event.pageX ? gt_event.pageX : gt_event.clientX;
        gv_mousePositionStart.y = gt_event.pageY ? gt_event.pageY : gt_event.clientY;
    }
}

/**
 * Zoom the paper in.
 *
 * @param {double} zoomFactor The zoom factor.
 * @param {Object} zoomPosition The position that has to be used as the center for the zoom.
 * @returns {void}
 */
function gf_paperZoomIn (zoomFactor, zoomPosition)
{
    if (!gf_isset(zoomFactor))
        zoomFactor = gv_zoomSettings.zoomIn;

    if (!gf_isset(zoomPosition))
        zoomPosition = gf_paperCenterPosition();

    // check if zoomLevel already hit zoomLimit
    if (gv_currentViewBox.zoom * zoomFactor <= gv_zoomSettings.max)
    {

        // the dimension of the current view box
        var gt_oldWidth		= gv_currentViewBox.width;
        var gt_oldHeight	= gv_currentViewBox.height;

        // the dimension of the view box after the zoom
        var gt_newWidth		= gv_currentViewBox.width/zoomFactor;
        var gt_newHeight	= gv_currentViewBox.height/zoomFactor;

        // calculate the difference between the current and the new dimensions
        var gt_diffWidth	= gt_oldWidth - gt_newWidth;
        var gt_diffHeight	= gt_oldHeight - gt_newHeight;

        // when the zoom position is set
        if (gf_isset(zoomPosition))
        {
            // adapt the mouse position to the current zoom level
            var gt_mouseDiffX	= zoomPosition.x / gv_currentViewBox.zoom;
            var gt_mouseDiffY	= zoomPosition.y / gv_currentViewBox.zoom;

            // update the position of the view box
            gv_currentViewBox.x		= gv_currentViewBox.x + gt_mouseDiffX * (1 - 1 / zoomFactor);
            gv_currentViewBox.y		= gv_currentViewBox.y + gt_mouseDiffY * (1 - 1 / zoomFactor);
        }
        else
        {
            // update the position of the view box
            gv_currentViewBox.x			= gv_graphID == "cv" ? gv_currentViewBox.x : gv_currentViewBox.x + (gt_diffWidth/2);
            gv_currentViewBox.y			= gv_graphID == "cv" ? gv_currentViewBox.y + (gt_diffHeight/2) : gv_currentViewBox.y;
        }

        // set the new dimensions
        gv_currentViewBox.width		= gt_newWidth;
        gv_currentViewBox.height	= gt_newHeight;

        // update the zoom level
        gv_currentViewBox.zoom		= gv_currentViewBox.zoom * zoomFactor;

        // apply the new settings
        gv_paper.setViewBox(gv_currentViewBox.x, gv_currentViewBox.y, gv_currentViewBox.width, gv_currentViewBox.height, false);
    }
}

/**
 * Zoom the paper out.
 *
 * @param {double} zoomFactor The zoom factor.
 * @param {Object} zoomPosition The position that has to be used as the center for the zoom.
 * @returns {void}
 */
function gf_paperZoomOut (zoomFactor, zoomPosition)
{
    if (!gf_isset(zoomFactor))
        zoomFactor = gv_zoomSettings.zoomOut;

    if (!gf_isset(zoomPosition))
        zoomPosition = gf_paperCenterPosition();

    // check if zoomLevel already hit zoomLimit
    if (gv_currentViewBox.zoom / zoomFactor >= gv_zoomSettings.min)
    {

        // the dimension of the current view box
        var gt_oldWidth		= gv_currentViewBox.width;
        var gt_oldHeight	= gv_currentViewBox.height;

        // the dimension of the view box after the zoom
        var gt_newWidth		= gv_currentViewBox.width*zoomFactor;
        var gt_newHeight	= gv_currentViewBox.height*zoomFactor;

        // calculate the diff between the current and the new dimensions
        var gt_diffWidth	= gt_oldWidth - gt_newWidth;
        var gt_diffHeight	= gt_oldHeight - gt_newHeight;

        // when the zoom position is set
        if (gf_isset(zoomPosition))
        {
            // adapt the mouse position to the current zoom level
            var gt_mouseDiffX	= zoomPosition.x / gv_currentViewBox.zoom;
            var gt_mouseDiffY	= zoomPosition.y / gv_currentViewBox.zoom;

            // update the position of the view box
            gv_currentViewBox.x		= gv_currentViewBox.x + gt_mouseDiffX * (1 - 1 * zoomFactor);
            gv_currentViewBox.y		= gv_currentViewBox.y + gt_mouseDiffY * (1 - 1 * zoomFactor);
        }
        else
        {
            // update the position of the view box
            gv_currentViewBox.x			= gv_graphID == "cv" ? gv_currentViewBox.x : gv_currentViewBox.x + (gt_diffWidth/2);
            gv_currentViewBox.y			= gv_graphID == "cv" ? gv_currentViewBox.y + (gt_diffHeight/2) : gv_currentViewBox.y;
        }

        // set the new dimensions
        gv_currentViewBox.width		= gv_currentViewBox.width*zoomFactor;
        gv_currentViewBox.height	= gv_currentViewBox.height*zoomFactor;

        // update the zoom level
        gv_currentViewBox.zoom		= gv_currentViewBox.zoom / zoomFactor;

        // apply the new settings
        gv_paper.setViewBox(gv_currentViewBox.x, gv_currentViewBox.y, gv_currentViewBox.width, gv_currentViewBox.height, false);
    }
}

/**
 * Reset the zoom level to 1.
 *
 * @returns {void}
 */
function gf_paperZoomReset ()
{
    // backup the view box to the position and the dimensions of the original view box
    gv_currentViewBox.width		= gv_originalViewBox.width;
    gv_currentViewBox.height	= gv_originalViewBox.height;
    gv_currentViewBox.x			= gv_originalViewBox.x;
    gv_currentViewBox.y			= gv_originalViewBox.y;

    // reset the zoom level to the original zoom level
    gv_currentViewBox.zoom		= gv_originalViewBox.zoom;

    // update the view box
    gv_paper.setViewBox(gv_originalViewBox.x, gv_originalViewBox.y, gv_originalViewBox.width, gv_originalViewBox.height, false);
}

/**
 * Save the graph in the given format.
 * When format is left empty this method will return the graph as an object.
 * Formats "pdf" and "svg" are currently not supported.
 *
 * @see GCcommunication.save()
 * @see GCcommunication.saveToJSON()
 * @see GCcommunication.saveToPDF()
 * @see GCcommunication.saveToSVG()
 * @param {String} format The format to save the graph in. Possible values: "json" (save as JSON string), "pdf", "svg", "object" (default: json)
 * @returns {String | Object} The graph in the given format.
 */
function gf_saveGraph (format)
{
    if (!gf_isset(format))
        format = "json";

    format	= format.toLowerCase();

    // save as an object
    if (format == "object")
    {
        return gv_graph.save();
    }

    // save to PDF
    else if (format == "pdf")
    {
        return gv_graph.saveToPDF();
    }

    // save to SVG
    else if (format == "svg")
    {
        return gv_graph.saveToSVG();
    }

    // save as JSON string
    else
    {
        return gv_graph.saveToJSON();
    }
}

/**
 * Select a conversation.
 * When an internal behavior is selected the conversation name will be passed to the behavior's selectConversation method.
 *
 * @see GCcommunication.selectConversation()
 * @param {String} conversation The name of the conversation to select. When set to "##conversations##" the available conversations will be displayed in the CV. When set to "##all##" all conversations will be displayed.
 * @returns {void}
 */
function gf_selectConversation (conversation)
{
    gv_graph.selectConversation(conversation);
}

/**
 * Selects a macro in the currently selected internal behavior.
 *
 * @see GCcommunication.selectMacro()
 * @param {String} macro The ID of the macro to select.
 * @returns {void}
 */
function gf_selectMacro (macro)
{
    gv_graph.selectMacro(macro);
}

/**
 * Sets the IDs of the divs that will hold the canvases for the behavioral view and the communication view.
 *
 * @param {String} ID of the div that will hold the canvas vor the behavioral view.
 * @param {String} ID of the div that will hold the canvas vor the communication view.
 * @returns {void}
 */
function gf_setDivs (bv, cv)
{
    if (gf_isset(bv, cv))
    {
        gv_elements.graphBVouter	= bv;
        gv_elements.graphCVouter	= cv;
    }
}

/**
 * Using this method an internal behavior can be loaded for a given subject with the current step being marked.
 * This draws the corresponding graph and marks the current step by selecting the node.
 * All you need to provide is a div element with the id "graph_bv_outer".
 *
 * @param {String} jsonProcess The complete process graph in JSON format.
 * @param {String} subject  The currently active subject.
 * @param {String} node The currently active step in the graph. This node will be marked as active.
 * @returns {void}
 */
function gf_showInternalBehavior (jsonProcess, subject, node)
{
    // load the process
    gv_graph.loadFromJSON(jsonProcess);

    gv_graph.selectedSubject = null;

    // get the internal behavior for the currently active subject
    var gt_behav = gv_graph.getBehavior(subject);
    if (gt_behav != null)
    {
        var gt_nodeId	= node;
        if (parseInt(gt_nodeId) != gt_nodeId && gf_isset(gt_behav.getMacro().nodeIDs[gt_nodeId]))
        {
            gt_nodeId = gt_behav.getMacro().nodeIDs[gt_nodeId];
        }

        // draw the graph for the internal behavior
        gf_clickedCVnode(subject);
        gf_toggleBV();

        // mark the currently selected node
        gf_paperClickNodeB(gt_nodeId);
    }
}

/**
 * Updates the selected element.
 *
 * @see GCcommunication.updateEdge()
 * @see GCcommunication.updateNode()
 * @returns {void}
 */
function gf_updateElement ()
{
    var gt_type	= gf_getSelectedElementType();

    if (gt_type == "node")
    {
        gv_graph.updateNode();
    }
    else if (gt_type == "edge")
    {
        gv_graph.updateEdge();
    }
}