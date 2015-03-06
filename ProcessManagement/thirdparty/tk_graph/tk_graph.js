/*
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

var gv_filePrefix = "thirdparty/tk_graph";

// include Raphael
gf_includeJS("../raphael/raphael.js");

// include classes
gf_includeJS("graph/classes/behavior.class.js");
gf_includeJS("graph/classes/communication.class.js");
gf_includeJS("graph/classes/edge.class.js");
gf_includeJS("graph/classes/dragdropmanager.class.js");
gf_includeJS("graph/classes/graphbv.class.js");
gf_includeJS("graph/classes/graphcv.class.js");
gf_includeJS("graph/classes/label.class.js");
gf_includeJS("graph/classes/macro.class.js");
gf_includeJS("graph/classes/node.class.js");
gf_includeJS("graph/classes/path.class.js");
gf_includeJS("graph/classes/renderedge.class.js");
gf_includeJS("graph/classes/rendernode.class.js");
gf_includeJS("graph/classes/subject.class.js");
gf_includeJS("graph/classes/time.class.js");

// include LinearTimeLayout
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.BasicEdge.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.Edge.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.Fragment.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.LowAndDescDFS.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.Node.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.NormGraph.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.NumberDFS.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.PST.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.RPST.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.RPSTNode.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.SplitCompDFS.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.TCTree.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.TCTreeNode.js");
gf_includeJS("graph/LinearTimeLayout/LinearTimeLayout.TCTreeSkeleton.js");

// include libs
gf_includeJS("graph/tk_graph.js");
gf_includeJS("graph/tk_graph_api.js");
gf_includeJS("graph/tk_graph_gui.js");

// include configs
gf_includeJS("config/tk_graph.config.js");
gf_includeJS("config/tk_graph_bv.config.js");
gf_includeJS("config/tk_graph_cv.config.js");
gf_includeJS("config/tk_graph_api.config.js");

function gf_includeJS (file)
{
	document.write('<script type="text/javascript" src="' + gv_filePrefix + '/' + file + '"></script>');
}
