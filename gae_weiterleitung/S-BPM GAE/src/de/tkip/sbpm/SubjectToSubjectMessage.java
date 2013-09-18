package de.tkip.sbpm;

import java.io.Serializable;

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
