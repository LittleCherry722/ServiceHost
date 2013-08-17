package de.tkip.test;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

import de.tkip.sbpm.proto.GAEexecution.Action;
import de.tkip.sbpm.proto.GAEexecution.ActionData;

@PersistenceCapable
public class ActionWrapper {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	private Action.Builder actionBuilder = Action.newBuilder();
	@Persistent(serialized = "true")
	private Action action;
	@Persistent
	private static int index = 0;

	public ActionWrapper(int userID, int processInstanceID, String subjectID,
			int stateID, String stateText, String stateType) {
		this.actionBuilder.setUserID(userID);
		this.actionBuilder.setProcessInstanceID(processInstanceID);
		this.actionBuilder.setSubjectID(subjectID);
		this.actionBuilder.setStateID(stateID);
		this.actionBuilder.setStateText(stateText);
		this.actionBuilder.setStateType(stateType);
		this.action = this.actionBuilder.build();
	}

	public void addActionData(ActionData actionData){
		this.actionBuilder.addActionData(actionData);
		index++;
		this.action = this.actionBuilder.build();
	}
	
	public Action getAction() {
		return action;
	}
	
}
