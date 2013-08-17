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
}
