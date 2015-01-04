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
 * @param {String} type The type of the subject. Possible values: "single", "multi", "external", "multiexternal" (default: "single")
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
	 * Either "external", "interface", "instantinterface" or "blackbox".
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
	 * The subjectIds of merged subjects
	 *
	 * @type Object
	 */
  this.mergedSubjects = [{id: id, name: text}];

	/**
	 * TODO
	 *
	 * @type String
	 */
	this.relatedInterface	= null;

	/**
	 * For blackbox subjects: the referenced blackboxname.
	 *
	 * @type String
	 */
	this.blackboxname		= null;

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
	 * For external subjects: is this subjuect an implementation or offer?
	 *
	 * @type String
	 */
	this.isImplementation	= false;

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
	this.implementations = [];

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
	 * @returns {String} The type of an external subject. Possible values are "external", "interface", "instantinterface", "blackbox"
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
	 * Returns the related interface.
	 *
	 * @returns {[String]} The related Interface
	 */
	this.getMergedSubjects = function ()
	{
		return this.mergedSubjects;
	};

	/**
	 * Returns the name of the blackbox (only for blackbox subjects).
	 *
	 * @returns {String} The name of the blackbox.
	 */
	this.getBlackboxname = function ()
	{
		return this.blackboxname;
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
	 * Returns wether the external subject in an implementation or an offer (only for external subjects).
	 *
	 * @returns {boolean} Wether this subject is an interface implementation or offer
	 */
	this.getIsImplementation = function ()
	{
		return this.isImplementation;
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
     * @returns {{dx: int, dy: int}}
     */
    this.getManualPositionOffset = function ()
    {
        return this.manualPositionOffset || {dx: 0, dy: 0};
    };

    /**
     * @returns {boolean} true if the the subject has a user-defined offset
     */
    this.hasManualPositionOffset = function ()
    {
        return this.manualPositionOffset !== null && 'dx' in this.manualPositionOffset && 'dy' in this.manualPositionOffset;
    };

	/**
	 * Returns the corresponding implementations.
	 *
	 * @returns {String} The corresponding implementations.
	 */
	this.getImplementations = function ()
	{
		return this.implementations;
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
		return !this.isExternal() || this.getExternalType() == "interface" || this.getExternalType() == "blackbox";
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
	 * @param {String} externalType The new externalType; possible values: external, interface, instantinterface, blackbox
	 * @returns {void}
	 */
	this.setExternalType = function (externalType)
	{
		if (this.isExternal() && gf_isset(externalType))
		{
			externalType = externalType.toLowerCase();

			if (externalType == "external" || externalType == "interface" || externalType == "instantinterface" || externalType == "blackbox")
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
	 * Sets the blackbox name.
	 *
	 * @param {String} blackboxname The name of the blackbox.
	 * @returns {void}
	 */
	this.setBlackboxname = function (blackboxname)
	{
		if (gf_isset(blackboxname))
		{
			this.blackboxname	= blackboxname;
		}
	};

	/**
	 * Sets the list of merged subjects name.
	 *
	 * @param {[String]} mergedSubjects The name of the blackbox.
	 * @returns {void}
	 */
	this.setMergedSubjects = function (mergedSubjects)
	{
		if (gf_isset(mergedSubjects))
		{
			this.mergedSubjects	= mergedSubjects;
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
	 * @param {Int} relatedProcess The ID of the related process.
	 * @returns {void}
	 */
	this.setRelatedProcess = function (relatedProcess)
	{
		if (gf_isset(relatedProcess))
		{
			this.relatedProcess = parseInt(relatedProcess, 10);
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
	 * Updates the ID of the corresponding subject in the related process (only for external subjects).
	 *
	 * @param {String} relatedSubject The ID of the corresponding subject in the related process.
	 * @returns {void}
	 */
	this.setIsImplementation = function (isImplementation)
	{
		this.isImplementation = !!isImplementation;
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
			var mergedSubject = this.mergedSubjects.filter(function(sub) {
				return sub.id === this.id;
			}, this);
			if (mergedSubject && mergedSubject[0]){
				mergedSubject[0].name = text;
			}
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
	 * Sets the corresponding implementations.
	 *
	 * @param {[Integer]} implementations The corresponding implementations.
	 * @returns {void}
	 */
	this.setImplementations = function (implementations)
	{
		if (gf_isset(implementations))
		{
			this.implementations	= implementations;
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

			if (this.getExternalType() == "blackbox")
				gt_external = "BB " + this.getBlackboxname();

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
