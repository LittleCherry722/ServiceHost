/*
 * S-BPM Groupware v0.8
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

var gv_filePrefix = "tk_graph";

// include Raphael
gf_includeJS("thirdparty/raphael-min.js");

// include jQuery scrollTo plugin
gf_includeJS("thirdparty/jquery.scrollTo-min.js");

// include jQuery MouseWheel plugi
gf_includeJS("thirdparty/jquery.mousewheel.min.js");

// include classes
gf_includeJS("graph/classes/behavior.class.js");
gf_includeJS("graph/classes/communication.class.js");
gf_includeJS("graph/classes/edge.class.js");
gf_includeJS("graph/classes/graphbv.class.js");
gf_includeJS("graph/classes/graphcv.class.js");
gf_includeJS("graph/classes/label.class.js");
gf_includeJS("graph/classes/node.class.js");
gf_includeJS("graph/classes/path.class.js");
gf_includeJS("graph/classes/subject.class.js");

// include libs
gf_includeJS("graph/tk_graph.js");
gf_includeJS("graph/tk_graph_api.js");

// include configs
gf_includeJS("config/tk_graph.config.js");
gf_includeJS("config/tk_graph_bv.config.js");
gf_includeJS("config/tk_graph_cv.config.js");

function gf_includeJS (file)
{
	document.write('<script type="text/javascript" src="' + gv_filePrefix + '/' + file + '"></script>');
}