package de.tkip.sbpm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ProcessInstance implements Serializable {
	public int processInstanceID;
	public Process processData;
	
	public ProcessInstance(){
		processData = new Process();
	}

	public Process getProcessData() {
		return processData;
	}

	public void setProcessData(Process processData) {
		this.processData = processData;
	}

	public int getProcessInstanceID() {
		return processInstanceID;
	}

	public void setProcessInstanceID(int processInstanceID) {
		this.processInstanceID = processInstanceID;
	}
}
