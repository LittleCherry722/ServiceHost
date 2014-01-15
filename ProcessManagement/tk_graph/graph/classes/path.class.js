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
 * Creates a Raphael path and contains some information about that path.
 * This represents an edge in the behavioral view.
 * 
 * @private
 * @class creates a Raphael path
 * @param {int} startx The x ordinate of the starting point.
 * @param {int} starty The y ordinate of the starting point.
 * @param {int} endx The x ordinate of the ending point.
 * @param {int} endy The y ordinate of the ending point.
 * @param {String} shape The shape of the path. Possible values are "I", "L", "Z", "U", "S", "C", "G", "SI", "ZU", "UI"
 * @param {String} text The text to display on the edge's label
 * @param {String} id The id of the edge.
 * @param {boolean} performanceMode When set to true the style won't be updated on init.
 * @returns {void}
 */
function GCpath (startx, starty, endx, endy, shape, text, id, performanceMode)
{
	/**
	 * Deactivation flag.
	 * When it is set to true the path will be displayed as deactivated.
	 * 
	 * @type boolean
	 */
	this.deactive = false;
	
	/**
	 * The center of the path.
	 * This is used to position the label of the path.
	 * 
	 * @type Object
	 */
	this.edgeCenter	= {x: 0, y: 0};
	
	/**
	 * The direction of the first line of the path.
	 * This is needed to correctly display the path.
	 * 
	 * @type String
	 */
	this.firstLine	= "v";
	
	/**
	 * The id of the path.
	 * 
	 * @type String
	 */
	this.id = "";
	
	/**
	 * The label of the path.
	 * 
	 * @type GClabel
	 */
	this.label	= null;
	
	/**
	 * Optional flag.
	 * Whe it is set to true the path will be displayed as an optional path.
	 * 
	 * @type boolean
	 */
	this.optional	= false;
	
	/**
	 * A Raphael Path.
	 * 
	 * @see Paper.path() at the <a href="http://raphaeljs.com/reference.html#Paper.path">Raphael documentation</a>
	 * @type Element (from Raphael)
	 */
	this.path	= null;


    /**
     * A Raphael Path which starts at the labels original position and ends on the labels offset position
     *
     * @see Paper.path() at the <a href="http://raphaeljs.com/reference.html#Paper.path">Raphael documentation</a>
     * @type Element (from Raphael)
     */
    this.pathToLabel	= null;

	/**
	 * A Raphael Path.
	 * This path will be a bit wider to ease the action of clicking a path.
	 * 
	 * @see Paper.path() at the <a href="http://raphaeljs.com/reference.html#Paper.path">Raphael documentation</a>
	 * @type Element (from Raphael)
	 */
	this.pathClick	= null;
	
	/**
	 * The path of this path element as segments.
	 * 
	 * @type Array
	 */
	this.pathSegments	= [];
	
	/**
	 * The string representation of the path.
	 * 
	 * @type String
	 */
	this.pathStr	= "";
	
	/**
	 * The end position of the path.
	 * 
	 * @type Object
	 */
	this.positionEnd = {x: 0, y: 0};
	
	/**
	 * The start position of the path.
	 * 
	 * @type Object
	 */
	this.positionStart = {x: 0, y: 0};
	
	/**
	 * Selected flag.
	 * When set to true the path will be displayed in a different way.
	 * 
	 * @type boolean
	 */
	this.selected = false;
	
	/**
	 * The shape of the path.
	 * Possible values: "I", "L", "Z", "U", "S", "C", "G", "SI", "ZU", "UI"
	 * 
	 * @type String
	 */
	this.shape		= "I";
	
	/**
	 * A space used to calculate the path.
	 * 
	 * @type int
	 */
	this.space1		= 0;
	
	/**
	 * A space used to calculate the path.
	 * 
	 * @type int
	 */
	this.space2		= 0;
	
	/**
	 * The style set for the path.
	 * 
	 * @type Object
	 */
	this.style = gv_defaultStyle;

    /**
     * The user-defined manual offset for the position of the path label
     *
     * @type {?{dx: int, dy: int}}
     */
    this.manualPositionOffsetLabel = null;


	/**
	 * Activate the path and its label and update its look.
	 * 
	 * @returns {void}
	 */
	this.activate = function ()
	{
		this.deactive = false;
		this.label.activate();
		this.refreshStyle();
	};
	
	/**
	 * Adds a new point to the path's segments list.
	 * A segment always consists of two consecutive points.
	 * 
	 * @param {Object} start Refreshes the path's segments array and stores the starting point {x,y}.
	 * @param {Object} point Adds a point to the path's segments list.
	 * @returns {void}
	 */
	this.calculatePathSegments	= function (start, point)
	{
		
		if (gf_isset(start))
		{
			this.pathSegments		= [];
			this.pathSegments[0]	= start;
		}
		
		if (gf_isset(point))
		{
			var gt_length	= this.pathSegments.length;
			this.pathSegments[gt_length]	= point;
		}
		
	};
	
	/**
	 * Calculate the path for the given parameters.
	 * The object returned contains the following information:
	 * <br />
	 * - path: the path string<br />
	 * - x: the x ordinate of the center of the path<br />
	 * - y: the y ordinate of the center of the path
	 * 
	 * @param {int} x1 The x ordinate of the starting point of the path.
	 * @param {int} y1 The y ordinate of the starting point of the path.
	 * @param {int} x2 The x ordinate of the ending point of the path.
	 * @param {int} y2 The y ordinate of the ending point of the path.
	 * @param {String} shape The shape of the path. Possible values: "I", "L", "U", "Z", "G", "C", "S", "UI", "ZU", "SI"
	 * @param {String} firstLine The direction of the first line of the path. Possible values: "v" (vertical) or "h" (horizontal)
	 * @param {int} space1 A space for the calculation of the path. 
	 * @param {int} space2 A space for the calculation of the path.
	 * @returns Object
	 */
	this.calculateShape = function (x1, y1, x2, y2, shape, firstLine, space1, space2)
	{
		var cPath	= "";
		var cX		= 0;
		var cY		= 0;
		
		if (gf_isset(x1, x2, y1, y2, shape))
		{
			shape = shape.toUpperCase();
			
			firstLine	= gf_isset(firstLine) && firstLine.toLowerCase() == "h" ? "h" : "v";
			space1		= gf_isset(space1) ? space1 : 0;
			space2		= gf_isset(space2) ? space2 : 0;
			
			var diffX	= Math.round(x1 - x2);
			var diffY	= Math.round(y1 - y2);
			var absDiffX	= Math.abs(diffX);
			var absDiffY	= Math.abs(diffY);
			
			var rcX		= Math.round((x1 + x2) / 2);
			var rcY		= Math.round((y1 + y2) / 2);
			
			// shape "straight" for ltl
			if (shape == "STRAIGHT")
			{
				cX		= rcX;
				cY		= rcY;
				cPath	= absDiffX > absDiffY ? "H" + x2 : "V" + y2;
				this.calculatePathSegments(null, {x: x2, y: y2});
			}
			
			// shapes for ltl with one straight element and one diagonal element
			else if (shape == "DIAGVERT" || shape == "VERTDIAG")
			{
				var bendX		= shape == "DIAGVERT" ? x2 : x1;
				
				cPath	= "L" + bendX + "," + rcY + "L" + x2 + "," + y2;
				cX		= bendX;
				cY		= rcY;
			}
			
			// shapes for ltl with one straight element and one diagonal element for left-to-right layout
			else if (shape == "DIAGHOR" || shape == "HORDIAG")
			{
				var bendY		= shape == "DIAGHOR" ? y2 : y1;
				
				cPath	= "L" + rcX + "," + bendY + "L" + x2 + "," + y2;
				cX		= rcX;
				cY		= bendY;
			}
			
			// loop edge in ltl
			else if (shape == "LOOP")
			{
				cPath	= "H" + x2 + "V" + y2 + "H" + x1;
				cX		= x2;
				cY		= rcY;
			}
			
			// loop edge in ltl for left-to-right layout
			else if (shape == "LOOPLTR")
			{
				console.log("keks");
				cPath	= "V" + y2 + "H" + x2 + "V" + y1;
				cX		= rcX;
				cY		= y2;
			}
			
			// diagonal line for ltl
			else if (shape == "DIAG")
			{
				// TODO: remove if and else if as they are already handled by shape == "STRAIGHT" ??
				// straight line for same x
				if (x2 == x1)
				{
					cPath	= "V" + y2;
					cX	= x1;
					cY	= rcY;
				}
				
				else if (y2 == y1)
				{
					cPath	= "H" + x2;
					cX	= rcX;
					cY	= y1;	
				}
				
				// diagonal lines
				else
				{
					var thirdX	= Math.abs(Math.floor(diffX / 3));
					var fifthY	= Math.abs(Math.floor(diffY / 5));
					
					var xTemp1	= (x1 < x2) ? x1 + thirdX : x1 - thirdX;
					var xTemp2	= (x1 < x2) ? x2 - thirdX : x2 + thirdX;
					var yTemp1	= (y1 < y2) ? y1 + fifthY : y1 - fifthY;
					var yTemp2	= (y1 < y2) ? y2 - fifthY : y2 + fifthY;
					
					cX	= (x1 < x2) ? xTemp1 : xTemp2;
					cY	= (x1 < x2) ? yTemp1 : yTemp2;
					cPath = "L" + xTemp1 + "," + yTemp1 + "L" + xTemp2 + "," + yTemp2 + "L" + x2 + "," + y2;
				}
			}
			
			else if (absDiffX > 0 || absDiffY > 0)
			{
				if (shape == "I" && absDiffX > 0 && absDiffY > 0)
				{
					shape = "L";
				}

				// calculate the path for an I shaped path								
				if (shape == "I")
				{
					cX		= rcX;
					cY		= rcY;
					cPath	= absDiffX > absDiffY ? "H" + x2 : "V" + y2;
					this.calculatePathSegments(null, {x: x2, y: y2});
				}
				
				// calculate the path for a L shaped path
				else if (shape == "L")
				{
					cX		= firstLine == "v" ? x1 : x2;
					cY		= firstLine == "h" ? y1 : y2;
	
					// an L shaped arrow consists of two I shaped arrows
					var part1 = this.calculateShape(x1, y1, cX, cY, "I");
					var part2 = this.calculateShape(cX, cY, x2, y2, "I");
					
					cPath = part1.path + part2.path;
				}
				
				// calculate the path for a Z shaped path
				else if (shape == "Z")
				{
					cX		= rcX;
					cY		= rcY;
					
					var part1 = this.calculateShape(x1, y1, cX, cY, "L", firstLine);
					var part2 = this.calculateShape(cX, cY, x2, y2, "L", firstLine == "h" ? "v" : "h");
					
					cPath = part1.path + part2.path;
				}
				
				// calculate the path for an U shaped path
				else if (shape == "U")
				{
					var tx	= space1 < 0 ? Math.min(x1, x2) : Math.max(x1, x2);
					var ty	= space1 < 0 ? Math.min(y1, y2) : Math.max(y1, y2);
					
					cX		= firstLine == "h" ? tx + space1 : rcX;
					cY		= firstLine == "v" ? ty + space1 : rcY;
	
					var part1 = this.calculateShape(x1, y1, cX, cY, "L", firstLine);
					var part2 = this.calculateShape(cX, cY, x2, y2, "L", firstLine == "h" ? "v" : "h");
					
					cPath = part1.path + part2.path;
				}
				
				// calculate the path for a G shaped path
				else if (shape == "G")
				{
					// firstLine: v => space1: u | d ; space2: l | r
					// firstLine: h => space1: l | r ; space2: u | d
	
					if (firstLine == "v")
					{
						if (x1 < x2 && y1 < y2 && space1 > 0 && space2 < 0) space2 *= -1;
						if (x1 > x2 && y1 > y2 && space1 < 0 && space2 > 0) space2 *= -1;
						if (x1 > x2 && y1 < y2 && space1 > 0 && space2 > 0) space2 *= -1;
						if (x1 < x2 && y1 > y2 && space1 < 0 && space2 < 0) space2 *= -1;
						
						cX = space2 < 0 ? Math.min(x1, x2) + space2 : Math.max(x1, x2) + space2;
						cY = space1 < 0 ? Math.min(y1, y2) + space1 : Math.max(y1, y2) + space1;
					}
					else
					{
						if (x1 < x2 && y1 < y2 && space1 > 0 && space2 < 0) space2 *= -1;
						if (x1 > x2 && y1 > y2 && space1 < 0 && space2 > 0) space2 *= -1;
						if (x1 > x2 && y1 < y2 && space1 < 0 && space2 < 0) space2 *= -1;
						if (x1 < x2 && y1 > y2 && space1 > 0 && space2 > 0) space2 *= -1;
						
						cX = space1 < 0 ? Math.min(x1, x2) + space1 : Math.max(x1, x2) + space1;
						cY = space2 < 0 ? Math.min(y1, y2) + space2 : Math.max(y1, y2) + space2;
					}
	
					var part1 = this.calculateShape(x1, y1, cX, cY, "L", firstLine);
					var part2 = this.calculateShape(cX, cY, x2, y2, "L", firstLine);
					
					cPath = part1.path + part2.path;
				}
				
				// calculate the path for a C shaped path
				else if (shape == "C")
				{
					
					// TODO: check
					
					cX = firstLine == "v" ? x1 + space2 : rcX;
					cY = firstLine == "h" ? y1 + space2 : rcY;
	
					space1	= firstLine == "v" && y1 < y2 ? 0 - space1 : space1;
					space1	= firstLine == "h" && x1 < x2 ? 0 - space1 : space1;
	
					var part1 = this.calculateShape(x1, y1, cX, cY, "U", firstLine, space1);
					var part2 = this.calculateShape(cX, cY, x2, y2, "U", firstLine, 0 - space1);
					
					cPath = part1.path + part2.path;
				}
				
				// calculate the path for a S shaped path
				else if (shape == "S")
				{
					cX = rcX;
					cY = rcY;
					
					var part1 = this.calculateShape(x1, y1, cX, cY, "U", firstLine, space1);
					var part2 = this.calculateShape(cX, cY, x2, y2, "U", firstLine, 0 - space1);
					
					cPath = part1.path + part2.path;
				}
				
				// calculate the path for an U shaped path with an I shaped path
				else if (shape == "UI")
				{
					var tx = x2;
					var ty = y2;
					
					if (firstLine == "v")
					{
						if (x1 > x2)
							tx += Math.abs(space2);
						else
							tx -= Math.abs(space2);
					}
					else
					{
						if (y1 > y2)
							ty += Math.abs(space2);
						else
							ty -= Math.abs(space2);	
					}
					
					var part1 = this.calculateShape(x1, y1, tx, ty, "U", firstLine, space1);
					var part2 = this.calculateShape(tx, ty, x2, y2, "I");
					
					cX		= part1.x;
					cY		= part1.y;
					cPath	= part1.path + part2.path;
				}
				
				// calculate the path for a Z shaped path with an U shaped path
				else if (shape == "ZU")
				{
					cX = firstLine == "h" ? rcX + space1 : x1;
					cY = firstLine == "v" ? rcY + space1 : y1;
				
					var zFirst = false;
					
					if (firstLine == "v")
					{		
						cX = space2 > 0 ? Math.max(x1, x2) + space2 : Math.min(x1, x2) + space2;
						
						zFirst = (y1 < y2 && space1 > 0) || (y1 > y2 && space1 < 0);
					}
					else
					{
						cY = space2 > 0 ? Math.max(y1, y2) + space2 : Math.min(y1, y2) + space2;
						
						zFirst = (x1 < x2 && space1 > 0) || (x1 > x2 && space1 < 0);
					}
					
					var part1 = null;
					var part2 = null;
					
					if (zFirst)
					{
						part1 = this.calculateShape(x1, y1, cX, cY, "Z", firstLine);
						part2 = this.calculateShape(cX, cY, x2, y2, "U", firstLine, space1);
					}
					else
					{
						part1 = this.calculateShape(x1, y1, cX, cY, "U", firstLine, space1);
						part2 = this.calculateShape(cX, cY, x2, y2, "Z", firstLine);
					}
					
					cPath = part1.path + part2.path;
				}
				
				// calculate the path for a S shaped path with an I shaped path
				else if (shape == "SI")
				{
					var tx = firstLine == "h" ? x2 : (x1 < x2 ? x2 + Math.abs(space1) : x2 - Math.abs(space1));
					var ty = firstLine == "v" ? y2 : (y1 < y2 ? y2 + Math.abs(space1) : y2 - Math.abs(space1));
					
					var part1 = this.calculateShape(x1, y1, tx, ty, "S", firstLine, space2);
					var part2 = this.calculateShape(tx, ty, x2, y2, "I");
					
					cX		= part1.x;
					cY		= part1.y;
					cPath	= part1.path + part2.path;
				}
				else
				{
					// unknown shape: draw "L" shape
					this.calculateShape(x1, y1, x2, y2, "L");
				}
			}
		}
		
		return {path: cPath, x: cX, y: cY};
	};
	
	/**
	 * Check if the path intersects with another path or a label on this paper.
	 * 
	 * @param {boolean} labelsOnly When set to true the intersection checks will only be executed against labels.
	 * @param {Object} coordinatesOrg Additional parameter containing the original start and end point of the path. {x1, x2, y1, y2}
	 * @returns {boolean} True when the path intersects with another object.
	 */
	this.checkIntersection = function (labelsOnly, coordinatesOrg)
	{
		
		if (!gf_isset(labelsOnly) || labelsOnly !== true)
			labelsOnly = false;
			
		// var thisLabelPath	= this.label.toPathSegments();
		
		// TODO: limit intersections (max 1 with the same path), check same segments
		
		var gt_isIntersection	= false;
		
		// gf_timeCalc("path - intersection checks (labels)");
		for (objId in gv_objects_nodes)
		{
			var tObject		= gv_objects_nodes[objId];
			
			var gt_tObjectSegments	= tObject.toPathSegments();
			
			// check intersection of path with node
			if (!labelsOnly && this.checkIntersectionSegments(this.pathSegments, gt_tObjectSegments))
			{
				gt_isIntersection	= true;				
				break;
			}
				
			// check intersection of path's label with node (deactivated)
			/*
			if (!labelsOnly && this.checkIntersectionSegments(thisLabelPath, tObject.toPathSegments()))
			{
				gt_isIntersection	= true;				
				break;
			}
			*/
		}
		// gf_timeCalc("path - intersection checks (labels)");
		
		if (gt_isIntersection)
			return true;
		
		
		// gf_timeCalc("path - intersection checks (paths)");
		// check whether the path intersects with other paths or with their labels
		for (objId in gv_objects_edges)
		{
			if (objId != this.id)
			{
				var tObject	= gv_objects_edges[objId];
				
				if (gf_isset(coordinatesOrg) && gf_objectHasAttribute(coordinatesOrg, ["x1", "x2", "y1", "y2"]))
				{
					// ignore paths that share the same segment at a node
					if (tObject.getPositionStart().x == coordinatesOrg.x1 && tObject.getPositionStart().y == coordinatesOrg.y1 && this.firstLine == tObject.firstLine)
					{
						continue;
					}
					
					if (tObject.getPositionEnd().x == coordinatesOrg.x2 && tObject.getPositionEnd().y == coordinatesOrg.y2)
					{
						continue;
					}
				}
				
				var gt_tObjectSegments	= tObject.label.toPathSegments();
				
				// check intersection of path with other paths (deactivated)
				/*
				if (!labelsOnly && this.checkIntersectionSegments(this.pathSegments, tObject.pathSegments))
				{
					gt_isIntersection	= true;				
					break;
				}
				*/
				
				// check intersection of path with other paths' labels
				if (this.checkIntersectionSegments(this.pathSegments, gt_tObjectSegments))
				{
					gt_isIntersection	= true;				
					break;
				}
					
				// check intersection of path's label with other paths (deactivated)
				/*
				if (!labelsOnly && this.checkIntersectionSegments(thisLabelPath, tObject.pathSegments))
				{
					gt_isIntersection	= true;				
					break;
				}
				*/
				
				// check intersection of path's label with other paths' labels (deactivated)
				/*
				if (this.checkIntersectionSegments(thisLabelPath, tObject.label.toPathSegments()))
				{
					gt_isIntersection	= true;				
					break;
				}
				*/
			}
		}
		// gf_timeCalc("path - intersection checks (paths)");
		
		if (gt_isIntersection)
			return true;
		
		return false;
	};
	
	/**
	 * Calculates the intersection point for the given segment lists.
	 * 
	 * @param {Array} segments1 A list of segments {x,y}.
	 * @param {Array} segments2 A list of segments {x,y}.
	 * @returns {boolean} True when there is an intersection point between both lines, false otherwise.
	 */
	this.checkIntersectionSegments = function (segments1, segments2)
	{
		var gt_intersects	= false;
		
		gf_timeCalc("path - intersection check - calculation");
		for (var gt_i1 = 0; gt_i1 < segments1.length - 1; gt_i1++)
		{
			for (var gt_i2 = 0; gt_i2 < segments2.length - 1; gt_i2++)
			{
				// gt_segment1point1 | gt_segment1point2 | gt_segment2point1 | gt_segment2point2
				var gt_s1p1	= segments1[gt_i1];
				var gt_s1p2	= segments1[gt_i1+1];
				
				var gt_s2p1	= segments2[gt_i2];
				var gt_s2p2	= segments2[gt_i2+1];
				
				var gt_a1	= gt_s1p2.y - gt_s1p1.y;
				var gt_a2	= gt_s2p2.y - gt_s2p1.y;
				
				var gt_b1	= gt_s1p2.x - gt_s1p1.x;
				var gt_b2	= gt_s2p2.x - gt_s2p1.x;
				
				var gt_denom = gt_a1 * gt_b2 - gt_a2 * gt_b1
			    if (gt_denom == 0)
			    {
			        //Lines are parallel
			    }
			    else
			    {
				
					var gt_c1	= gt_a1 * gt_s1p1.x + gt_b1 * gt_s1p1.y;
					var gt_c2	= gt_a2 * gt_s2p1.x + gt_b2 * gt_s2p1.y;
				
			        var gt_x = (gt_b2 * gt_c1 - gt_b1 * gt_c2) / gt_denom;
			        var gt_y = (gt_a1 * gt_c2 - gt_a2 * gt_c1) / gt_denom;
			        
			        if ((Math.min(gt_s1p1.x, gt_s1p2.x) <= gt_x && gt_x <= Math.max(gt_s1p1.x, gt_s1p2.x)) &&
			        	(Math.min(gt_s1p1.y, gt_s1p2.y) <= gt_y && gt_y <= Math.max(gt_s1p1.y, gt_s1p2.y)) &&
			        	(Math.min(gt_s2p1.x, gt_s2p2.x) <= gt_x && gt_x <= Math.max(gt_s2p1.x, gt_s2p2.x)) &&
			        	(Math.min(gt_s2p1.y, gt_s2p2.y) <= gt_y && gt_y <= Math.max(gt_s2p1.y, gt_s2p2.y)))
			        		gt_intersects = true;
			    }
			}
		}
		gf_timeCalc("path - intersection check - calculation");
		
		return gt_intersects;
	};
	
	/**
	 * Activate the event handlers for click and dblClick on the path and its label.
	 * 
	 * @returns {void}
	 */
	this.click = function ()
	{
		id = this.id;
		this.path.click(function () {gf_paperClickEdge(id); });
		this.pathClick.click(function () {gf_paperClickEdge(id); });
		this.label.click("bv");
	};
	
	/**
	 * Deactivate the path and its label and update its look.
	 * 
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.deactivate = function (performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		this.deactive = true;
		this.label.deactivate(performanceMode);
		
		if (!performanceMode)
			this.refreshStyle();
	};	
	
	/**
	 * Deselect the path and its label and update its look.
	 * 
	 * @returns {void}
	 */
	this.deselect = function ()
	{
		this.selected = false;
		this.label.deselect();
		this.refreshStyle();
	};
	
	/**
	 * Returns the end point of the path.
	 * 
	 * @returns {Object}
	 */
	this.getPositionEnd = function ()
	{
		return this.positionEnd;
	};
	
	/**
	 * Returns the starting point of the path.
	 * 
	 * @returns {Object}
	 */
	this.getPositionStart = function ()
	{
		return this.positionStart;
	};
	
	/**
	 * Returns the status dependent string for selecting the right information from the style set.
	 * 
	 * @returns {String} The string addition for the correct style.
	 */
	this.getStatusDependent = function ()
	{
		var statusDependent = "";
		if (this.optional === true)
		{
			statusDependent += "Opt";
		}
		if (this.selected === true)
		{
			statusDependent += "Sel";
		}
		if (this.deactive === true)
		{
			statusDependent += "Deact";
		}
		
		return statusDependent;
	}
	
	/**
	 * Hide the path and its label.
	 * 
	 * @returns {void}
	 */
	this.hide = function ()
	{
		this.path.hide();
		this.label.hide();
	};
		
	/**
	 * Initialize the Raphael Elements of this path.
	 * 
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.init = function (performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		this.path		    = gv_paper.path("M0,0L10,10");
        this.pathToLabel	= gv_paper.path("M0,0L0,0");
		this.pathClick	    = gv_paper.path("M0,0L10,10");
		this.label		    = new GClabel(0, 0, text, "roundedrectangle", id, true, performanceMode);
	};
	
	/**
	 * Returns true when the path is set to be optional.
	 * 
	 * @returns {boolean} True when the path is set to be optional.
	 */
	this.isOptional = function ()
	{
		return this.optional === true;
	};
	
	/**
	 * Read a value from the style set.
	 * 
	 * @param {String} key The key to read from the style set.
	 * @param {String} type A type to determine the default value if the key was not found in the style.
	 * @return {String} The style value.
	 */
	this.readStyle = function (key, type)
	{
		return gf_getStyleValue(this.style, key, type);
	};
	
	/**
	 * Update the style information of all Raphael elements belonging to this path.
	 * 
	 * @returns {void}
	 */
	this.refreshStyle = function ()
	{
		/*
		 * status dependent styles
		 */
		var statusDependent = this.getStatusDependent();
		
		var strokeDasharray	= gf_getStrokeDasharray(this.readStyle("arrowStyle" + statusDependent, ""));
		var strokeWidth		= strokeDasharray == "none" ? 0 : this.readStyle("arrowWidth" + statusDependent, "int");
		// apply the settings to the path
		var params						= {};
			params["opacity"]			= this.readStyle("opacity" + statusDependent, "float");
			params["stroke-dasharray"]	= strokeDasharray;
			params["stroke-opacity"]	= this.readStyle("arrowOpacity" + statusDependent, "float");
			params["stroke-width"]		= strokeWidth;
			params["stroke"]			= this.readStyle("arrowColor" + statusDependent, "");
			params["arrow-end"]			= this.readStyle("arrowHeadType", "") + "-" + this.readStyle("arrowHeadWidth", "") + "-" + this.readStyle("arrowHeadLength", "");
			params["stroke-linecap"]	= this.readStyle("arrowLinecap", "");
			params["stroke-linejoin"]	= this.readStyle("arrowLinejoin", "");
			
		this.path.attr(params);
		
			params						= {};
			params["opacity"]			= 0;
			params["stroke-width"]		= strokeWidth * 6;	// set the width to 5 times the normal width
		this.pathClick.attr(params);

        this.pathToLabel.attr({'opacity': 0.5});
	};
	
	/**
	 * Select the path and its label and update its look.
	 * 
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.select = function (performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		this.selected = true;
		this.label.select(performanceMode);
		
		if (!performanceMode)
			this.refreshStyle();
	};
	
	/**
	 * Sets a flag whether the text dimensions should be estimated or not.
	 * 
	 * @param {boolean} estimate True or false.
	 * @returns {void}
	 */
	this.setEstimateTextDimensions = function (estimate)
	{
		this.label.setEstimateTextDimensions(estimate);
	};
	
	/**
	 * Set the firstLine of this path.
	 * 
	 * @param {String} firstLine The direction of the first line of the path. Possible values: "v" (vertical line), "h" (horizontal line)
	 * @returns {void}
	 */
	this.setFirstLine = function (firstLine)
	{
		this.firstLine = firstLine;
	};
	
	/**
	 * Set the optional flag to the path.
	 * 
	 * @param {boolean} optional When set to true the path will be set to be optional.
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.setOptional = function (optional, performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		this.optional = gf_isset(optional) && optional === true;
		this.label.setOptional(this.optional, performanceMode);
		
		if (!performanceMode)
			this.refreshStyle();
	};
	
	/**
	 * Update the end position of the path.
	 * 
	 * @param {int} x The x ordinate of the end position.
	 * @param {int} y The y ordinate of the end position.
	 * @returns {void}
	 */
	this.setPositionEnd = function (x, y)
	{
		if (gf_isset(x, y))
		{
			this.positionEnd = {x: Math.round(x), y: Math.round(y)};
		}
	};
	
	/**
	 * Update the starting position of the path.
	 * 
	 * @param {int} x The x ordinate of the starting position.
	 * @param {int} y The y ordinate of the starting position.
	 * @returns {void}
	 */
	this.setPositionStart = function (x, y)
	{
		if (gf_isset(x, y))
		{
			this.positionStart = {x: Math.round(x), y: Math.round(y)};
		}
	};
	
	/**
	 * Update the shape of the path.
	 * 
	 * @param {String} shape The shape of the path. Possible values: "I", "L", "U", "Z", "G", "C", "S", "UI", "ZU", "SI"
	 * @param {int} performanceMode The performance mode will increase performance by applying a reduced set on rules to the path's label.
	 * @returns {void}
	 */
	this.setShape = function (shape, performanceMode)
	{
		if (!gf_isset(performanceMode))
			performanceMode = 0;
			
		this.shape = shape;
		this.updatePath(performanceMode);
	};
	
	/**
	 * Update space1 of the path.
	 * 
	 * @param {int} space1
	 * @returns {void}
	 */
	this.setSpace1 = function (space1)
	{
		this.space1 = space1;
	};
	
	/**
	 * Update space2 of the path.
	 * 
	 * @param {int} space2
	 * @returns {void}
	 */
	this.setSpace2 = function (space2)
	{
		this.space2 = space2;
	};
	
	/**
	 * Loads a new style set and calls the refreshStyle method.
	 * 
	 * @param {Object} style The style set to load.
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.setStyle = function (style, performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		this.style = gf_mergeStyles(gv_defaultStyle, style);
		this.label.setStyle(style, performanceMode);
		
		if (!performanceMode)
			this.refreshStyle();
	};
	
	/**
	 * Update the text of the path's label.
	 * 
	 * @param {String} text The text of the path's label.
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.setText = function (text, performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		this.label.setText(text, performanceMode);
	};

    /**
     * The user-defined manual offset for the path label position. If the user defined no offset, an offstet of 0 pixels in
     * each direction is returned
     *
     * @returns {{dx: int, dy: int}}
     */
    this.getManualPositionOffsetLabel = function ()
    {
        return this.manualPositionOffsetLabel || {dx: 0, dy: 0};
    };

    /**
     * Sets the user-defined manual offset for the path label position
     *
     * @param {null|{dx: int, dy: int}} offset
     * @returns {void}
     */
    this.setManualPositionOffsetLabel = function (offset)
    {
        this.manualPositionOffsetLabel = offset;
    };

    /**
     * @returns {boolean} true if the the path label has a user-defined offset
     */
    this.hasManualPositionOffsetLabel = function ()
    {
        return this.manualPositionOffsetLabel !== null && 'dx' in this.manualPositionOffsetLabel && 'dy' in this.manualPositionOffsetLabel;
    };

	/**
	 * Show the path and its label.
	 * 
	 * @param {boolean} performanceMode When set to true, the style won't be refreshed.
	 * @returns {void}
	 */
	this.show = function (performanceMode)
	{
		if (!gf_isset(performanceMode) || performanceMode != true)
			performanceMode	= false;
			
		this.path.show();
        this.pathToLabel.show();
		this.label.show(performanceMode);
	};
	
	/**
	 * Update the path.
	 * Adapt the path to the start and end point and to the shape.
	 * 
	 * @param {int} performanceMode The performance mode will increase performance by applying a reduced set on rules to the path's label.
	 * @returns {void}
	 */
	this.updatePath = function (performanceMode)
	{
		if (!gf_isset(performanceMode))
			performanceMode = 0;
			
		var x1 = this.positionStart.x;
		var y1 = this.positionStart.y;
		var x2 = this.positionEnd.x;
		var y2 = this.positionEnd.y;
		
		var shape	= this.shape;

        var offsetPosX, offsetPosY;
			
		// calculate the shape
		gf_timeCalc("path - update path (shape calculation)");
		this.calculatePathSegments({x: x1, y: y1});
		var newPath	= this.calculateShape(x1, y1, x2, y2, shape, this.firstLine, this.space1, this.space2);
		gf_timeCalc("path - update path (shape calculation)");
		
		gf_timeCalc("path - update path (path attr)");
		if (performanceMode != 1)
		{
			gf_taskCounterCount("path - path attr");
			
			this.pathStr	= "M" + x1 + "," + y1 + newPath.path;
			this.path.attr("path", this.pathStr);
			this.pathClick.attr("path", this.pathStr);
		}
		gf_timeCalc("path - update path (path attr)");
		
		this.edgeCenter.x	= newPath.x;
		this.edgeCenter.y	= newPath.y;
		
		// move the label to the center of the label
		gf_timeCalc("path - update path (label position)");
        offsetPosX = newPath.x + this.getManualPositionOffsetLabel()['dx'];
        offsetPosY = newPath.y + this.getManualPositionOffsetLabel()['dy'];
        this.label.setPosition(offsetPosX, offsetPosY, performanceMode);
        this.pathToLabel.attr("path", "M" + newPath.x + "," + newPath.y + "L" + offsetPosX + "," + offsetPosY);
        gf_timeCalc("path - update path (label position)");
	};
	
	if (!gf_isset(performanceMode) || performanceMode != true)
		performanceMode	= false;

	// set the id
	this.id = id;
	
	// initialize the path
	this.init(true);
	
	// set the starting and end position
	this.setPositionStart(startx, starty);
	this.setPositionEnd(endx, endy);
	
	// set the shape
	this.setShape(shape, 1);
		
	if (!performanceMode)
		this.refreshStyle();
	
	// set the text of the label
	this.setText(text, true);
	
	gv_objects_edges[id] = this;
	
	// move path to back so line-crossings aren't that obvious
	this.path.toBack();
	this.pathClick.toBack();
}