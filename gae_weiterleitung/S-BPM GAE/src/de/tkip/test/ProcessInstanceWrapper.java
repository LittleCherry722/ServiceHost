package de.tkip.test;


import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PrimaryKey;
import com.google.appengine.api.datastore.Key;


import de.tkip.sbpm.proto.GAEexecution.Action;
import de.tkip.sbpm.proto.GAEexecution.Graph;
import de.tkip.sbpm.proto.GAEexecution.ProcessInstanceData;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class ProcessInstanceWrapper {
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	private ProcessInstanceData.Builder processInstanceBuilder = ProcessInstanceData.newBuilder();	
	@Persistent(serialized = "true")
	private ProcessInstanceData processInstance;
	@Persistent
	private static int index = 0;
	
	public ProcessInstanceWrapper(int id, int processid, Graph graph,
			boolean isTerminated, String history) {
		this.processInstanceBuilder.setId(id);
		this.processInstanceBuilder.setProcessId(processid);
		this.processInstanceBuilder.setGraph(graph);
		this.processInstanceBuilder.setIsTerminated(isTerminated);
		this.processInstanceBuilder.setHistory(history);
		this.processInstance = this.processInstanceBuilder.build();
	}
	public ProcessInstanceWrapper(ProcessInstanceData processInstance){
		this.processInstance = processInstance;
	}

	public void addAction(Action action){
		this.processInstanceBuilder = this.processInstance.toBuilder();
		this.processInstanceBuilder.addActions(action);
		index++;
		this.processInstance = this.processInstanceBuilder.build();
	}
	public ProcessInstanceData getProcessInstance(){
		return this.processInstance;
	}

	public void setProcessInstance(ProcessInstanceData processInstance) {
		this.processInstance = processInstance;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

}
