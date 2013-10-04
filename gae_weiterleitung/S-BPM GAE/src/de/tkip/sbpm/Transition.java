package de.tkip.sbpm;

import java.io.Serializable;

/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

public class Transition implements Serializable {
	public String transitionType;
	public String text;
	public String relatedSubject;
	public int successorID;
	public int priority;
	public boolean disabled;
	public boolean optional;
	public boolean manualTimeout;
	
	
	public Transition(String transitionType, String text, int successorID) {
		super();
		this.transitionType = transitionType;
		this.text = text;
		this.successorID = successorID;
	}

	

	public Transition() {
	}

	public String getTransitionType() {
		return transitionType;
	}

	public void setTransitionType(String transitionType) {
		this.transitionType = transitionType;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getSuccessorID() {
		return successorID;
	}

	public void setSuccessorID(int successorID) {
		this.successorID = successorID;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}



	public boolean isDisabled() {
		return disabled;
	}



	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}



	public boolean isOptional() {
		return optional;
	}



	public void setOptional(boolean optional) {
		this.optional = optional;
	}



	public boolean isManualTimeout() {
		return manualTimeout;
	}



	public void setManualTimeout(boolean manualTimeout) {
		this.manualTimeout = manualTimeout;
	}



	public String getRelatedSubject() {
		return relatedSubject;
	}



	public void setRelatedSubject(String relatedSubject) {
		this.relatedSubject = relatedSubject;
	}
}