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

// graphid that will be set on gv_xx_ctx.ga_id to identify the ctx
var gv_graphID	= 0;

/*
 * function to check if a variable / array-index is set
 */
function gf_isset ()
{
    // http://kevin.vanzonneveld.net
    // +   original by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // +   improved by: FremyCompany
    // +   improved by: Onno Marsman
    // +   improved by: Rafał Kukawski
    var gt_a = arguments,
    	gt_l = gt_a.length,
    	gt_i = 0,
    	gt_undef;

    if (gt_l === 0)
    {
        throw new Error('Empty isset');
    }

    while (gt_i !== gt_l)
    {
        if (gt_a[gt_i] === gt_undef || gt_a[gt_i] === null)
        {
            return false;
        }
        gt_i++;
    }
    return true;
}

/*
 * function to count elements of an array / object (same as array.length but also works for non-numerical indexes)
 */
function gf_count (gt_array)
{
	var gt_count = 0;
	
	if (gf_isArray(gt_array))
	{
		for (var gt_key in gt_array)
		{
			gt_count++;
		}
	}
	
	return gt_count;
}

/*
 * splits a text by all common new-line indicators (like <br>, \n) and returns the resulting array
 */
function gf_splitText (gt_text)
{
	return gt_text.split(/<br>|<br \/>|<br\/>|\r\n|\r|\n/gi);
}

/*
 * checks if an object is an array
 */
function gf_isArray (gt_object)
{
    return Object.prototype.toString.call(gt_object) === '[object Array]';
}

/*
 * checks if an HTML element with the given ID exists
 */
function gf_elementExists ()
{
	var gt_argv = arguments;
	var gt_argc = gt_argv.length;

	for (var gt_i = 0; gt_i < gt_argc; gt_i++)
	{
		if (document.getElementById(gt_argv[gt_i]) === null)
		{
			return false
		}
	}
	
	return true;
}

/*
 * retrieves the x and y coordinates of a mouse click
 */
function gf_getClickPosition (gt_event, gt_canvasElem, gt_clickPosition)
{
	var gt_canvasLeft	= gt_canvasElem.offsetLeft;
	var gt_canvasTop	= gt_canvasElem.offsetTop;
	
	var gt_canvasBorderLeft	= gf_isset(gt_canvasElem.style.borderLeftWidth) ? parseInt(gt_canvasElem.style.borderLeftWidth) : 0;
	var gt_canvasBorderTop	= gf_isset(gt_canvasElem.style.borderTopWidth)  ? parseInt(gt_canvasElem.style.borderTopWidth)  : 0;
	
	var gt_windowOffsetX	= gf_isset(window.pageXOffset) ? window.pageXOffset : 0;
	var gt_windowOffsetY	= gf_isset(window.pageYOffset) ? window.pageYOffset : 0;
	
	var gt_mousePositionX	= gt_event.clientX;
	var gt_mousePositionY	= gt_event.clientY;
	
	gt_clickPosition.x = gt_mousePositionX + gt_windowOffsetX - gt_canvasLeft - gt_canvasBorderLeft;
	gt_clickPosition.y = gt_mousePositionY + gt_windowOffsetY - gt_canvasTop  - gt_canvasBorderTop;
}

/*
 * merges style arrays (like PHP's array_merge)
 */
function gf_mergeStyles ()
{
	var gt_args		= arguments;
	var gt_count	= gt_args.length;
	var gt_style	= {};
	
	if (gt_count > 1)
	{
		for (var gt_i in gt_args)
		{
			for (var gt_s in gt_args[gt_i])
			{
				gt_style[gt_s] = gt_args[gt_i][gt_s];
			}
		}
	}
	
	return gt_style;
}

/*
 * reads a value from a given style array
 */
function gf_getStyleValue (gt_style, gt_key, gt_type)
{
	if (!gf_isset(gt_type))
		gt_type = "";
	
	if (gf_isset(gt_style, gt_key))
	{
		return gf_isset(gt_style[gt_key]) ? gt_style[gt_key] : gv_defaultStyle[gt_key];		
	}
	
	if (gt_type == "bool")
		return false;
	
	if (gt_type == "int")
		return 0;
	
	return "";
}

/*
 * checks whether a given object / array of objects has a certain attribute
 */
function gf_objectHasAttribute (gt_object, gt_attribute)
{
	if (!gf_isset(gt_attribute, gt_object))
		return false;
	
	if (!gf_isArray(gt_attribute))
		gt_attribute = [gt_attribute];
	
	if (!gf_isArray(gt_object))
		gt_object = [gt_object];
	
	for (var gt_o in gt_object)
	{
		if (!gf_isset(gt_object[gt_o]))
			return false;
		
		for (var gt_i in gt_attribute)
		{
			if (!gf_isset(gt_object[gt_o][gt_attribute[gt_i]]))
				return false;
		}
	}
	
	return true;
}

/*
 * DRAWING FUNCTIONS
 */

/*
 * draws a label at a given position with the given text and style
 */
function gf_drawLabel (gt_ctx, gt_posx, gt_posy, gt_text, gt_style, gt_selected)
{
	if (!gf_isset(gt_style))
		gt_style = gv_defaultStyle;
	
	if (!gf_isset(gt_selected) || gt_selected != true)
		gt_selected = false;
	
	if (gf_isset(gt_ctx, gt_posx, gt_posy, gt_text))
	{
		// read relevant style information
		var gt_font			= gf_getStyleValue(gt_style, "font");
		var gt_fontSize		= gf_getStyleValue(gt_style, "fontSize");
		var gt_liSymbol		= gf_getStyleValue(gt_style, "liSymbol");
		var gt_textAlign	= gf_getStyleValue(gt_style, "textAlign");
		var gt_textVAlign	= gf_getStyleValue(gt_style, "textVAlign");
		
		var gt_paddingTop		= gf_getStyleValue(gt_style, "paddingTop");
		var gt_paddingLeft		= gf_getStyleValue(gt_style, "paddingLeft");
		var gt_paddingRight		= gf_getStyleValue(gt_style, "paddingRight");
		var gt_paddingBottom	= gf_getStyleValue(gt_style, "paddingBottom");
		var gt_lineSpacing		= gf_getStyleValue(gt_style, "lineSpacing");
				
		var gt_bgColor		= gf_getStyleValue(gt_style, "bgColor");
		var gt_fgColor		= gf_getStyleValue(gt_style, "fgColor");
		var gt_borderColor	= gt_selected ? gf_getStyleValue(gt_style, "borderColorSelected") : gf_getStyleValue(gt_style, "borderColor");
		var gt_borderWidth	= gf_getStyleValue(gt_style, "borderWidth");
		
		var gt_fixedWidth	= gf_getStyleValue(gt_style, "width");
		var gt_fixedHeight	= gf_getStyleValue(gt_style, "height");
		var gt_minWidth		= gf_getStyleValue(gt_style, "minWidth");
		var gt_minHeight	= gf_getStyleValue(gt_style, "minHeight");
		
		// add the list-symbol to list entries
		var gt_textArray	= gf_isArray(gt_text) ? gt_text : gf_splitText(gt_text.replace(/<li>/g, gt_liSymbol));
		
		var gt_width		= 0;
		var gt_height		= 0;
		var gt_tmpWidth		= 0;
		var gt_tmpText		= "";
		var gt_textHeight	= 0;
		
		var gt_top		= 0;
		var gt_left		= 0;
		var gt_textx	= 0;
		var gt_texty	= 0;
		
		gt_ctx.font			= gt_fontSize + "px " + gt_font;
		gt_ctx.textBaseline = "top";
		
		// calculate maxWidth
		for (var gt_t in gt_textArray)
		{
			gt_tmpText	= gt_textArray[gt_t];
			gt_tmpWidth	= gt_ctx.measureText(gt_tmpText).width;
		
			if (gt_fixedWidth > 0 && gt_tmpWidth > gt_fixedWidth)
				gt_textArray[gt_t] = null;
			
			if (gt_tmpWidth > gt_width)
				gt_width = gt_tmpWidth;
		}
		
		gt_textHeight	= (gt_fontSize * gt_textArray.length) + (gt_lineSpacing * (gt_textArray.length - 1));
		gt_width		= gt_width + gt_paddingLeft + gt_paddingRight;
		gt_height		= gt_textHeight + gt_paddingBottom + gt_paddingTop;

		if (gt_fixedHeight > 0 && gt_height > gt_fixedHeight)
			gt_textVAlign = "top";
		
		if (gt_minWidth > gt_width)
			gt_width = gt_minWidth;
		
		if (gt_minHeight > gt_height)
			gt_height = gt_minHeight;
		
		if (gt_fixedWidth > 0)
			gt_width = gt_fixedWidth;
		
		if (gt_fixedHeight > 0)
			gt_height = gt_fixedHeight;
		
		gt_top	= gt_posy - Math.round(gt_height / 2);
		gt_left	= gt_posx - Math.round(gt_width / 2);
		
		gt_ctx.beginPath();
		gt_ctx.rect(gt_left, gt_top, gt_width, gt_height)
		gt_ctx.closePath();
		
		if (gt_borderColor !== false && gt_borderWidth > 0)
		{
			gt_ctx.lineWidth	= gt_borderWidth;
			gt_ctx.strokeStyle	= gt_borderColor;
			gt_ctx.stroke();
		}
		
		if (gt_bgColor !== false)
		{
			gt_ctx.fillStyle	= gt_bgColor;
			gt_ctx.fill();
		}
		
		if (gt_fgColor !== false)
		{
			gt_ctx.fillStyle	= gt_fgColor;
			gt_ctx.textAlign	= gt_textAlign;
			
			if (gt_textVAlign == "bottom")
			{
				gt_texty = gt_top + gt_height - gt_paddingBottom - gt_textHeight;
			}
			else if (gt_textVAlign == "middle")
			{
				gt_texty = gt_posy - gt_textHeight / 2;
			}
			else
			{
				gt_texty = gt_top + gt_paddingTop;	// default is top
			}
			
			if (gt_textAlign == "right")
			{
				gt_textx = gt_left + gt_width - gt_paddingRight;
			}
			else if (gt_textAlign == "left")
			{
				gt_textx = gt_left + gt_paddingLeft;
			}
			else
			{
				gt_textx = gt_posx;		// default is center
			}
			
			// draw all lines of text
			for (var gt_t in gt_textArray)
			{
				if (gt_fixedHeight > 0 && (gt_texty + gt_fontSize - gt_top) > gt_fixedHeight)
					break;
				
				gt_tmpText = gt_textArray[gt_t];
				gt_ctx.fillText(gt_tmpText, gt_textx, gt_texty);
				gt_texty += gt_fontSize + gt_lineSpacing;
			}
		}
		
		return {width: gt_width, height: gt_height, stroke: gt_borderWidth, left: gt_left, top: gt_top};
	}
	
	return {width: 0, height: 0, stroke: 0, left: 0, top: 0};
}

/*
 * draws an I shaped arrow (one single line)
 */
function gf_drawArrowI (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_endx, gt_endy, gt_color, gt_width)
{
	gt_ctx.beginPath();
	gt_ctx.moveTo(gt_startx, gt_starty);
	gt_ctx.lineTo(gt_endx, gt_endy);
	gt_ctx.closePath();
	
	gt_ctx.lineWidth	= gt_width;
	gt_ctx.strokeStyle	= gt_color;
	gt_ctx.stroke();
	
	var gt_l	= gt_startx < gt_endx ? gt_startx - gt_width/2 - 2 : gt_endx - gt_width/2 - 2;
	var gt_t	= gt_starty < gt_endy ? gt_starty - gt_width/2 - 2 : gt_endy - gt_width/2 - 2;
	var gt_r	= gt_startx > gt_endx ? gt_startx + gt_width/2 + 2 : gt_endx + gt_width/2 + 2;
	var gt_b	= gt_starty > gt_endy ? gt_starty + gt_width/2 + 2 : gt_endy + gt_width/2 + 2;
	
	// if the arrow is drawn on the behavioral view canvas: add click and store line (for intersection checks)
	if (gt_graph == "bv")
	{
		gf_bv_storeClick(gt_id, "e", gt_l, gt_t, gt_r, gt_b);
		gf_bv_storeLine(gt_startx, gt_starty, gt_endx, gt_endy);
	}
	
	return {x: (gt_startx + gt_endx)/2, y: (gt_starty + gt_endy)/2};	// label center
}

/*
 * draws an L shaped arrow
 */
function gf_drawArrowL (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine)
{
	var gt_x2	= gt_firstLine == "v" ? gt_startx : gt_endx;
	var gt_y2	= gt_firstLine == "h" ? gt_starty : gt_endy;
	
	// an L shaped arrow consists of two I shaped arrows
	gf_drawArrowI (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_x2, gt_y2, gt_color, gt_width);
	gf_drawArrowI (gt_ctx, gt_graph, gt_id, gt_x2, gt_y2, gt_endx, gt_endy, gt_color, gt_width);
	
	return {x: gt_x2, y: gt_y2};	// label center
}

/*
 * draws a Z shaped arrow
 */
function gf_drawArrowZ (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine)
{
	var gt_x2	= (gt_startx + gt_endx) / 2;
	var gt_y2	= (gt_starty + gt_endy) / 2;
	
	gf_drawArrowL (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_x2, gt_y2, gt_color, gt_width, gt_firstLine);
	gf_drawArrowL (gt_ctx, gt_graph, gt_id, gt_x2, gt_y2, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine == "h" ? "v" : "h");
	
	return {x: gt_x2, y: gt_y2};	// label center
}

/*
 * draws an U shaped arrow
 */
function gf_drawArrowU (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine, gt_space)
{
	var gt_x1 = gt_space < 0 ? Math.min(gt_startx, gt_endx) : Math.max(gt_startx, gt_endx);
	var gt_y1 = gt_space < 0 ? Math.min(gt_starty, gt_endy) : Math.max(gt_starty, gt_endy);
	
	var gt_x2 = gt_firstLine == "h" ? gt_x1 + gt_space : (gt_startx + gt_endx) / 2;
	var gt_y2 = gt_firstLine == "v" ? gt_y1 + gt_space : (gt_starty + gt_endy) / 2;
	
	gf_drawArrowL (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_x2, gt_y2, gt_color, gt_width, gt_firstLine);
	gf_drawArrowL (gt_ctx, gt_graph, gt_id, gt_x2, gt_y2, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine == "h" ? "v" : "h");
	
	return {x: gt_x2, y: gt_y2};	// label center
}

/*
 * draws a G shaped arrow
 */
function gf_drawArrowG (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine, gt_space1, gt_space2)
{	
	
	// firstLine: v => space1: u | d ; space2: l | r
	// firstLine: h => space1: l | r ; space2: u | d
	
	var gt_x2 = 0;
	var gt_y2 = 0;
	
	if (gt_firstLine == "v")
	{
		if (gt_startx < gt_endx && gt_starty < gt_endy && gt_space1 > 0 && gt_space2 < 0) gt_space2 *= -1;
		if (gt_startx > gt_endx && gt_starty > gt_endy && gt_space1 < 0 && gt_space2 > 0) gt_space2 *= -1;
		if (gt_startx > gt_endx && gt_starty < gt_endy && gt_space1 > 0 && gt_space2 > 0) gt_space2 *= -1;
		if (gt_startx < gt_endx && gt_starty > gt_endy && gt_space1 < 0 && gt_space2 < 0) gt_space2 *= -1;
		
		gt_x2 = gt_space2 < 0 ? Math.min(gt_startx, gt_endx) + gt_space2 : Math.max(gt_startx, gt_endx) + gt_space2;
		gt_y2 = gt_space1 < 0 ? Math.min(gt_starty, gt_endy) + gt_space1 : Math.max(gt_starty, gt_endy) + gt_space1;
	}
	else
	{
		if (gt_startx < gt_endx && gt_starty < gt_endy && gt_space1 > 0 && gt_space2 < 0) gt_space2 *= -1;
		if (gt_startx > gt_endx && gt_starty > gt_endy && gt_space1 < 0 && gt_space2 > 0) gt_space2 *= -1;
		if (gt_startx > gt_endx && gt_starty < gt_endy && gt_space1 < 0 && gt_space2 < 0) gt_space2 *= -1;
		if (gt_startx < gt_endx && gt_starty > gt_endy && gt_space1 > 0 && gt_space2 > 0) gt_space2 *= -1;
		
		gt_x2 = gt_space1 < 0 ? Math.min(gt_startx, gt_endx) + gt_space1 : Math.max(gt_startx, gt_endx) + gt_space1;
		gt_y2 = gt_space2 < 0 ? Math.min(gt_starty, gt_endy) + gt_space2 : Math.max(gt_starty, gt_endy) + gt_space2;
	}
	
	gf_drawArrowL (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_x2, gt_y2, gt_color, gt_width, gt_firstLine);
	gf_drawArrowL (gt_ctx, gt_graph, gt_id, gt_x2, gt_y2, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine);
	
	return {x: gt_x2, y: gt_y2};	// label center	
}

/*
 * draws a C shaped arrow
 */
function gf_drawArrowC (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine, gt_space1, gt_space2)
{
	var gt_x2 = gt_firstLine == "v" ? gt_startx + gt_space2 : (gt_startx + gt_endx) / 2;
	var gt_y2 = gt_firstLine == "h" ? gt_starty + gt_space2 : (gt_starty + gt_endy) / 2;
	
	gt_space1	= gt_firstLine == "v" && gt_starty < gt_endy ? 0 - gt_space1 : gt_space1;
	gt_space1	= gt_firstLine == "h" && gt_startx < gt_endx ? 0 - gt_space1 : gt_space1;
	
	gf_drawArrowU (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_x2, gt_y2, gt_color, gt_width, gt_firstLine, gt_space1);
	gf_drawArrowU (gt_ctx, gt_graph, gt_id, gt_x2, gt_y2, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine, 0-gt_space1);
	
	return {x: gt_x2, y: gt_y2};	// label center
}

/*
 * draws an S shaped arrow
 */
function gf_drawArrowS (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine, gt_space)
{
	var gt_x2 = (gt_startx + gt_endx) / 2;
	var gt_y2 = (gt_starty + gt_endy) / 2;
	
	gf_drawArrowU (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_x2, gt_y2, gt_color, gt_width, gt_firstLine, gt_space);
	gf_drawArrowU (gt_ctx, gt_graph, gt_id, gt_x2, gt_y2, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine, 0-gt_space);
	
	return {x: gt_x2, y: gt_y2};	// label center
}

/*
 * draws an U shaped arrow connected with another I shaped arrow
 */
function gf_drawArrowUI (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine, gt_space1, gt_space2)
{
	
	var gt_x2 = gt_endx;
	var gt_y2 = gt_endy;
	
	var gt_labelCenter = {x: 0, y: 0};
	
	if (gt_firstLine == "v")
	{
		if (gt_startx > gt_endx)
			gt_x2 += Math.abs(gt_space2);
		else
			gt_x2 -= Math.abs(gt_space2);
	}
	else
	{
		if (gt_starty > gt_endy)
			gt_y2 += Math.abs(gt_space2);
		else
			gt_y2 -= Math.abs(gt_space2);	
	}

	gt_labelCenter	= gf_drawArrowU (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_x2, gt_y2, gt_color, gt_width, gt_firstLine, gt_space1);
					  gf_drawArrowI (gt_ctx, gt_graph, gt_id, gt_x2, gt_y2, gt_endx, gt_endy, gt_color, gt_width);
	
	return {x: gt_labelCenter.x, y: gt_labelCenter.y};	// label center
}

/*
 * draws a Z shaped arrow connected with a U
 */
function gf_drawArrowZU (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine, gt_space1, gt_space2)
{
	var gt_x2 = gt_firstLine == "h" ? (gt_startx + gt_endx) / 2 + gt_space1 : gt_startx;
	var gt_y2 = gt_firstLine == "v" ? (gt_starty + gt_endy) / 2 + gt_space1 : gt_starty;

	var gt_firstZ = false;
	
	if (gt_firstLine == "v")
	{		
		gt_x2 = gt_space2 > 0 ? Math.max(gt_startx, gt_endx) + gt_space2 : Math.min(gt_startx, gt_endx) + gt_space2;
		
		gt_firstZ = (gt_starty < gt_endy && gt_space1 > 0) || (gt_starty > gt_endy && gt_space1 < 0);
	}
	else
	{
		gt_y2 = gt_space2 > 0 ? Math.max(gt_starty, gt_endy) + gt_space2 : Math.min(gt_starty, gt_endy) + gt_space2;
		
		gt_firstZ = (gt_startx < gt_endx && gt_space1 > 0) || (gt_startx > gt_endx && gt_space1 < 0);
	}
	
	if (gt_firstZ)
	{
		gf_drawArrowZ (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_x2, gt_y2, gt_color, gt_width, gt_firstLine);
		gf_drawArrowU (gt_ctx, gt_graph, gt_id, gt_x2, gt_y2, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine, gt_space1);	
	}
	else
	{
		gf_drawArrowU (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_x2, gt_y2, gt_color, gt_width, gt_firstLine, gt_space1);
		gf_drawArrowZ (gt_ctx, gt_graph, gt_id, gt_x2, gt_y2, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine);	
	}
	
	return {x: gt_x2, y: gt_y2};	// label center
}

/*
 * draws an S shaped arrow connected with another I shaped arrow
 */
function gf_drawArrowSI (gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_endx, gt_endy, gt_color, gt_width, gt_firstLine, gt_space1, gt_space2)
{
	var gt_x2 = gt_firstLine == "h" ? gt_endx : (gt_startx < gt_endx ? gt_endx + Math.abs(gt_space1) : gt_endx - Math.abs(gt_space1));
	var gt_y2 = gt_firstLine == "v" ? gt_endy : (gt_starty < gt_endy ? gt_endy + Math.abs(gt_space1) : gt_endy - Math.abs(gt_space1));
	
	var gt_labelCenter	= {x: 0, y: 0};
	
	gt_labelCenter	= gf_drawArrowS(gt_ctx, gt_graph, gt_id, gt_startx, gt_starty, gt_x2, gt_y2, gt_color, gt_width, gt_firstLine, gt_space2);
					  gf_drawArrowI(gt_ctx, gt_graph, gt_id, gt_x2, gt_y2, gt_endx, gt_endy, gt_color, gt_width);

	return {x: gt_labelCenter.x, y: gt_labelCenter.y};	// label center
}

/*
 * draws the head of an arrow
 */
function gf_drawArrowHead (gt_ctx, gt_object, gt_position, gt_color, gt_correction)
{
	if (gf_isset(gt_object, gt_ctx))
	{
		
		if (!gf_isset(gt_position))
			gt_position = "b";
		
		if (!gf_isset(gt_color))
			gt_color = "#000000";
		
		var gt_posx			= 0;
		var gt_posy			= 0;
		
		// gt_position = (t)op | (b)ottom | (l)eft | (r)ight
		if (gt_position == "l")
		{
			gt_posx			= gt_object.l;
			gt_posy			= gt_object.y;
		}
		else if (gt_position == "r")
		{
			gt_posx			= gt_object.r;
			gt_posy			= gt_object.y;
		}
		else if (gt_position == "b")
		{
			gt_posx			= gt_object.x;
			gt_posy			= gt_object.b;
		}
		else
		{
			gt_posx			= gt_object.x;
			gt_posy			= gt_object.t;
		}
		
		if (gf_isset(gt_correction))
		{
			if (gt_position == "t" || gt_position == "b")
			{
				gt_posx += gt_correction;
			}
			
			if (gt_position == "l" || gt_position == "r")
			{
				gt_posy += gt_correction;
			}
		}
		
		var gt_rotate = 0;
			gt_rotate = gt_position == "l" ?  Math.PI / 2	: gt_rotate;
			gt_rotate = gt_position == "r" ? -Math.PI / 2	: gt_rotate;
			gt_rotate = gt_position == "t" ?  Math.PI		: gt_rotate;
		
		var gt_width	= gf_isset(gv_arrowHead) ? gv_arrowHead.width  : 11;
		var gt_length	= gf_isset(gv_arrowHead) ? gv_arrowHead.length : 14;
			
		gt_ctx.save();
		gt_ctx.fillStyle = gt_color;
		gt_ctx.translate(gt_posx, gt_posy);
		gt_ctx.rotate(gt_rotate);
		
		gt_ctx.beginPath();
		gt_ctx.moveTo(0, 0);
		gt_ctx.lineTo(gt_width / 2, gt_length);
		gt_ctx.lineTo(0, gt_length * 0.8)
		gt_ctx.lineTo(-gt_width / 2, gt_length)
		gt_ctx.lineTo(0, 0);
		gt_ctx.closePath();
		
		gt_ctx.fill();
		gt_ctx.restore();
	}
}