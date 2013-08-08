package de.tkip.sbpm;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

import de.tkip.sbpm.proto.GAEexecution.ActionData;
import de.tkip.sbpm.proto.GAEexecution.MessageData;
import de.tkip.sbpm.proto.GAEexecution.TargetUserData;

@PersistenceCapable
public class ActionDataWrapper {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	private ActionData.Builder actionDataBuilder = ActionData.newBuilder();
	@Persistent(serialized = "true")
	private ActionData actionData;
	@Persistent
	private static int index = 0;
	public ActionDataWrapper(String text, boolean executable,
			String transitionType, TargetUserData targetUserData,
			String relatedSubject) {
			this.actionDataBuilder.setText(text);
			this.actionDataBuilder.setExecutable(executable);
			this.actionDataBuilder.setTransitionType(transitionType);
			this.actionDataBuilder.setTargetUserData(targetUserData);
			this.actionDataBuilder.setRelatedSubject(relatedSubject);
			this.actionData = this.actionDataBuilder.build();
	}
	
	public ActionDataWrapper(String text, boolean executable,
			String transitionType, TargetUserData targetUserData) {
			this.actionDataBuilder.setText(text);
			this.actionDataBuilder.setExecutable(executable);
			this.actionDataBuilder.setTransitionType(transitionType);
			this.actionDataBuilder.setTargetUserData(targetUserData);
			this.actionData = this.actionDataBuilder.build();
	}
	
	public ActionDataWrapper(String text, boolean executable,
			String transitionType, String relatedSubject) {
			this.actionDataBuilder.setText(text);
			this.actionDataBuilder.setExecutable(executable);
			this.actionDataBuilder.setTransitionType(transitionType);
			this.actionDataBuilder.setRelatedSubject(relatedSubject);
			this.actionData = this.actionDataBuilder.build();
	}

	public void addMessageData(MessageData messageData){
		this.actionDataBuilder.addMessages(messageData);
		index++;
		this.actionData = this.actionDataBuilder.build();
	}
	public ActionData getActionData() {
		return actionData;
	}
}
