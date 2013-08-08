package de.tkip.sbpm;

public class SubjectToSubjectMessage {
	int messageID;
	int userID;
	int from_subjectID;
	int target_subjectID;
	String messageType;
	String messageContent;

	public SubjectToSubjectMessage(int messageID, int userID,
			int from_subjectID, int target_subjectID, String messageType,
			String messageContent) {
		this.messageID = messageID;
		this.userID = userID;
		this.from_subjectID = from_subjectID;
		this.target_subjectID = target_subjectID;
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

	public int getFrom_subjectID() {
		return from_subjectID;
	}

	public void setFrom_subjectID(int from_subjectID) {
		this.from_subjectID = from_subjectID;
	}

	public int getTarget_subjectID() {
		return target_subjectID;
	}

	public void setTarget_subjectID(int target_subjectID) {
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
