package de.tkip.sbpm;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProcessInstance implements Serializable {
	public int processInstanceID;
	public String name;
	public boolean terminated = false;
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

	public String getDate() {
		return name;
	}

	public void setDate(String date) {
		this.name = date;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
