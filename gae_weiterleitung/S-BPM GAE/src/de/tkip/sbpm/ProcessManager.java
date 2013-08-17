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

//@PersistenceCapable
public class ProcessManager {
//	@PrimaryKey
//    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
//    private Key key;
	
//	@Persistent
	public static int processInstanceID = 10000;
//	@Persistent(serialized = "true")
	public static List<Process> processList = new ArrayList<Process>();;
//	@Persistent(serialized = "true")
	public static List<ProcessInstance> processInstanceList = new ArrayList<ProcessInstance>();
//	@NotPersistent
	public static Map<State,Boolean> availbleActions = new HashMap<State,Boolean>();
	
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
	
	public void addAvailbleActions(State state, boolean b){
		this.availbleActions.put(state, b);
	}
	
	public void addProcess(Process process){
		this.processList.add(process);
	}
	
	public void addProcessInstance(ProcessInstance pi){
		this.processInstanceList.add(pi);
	}

//	public Key getKey() {
//		return key;
//	}
//
//	public void setKey(Key key) {
//		this.key = key;
//	}
}
