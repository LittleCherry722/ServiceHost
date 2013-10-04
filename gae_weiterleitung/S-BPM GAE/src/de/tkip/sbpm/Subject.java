package de.tkip.sbpm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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

public class Subject implements Serializable {
	
	public int userID;
	public int processID;
	public int processInstanceID;
	public String subjectID;
	public String subjectName;
	public String subjectType;
	public boolean isDisabled;
	public boolean isStartSubject;
	public InputPool inputPool = new InputPool();
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
	
	public int checkMessageNumberFromSubjectIDAndType(String sID,String messageType){
		return inputPool.checkMessageNumberFromSubjectIDAndType(sID, messageType);
	}
	
	public String getMessageFromSubjcetIDAndType(String sID, String messageType){
		return inputPool.getMessageFromSubjectIDAndType(sID,messageType);
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

	public String getSubjectID() {
		return subjectID;
	}

	public void setSubjectID(String subjectID) {
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

	public InternalBehavior getInternalBehavior() {
		return internalBehavior;
	}

	public void setInternalBehavior(InternalBehavior internalBehavior) {
		this.internalBehavior = internalBehavior;
	}

	public String getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(String subjectType) {
		this.subjectType = subjectType;
	}

	public boolean isDisabled() {
		return isDisabled;
	}

	public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	public boolean isStartSubject() {
		return isStartSubject;
	}

	public void setStartSubject(boolean isStartSubject) {
		this.isStartSubject = isStartSubject;
	}

}

