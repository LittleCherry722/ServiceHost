package de.tkip.sbpm;

import java.io.Serializable;

public class Transition implements Serializable {
	public String transitionType;
	public String text;
	public int successorID;
	
	public Transition(String transitionType, String text, int successorID) {
		super();
		this.transitionType = transitionType;
		this.text = text;
		this.successorID = successorID;
	}

	public int priority;

	public Transition() {
	}
}