package de.tkip.sbpm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class Process extends SerialCloneable {	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4673557180565126612L;
	public int processID;
	public String processName;
	public String date;
	public List<Subject> subjects = new ArrayList<Subject>();
	
	public Process(){
	}
	
	public void addSubject(Subject subject){
		this.subjects.add(subject);
	}
	
	public Subject getSubjectByID(String subjectID){
		Iterator it = this.subjects.iterator();
		while(it.hasNext()){
			Subject sub = (Subject) it.next();
			if (sub.getSubjectID().equals(subjectID)) {
				return sub;
			}
		}
		return null;
	}
	
	public int getProcessID() {
		return processID;
	}
	public void setProcessID(int processID) {
		this.processID = processID;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public List<Subject> getSubjects() {
		return subjects;
	}
	public void setSubjects(List<Subject> subjects) {
		this.subjects = subjects;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
