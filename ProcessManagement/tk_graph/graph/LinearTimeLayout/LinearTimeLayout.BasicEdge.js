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
 * create BasicEdge SubClass
 */
LinearTimeLayout.prototype.BasicEdge	= function (id, v1, v2)
{
	this.components	= {};
	this.virtual	= false;
	this.orgId		= null;
	this.type		= null;
	
	this.id	= id;
	this.v1	= v1;
	this.v2	= v2;
};

/*
 * Edge Methods
 */
LinearTimeLayout.prototype.BasicEdge.prototype.changeOrientation = function (v1, v2)
{
	if (v2 == this.v1.id || v1 == this.v2.id)
	{
		var temp	= this.v1;
		this.v1		= this.v2;
		this.v2		= temp;
	}
};

LinearTimeLayout.prototype.BasicEdge.prototype.connectsVertices = function (v1, v2)
{
	return this.v1.id == v1 && this.v2.id == v2 || this.v1.id == v2 && this.v2.id == v1;
};

LinearTimeLayout.prototype.BasicEdge.prototype.getOtherVertex = function (v)
{
	if (v == this.v1.id)
		return this.v2.id;
		
	if (v == this.v2.id)
		return this.v1.id;
		
	return null;
};

LinearTimeLayout.prototype.BasicEdge.prototype.getVertices = function ()
{
	return [this.v1.id, this.v2.id];
};

LinearTimeLayout.prototype.BasicEdge.prototype.isSelfLoop = function ()
{
	return this.v1.id == this.v2.id && this.v1 != null && this.v2 != null;
};

LinearTimeLayout.prototype.BasicEdge.prototype.isVirtual = function ()
{
	return this.virtual === true;
};

LinearTimeLayout.prototype.BasicEdge.prototype.setVirtual = function (virtual)
{
	if (gf_isset(virtual) && virtual === true)
	{
		this.virtual	= true;
	}
};

LinearTimeLayout.prototype.BasicEdge.prototype.toString = function ()
{
	return this.v1.name + "->" + this.v2.name;
};