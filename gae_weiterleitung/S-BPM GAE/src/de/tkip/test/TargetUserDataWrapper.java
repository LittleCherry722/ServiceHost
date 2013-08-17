package de.tkip.test;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

import de.tkip.sbpm.proto.GAEexecution.TargetUserData;

@PersistenceCapable
public class TargetUserDataWrapper {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;
	
	@Persistent(serialized = "true")
	private TargetUserData.Builder targetUserDataBuilder = TargetUserData.newBuilder();
	@Persistent(serialized = "true")
	private TargetUserData targetUserData;
	@Persistent
	private static int index = 0;
	
	public TargetUserDataWrapper(int min, int max){
		this.targetUserDataBuilder.setMin(min);
		this.targetUserDataBuilder.setMax(max);
		this.targetUserData = this.targetUserDataBuilder.build();
	}

	public void addTargetUsers(int targetUser){
		this.targetUserDataBuilder.addTargetUsers(targetUser);
		index++;
		this.targetUserData = this.targetUserDataBuilder.build();
	}
	public TargetUserData getTargetUserData() {
		return targetUserData;
	}
}
