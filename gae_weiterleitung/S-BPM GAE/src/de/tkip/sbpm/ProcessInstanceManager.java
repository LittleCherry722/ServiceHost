package de.tkip.sbpm;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tkip.sbpm.State.StateType;
import de.tkip.sbpm.proto.GAEexecution.Action;
import de.tkip.sbpm.proto.GAEexecution.ActionData;
import de.tkip.sbpm.proto.GAEexecution.CreateProcessInstance;
import de.tkip.sbpm.proto.GAEexecution.ExecuteAction;
import de.tkip.sbpm.proto.GAEexecution.Graph;
import de.tkip.sbpm.proto.GAEexecution.ListActions;
import de.tkip.sbpm.proto.GAEexecution.MessageData;
import de.tkip.sbpm.proto.GAEexecution.ProcessInstanceData;
import de.tkip.sbpm.proto.GAEexecution.TargetUserData;

public class ProcessInstanceManager extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Get ok.");
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		InputStream is = req.getInputStream();
		String url = req.getRequestURI();
		int userID = 1;
		if(url.equals("/post")){
			try {
				int size = req.getContentLength();
				byte[] byteProto = new byte[size];
				is.read(byteProto);
				CreateProcessInstance cp = CreateProcessInstance.parseFrom(byteProto);
				int processID = cp.getProcessId();
				PersistenceManager pm = PMF.get().getPersistenceManager();
				try {
					Query query = pm.newQuery(ProcessManager.class);
					List<ProcessManager> processManagerList = (List<ProcessManager>) query.execute();
					if (processManagerList.isEmpty()) {
						ProcessManager processManager = new ProcessManager();
						pm.makePersistent(processManager);
						System.out.println("waiting for process manager initialization");
					}else{
						pm.currentTransaction().begin();
						ProcessManager processManager = processManagerList.get(0);
						if(!processManager.containsProcess(processID)){
							Graph graph = cp.getGraph();
							processManager.addGraph(graph);
							int subjectNum = graph.getSubjectsCount();
							Process process = new Process();
							process.setProcessID(processID);
							process.setDate(graph.getDate());
							process.setProcessName(cp.getName());
							Map<String, String> msgMap = new HashMap<String, String>();
							for(int i = 0; i< graph.getMessagesCount(); i++){
								String msgID = graph.getMessages(i).getId();
								String msg = graph.getMessages(i).getName();
								msgMap.put(msgID, msg);
							}
							for(int i = 0; i < subjectNum; i++){
								Subject subject = new Subject();
								subject.setProcessID(processID);
								subject.setSubjectID(graph.getSubjects(i).getId());
								subject.setSubjectName(graph.getSubjects(i).getName());
								subject.setSubjectType(graph.getSubjects(i).getSubjectType());
								subject.setDisabled(graph.getSubjects(i).getIsDisabled());
								subject.setStartSubject(graph.getSubjects(i).getIsStartSubject());
								subject.getInputPool().setMessageLimit(graph.getSubjects(i).getInputPool());
								subject.getInternalBehavior().setSubjectID(graph.getSubjects(i).getId());
								int graphNodeNum = graph.getSubjects(i).getMacros(0).getNodesCount();
								for(int j = 0; j < graphNodeNum; j++){
									State state = new State();
									state.setId(graph.getSubjects(i).getMacros(0).getNodes(j).getId());
									state.setSubjectID(graph.getSubjects(i).getId());
									state.setText(graph.getSubjects(i).getMacros(0).getNodes(j).getText());
									state.setStartState(graph.getSubjects(i).getMacros(0).getNodes(j).getIsStart());
									state.setEndState(graph.getSubjects(i).getMacros(0).getNodes(j).getIsEnd());
									state.setStateType(StateType.valueOf(graph.getSubjects(i).getMacros(0).getNodes(j).getNodeType()));
									state.setDisabled(graph.getSubjects(i).getMacros(0).getNodes(j).getIsDisabled());
									state.setMajorStart(graph.getSubjects(i).getMacros(0).getNodes(j).getIsMajorStartNode());
									subject.getInternalBehavior().addState(state);
								}
								int graphEdgeNum = graph.getSubjects(i).getMacros(0).getEdgesCount();
								for(int j = 0; j < graphEdgeNum; j++){
									int stateID = graph.getSubjects(i).getMacros(0).getEdges(j).getStartNodeId();
									State state = subject.getInternalBehavior().getStatesMap().get(stateID);
									Transition transition = new Transition();
									if(msgMap.containsKey(graph.getSubjects(i).getMacros(0).getEdges(j).getText())){
										transition.setText(msgMap.get(graph.getSubjects(i).getMacros(0).getEdges(j).getText()));
									}else{
										transition.setText(graph.getSubjects(i).getMacros(0).getEdges(j).getText());
									}
									transition.setTransitionType(graph.getSubjects(i).getMacros(0).getEdges(j).getEdgeType());
									transition.setSuccessorID(graph.getSubjects(i).getMacros(0).getEdges(j).getEndNodeId());
									transition.setDisabled(graph.getSubjects(i).getMacros(0).getEdges(j).getIsDisabled());
									transition.setOptional(graph.getSubjects(i).getMacros(0).getEdges(j).getIsOptional());
									transition.setManualTimeout(graph.getSubjects(i).getMacros(0).getEdges(j).getManualTimeout());
									transition.setPriority(graph.getSubjects(i).getMacros(0).getEdges(j).getPriority());
									transition.setRelatedSubject(graph.getSubjects(i).getMacros(0).getEdges(j).getTarget().getSubjectId());			
									state.getTransitions().add(transition);
								}
								process.addSubject(subject);
							}
								processManager.addProcess(process);
						}
						if(processManager.containsProcess(processID)){
							ProcessInstance pi = new ProcessInstance();
							Process process = processManager.getProcess(processID);
							int processInstanceID = processManager.getProcessInstanceID();
							Date dt=new Date();
							SimpleDateFormat matter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String date = matter.format(dt);
							pi.setProcessInstanceID(processInstanceID) ;
							pi.setDate(date);
							pi.setProcessData(process);
							pi.setName(cp.getName());
							if(!pi.getProcessData().getSubjects().isEmpty()){
								Iterator it = pi.getProcessData().getSubjects().iterator();
								while(it.hasNext()){
									Subject subject = (Subject) it.next();
									subject.getInternalBehavior().setProcessInstanceIDofStates(processInstanceID);
									State state = subject.getInternalBehavior().getStatesMap().get(subject.getInternalBehavior().getStartState());
									boolean executable = true;
									if(state.getStateType().equals(StateType.receive)){
										for(int i = 0; i < state.getTransitions().size(); i++){
											String text = state.getTransitions().get(i).getText();
											String relatedSubjectID = state.getTransitions().get(i).getRelatedSubject();
											int num = subject.checkMessageNumberFromSubjectIDAndType(relatedSubjectID, text);
											if(num > 0){
												break;
											}else{
												executable = false;
											}
										}
									}
									subject.getInternalBehavior().setExecutable(executable);
									if(subject.isStartSubject){
										Action.Builder actionBuilder = Action.newBuilder();
										actionBuilder.setUserID(userID)
													 .setProcessInstanceID(state.getProcessInstanceID())
													 .setSubjectID(state.getSubjectID())
													 .setStateID(state.getId())
													 .setStateText(state.getText())
													 .setStateType(state.getStateType().name());
										for(int i = 0; i < state.getTransitions().size(); i++){
											String text  = state.getTransitions().get(i).getText();
											String transitionType = state.getTransitions().get(i).getTransitionType();
											int processInstanceID1 = state.getProcessInstanceID();
											String subjectID1 = state.getSubjectID();
											ActionData.Builder actionDataBuilder = ActionData.newBuilder();
											actionDataBuilder.setText(text)
															 .setExecutable(executable)
															 .setTransitionType(transitionType);
											if(state.getStateType().equals(StateType.send)){
												TargetUserData.Builder tudBuilder = TargetUserData.newBuilder();
												tudBuilder.setMin(1)
														  .setMax(1)
														  .addTargetUsers(userID);
												TargetUserData tud = tudBuilder.build();
												actionDataBuilder.setTargetUserData(tud);
												actionDataBuilder.setRelatedSubject(state.getTransitions().get(i).getRelatedSubject());
											}
											if(state.getStateType().equals(StateType.receive)){
												String relatedSubject = state.getTransitions().get(i).getRelatedSubject();
												actionDataBuilder.setRelatedSubject(relatedSubject);
											}
											ActionData actionData = actionDataBuilder.build();
											actionBuilder.addActionData(actionData);
										}
										Action newAction = actionBuilder.build();
										processManager.addAvailableActions(newAction);
									}				
								}
							}
							processManager.addProcessInstance(pi);
							int t = processManager.getProcessInstanceID() +1;
							processManager.setProcessInstanceID(t);
							
							ProcessInstanceData.Builder pidBuilder = ProcessInstanceData.newBuilder();
							pidBuilder.setId(processInstanceID)
									  .setName(pi.getName())
									  .setProcessId(processID)
									  .setProcessName(process.getProcessName())
									  .setIsTerminated(pi.isTerminated())
									  .setDate(date)
									  .setOwner(userID)
									  .setHistory(pi.getHistory());
							pidBuilder.setGraph(cp.getGraph());
							Iterator it = processManager.getAvailableActionsList().iterator();
							while(it.hasNext()){
								Action action1 = (Action) it.next();
								if(action1.getProcessInstanceID() == processInstanceID){
									pidBuilder.addActions(action1);
								}
							}
							ProcessInstanceData pid = pidBuilder.build();
							resp.getOutputStream().write(pid.toByteArray());
				            resp.getOutputStream().flush();
				            resp.getOutputStream().close();
							pm.currentTransaction().commit();
						}else{
							System.out.println("no process");
							pm.currentTransaction().commit();
						}	
					}
				} catch(Exception e){
					e.printStackTrace();
					pm.currentTransaction().rollback();
				}finally {
					pm.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}else{
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				pm.currentTransaction().begin();
				String[] urls = url.split("/");
				int id = Integer.valueOf(urls[urls.length - 1]);
				int size = req.getContentLength();
				byte[] byteProto = new byte[size];
				is.read(byteProto);
				ExecuteAction exeAction = ExecuteAction.parseFrom(byteProto);
				Action action = exeAction.getAction();
				int processInstanceID = action.getProcessInstanceID();
				Query query = pm.newQuery(ProcessManager.class);
				List<ProcessManager> processManagerList = (List<ProcessManager>) query.execute();
				if (processManagerList.isEmpty()) {
					System.out.println("no process manager.");
				}else{
					ProcessManager processManager = processManagerList.get(0);
					if(processManager.containsProcessInstance(processInstanceID)){
						ProcessInstance pi = processManager.getProcessInstance(processInstanceID);
						Subject subject = pi.getProcessData().getSubjectByID(action.getSubjectID());
						if(true || action.getStateID() == subject.getInternalBehavior().getCurrentState()){
							State currentState = subject.getInternalBehavior().getStatesMap().get(action.getStateID());
							boolean isEnd = false;
							switch(action.getStateType()){
							case "action":
								System.out.println("type: action");
								break;
							case "receive":
								System.out.println("type: receive");
								break;
							case "send":
								String from_subjectID = subject.getSubjectID();
								String target_subjectID = action.getActionData(0).getRelatedSubject();	
								String messageType = currentState.getTransitions().get(0).getText();
								String msgContent = action.getActionData(0).getMessages(0).getMessageContent();
								SubjectToSubjectMessage stsmsg = new SubjectToSubjectMessage(userID, from_subjectID, target_subjectID, processInstanceID, messageType, msgContent);
								Subject targetSubject = pi.getProcessData().getSubjectByID(target_subjectID);
								targetSubject.addMessage(stsmsg);
								State state = targetSubject.getInternalBehavior().getStatesMap().get(targetSubject.getInternalBehavior().getCurrentState());
//								processManager.checkReceiveActions();
								if(processManager.getAction(processInstanceID, target_subjectID) == null && targetSubject.getInternalBehavior().getCurrentState() == targetSubject.getInternalBehavior().getStartState()){
									System.out.println("null");
									Action.Builder actionBuilder = Action.newBuilder();
									actionBuilder.setUserID(userID)
												 .setProcessInstanceID(state.getProcessInstanceID())
												 .setSubjectID(state.getSubjectID())
												 .setStateID(state.getId())
												 .setStateText(state.getText())
												 .setStateType(state.getStateType().name());
									for(int i = 0; i < state.getTransitions().size(); i++){
										String text1  = state.getTransitions().get(i).getText();
										String transitionType = state.getTransitions().get(i).getTransitionType();
										int processInstanceID1 = state.getProcessInstanceID();
										String subjectID1 = state.getSubjectID();
										ActionData.Builder actionDataBuilder = ActionData.newBuilder();
										actionDataBuilder.setText(text1)
														 .setExecutable(false)
														 .setTransitionType(transitionType);
										actionDataBuilder.setRelatedSubject(state.getTransitions().get(i).getRelatedSubject());
										int num = targetSubject.checkMessageNumberFromSubjectIDAndType(from_subjectID, text1);
										if(num > 0){
											actionDataBuilder.setExecutable(true);
											MessageData.Builder msgBuilder = MessageData.newBuilder();
											msgBuilder.setUserID(userID);
											String msg = targetSubject.getMessageFromSubjcetIDAndType(from_subjectID, text1);
											msgBuilder.setMessageContent(msg);
											MessageData msgData = msgBuilder.build();
											actionDataBuilder.addMessages(msgData);
											actionDataBuilder.setMessageContent(msg);
										}
										ActionData actionData = actionDataBuilder.build();
										actionBuilder.addActionData(actionData);
									}		
									Action newAction = actionBuilder.build();
									processManager.addAvailableActions(newAction);
								}else {
									Action receiveAction = processManager.getAction(processInstanceID, target_subjectID);
									processManager.removeAvailableActions(receiveAction);
									Action.Builder receiveActionBuilder = receiveAction.toBuilder();
									for(int i = 0; i < state.getTransitions().size(); i++){
										String text1  = state.getTransitions().get(i).getText();
										ActionData receiveActionData = receiveAction.getActionData(i);
										ActionData.Builder receiveActionDataBuilder = receiveActionData.toBuilder();
										int num = targetSubject.checkMessageNumberFromSubjectIDAndType(from_subjectID, text1);
										if(num > 0){
											receiveActionDataBuilder.setExecutable(true);
											MessageData.Builder msgBuilder = MessageData.newBuilder();
											msgBuilder.setUserID(userID);
											String msg = targetSubject.getMessageFromSubjcetIDAndType(from_subjectID, text1);
											msgBuilder.setMessageContent(msg);
											MessageData msgData = msgBuilder.build();
											receiveActionDataBuilder.addMessages(msgData);
											receiveActionDataBuilder.setMessageContent(msg);
										}
										receiveActionData = receiveActionDataBuilder.build();
										receiveActionBuilder.setActionData(i, receiveActionData);
									}
									receiveAction = receiveActionBuilder.build();
									processManager.addAvailableActions(receiveAction);
								}
								System.out.println("type: send");
								break;
							case "end":
								isEnd = true;
								boolean isPIEnd = true;
								Iterator it1 = pi.getProcessData().getSubjects().iterator();
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
								System.out.println("type: end");
								break;
							default: 
								System.out.println("wrong type");
							}
							Action cAction = processManager.getAction(processInstanceID, action.getSubjectID());
							processManager.removeAvailableActions(cAction);
							if(!isEnd){
								String transitionText = action.getActionData(0).getText();
								int nextStateID = subject.getInternalBehavior().getNextStateID(transitionText);
								if(nextStateID != -1){
									subject.getInternalBehavior().nextState(nextStateID);
									State state = subject.getInternalBehavior().getStatesMap().get(nextStateID);
									System.out.println("next state: " + state.getStateType());
									boolean executable = true;
									if(state.getStateType().equals(StateType.receive)){
										for(int i = 0; i < state.getTransitions().size(); i++){
											String text = state.getTransitions().get(i).getText();
											String relatedSubjectID = state.getTransitions().get(i).getRelatedSubject();
											int num = subject.checkMessageNumberFromSubjectIDAndType(relatedSubjectID, text);
											if(num > 0){
												break;
											}else{
												executable = false;
											}
										}
									}
									subject.getInternalBehavior().setExecutable(executable);
									Action.Builder actionBuilder = Action.newBuilder();
									actionBuilder.setUserID(userID)
												 .setProcessInstanceID(state.getProcessInstanceID())
												 .setSubjectID(state.getSubjectID())
												 .setStateID(state.getId())
												 .setStateText(state.getText())
												 .setStateType(state.getStateType().name());
									for(int i = 0; i < state.getTransitions().size(); i++){
										String text  = state.getTransitions().get(i).getText();
										String transitionType = state.getTransitions().get(i).getTransitionType();
										int processInstanceID1 = state.getProcessInstanceID();
										String subjectID1 = state.getSubjectID();
										ActionData.Builder actionDataBuilder = ActionData.newBuilder();
										actionDataBuilder.setText(text)
														 .setExecutable(executable)
														 .setTransitionType(transitionType);
										if(state.getStateType().equals(StateType.send)){
											TargetUserData.Builder tudBuilder = TargetUserData.newBuilder();
											tudBuilder.setMin(1)
													  .setMax(1)
													  .addTargetUsers(userID);
											TargetUserData tud = tudBuilder.build();
											actionDataBuilder.setTargetUserData(tud);
											actionDataBuilder.setRelatedSubject(state.getTransitions().get(i).getRelatedSubject());
										}
										if(state.getStateType().equals(StateType.receive)){
											actionDataBuilder.setRelatedSubject(state.getTransitions().get(i).getRelatedSubject());
											int num = subject.checkMessageNumberFromSubjectIDAndType(state.getTransitions().get(i).getRelatedSubject(), text);
											if(num > 0){
												actionDataBuilder.setExecutable(true);
												MessageData.Builder msgBuilder = MessageData.newBuilder();
												msgBuilder.setUserID(userID);
												String msg = subject.getMessageFromSubjcetIDAndType(state.getTransitions().get(i).getRelatedSubject(), text);
												msgBuilder.setMessageContent(msg);
												MessageData msgData = msgBuilder.build();
												actionDataBuilder.addMessages(msgData);
												actionDataBuilder.setMessageContent(msg);
											}
										}
										ActionData actionData = actionDataBuilder.build();
										actionBuilder.addActionData(actionData);
									}		
									Action newAction = actionBuilder.build();
									processManager.addAvailableActions(newAction);
									
									ProcessInstanceData.Builder pidBuilder = ProcessInstanceData.newBuilder();
									pidBuilder.setId(processInstanceID)
											  .setName(pi.getName())
											  .setProcessId(pi.getProcessData().getProcessID())
											  .setProcessName(pi.getProcessData().getProcessName())
											  .setIsTerminated(pi.isTerminated())
											  .setDate(pi.getDate())
											  .setOwner(userID)
											  .setHistory(pi.getHistory())
											  .setGraph(processManager.getGraphFromProcessID(pi.getProcessData().getProcessID()));
									Iterator it = processManager.getAvailableActionsList().iterator();
									while(it.hasNext()){
										Action action1 = (Action) it.next();
										if(action1.getProcessInstanceID() == processInstanceID){
											pidBuilder.addActions(action1);
//											System.out.println("action: " + action1.getStateType());
//											System.out.println("executeable: " + action1.getActionData(0).getExecutable());
										}
									}
									ProcessInstanceData pid = pidBuilder.build();
									resp.getOutputStream().write(pid.toByteArray());
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
}
