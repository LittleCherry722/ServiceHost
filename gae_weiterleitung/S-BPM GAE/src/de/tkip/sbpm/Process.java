package de.tkip.sbpm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
