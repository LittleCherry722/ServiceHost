package de.tkip.sbpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalBehavior {
	private int subjectID;
	private int userID;
	private Map<Integer, State> statesMap = new HashMap<Integer, State>();
	private int startState = 0;
//	private BehaviorState currentState;
	private int currentState; 
	private InternalStatus internalStatus;
	
	public InternalBehavior(){
		
	}
	
	public void addState(State state){
		if(state.startState){
			startState = state.id;
			currentState = startState;
		}
		statesMap.put(state.id, state);
	}
	
	public void nextState(int stateID){
		currentState = stateID;
	}
	
	public void changeState(ChangeState change){
		this.internalStatus = change.internalStatus;
		nextState(change.nextState);
		currentState = change.currentState;
	}

	public int getSubjectID() {
		return subjectID;
	}

	public void setSubjectID(int subjectID) {
		this.subjectID = subjectID;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public Map<Integer, State> getStatesMap() {
		return statesMap;
	}

	public void setStatesMap(Map<Integer, State> statesMap) {
		this.statesMap = statesMap;
	}

	public int getStartState() {
		return startState;
	}

	public void setStartState(int startState) {
		this.startState = startState;
	}

	public int getCurrentState() {
		return currentState;
	}

	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}

	public InternalStatus getInternalStatus() {
		return internalStatus;
	}

	public void setInternalStatus(InternalStatus internalStatus) {
		this.internalStatus = internalStatus;
	}
	
}

class InternalStatus{
	boolean subjectStartedSent = false;
	Map<String,Variable> variables = new HashMap<String,Variable>();
}

class Variable{
	List<SubjectToSubjectMessage> messages = new ArrayList<SubjectToSubjectMessage>();
	public void addMessage(SubjectToSubjectMessage message){
		messages.add(message);
	}
}

class ChangeState{
	int currentState;
	int nextState;
	InternalStatus internalStatus;
	HistoryMessage history;
}

class HistoryMessage{
	int id;
	String messageType;
	String from;
	String to;
	String data;
}