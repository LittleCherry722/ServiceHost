package de.tkip.sbpm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tkip.sbpm.State.StateType;
import de.tkip.sbpm.proto.GAEexecution.Action;
import de.tkip.sbpm.proto.GAEexecution.ActionData;

public class ActionReceiver extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
//		PersistenceManager pm = PMF.get().getPersistenceManager();
		InputStream is = req.getInputStream();
		try {
			int size = req.getContentLength();
			byte[] byteProto = new byte[size];
			is.read(byteProto);
			Action action = Action.parseFrom(byteProto);
			int processInstanceID = action.getProcessInstanceID();		
//			pm.currentTransaction().begin();
//			Query query = pm.newQuery(ProcessManager.class);
//			List<ProcessManager> processManagerList = (List<ProcessManager>) query.execute();
//			if (processManagerList.isEmpty()) {
//				System.out.println("Try again later.");
//			}else{
//				ProcessManager processManager = processManagerList.get(0);
				ProcessManager processManager = new ProcessManager();
				if(processManager.containsProcessInstance(processInstanceID)){
					ProcessInstance pi = processManager.getProcessInstance(processInstanceID);
					Subject s = pi.processData.subjects.get(Integer.parseInt(action.getSubjectID()));
					if(action.getStateID() == s.internalBehavior.getCurrentState() && s.internalBehavior.isExecutable()){
						State currentState = s.internalBehavior.getStatesMap().get(action.getStateID());
						String msg;
						boolean isEnd = false;
						switch(action.getStateType()){
						case "action":
							break;
						case "receive":
							String[] str = currentState.getTransitions().get(0).text.split("(1)");
							String text = str[0].trim();
							msg = s.getMessageFromSubjcetIDAndType(s.subjectID, text);
							break;
						case "send":
							int messageID = 0;
							int userID = 0;
							int from_subjectID = s.getSubjectID();
							int target_subjectID = 0;
							String[] str1 = currentState.transitions.get(0).text.split("to:");
							String sName = str1[str1.length-1].trim();
							String messageType = str1[0].trim();
							String msgContent = action.getActionData(0).getMessages(0).getMessageContent();
							Iterator it = pi.processData.subjects.keySet().iterator();
							while(it.hasNext()){
								int subjectID = (int)it.next();
								String subjectName = pi.processData.subjects.get(subjectID).subjectName;
								if(sName.equals(subjectName)){
									target_subjectID = subjectID;
								}
							}	
							SubjectToSubjectMessage stsmsg = new SubjectToSubjectMessage(messageID, userID, from_subjectID, target_subjectID, processInstanceID, messageType, msgContent);
							s.addMessage(stsmsg);
							break;
						case "end":
							isEnd = true;
							break;
						}
						if(!isEnd){
							String transitonText = action.getActionData(0).getText();
							int nextStateID = s.internalBehavior.getNextStateID(transitonText);
							if(nextStateID != -1){
								s.internalBehavior.nextState(nextStateID);
								State state = s.internalBehavior.getStatesMap().get(nextStateID);
								boolean executable = true;
								if(state.stateType.equals(StateType.receive)){
									String[] str = state.transitions.get(0).text.split("(1)");
									String text = str[0].trim();
									int num = s.checkMessageNumberFromSubjectIDAndType(s.subjectID, text);
									if(num == 0){
										executable = false;
									}		
								}
								s.internalBehavior.setExecutable(executable);
								processManager.addAvailbleActions(state, executable);
								Action.Builder actionBuilder = Action.newBuilder();
								actionBuilder.setUserID(0)
											 .setProcessInstanceID(processInstanceID)
											 .setSubjectID(Integer.toString(s.getSubjectID()))
											 .setStateID(nextStateID)
											 .setStateText(state.text)
											 .setStateType(state.stateType.name());
								for(int i = 0; i < state.getTransitions().size(); i++){
									String text  = state.getTransitions().get(i).text;
									String transitionType = state.getTransitions().get(i).transitionType;
									ActionData.Builder actionDataBuilder = ActionData.newBuilder();
									actionDataBuilder.setText(text)
													 .setExecutable(executable)
													 .setTransitionType(transitionType);
									ActionData actionData = actionDataBuilder.build();
									actionBuilder.addActionData(actionData);
								}
								Action newAction = actionBuilder.build();
								resp.getOutputStream().write(newAction.toByteArray());
					            resp.getOutputStream().flush();
					            resp.getOutputStream().close();
							}else{
								System.out.println("wrong actionData");
							}
						}	
					}else{
						System.out.println("wrong action");
					}
				}else{
					System.out.println("wrong process instance");
				}
//			}
//			pm.currentTransaction().commit();
		}catch(Exception e){
//			pm.currentTransaction().rollback();
			e.printStackTrace();
		}finally{
//			pm.close();
		}
	}

}