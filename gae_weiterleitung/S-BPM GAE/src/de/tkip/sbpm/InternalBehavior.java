package de.tkip.sbpm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

public class InternalBehavior implements Serializable{
	public String subjectID;
	public int userID;
	public List<State> statesList = new ArrayList<State>();
	public int startState = 0;
	public int currentState = 0; 
	public boolean executable;
	
	public InternalBehavior(){
		
	}
	
	public void addState(State state){
		this.statesList.add(state);
	}
	
	public void nextState(int stateID){
		this.currentState = stateID;
	}

	public State getStateByID(int stateID){
		Iterator it = this.statesList.iterator();
		while(it.hasNext()){
			State state = (State) it.next();
			if(state.getId() == stateID){
				return state;
			}
		}
		return null;
	}
	
	public void setProcessInstanceIDofStates(int processInstanceID){
		Iterator it = this.statesList.iterator();
		System.out.println("num: " + statesList.size());
		while(it.hasNext()){
			State state = (State) it.next();
			state.setProcessInstanceID(processInstanceID);
		}
	}
	
	public int getNextStateID(String transitonText){
		for(int i = 0; i < getStateByID(currentState).transitions.size(); i++){
			String text = getStateByID(currentState).transitions.get(i).text;
			if(text.equals(transitonText)){
				return this.getStateByID(currentState).transitions.get(i).getSuccessorID();
			}
		}
		return getStateByID(currentState).transitions.get(0).getSuccessorID();
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

	public List<State> getStatesList() {
		return statesList;
	}

	public void setStatesList(List<State> statesList) {
		this.statesList = statesList;
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