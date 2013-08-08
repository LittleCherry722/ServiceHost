package de.tkip.sbpm;

import java.util.ArrayList;
import java.util.List;

public class State {
	public enum StateType{
		action, send, receive, end;
	}
	
	int id;
	String text;
	int processInstanceID;
	StateType stateType;
	boolean startState;
	List<Transition> transitions = new ArrayList<Transition>();
	
	public State(int id, String text,
			StateType stateType, boolean startState) {
		super();
		this.id = id;
		this.text = text;
		this.stateType = stateType;
		this.startState = startState;
	}
	
}

class Transition{
	String transitionType;
	String text;
	int successorID;
	int priority;
	public Transition(String transitionType, String text, int successorID) {
		super();
		this.successorID = successorID;
		this.transitionType = transitionType;
		this.text = text;
	}
}