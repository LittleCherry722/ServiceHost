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
 * The representation of a subject in the communication view.
 * 
 * @private
 * @class represents a Subject in the communication view
 * @param {String} id The id of the subject.
 * @param {String} text The label of the subject.
 * @param {String} type The type of the subject. Possible values: "sungle", "multi", "external", "multiexternal" (default: "single")
 * @param {int} inputPool The size of the input pool (-1 for infinite)
 * @returns {void}
 */
function GCsubject (id, text, type, inputPool)
{	
	// if no text is given set it to ""
	if (!gf_isset(text))
		text = "";
		
	// if no type is given set it to "single"
	if (!gf_isset(type))
		type = "single";		// single, multi, external, multiexternal
		
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
	 * Comment for this subject.
	 * 
	 * @type String
	 */
	this.comment	= "";
	
	/**
	 * A flag to indicate whether or not the subject is deactivated.
	 * Deactivated subjects are displayed in a different way.
	 * 
	 * @type boolean
	 */
	this.deactivated	= false;
	
	/**
	 * The type of an external subject.
	 * Either "external", "interface" or "instantinterface".
	 * 
	 * @type String
	 */
	this.externalType	= "external";
	
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
	 * TODO
	 * 
	 * @type String
	 */
	this.relatedInterface	= null;
	
	/**
	 * For external subjects: the referenced process.
	 * 
	 * @type String
	 */
	this.relatedProcess		= null;
	
	/**
	 * For external subjects: the referenced subject.
	 * 
	 * @type String
	 */
	this.relatedSubject		= null;
	
	/**
	 * The ID of the role that is assigned to this subject.
	 * 
	 * @type String
	 */
	this.role		= "";
	
	/**
	 * Flag if the subject can be the start subject of a process instance.
	 * 
	 * @type boolean
	 */
	this.startSubject	= false;
	
	/**
	 * The label of the subject.
	 * 
	 * @type String
	 */
	this.text		= text;
	
	/**
	 * The type of the subject.
	 * Possible values "single", "multi", "external", "multiexternal"
	 * 
	 * @type String
	 */
	this.type		= type;

    /**
     * The user-defined manual offset for the subject position
     *
     * @type {?{dx: int, dy: int}}
     */
    this.manualPositionOffset = null;
	
	/**
	 * TODO
	 * 
	 * @type String
	 */
	this.url		= null;
	
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
	 * Returns the subject's comment.
	 * 
	 * @returns {String} The subject's comment.
	 */
	this.getComment	= function ()
	{
		return this.comment;
	};
	
	/**
	 * Returns the type of an external subject.
	 * 
	 * @returns {String} The type of an external subject. Possible values are "external", "interface", "instantinterface"
	 */
	this.getExternalType = function ()
	{
		return this.externalType;
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
	 * TODO
	 * Returns the related interface.
	 * 
	 * @returns {String} The related Interface 
	 */
	this.getRelatedInterface = function ()
	{
		return this.relatedInterface;
	};
	
	/**
	 * Returns the ID of the related process (only for external subjects).
	 * 
	 * @returns {String} The ID of the related process.
	 */
	this.getRelatedProcess = function ()
	{
		return this.relatedProcess;
	};
	
	/**
	 * Returns the ID of the corresponding subject in the related process (only for external subjects).
	 * 
	 * @returns {String} The ID of the corresponding subject in the related process.
	 */
	this.getRelatedSubject = function ()
	{
		return this.relatedSubject;
	};
	
	/**
	 * Returns the ID of the role that is assigned to this subject.
	 * 
	 * @returns {String} The ID of the role / user that is assigned to this subject.
	 */
	this.getRole = function ()
	{
		if (this.role == null || this.role == "" || this.role == "noUser" || this.role == "noRole")
			return gv_graph.getProcessText("noRole");
			
		return this.role;
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
     * The user-defined manual offset for the subject position
     *
     * @returns {null|{dx: int, dy: int}}
     */
    this.getManualPositionOffset = function ()
    {
        return this.manualPositionOffset;
    };
	
	/**
	 * Returns the corresponding url.
	 * 
	 * @returns {String} The corresponding url.
	 */
	this.getUrl = function ()
	{
		return this.url;
	};
	
	/**
	 * Returns true when the subject has an internal behavior.
	 * All non-external subjects have an internal behavior.
	 * So do interfaces.
	 * 
	 * @returns {boolean} True when the subject has an internal behavior.
	 */
	this.hasInternalBehavior = function ()
	{
		return !this.isExternal() || this.getExternalType() == "interface";
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
	 * Returns true when the subject is an external subject.
	 * 
	 * @returns {boolean} Returns true when the subject is an external subject.
	 */
	this.isExternal = function ()
	{
		return this.getType() == "external" || this.getType() == "multiexternal";
	};
	
	/**
	 * Returns true when the subject is a multi-subject.
	 * 
	 * @returns {boolean} Returns true when the subject is a multi-subject.
	 */
	this.isMulti = function ()
	{
		return this.getType() == "multi" || this.getType() == "multiexternal";	
	};
	
	/**
	 * Returns true when the subject can be the start subject of the process instance.
	 * 
	 * @returns {boolean} Returns true when subject can be start subject.
	 */
	this.isStartSubject = function ()
	{
		return this.startSubject === true;
	};
	
	/**
	 * Updates the subject's comment.
	 * 
	 * @param {String} comment The new comment.
	 * @returns {void}
	 */
	this.setComment = function (comment)
	{
		this.comment = comment;
	};
	
	/**
	 * The externalType attribute is only used for external subjects.
	 * Using this method the externalType will be updated
	 * 
	 * @param {String} externalType The new externalType; possible values: external, interface, instantinterface
	 * @returns {void}
	 */
	this.setExternalType = function (externalType)
	{
		if (this.isExternal() && gf_isset(externalType))
		{
			externalType = externalType.toLowerCase();
			
			if (externalType == "external" || externalType == "interface" || externalType == "instantinterface")
				this.externalType	= externalType;
		}
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
	 * Sets the related interface.
	 * 
	 * @param {String} relatedInterface The related interface.
	 * @returns {void}
	 */
	this.setRelatedInterface = function (relatedInterface)
	{
		if (gf_isset(relatedInterface))
		{
			this.relatedInterface	= relatedInterface;
		}
	};
	
	/**
	 * Returns the ID of the related process (only for external subjects).
	 * 
	 * @param {String} relatedProcess The ID of the related process.
	 * @returns {void}
	 */
	this.setRelatedProcess = function (relatedProcess)
	{
		if (gf_isset(relatedProcess))
		{
			this.relatedProcess = relatedProcess;
		}
	};
	
	/**
	 * Updates the ID of the corresponding subject in the related process (only for external subjects).
	 * 
	 * @param {String} relatedSubject The ID of the corresponding subject in the related process.
	 * @returns {void}
	 */
	this.setRelatedSubject = function (relatedSubject)
	{
		if (gf_isset(relatedSubject))
		{
			this.relatedSubject = relatedSubject;
		}
	};
	
	/**
	 * Updates the role that is assigned to the subject.
	 * 
	 * @param {String} role The ID of the role assigned to this subject.
	 * @returns {void} 
	 */
	this.setRole = function (role)
	{
		this.role = role;
	};
	
	/**
	 * Updates the startSubject status of the subject.
	 * 
	 * @param {boolean} startSubject New status of the subject.
	 * @returns {void}
	 */
	this.setStartSubject = function (startSubject)
	{
		if (gf_isset(startSubject))
		{
			this.startSubject = startSubject === true;
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
	 * @param {String} type The type of the subject. Possile values are "single", "multi", "external", "multiexternal". (default: "single")
	 * @returns {void}
	 */
	this.setType = function (type)
	{
		if (gf_isset(type))
		{
			type = type.toLowerCase();
			if (type == "single" || type == "multi" || type == "external" || type == "multiexternal")
			{
				this.type = type;
			}
		}
	};
	
	/**
	 * Sets the corresponding url.
	 * 
	 * @param {String} url The corresponding url.
	 * @returns {void}
	 */
	this.setUrl = function (url)
	{
		if (gf_isset(url))
		{
			this.url	= url;
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
		var gt_text			= this.text;
		
		if (this.isExternal())
		{
			// gt_text += "\n\n(reference: " + this.relatedSubject + " @ " + this.relatedProcess + ")";
			// TODO
			var gt_external	= "E";
			
			if (this.getExternalType() == "interface")
				gt_external	= "IF";
				
			if (this.getExternalType() == "instantinterface")
				gt_external = "IIF";
			
			gt_text += " (" + gt_external + ")";
		}
		
		gt_text += "\n(" + this.getRole() + ")\n \n[InputPool: " + gt_inputPool + "]";
		
		return gt_text;
	};
	
	// set the type
	this.setType(type);
	
	// set the input-pool size
	this.setInputPool(inputPool);
}