package de.tkip.sbpm;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProcessInstance implements Serializable {
	public int processInstanceID;
	public String date;
	public String name;
	public boolean terminated = false;
	public String history = "";
	public Process processData = new Process();
	
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
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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

	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
	}
}