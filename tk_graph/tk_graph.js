/**
 * 
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