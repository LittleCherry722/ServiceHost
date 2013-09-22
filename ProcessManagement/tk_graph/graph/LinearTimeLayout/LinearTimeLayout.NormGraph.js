/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Matthias Schrammek, Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 * 
 * Parts of this Code are based on or ported from the jBPT framework v0.2.393 (http://code.google.com/p/jbpt/).
 * All rights of the jBPT framework belong to their respective owners.
 * The jBPT framework is originally licensed under the Lesser General Public License (LGPL).
 * A copy of this license can be obtained at http://www.gnu.org/licenses/lgpl-3.0.en.html.
 */

/*
 * NormGraph (normalized graph)
 */
LinearTimeLayout.prototype.NormGraph = function (parent)
{
	this.edgeID		= 0;
	this.edges		= {};
	this.inEdges	= {};
	this.nodeID		= 0;
	this.nodes		= {};
	this.parent		= parent;
};

/*
 * NormGraph Methods
 */
LinearTimeLayout.prototype.NormGraph.prototype.addEdge = function (v1, v2, isVirtual)
{
	if (!gf_isset(isVirtual) || isVirtual !== true)
		isVirtual = false;
	
	var id		= "e" + this.edgeID++;
	var edge	= new this.parent.BasicEdge(id, this.nodes[v1], this.nodes[v2]);
		edge.setVirtual(isVirtual);
	
	this.edges[id]	= edge;
	
	if (!gf_isset(this.inEdges[v2]))
		this.inEdges[v2]	= new Array();
		
	this.inEdges[v2].push(id);
	
	return id;
};

LinearTimeLayout.prototype.NormGraph.prototype.addNode = function (name, virtual)
{
	if (!gf_isset(virtual) || virtual !== true)
		virtual	= false;
	
	var id	= "n" + this.nodeID++;
	this.nodes[id]	= {name: name, virtual: virtual, id: id};
	return id;
};

LinearTimeLayout.prototype.NormGraph.prototype.removeEdge = function (edge)
{
	delete this.edges[edge];
};

LinearTimeLayout.prototype.NormGraph.prototype.source = function (edge)
{
	if (gf_isset(this.edges[edge]))
		return this.edges[edge].v1.id;
		
	return null;
};

LinearTimeLayout.prototype.NormGraph.prototype.target = function (edge)
{
	if (gf_isset(this.edges[edge]))
		return this.edges[edge].v2.id;
		
	return null;
};