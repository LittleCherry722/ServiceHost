package de.tkip.sbpm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Process implements Serializable {	
	public int processID;
	public String processName;
	public Map<Integer,Subject> subjects;
	
	public Process(){
		subjects = new HashMap<Integer,Subject>();
	}
	public void addSubject(int subjectID,Subject subject){
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
	public Map<Integer, Subject> getSubjects() {
		return subjects;
	}
	public void setSubjects(Map<Integer, Subject> subjects) {
		this.subjects = subjects;
	}
}
