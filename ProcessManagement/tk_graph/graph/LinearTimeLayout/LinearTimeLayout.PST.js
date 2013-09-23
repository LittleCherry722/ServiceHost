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
 * PST (process structure tree)
 */
LinearTimeLayout.prototype.PST = function(parent) {
	this.parent = parent;
	this.virtualEdgeID = 0;
};

/*
 * PST Methods
 */

LinearTimeLayout.prototype.PST.prototype.decompose = function() {

	// Step 1. Compute the tree of the triconnected components of the TTG.
	// var tctree = this.triconnectivity(graph);
	var tctree = new this.parent.TCTree(this.parent, this.parent.backedge);

	// changed to simplified version of [2010]

	// 1) G' = (V', E', l') is the normalized version of G
	// step 1 is already done by parent.preprocess()

	// 2) T' is the tree of the triconnected components of G'
	// step 2 is done above (step 1 as in [2009])

	// 3) T is T' without trivial fragments in E' \ E
	var toRemove = {};
	for (var n in tctree.nodes) {
		var node = tctree.nodes[n];
		var edges = node.skeleton.o2e;
		for (var o in edges) {
			var edge = edges[o];
			if (gf_isset(this.parent.extraEdges[edge.orgId])) {
				node.skeleton.removeOriginalEdge(edge.orgId);
				if (node.type == "trivial") {
					toRemove[node.id] = node;
				}
			}
		}
	}
	tctree.removeNodes(toRemove);

	for (var n in tctree.nodes) {
		var node = tctree.nodes[n];
		var children = tctree.getChildren(node.id);
		if (children.length == 1) {
			var child = children.shift();
			var childNode = tctree.nodes[child];

			if (tctree.isRoot(node.id)) {
				tctree.removeNode(node.id);
				tctree.reRoot(child);
			} else {
				var parent = tctree.getParent(node.id);
				tctree.removeNode(node.id);
				tctree.addEdge(parent, child);
			}
		}
	}

	for (var n in tctree.nodes) {
		var node = tctree.nodes[n];
		var children = tctree.getChildren(node.id);
		var origEdges = node.skeleton.o2e;
		var origEdgesCount = node.skeleton.originalEdgesCount();

		if (node.type == "polygon" && children.length == 0 && origEdgesCount == 1) {
			// get first original edge
			var origEdge = null;
			for (var o in origEdges) {
				origEdge = origEdges[o];
			}

			var parentNodeID = tctree.getParent(node.id);
			var parentNode = tctree.nodes[parentNodeID];
			parentNode.skeleton.addEdge(origEdge);
			tctree.removeNode(node.id);
		}
	}

	// 4) R is T without redundant fragments
	var t2r = {};
	var rpst = new this.parent.RPST(this.parent);

	if (tctree.edgesEmpty()) {
		var node = null;
		for (var n in tctree.nodes) {
			node = tctree.nodes[n];
			break;
		}

		var newNode = new this.parent.RPSTNode(this.parent, rpst, node);
		rpst.addNode(newNode);
		rpst.root = newNode.id;
	} else {
		for (var e in tctree.edges) {
			var edge = tctree.edges[e];
			var sourceID = edge.v1.id;
			var targetID = edge.v2.id;
			var source = edge.v1;
			var target = edge.v2;

			// ignore extra edges
			if (target.type == "trivial" && target.skeleton.originalEdgesCount() == 0)
				continue;

			var rSource = gf_isset(t2r[sourceID]) ? t2r[sourceID] : null;
			var rTarget = gf_isset(t2r[targetID]) ? t2r[targetID] : null;

			if (rSource == null) {
				var rSource = new this.parent.RPSTNode(this.parent, rpst, source);
				rpst.addNode(rSource);
				t2r[sourceID] = rSource;
			}

			if (rTarget == null) {
				var rTarget = new this.parent.RPSTNode(this.parent, rpst, target);
				rpst.addNode(rTarget);

				// TODO
				if (rTarget.type == "trivial") {
					//					rTarget.name	= rTarget.getFragment().toString();
				}

				t2r[targetID] = rTarget;
			}

			if (tctree.isRoot(sourceID)) {
				rpst.root = rSource.id;
			}

			if (tctree.isRoot(targetID)) {
				rpst.root = rTarget.id;
			}

			rpst.addEdge(rSource.id, rTarget.id);
		}
	}
	return rpst;
};