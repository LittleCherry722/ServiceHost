package de.tkip.sbpm;

import java.util.List;
import java.util.Map;

public class BehaviorState {
	
	private StateData data;
	private State model;
	private int id;
	private int userID;
	private int processID;
	private int processInstanceID;
	private int subjectID;
	private String stateText;
	private boolean startState;
	private State.StateType stateType;
	private List<Transition> transitions;
	private InternalBehavior internalBehavior;
//	private ProcessInstance processInstance;
	private InputPool inputPool;
	private InternalStatus internalStatus;
	private Map<String, Variable> variables;
	
	public BehaviorState(StateData data) {
		this.data = data;
		this.model = data.stateModel;
		this.id = model.id;
		this.userID = data.userID;
//		this.processID = data.subjectData.processID;
//		this.processInstanceID = data.subjectData.processInstanceID;
		this.subjectID = data.subjectID;
		this.stateText = model.text;
		this.startState = model.startState;
		this.stateType = model.stateType;
		this.transitions = model.transitions;
		this.internalBehavior = data.internalBehavior;
//		this.processInstance = data.processInstance;
		this.inputPool = data.inputPool;
		this.internalStatus = data.internalStatus;
		this.variables = internalStatus.variables;
	}
	
}

class StateData{

	State stateModel;
	int userID;
	int subjectID;
	InternalBehavior internalBehavior;
//	ProcessInstance processInstance;
	InputPool inputPool;
	InternalStatus internalStatus;
	
	public StateData(State stateModel, int userID,
			int subjectID, InternalBehavior internalBehavior,
			InputPool inputPool, InternalStatus internalStatus) {
		this.stateModel = stateModel;
		this.userID = userID;
		this.subjectID = subjectID;
		this.internalBehavior = internalBehavior;
		this.inputPool = inputPool;
		this.internalStatus = internalStatus;
	}
}