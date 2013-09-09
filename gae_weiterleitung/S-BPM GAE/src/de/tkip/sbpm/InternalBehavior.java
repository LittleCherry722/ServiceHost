package de.tkip.sbpm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InternalBehavior implements Serializable{
	public String subjectID;
	public int userID;
	public Map<Integer, State> statesMap = new HashMap<Integer, State>();
	public int startState = 0;
	public int currentState = 0; 
	public boolean executable;
	
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

	public void setProcessInstanceIDofStates(int processInstanceID){
		Iterator it = statesMap.values().iterator();
		while(it.hasNext()){
			State state = (State) it.next();
			state.processInstanceID = processInstanceID;
		}
	}
	
	public int getNextStateIDNoBranch(){
		return statesMap.get(currentState).transitions.get(0).getSuccessorID();
	}
	
	public int getNextStateID(String transitonText){
		for(int i = 0; i < statesMap.get(currentState).transitions.size(); i++){
			String text = statesMap.get(currentState).transitions.get(i).text;
			if(text.equals(transitonText)){
				return statesMap.get(currentState).transitions.get(i).getSuccessorID();
			}
		}
		return -1;
	}
	
	public String getSubjectID() {
		return subjectID;
	}

	public void setSubjectID(String subjectID) {
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

	public boolean isExecutable() {
		return executable;
	}

	public void setExecutable(boolean executable) {
		this.executable = executable;
	}
	
}