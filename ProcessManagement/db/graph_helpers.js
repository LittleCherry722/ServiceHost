/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2012 Thorsten Jacobi, Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

function graphGenerateEmpty(){
	return JSON.parse("{}");
}

function graphAdd(type, graph, node){
	if(typeof(graph[type]) == 'undefined') graph[type] = graphGenerateEmpty();
	if(typeof(graph[type]["count"]) == 'undefined') graph[type]["count"] = 0;

	var nodecount = graph[type]["count"];

	graph[type][nodecount] = node;
	graph[type]["count"] = nodecount +1;
}
function graphRemove(type, graph, index){
	if(typeof(graph[type]) == 'undefined') graph[type] = graphGenerateEmpty();
	if(typeof(graph[type]["count"]) == 'undefined') graph[type]["count"] = 0;

	var nodecount = graph[type]["count"]-1;
	
	for (var i = index; i < nodecount; i++)
		graph[type][i] = graph[type][i+1];
	
	delete graph[type][nodecount];
	if (nodecount < 0) nodecount = 0;
	graph[type]["count"] = nodecount;
}
function graphRemoveByID(type, graph, id){
	if(typeof(graph[type]) == 'undefined') graph[type] = graphGenerateEmpty();
	if(typeof(graph[type]["count"]) == 'undefined') graph[type]["count"] = 0;

	var nodecount = graph[type]["count"]-1;
	
	var position = -1;
	for (var i = 0; i < nodecount+1; i++)
		if ( graph[type][i]['id'] == id ){
			position = i;
			break;
		}
	if ( position == -1 ) return false;
	
	for (var i = position; i < nodecount; i++)
		graph[type][i] = graph[type][i+1];
	
	delete graph[type][nodecount];
	if (nodecount < 0) nodecount = 0;
	graph[type]["count"] = nodecount;
	return true;
}
function graphGetObjectByID(type, graph, id){
	if(typeof(graph[type]) == 'undefined') graph[type] = graphGenerateEmpty();
	if(typeof(graph[type]["count"]) == 'undefined') graph[type]["count"] = 0;
	
	var nodecount = graph[type]["count"];
	
	for (var i = 0; i < nodecount; i++)
		if (graph[type][i]['id'] == id)
			return graph[type][i];

	return null;
}
function graphGetUniqueID(type, graph){
	for (var id = 0; id < 100000; id++)
		if (graphGetObjectByID(type, graph, id) == null)
			return id;
	return -1;
}
function graphGetObjectsBySubject(type, graph, subjectid){
	if(typeof(graph[type]) == 'undefined') graph[type] = graphGenerateEmpty();
	if(typeof(graph[type]["count"]) == 'undefined') graph[type]["count"] = 0;
	
	var nodecount = graph[type]["count"];
	var ret = new Array();
	
	for (var i = 0; i < nodecount; i++)
		if (graph[type][i]['subjectid'] == subjectid)
			ret.push(graph[type][i]);

	return ret;
}

function graphAddNode(graph, node){
	return graphAdd("nodes", graph, node);
}
function graphRemoveNode(graph, index){
	return graphRemove("nodes", graph, index);
}

function graphAddEdge(graph, edge){
	return graphAdd("edges", graph, edge);
}
function graphRemoveEdge(graph, index){
	return graphRemove("edges", graph, index);
}