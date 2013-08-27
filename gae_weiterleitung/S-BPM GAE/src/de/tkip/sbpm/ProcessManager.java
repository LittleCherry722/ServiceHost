package de.tkip.sbpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

import de.tkip.sbpm.State.StateType;

@PersistenceCapable
public class ProcessManager {
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
	@Persistent
	public int processInstanceID = 10000;
	@Persistent(serialized = "true")
	public List<Process> processList = new ArrayList<Process>();;
	@Persistent(serialized = "true")
	public List<ProcessInstance> processInstanceList = new ArrayList<ProcessInstance>();
	@Persistent(serialized = "true")
	public Map<State,Boolean> availableActions = new HashMap<State,Boolean>();
	
	public ProcessManager(){
//		processInstanceID = 10000;
//		processList = new ArrayList<Process>();
//		processInstanceList = new ArrayList<ProcessInstance>();
//		availbleActions = new HashMap<State,Boolean>();
	}
	
	public boolean containsProcess(int id){
		if(!processList.isEmpty()){
			Iterator it = processList.iterator();
			while(it.hasNext()){
				Process process = (Process) it.next();
				System.out.println("promana:" + process.processID);
				if(id == process.processID){
					return true;
				}
			}
		}		
		return false;
	}
	
	public Process getProcess(int id){
		if(!processList.isEmpty()){
			Iterator it = processList.iterator();
			while(it.hasNext()){
				Process process = (Process) it.next();
				if(id == process.processID){
					System.out.println("process" + process.processID);
					return process;
				}
			}
		}	
		return null;
	}
	
	public boolean containsProcessInstance(int id){
		if(!processInstanceList.isEmpty()){
			Iterator it = processInstanceList.iterator();
			while(it.hasNext()){
				ProcessInstance processInstance = (ProcessInstance) it.next();
				if(id == processInstance.processInstanceID){
					return true;
				}
			}	
		}	
		return false;
	}
	
	public ProcessInstance getProcessInstance(int id){
		if(!processInstanceList.isEmpty()){
			Iterator it = processInstanceList.iterator();
			while(it.hasNext()){
				ProcessInstance processInstance = (ProcessInstance) it.next();
				if(id == processInstance.processInstanceID){
					return processInstance;
				}
			}
		}	
		return null;
	}
	
	public void removeProcessInstance(ProcessInstance pi){
		this.processInstanceList.remove(pi);
	}
	
	public State getState(int processInstanceID, int stateID){
		Iterator it = availableActions.keySet().iterator();
		while(it.hasNext()){
			State state = (State) it.next();
			if(state.getProcessInstanceID() == processInstanceID && state.getId() == stateID){
				return state;
			}
		}
		return null;
	}
	
	public void checkReceiveActions(){
		Iterator it = this.availableActions.keySet().iterator();
		while(it.hasNext()){
			State state = (State) it.next();
			if(state.stateType.equals(StateType.receive)){
				String[] str = state.getTransitions().get(0).getText().split("(1)");
				String text = str[0].trim();
				ProcessInstance pi = getProcessInstance(state.getProcessInstanceID());
				if(pi.getProcessData().getSubjects().get(state.getSubjectID()).checkMessageNumberFromSubjectIDAndType(state.getSubjectID(), text) > 0){
					this.availableActions.put(state, true);
				}else{
					this.availableActions.put(state, false);
				}	
			}
		}
	}
	
	public void addAvailableActions(State state, boolean b){
		this.availableActions.put(state, b);
	}
	
	public void addProcess(Process process){
		this.processList.add(process);
	}
	
	public void addProcessInstance(ProcessInstance pi){
		this.processInstanceList.add(pi);
	}
	
	public void removeAvailableActions(State state){
		this.availableActions.remove(state);
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public int getProcessInstanceID() {
		return processInstanceID;
	}

	public void setProcessInstanceID(int processInstanceID) {
		this.processInstanceID = processInstanceID;
	}

	public List<Process> getProcessList() {
		return processList;
	}

	public void setProcessList(List<Process> processList) {
		this.processList = processList;
	}

	public List<ProcessInstance> getProcessInstanceList() {
		return processInstanceList;
	}

	public void setProcessInstanceList(List<ProcessInstance> processInstanceList) {
		this.processInstanceList = processInstanceList;
	}

	public Map<State, Boolean> getAvailbleActions() {
		return availableActions;
	}

	public void setAvailbleActions(Map<State, Boolean> availbleActions) {
		this.availableActions = availbleActions;
	}
}
