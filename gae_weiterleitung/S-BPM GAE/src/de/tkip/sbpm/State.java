package de.tkip.sbpm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class State implements Serializable{
	
	public enum StateType{
		action, send, receive, end;
	}
	public int id;
	public String text;
	public int processInstanceID;
	public String subjectID;
	public StateType stateType;
	public boolean startState;
	public boolean endState;
	public boolean disabled;
	public boolean MajorStart;
	public List<Transition> transitions = new ArrayList<Transition>();
	
	public State(int id, String text,
			StateType stateType, boolean startState) {
		super();
		this.id = id;
		this.text = text;
		this.stateType = stateType;
		this.startState = startState;
	}

	public State() {
		
	}

	public List<Transition> getTransitions() {
		return transitions;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getProcessInstanceID() {
		return processInstanceID;
	}

	public void setProcessInstanceID(int processInstanceID) {
		this.processInstanceID = processInstanceID;
	}

	public StateType getStateType() {
		return stateType;
	}

	public void setStateType(StateType stateType) {
		this.stateType = stateType;
	}

	public boolean isStartState() {
		return startState;
	}

	public void setStartState(boolean startState) {
		this.startState = startState;
	}

	public void setTransitions(List<Transition> transitions) {
		this.transitions = transitions;
	}

	public String getSubjectID() {
		return subjectID;
	}

	public void setSubjectID(String subjectID) {
		this.subjectID = subjectID;
	}

	public boolean isEndState() {
		return endState;
	}

	public void setEndState(boolean endState) {
		this.endState = endState;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isMajorStart() {
		return MajorStart;
	}

	public void setMajorStart(boolean majorStart) {
		MajorStart = majorStart;
	}
}
