package de.tkip.sbpm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Process implements Serializable {	
	public int processID;
	public String processName;
	public String date;
	public Map<String,Subject> subjects = new HashMap<String,Subject>();
	
	public Process(){
	}
	
	public void addSubject(Subject subject){
		String subjectID = subject.getSubjectID();
		subjects.put(subjectID,subject);
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
	public Map<String, Subject> getSubjects() {
		return subjects;
	}
	public void setSubjects(Map<String, Subject> subjects) {
		this.subjects = subjects;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
