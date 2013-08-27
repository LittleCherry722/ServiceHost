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
import de.tkip.sbpm.proto.GAEexecution.ListActions;

public class ActionReceiver extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			ListActions.Builder listActionsBuilder = ListActions.newBuilder();
			pm.currentTransaction().begin();
			Query query = pm.newQuery(ProcessManager.class);
			List<ProcessManager> processManagerList = (List<ProcessManager>) query.execute();
			if (processManagerList.isEmpty()) {
				System.out.println("Try again later.");
			}else{
				ProcessManager processManager = processManagerList.get(0);
				Iterator it = processManager.getAvailbleActions().keySet().iterator();
				while(it.hasNext()){
					State state1 = (State) it.next();
					Action.Builder actionBuilder = Action.newBuilder();
					actionBuilder.setUserID(0)
								 .setProcessInstanceID(state1.getProcessInstanceID())
								 .setSubjectID(state1.getSubjectID())
								 .setStateID(state1.getId())
								 .setStateText(state1.getText())
								 .setStateType(state1.getStateType().name());
					for(int i = 0; i < state1.getTransitions().size(); i++){
						String text  = state1.getTransitions().get(i).getText();
						String transitionType = state1.getTransitions().get(i).getTransitionType();
						ActionData.Builder actionDataBuilder = ActionData.newBuilder();
						actionDataBuilder.setText(text)
										 .setExecutable(processManager.getAvailbleActions().get(state1))
										 .setTransitionType(transitionType);
						ActionData actionData = actionDataBuilder.build();
						actionBuilder.addActionData(actionData);
					}
					Action newAction = actionBuilder.build();
					listActionsBuilder.addActions(newAction);
				}
				ListActions listActions = listActionsBuilder.build();
				resp.getOutputStream().write(listActions.toByteArray());
		        resp.getOutputStream().flush();
		        resp.getOutputStream().close();
			}
			pm.currentTransaction().commit();
		} catch (Exception e) {
			pm.currentTransaction().rollback();
			e.printStackTrace();
		}finally{
			pm.close();
		}
		
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		InputStream is = req.getInputStream();
		try {
			int size = req.getContentLength();
			byte[] byteProto = new byte[size];
			is.read(byteProto);
			Action action = Action.parseFrom(byteProto);
			int processInstanceID = action.getProcessInstanceID();		
			pm.currentTransaction().begin();
			Query query = pm.newQuery(ProcessManager.class);
			List<ProcessManager> processManagerList = (List<ProcessManager>) query.execute();
			if (processManagerList.isEmpty()) {
				System.out.println("Try again later.");
			}else{
				ProcessManager processManager = processManagerList.get(0);
				if(processManager.containsProcessInstance(processInstanceID)){
					ProcessInstance pi = processManager.getProcessInstance(processInstanceID);
					Subject s = pi.getProcessData().getSubjects().get(action.getSubjectID());
					if(action.getStateID() == s.getInternalBehavior().getCurrentState() && s.getInternalBehavior().isExecutable()){
						State currentState = s.getInternalBehavior().getStatesMap().get(action.getStateID());
						String msg;
						boolean isEnd = false;
						switch(action.getStateType()){
						case "action":
							break;
						case "receive":
							String[] str = currentState.getTransitions().get(0).getText().split("(1)");
							String text = str[0].trim();
							msg = s.getMessageFromSubjcetIDAndType(s.getSubjectID(), text);
							break;
						case "send":
							int messageID = 0;
							int userID = 0;
							String from_subjectID = s.getSubjectID();
							String target_subjectID = "0";
							String[] str1 = currentState.getTransitions().get(0).getText().split("to:");
							String sName = str1[str1.length-1].trim();
							String messageType = str1[0].trim();
							String msgContent = action.getActionData(0).getMessages(0).getMessageContent();
							Iterator it = pi.getProcessData().getSubjects().keySet().iterator();
							while(it.hasNext()){
								String subjectID = (String)it.next();
								String subjectName = pi.getProcessData().getSubjects().get(subjectID).getSubjectName();
								if(sName.equals(subjectName)){
									target_subjectID = subjectID;
								}
							}	
							SubjectToSubjectMessage stsmsg = new SubjectToSubjectMessage(messageID, userID, from_subjectID, target_subjectID, processInstanceID, messageType, msgContent);
							s.addMessage(stsmsg);
							processManager.checkReceiveActions();
							break;
						case "end":
							isEnd = true;
							boolean isPIEnd = true;
							Iterator it1 = pi.getProcessData().getSubjects().values().iterator();
							while(it1.hasNext()){
								Subject s1 = (Subject) it1.next();
								int cs = s1.getInternalBehavior().getCurrentState();
								if(!s1.getInternalBehavior().getStatesMap().get(cs).getStateType().equals(StateType.end)){
									isPIEnd = false;
									break;
								}
							}
							if(isPIEnd){
								processManager.removeProcessInstance(pi);
							}
							break;
						}
						State cState = processManager.getState(processInstanceID, action.getStateID());
						processManager.removeAvailableActions(cState);
						if(!isEnd){
							String transitonText = action.getActionData(0).getText();
							int nextStateID = s.getInternalBehavior().getNextStateID(transitonText);
							if(nextStateID != -1){
								s.getInternalBehavior().nextState(nextStateID);
								State state = s.getInternalBehavior().getStatesMap().get(nextStateID);
								boolean executable = true;
								if(state.getStateType().equals(StateType.receive)){
									String[] str = state.getTransitions().get(0).getText().split("(1)");
									String text = str[0].trim();
									int num = s.checkMessageNumberFromSubjectIDAndType(s.getSubjectID(), text);
									if(num == 0){
										executable = false;
									}		
								}
								s.getInternalBehavior().setExecutable(executable);
								processManager.addAvailableActions(state, executable);
								ListActions.Builder listActionsBuilder = ListActions.newBuilder();
								Iterator it = processManager.getAvailbleActions().keySet().iterator();
								while(it.hasNext()){
									State state1 = (State) it.next();
									Action.Builder actionBuilder = Action.newBuilder();
									actionBuilder.setUserID(0)
												 .setProcessInstanceID(state1.getProcessInstanceID())
												 .setSubjectID(state1.getSubjectID())
												 .setStateID(state1.getId())
												 .setStateText(state1.getText())
												 .setStateType(state1.getStateType().name());
									for(int i = 0; i < state1.getTransitions().size(); i++){
										String text  = state1.getTransitions().get(i).getText();
										String transitionType = state1.getTransitions().get(i).getTransitionType();
										ActionData.Builder actionDataBuilder = ActionData.newBuilder();
										actionDataBuilder.setText(text)
														 .setExecutable(processManager.getAvailbleActions().get(state1))
														 .setTransitionType(transitionType);
										ActionData actionData = actionDataBuilder.build();
										actionBuilder.addActionData(actionData);
									}
									Action newAction = actionBuilder.build();
									listActionsBuilder.addActions(newAction);
								}
								ListActions listActions = listActionsBuilder.build();
								resp.getOutputStream().write(listActions.toByteArray());
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
			}
			pm.currentTransaction().commit();
		}catch(Exception e){
			pm.currentTransaction().rollback();
			e.printStackTrace();
		}finally{
			pm.close();
		}
	}

}
