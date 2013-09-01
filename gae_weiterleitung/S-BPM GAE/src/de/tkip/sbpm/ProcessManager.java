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
import de.tkip.sbpm.proto.GAEexecution.Action;
import de.tkip.sbpm.proto.GAEexecution.Graph;

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
	public List<Action> availableActionsList = new ArrayList<Action>();
	@Persistent(serialized = "true")
	public List<Graph> graph = new ArrayList<Graph>();
	
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
	
	public Action getAction(int processInstanceID, int stateID){
		Iterator it = this.availableActionsList.iterator();
		while(it.hasNext()){
			Action action = (Action) it.next();
			if(action.getProcessInstanceID() == processInstanceID && action.getStateID() == stateID){
				return action;
			}
		}
		return null;
	}
	
	public void checkReceiveActions(){
		Iterator it = this.availableActionsList.iterator();
		while(it.hasNext()){
			Action action = (Action) it.next();
			int processInstanceID = action.getProcessInstanceID();
			String subjectID = action.getSubjectID();
			State state = getProcessInstance(processInstanceID).getProcessData().getSubjects().get(subjectID).getInternalBehavior().getStatesMap().get(action.getStateID());
			if(state.stateType.equals(StateType.receive)){
				String[] str = state.getTransitions().get(0).getText().split("(1)");
				String text = str[0].trim();
				ProcessInstance pi = getProcessInstance(state.getProcessInstanceID());
				if(pi.getProcessData().getSubjects().get(state.getSubjectID()).checkMessageNumberFromSubjectIDAndType(state.getSubjectID(), text) > 0){
					this.processInstanceList.get(processInstanceID).getProcessData().getSubjects().get(subjectID).getInternalBehavior().setExecutable(true);
				}else{
					this.processInstanceList.get(processInstanceID).getProcessData().getSubjects().get(subjectID).getInternalBehavior().setExecutable(false);
				}	
			}
		}
	}
	
	public Graph getGraphFromProcessID(int processID){
		Iterator it = this.graph.iterator();
		while(it.hasNext()){
			Graph graph = (Graph) it.next();
			int pid = graph.getProcessId();
			if(pid == processID){
				return graph;
			}
		}
		return null;
	}
	
	
	public void addAvailableActions(Action action){
		this.availableActionsList.add(action);
	}
	
//	public void addAvailableActions(State state){
//		this.availableActions.add(state);
//	}
	
	public void addProcess(Process process){
		this.processList.add(process);
	}
	
	public void addProcessInstance(ProcessInstance pi){
		this.processInstanceList.add(pi);
	}
	
	public void removeAvailableActions(Action action){
		this.availableActionsList.remove(action);
	}
	
	public void addGraph(Graph graph){
		this.graph.add(graph);
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

	public List<Action> getAvailableActionsList() {
		return availableActionsList;
	}

	public void setAvailableActionsList(List<Action> availableActionsList) {
		this.availableActionsList = availableActionsList;
	}

	public List<Graph> getGraph() {
		return graph;
	}

	public void setGraph(List<Graph> graph) {
		this.graph = graph;
	}

}
