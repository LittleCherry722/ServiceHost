package de.tkip.sbpm;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tkip.sbpm.proto.GAEexecution.Action;
import de.tkip.sbpm.proto.GAEexecution.ListActions;

/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

public class ActionReceiver extends HttpServlet {
	@Override
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
				Iterator it = processManager.getAvailableActionsList().iterator();
				while(it.hasNext()){
					Action action = (Action) it.next();
					listActionsBuilder.addActions(action);
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
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
//		PersistenceManager pm = PMF.get().getPersistenceManager();
//		InputStream is = req.getInputStream();
//		String url = req.getRequestURI();
//		try {
//			pm.currentTransaction().begin();
//			String[] urls = url.split("/");
//			System.out.println("Action Receiver: ");
//			int id = Integer.valueOf(urls[urls.length - 1]);
//			int size = req.getContentLength();
//			byte[] byteProto = new byte[size];
//			is.read(byteProto);
//			ExecuteAction exeAction = ExecuteAction.parseFrom(byteProto);
//			Action action = exeAction.getAction();
//			int processInstanceID = action.getProcessInstanceID();
//			Query query = pm.newQuery(ProcessManager.class);
//			List<ProcessManager> processManagerList = (List<ProcessManager>) query.execute();
//			if (processManagerList.isEmpty()) {
//				System.out.println("no process manager.");
//			}else{
//				ProcessManager processManager = processManagerList.get(0);
//				if(processManager.containsProcessInstance(processInstanceID)){
//					processManager.checkReceiveActions();
//					ProcessInstance pi = processManager.getProcessInstance(processInstanceID);
//					Subject subject = pi.getProcessData().getSubjects().get(action.getSubjectID());
//					if(action.getStateID() == subject.getInternalBehavior().getCurrentState() && subject.getInternalBehavior().isExecutable()){
//						State currentState = subject.getInternalBehavior().getStatesMap().get(action.getStateID());
//						String msg;
//						boolean isEnd = false;
//						switch(action.getStateType()){
//						case "action":
//							System.out.println("type: action");
//							break;
//						case "receive":
//							String[] str = currentState.getTransitions().get(0).getText().split("(1)");
//							String text = str[0].trim();
//							msg = subject.getMessageFromSubjcetIDAndType(subject.getSubjectID(), text);
//							System.out.println("type: receive");
//							break;
//						case "send":
//							int messageID = 0;
//							int userID = 7;
//							String from_subjectID = subject.getSubjectID();
//							String target_subjectID = "0";
//							String[] str1 = currentState.getTransitions().get(0).getText().split("to:");
//							String sName = str1[str1.length-1].trim();
//							String messageType = str1[0].trim();
//							String msgContent = action.getActionData(0).getMessages(0).getMessageContent();
//							Iterator it = pi.getProcessData().getSubjects().keySet().iterator();
//							while(it.hasNext()){
//								String subjectID = (String)it.next();
//								String subjectName = pi.getProcessData().getSubjects().get(subjectID).getSubjectName();
//								if(sName.equals(subjectName)){
//									target_subjectID = subjectID;
//								}
//							}	
//							SubjectToSubjectMessage stsmsg = new SubjectToSubjectMessage(messageID, userID, from_subjectID, target_subjectID, processInstanceID, messageType, msgContent);
//							subject.addMessage(stsmsg);
//							processManager.checkReceiveActions();
//							System.out.println("type: send");
//							break;
//						case "end":
//							isEnd = true;
//							boolean isPIEnd = true;
//							Iterator it1 = pi.getProcessData().getSubjects().values().iterator();
//							while(it1.hasNext()){
//								Subject s1 = (Subject) it1.next();
//								int cs = s1.getInternalBehavior().getCurrentState();
//								if(!s1.getInternalBehavior().getStatesMap().get(cs).getStateType().equals(StateType.end)){
//									isPIEnd = false;
//									break;
//								}
//							}
//							if(isPIEnd){
//								processManager.removeProcessInstance(pi);
//							}
//							System.out.println("type: end");
//							break;
//						default: System.out.println("wrong type");
//						}
//						Action cAction = processManager.getAction(processInstanceID, action.getStateID());
//						processManager.removeAvailableActions(cAction);
//						if(!isEnd){
//							System.out.println("action count: " + action.getActionDataCount());
//							int nextStateID = subject.getInternalBehavior().getNextStateIDNoBranch();
//							System.out.println("nextStateID: " + nextStateID);
////							String transitonText = action.getActionData(0).getText();
////							int nextStateID = s.getInternalBehavior().getNextStateID(transitonText);
//							if(nextStateID != -1){
//								subject.getInternalBehavior().nextState(nextStateID);
//								State state = subject.getInternalBehavior().getStatesMap().get(nextStateID);
//								boolean executable = true;
//								if(state.getStateType().equals(StateType.receive)){
//									String[] str = state.getTransitions().get(0).getText().split("(1)");
//									String text = str[0].trim();
//									int num = subject.checkMessageNumberFromSubjectIDAndType(subject.getSubjectID(), text);
//									if(num == 0){
//										executable = false;
//									}		
//								}
//								subject.getInternalBehavior().setExecutable(executable);
//								Action.Builder actionBuilder = Action.newBuilder();
//								actionBuilder.setUserID(0)
//											 .setProcessInstanceID(state.getProcessInstanceID())
//											 .setSubjectID(state.getSubjectID())
//											 .setStateID(state.getId())
//											 .setStateText(state.getText())
//											 .setStateType(state.getStateType().name());
//								for(int i = 0; i < state.getTransitions().size(); i++){
//									String text  = state.getTransitions().get(i).getText();
//									String transitionType = state.getTransitions().get(i).getTransitionType();
//									int processInstanceID1 = state.getProcessInstanceID();
//									String subjectID1 = state.getSubjectID();
//									ActionData.Builder actionDataBuilder = ActionData.newBuilder();
//									actionDataBuilder.setText(text)
//													 .setExecutable(executable)
//													 .setTransitionType(transitionType);
//									ActionData actionData = actionDataBuilder.build();
//									actionBuilder.addActionData(actionData);
//								}
//								Action newAction = actionBuilder.build();
//								processManager.addAvailableActions(newAction);
//								
//								ListActions.Builder listActionsBuilder = ListActions.newBuilder();
//								Iterator it = processManager.getAvailableActionsList().iterator();
//								while(it.hasNext()){
//									Action action1 = (Action) it.next();
//									listActionsBuilder.addActions(action1);
//								}
//								ListActions listActions = listActionsBuilder.build();
//								resp.getOutputStream().write(listActions.toByteArray());
//					            resp.getOutputStream().flush();
//					            resp.getOutputStream().close();
//							}else{
//								System.out.println("wrong actionData");
//							}
//						}	
//					}else{
//						System.out.println("wrong action");
//					}
//				}else{
//					System.out.println("wrong process instance");
//				}
//			}
//			pm.currentTransaction().commit();
//		}catch(Exception e){
//			pm.currentTransaction().rollback();
//			e.printStackTrace();
//		}finally{
//			pm.close();
//		}
	}
}
