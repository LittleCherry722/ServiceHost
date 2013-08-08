package de.tkip.sbpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Subject {
	
	private int userID;
	private int processID;
	private int processInstanceID;
	private int subjectID;
	private String subjectName;
	private InputPool inputPool = new InputPool();
	public InternalBehavior internalBehavior = new InternalBehavior();
	
	public Subject() {
		
	}

	public void addMessage(SubjectToSubjectMessage msg){
		inputPool.addMessage(msg);
	}
	
	public String getMessageTypeFromSubjcetID(int sID){
		return inputPool.getMessageTypeFromSubjcetID(sID);
	}
	public int getMessageLimit(){
		return inputPool.getMessageLimit();
	}
	
	public int getMessageNumber(){
		return inputPool.getMessageNumber();
	}
	
	public String getMessageFromSubjcetIDAndType(int sID, String messageType){
		return inputPool.getMessageFromSubjcetIDAndType(sID,messageType);
	}
	
	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public int getProcessID() {
		return processID;
	}

	public void setProcessID(int processID) {
		this.processID = processID;
	}

	public int getProcessInstanceID() {
		return processInstanceID;
	}

	public void setProcessInstanceID(int processInstanceID) {
		this.processInstanceID = processInstanceID;
	}

	public int getSubjectID() {
		return subjectID;
	}

	public void setSubjectID(int subjectID) {
		this.subjectID = subjectID;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public InputPool getInputPool() {
		return inputPool;
	}

	public void setInputPool(InputPool inputPool) {
		this.inputPool = inputPool;
	}

}

//class Subject{
//	int id;
//	int inputPoolLimit;
//	List<State> states = new ArrayList<State>();
//	boolean multi = false;
//	boolean external = false;
//}

