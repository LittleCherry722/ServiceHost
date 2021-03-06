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
 * TCTreeNode
 */

/**
 * Node of TCTree
 * 
 * @class TCTreeNode
 * @see org.jbpt.algo.tree.tctree.TCTreeNode
 * @param {Object} parent - Instance of LinearTimeLayout
 */
LinearTimeLayout.prototype.TCTreeNode = function (parent)
{
	/**
	 * ID of the node.
	 * @memberof! TCTreeNode
	 * @type {String}
	 */
	this.id			= "";
	
	/**
	 * Name of the node.
	 * @memberof! TCTreeNode
	 * @type {String}
	 */
	this.name		= "";
	
	/**
	 * Instance of Linear Time Layout.
	 * @memberof! TCTreeNode
	 * @type {Object}
	 */
	this.parent		= parent;
	
	/**
	 * Skeleton instance of the TCTree.
	 * @memberof! TCTreeNode
	 * @type {Object}
	 */
	this.skeleton	= new this.parent.TCTreeSkeleton(this.parent);
	
	/**
	 * Type of the triconnected component.
	 * @memberof! TCTreeNode
	 * @type {String}
	 */
	this.type		= null;
};