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

public class SubjectToSubjectMessage implements Serializable{
	int messageID;
	int userID;
	String from_subjectID;
	String target_subjectID;
	int processInstanceID;
	String messageType;
	String messageContent;

	public SubjectToSubjectMessage(int userID,
			String from_subjectID, String target_subjectID,int processInstanceID, String messageType,
			String messageContent) {
		this.userID = userID;
		this.from_subjectID = from_subjectID;
		this.target_subjectID = target_subjectID;
		this.processInstanceID = processInstanceID;
		this.messageType = messageType;
		this.messageContent = messageContent;
	}

	public SubjectToSubjectMessage(){
		
	}
	
	public int getMessageID() {
		return messageID;
	}

	public void setMessageID(int messageID) {
		this.messageID = messageID;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getFrom_subjectID() {
		return from_subjectID;
	}

	public void setFrom_subjectID(String from_subjectID) {
		this.from_subjectID = from_subjectID;
	}

	public String getTarget_subjectID() {
		return target_subjectID;
	}

	public void setTarget_subjectID(String target_subjectID) {
		this.target_subjectID = target_subjectID;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
}
