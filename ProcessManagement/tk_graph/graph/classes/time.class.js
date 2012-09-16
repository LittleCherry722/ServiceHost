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
 * An instance of this class holds a certain timestamp and is used to either transform the timestamp into a string or a given string into a timestamp.
 * 
 * @returns {void}
 */
function GCtime ()
{
	/**
	 * Base definition for the time units.
	 * 
	 * @type {Object}
	 */
	this.def	= gv_timeDefinition;
	
	/**
	 * The timestamp of the instance.
	 * 
	 * @type {int}
	 */
	this.timestamp	= 0;
	
	/**
	 * Used to calculate the time in seconds for each time unit.
	 * 
	 * @returns {void}
	 */
	this.calcTimes	= function ()
	{
		var gt_current	= "minute";
		
		while (gt_current != "")
		{
			this.def[gt_current]["time"]	= this.def[gt_current]["num"] * this.def[this.def[gt_current]["parts"]]["time"];
			gt_current						= this.def[gt_current]["n"];
		}
	};
	
	/**
	 * Returns a sample for a correct input string.
	 * 
	 * @returns {String} A sample input string.
	 */
	this.getExample	= function ()
	{
		var gt_exampleTimestamp	= 820800; 			// 1w 2d 12h
		
		return this.timestampToString(gt_exampleTimestamp, "unit");
	};
	
	/**
	 * TODO
	 */
	this.getExplanation	= function ()
	{
		// TODO
	};
	
	/**
	 * Returns the stored time as either a timestamp (timestamp) or a string (abbr, unit, full).
	 * By setting type to "example" an example timeString will be returned to demonstrate the use of the pattern.
	 * 
	 * @see GCtime::getTimestamp(), GCtime::getTimestring(), GCtime::getExample()
	 * 
	 * @param {String} type The type of the return (timestamp, abbr, unit, full, example)
	 * @returns {String|int} Returns the stored time as either a timestamp (int) or a String.
	 */
	this.getTime = function (type)
	{
		if (!gf_isset(type))
			type = "timestamp";
			
		type = type.toLowerCase();
		
		if (type == "timestamp")
		{
			return this.getTimestamp();
		}
		else if (type == "example")
		{
			return this.getExample();
		}
		else
		{
			return this.getTimeString(type);
		}
	};
	
	/**
	 * Returns the stored timestamp.
	 * 
	 * @returns {int} The stored timestamp.
	 * 
	 */
	this.getTimestamp	= function ()
	{
		return this.timestamp;
	};

	/**
	 * Returns the stored timestamp as a string.
	 * 
	 * @param {String} type The type refers to the format the timestamp will be formatted in. Possible values: unit (default setting, e.g. 1w 3d), abbr (e.g. 1 wk 3 d 2hr), full (e.g. 1 week 3 days 2 hours)
	 * @returns {String} The stored timestamp as a string in the chosen format like "1w 3d" or "1 week 3 days".
	 */
	this.getTimeString	= function (type)
	{
		return this.timestampToString(this.timestamp, type);
	};
	
	/**
	 * Initialize the object.
	 * 
	 * @returns {void}
	 */
	this.init			= function ()
	{
		this.calcTimes();
	};
	
	/**
	 * Sets the object's timestamp to the given value.
	 * When time is a string it will be converted to a timestamp.
	 * 
	 * @see GCtime::setTimeString()
	 * 
	 * @param {String|int} time Either a timestamp or a proper timestring that will be converted.
	 * @returns {void}
	 */
	this.setTime		= function (time)
	{
		if (gf_isset(time))
		{
			if (parseInt(time) == time)
			{
				this.setTimestamp(time);
			}
			else
			{
				this.setTimeString(time);
			}
		}
	}

	/**
	 * Sets a new timestamp.
	 * 
	 * @param {int} timestamp The new timestamp.
	 * @returns {void}
	 */
	this.setTimestamp	= function (timestamp)
	{
		if (gf_isset(timestamp))
		{
			this.timestamp	= timestamp;
		}
	};
	
	/**
	 * Converts the given timeString and sets the resulting timestamp as the new object's timestamp.
	 * 
	 * @param {String} timeString A proper string representation of the time.
	 * @returns {void}
	 */
	this.setTimeString	= function (timeString)
	{
		this.timestamp	= this.timeStringToStamp(timeString);
	};
	
	/**
	 * Sorting methods.
	 * This sorts the time units by unit-length (descending).
	 * 
	 * @param {Object} obj1 First object to compare.
	 * @param {Object} obj2 Second object to compare.
	 * @returns {int} 1 (obj1 is shorter than obj2), -1 (obj1 is longer than obj2), 0 (otherwise)
	 */
	this.sortUnits = function (obj1, obj2)
	{
		if (obj1.length < obj2.length)
			return 1;
		if (obj1.length > obj2.length)
			return -1;
		return 0;	
	};
	
	/**
	 * Converts a given timestamp into a timeString.
	 * 
	 * @param {int} timestamp The timestamp to convert.
	 * @param {String} type The format of the output (possible values: unit, abbr, full)
	 * @returns {String} The timestamp as a string with the chosen format.
	 */
	this.timestampToString	= function (timestamp, type)
	{
		// type = unit | abbr | full (plural s)
		
		if (!gf_isset(timestamp))
			timestamp = 0;
		
		if (!gf_isset(type) || (type.toLowerCase() != "full" && type.toLowerCase() != "abbr" && type.toLowerCase() != "unit"))
			type = "unit";
		
		type	= type.toLowerCase();
		
		var gt_current	= "year";
		var gt_string	= "";
		var gt_value	= 0;
		var gt_pluralS	= "";
		var gt_curDef	= null;
		var gt_space	= type == "full" || type == "abbr" ? " " : "";
		var gt_showAll	= false;
		
		while (gt_current != "")
		{
			gt_curDef	= this.def[gt_current];
			
			if (timestamp >= gt_curDef["time"] || (gt_string != "" && gt_showAll) || (gt_current == "second" && (gt_string == "" || gt_showAll)))
			{
				gt_value	= Math.floor(timestamp / gt_curDef["time"]);
				gt_pluralS	= gt_value != 1 && type == "full" ? "s" : "";
				
				gt_string += " " + gt_value + gt_space + gt_curDef[type] + gt_pluralS;
				
				timestamp -= gt_value * gt_curDef["time"];
			}
			gt_current	= gt_curDef["p"];
		}
		
		return gt_string.length > 0 ? gt_string.substring(1) : gt_string;
	};
	
	/**
	 * Converts a proper timeString into a timestamp.
	 * A proper timeString may only contain numbers directly followed by one of the units stored in GCtime::def (e.g. 1w 2d 12h).
	 * 
	 * @param {String} timeString A proper timeString.
	 * @returns {int} The timestamp that represents the given timeString.
	 */
	this.timeStringToStamp	= function (timeString)
	{
		var gt_value	= 0;
		if (gf_isset(timeString))
		{
			timeString = timeString.replace(/ |,|\.|:/gi, "");
			
			// collect units
			var gt_pattern	= "";
			var gt_unitsS	= new Array();
			var gt_times	= {};
			for (var gt_curDef in this.def)
			{
				gt_unitsS[gt_unitsS.length]				= this.def[gt_curDef]["unit"];
				gt_times[this.def[gt_curDef]["unit"]]	= this.def[gt_curDef]["time"];
			}
			
			// sort the units to have the longest units in first positions (to avoid problems with match)
			gt_unitsS.sort(this.sortUnits);
			
			// create a regex of the units (any number of digits directly followed by a unit)
			for (var gt_curUnit in gt_unitsS)
			{
				gt_pattern += "|\\d+" + gt_unitsS[gt_curUnit];
			}
			
			gt_pattern	= gt_pattern.substring(1);
			
			var gt_pos	= -1;
			var gt_rex1	= new RegExp(gt_pattern, "gi");
			var gt_rex2	= new RegExp("[^0-9]", "gi");

			var gt_unit		= "";

			// match the timeString against the regex
			var gt_matches	= timeString.match(gt_rex1);
			
			var gt_match	= "";
			
			// calculate the time for each part of the timeString
			for (var gt_matchID in gt_matches)
			{
				gt_match	= gt_matches[gt_matchID];
				
				gt_pos	= gt_match.search(gt_rex2);
				
				if (gt_pos >= 0)
				{
					gt_unit	= gt_match.substring(gt_pos);
					
					if (gf_isset(gt_times[gt_unit]))
					{
						gt_value += gt_times[gt_unit] * parseInt(gt_match.substring(0, gt_pos));
					}
				}
			}
		}
		
		return gt_value;
	};
	
	// initialize
	this.init();
}
