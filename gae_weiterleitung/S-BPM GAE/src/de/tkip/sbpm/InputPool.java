package de.tkip.sbpm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

class MessageKey implements Serializable{
	String subjectID;
	String messageType;
	public MessageKey(String sID, String messageType) {
		super();
		this.subjectID = sID;
		this.messageType = messageType;
	}
	
	@Override
	public int hashCode(){
		return subjectID.hashCode() + messageType.hashCode();
	}

	@Override
	public boolean equals(Object o){
		MessageKey key = (MessageKey) o;
		return this.subjectID.equals(key.subjectID) && this.messageType.equals(key.messageType);
	}
}

public class InputPool implements Serializable {

	private int userID;
	private int messageLimit = 100;
	private Map<MessageKey,Queue<SubjectToSubjectMessage>> messageQueueMap = new HashMap<MessageKey,Queue<SubjectToSubjectMessage>>();
	
	public InputPool() {
		
	}
	
	public void addMessage(SubjectToSubjectMessage msg){
		MessageKey key = new MessageKey(msg.from_subjectID,msg.messageType);
		if(!messageQueueMap.containsKey(key)){
			Queue<SubjectToSubjectMessage> msgPool = new LinkedList<SubjectToSubjectMessage>();
			msgPool.offer(msg);		
			messageQueueMap.put(key, msgPool);
			messageLimit--;
		}else{
			Queue<SubjectToSubjectMessage> msgPool = messageQueueMap.get(key);
			msgPool.offer(msg);
			messageLimit--;
		}
	}
	
	
	public int getMessageNumber(){
		Iterator it = messageQueueMap.keySet().iterator();
		int sum = 0;
		while(it.hasNext()){
			MessageKey key = (MessageKey) it.next();
			sum += messageQueueMap.get(key).size();
		}
		return sum;
	}
	
	public int checkMessageNumberFromSubjectIDAndType(String sID,String messageType){
		MessageKey key = new MessageKey(sID, messageType);
		if(!messageQueueMap.containsKey(key) || messageQueueMap.get(key).isEmpty()){
			return 0;
		}else{
			return messageQueueMap.get(key).size();
		}
	}
	
	public String getMessageFromSubjectIDAndType(String sID,String messageType){
		MessageKey key = new MessageKey(sID, messageType);
		if(messageQueueMap.containsKey(key) && !messageQueueMap.get(key).isEmpty()){
			Queue<SubjectToSubjectMessage> msgPool = messageQueueMap.get(key);
			String msg = msgPool.poll().getMessageContent();
			messageLimit++;
			return msg;
		}else{
			return null;
		}
	}
	
	public String getMessageTypeFromSubjcetID(int sID){
		Iterator it = messageQueueMap.keySet().iterator();
		String type = null;
		while(it.hasNext()){
			MessageKey key = (MessageKey) it.next();
			type = key.messageType;
		}
		return type;
	}
	
	public boolean isMessagePoolEmpty(String sID, String messageType){
		MessageKey key = new MessageKey(sID, messageType);
		return messageQueueMap.get(key).isEmpty();
	}
	
	public boolean isInputPoolEmpty(){
		boolean result = true;
		Iterator it = messageQueueMap.keySet().iterator();
		while(it.hasNext()){
			MessageKey key = (MessageKey) it.next();
			if(!messageQueueMap.get(key).isEmpty()){
				result = false;
				break;
			}
		}
		return result;
	}

	public int getMessageLimit() {
		return messageLimit;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public Map<MessageKey, Queue<SubjectToSubjectMessage>> getMessageQueueMap() {
		return messageQueueMap;
	}

	public void setMessageQueueMap(
			Map<MessageKey, Queue<SubjectToSubjectMessage>> messageQueueMap) {
		this.messageQueueMap = messageQueueMap;
	}

	public void setMessageLimit(int messageLimit) {
		this.messageLimit = messageLimit;
	}
	
}	

//
//class SubscribeIncomingMessages{
//	int stateID;
//	int fromSubject;
//	String messageType;
//	int remainingCount;
//	
//	public SubscribeIncomingMessages(int stateID, int fromSubject,
//			String messageType, int remainingCount) {
//		super();
//		this.stateID = stateID;
//		this.fromSubject = fromSubject;
//		this.messageType = messageType;
//		this.remainingCount = remainingCount;
//	}
//	
//}