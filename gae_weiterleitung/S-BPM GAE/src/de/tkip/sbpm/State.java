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
	public StateType stateType;
	public boolean startState;
	public List<Transition> transitions = new ArrayList<Transition>();
	
	public State(int id, String text,
			StateType stateType, boolean startState) {
		super();
		this.id = id;
		this.text = text;
		this.stateType = stateType;
		this.startState = startState;
	}

	public List<Transition> getTransitions() {
		return transitions;
	}	
}
