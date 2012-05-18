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

// include libs
gf_includeJS("graph/tk_graph.js");
gf_includeJS("graph/tk_graph_bv.js");
gf_includeJS("graph/tk_graph_cv.js");
gf_includeJS("graph/tk_graph_api.js");

// include configs
gf_includeJS("config/tk_graph.config.js");
gf_includeJS("config/tk_graph_bv.config.js");
gf_includeJS("config/tk_graph_cv.config.js");

function gf_includeJS (file)
{
	document.write('<script type="text/javascript" src="' + gv_filePrefix + '/' + file + '"></script>');
}