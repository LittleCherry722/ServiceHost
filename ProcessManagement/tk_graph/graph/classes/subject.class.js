/**
 * S-BPM Groupware v1.0
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
 * The representation of a subject in the communication view.
 * 
 * @private
 * @class represents a Subject in the communication view
 * @param {String} id The id of the subject.
 * @param {String} text The label of the subject.
 * @param {String} type The type of the subject. Possible values: "sungle", "multi", "external" (default: "single")
 * @param {int} inputPool The size of the input pool (-1 for infinite)
 * @returns {void}
 */
function GCsubject (id, text, type, inputPool)
{
	// TODO: add function to set process to be loaded on dblClick on external subject
	
	// if no text is given set it to ""
	if (!gf_isset(text))
		text = "";
		
	// if no type is given set it to "single"
	if (!gf_isset(type))
		type = "single";		// single, multi, external
		
	// set a default for the size of the input-pool
	if (!gf_isset(inputPool))
		inputPool = -1;
	
	/**
	 * The internal behavior of this subject.
	 * 
	 * @type GCbehavior
	 */
	this.behavior	= new GCbehavior(id);
	
	/**
	 * A flag to indicate whether or not the subject is deactivated.
	 * Deactivated subjects are displayed in a different way.
	 * 
	 * @type boolean
	 */
	this.deactivated	= false;
	
	/**
	 * The id of the subject.
	 * 
	 * @type String
	 */
	this.id			= id;
	
	/**
	 * The size of the subject's input pool
	 * 
	 * @type int
	 */
	this.inputPool	= -1;
	
	/**
	 * The label of the subject.
	 * 
	 * @type String
	 */
	this.text		= text;
	
	/**
	 * The type of the subject.
	 * Possible values "single", "multi", "external"
	 * 
	 * @type String
	 */
	this.type		= type;
	
	/**
	 * Activates a subject.
	 * 
	 * @returns {void}
	 */
	this.activate = function ()
	{
		this.deactivated = false;
	};
	
	/**
	 * Deactivates a subject.
	 * 
	 * @returns {void}
	 */
	this.deactivate = function ()
	{
		this.deactivated = true;
	};
	
	/**
	 * Returns the behavior of the subject.
	 * 
	 * @returns {GCbehavior} The subject's GCbehavior instance.
	 */
	this.getBehavior = function ()
	{
		return this.behavior;
	};
	
	/**
	 * Returns the id of the subject.
	 * 
	 * @returns {String} The id of the subject.
	 */
	this.getId = function ()
	{
		return this.id;
	};
	
	/**
	 * Returns the size of the subject's input-pool.
	 * 
	 * @returns {int} The size of the subject's input-pool.
	 */
	this.getInputPool = function ()
	{
		return this.inputPool;
	};
	
	/**
	 * Returns the label of the subject.
	 * 
	 * @returns {String} The label of the subject.
	 */
	this.getText = function ()
	{
		return this.text;
	};
	
	/**
	 * Returns the type of the subject.
	 * 
	 * @returns {String} The type of the subject.
	 */
	this.getType = function ()
	{
		return this.type.toLowerCase();
	};
	
	/**
	 * Returns the deactivate status of this subject.
	 * 
	 * @returns {boolean} True when the subject is deactivated.
	 */
	this.isDeactivated = function ()
	{
		return this.deactivated === true;
	};
	
	/**
	 * Updates the id of this subject with the given id.
	 * 
	 * @param {String} id The id of the subject.
	 * @returns {void}
	 */
	this.setId = function (id)
	{
		if (gf_isset(id))
		{
			this.id = id;
		}
	};
	
	/**
	 * Updates the size of the subject's input-pool.
	 * 
	 * @param {int} inputPool The size of the subject's input-pool.
	 * @returns {void}
	 */
	this.setInputPool = function (inputPool)
	{
		if (gf_isset(inputPool))
		{
			var gt_val	= parseInt(inputPool);
			
			this.inputPool = isNaN(gt_val) || gt_val < 0 ? -1 : gt_val;
		}
	};
	
	/**
	 * Updates the label of this node with the given text.
	 * 
	 * @param {String} text The label of the subject.
	 * @returns {void}
	 */
	this.setText = function (text)
	{
		if (gf_isset(text))
		{
			this.text = text;
		}
	};
	
	/**
	 * Updates the type of this subject.
	 * 
	 * @param {String} type The type of the subject. Possile values are "single", "multi", "external". (default: "single")
	 * @returns {void}
	 */
	this.setType = function (type)
	{
		if (gf_isset(type))
		{
			type = type.toLowerCase();
			if (type == "single" || type == "multi" || type == "external")
			{
				this.type = type;
			}
		}
	};
	
	/**
	 * Returns the label and the id of this subject as one string.
	 * The resulting string is of the form:
	 * <br />
	 * <br />
	 * <i>
	 * 		label
	 * 		(subjectId)
	 * </i>
	 * 
	 * @returns {String} The label of the subject and its id.
	 */
	this.textToString = function ()
	{
		var gt_inputPool	= this.getInputPool() >= 0 ? this.getInputPool() : "\u221e";
		return this.text + "\n(" + this.id + ")\n \n[InputPool: " + gt_inputPool + "]";
	};
	
	// set the type
	this.setType(type);
	
	// set the input-pool size
	this.setInputPool(inputPool);
}