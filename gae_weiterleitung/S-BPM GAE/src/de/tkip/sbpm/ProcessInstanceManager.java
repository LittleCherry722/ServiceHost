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
			System.out.println("/post");
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
								Iterator it = pi.getProcessData().getSubjects().keySet().iterator();
								while(it.hasNext()){
									String id = (String) it.next();
									Subject sub = pi.getProcessData().getSubjects().get(id);
									sub.getInternalBehavior().setProcessInstanceIDofStates(processInstanceID);
									State state = sub.getInternalBehavior().getStatesMap().get(sub.getInternalBehavior().getStartState());
									boolean executable = true;
									if(state.getStateType().equals(StateType.receive)){
										String[] s = state.getTransitions().get(0).getText().split("(1)");
										String text = s[0].trim();
										int num = sub.checkMessageNumberFromSubjectIDAndType(sub.getSubjectID(), text);
										if(num == 0){
											executable = false;
										}		
									}
									sub.getInternalBehavior().setExecutable(executable);
									if(sub.isStartSubject){
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
												MessageData.Builder msgBuilder = MessageData.newBuilder();
												String[] s = state.getTransitions().get(0).getText().split("(1)");
												String text1 = s[0].trim();
												int num = sub.checkMessageNumberFromSubjectIDAndType(sub.getSubjectID(), text1);
												if(num == 0){
													msgBuilder.setMessageContent("");
												}else{
													msgBuilder.setMessageContent(sub.getMessageFromSubjcetIDAndType(sub.getSubjectID(), text1));
												}
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
				System.out.println("Action Receiver: /post/" + id);
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
						processManager.checkReceiveActions();
						ProcessInstance pi = processManager.getProcessInstance(processInstanceID);
						Subject subject = pi.getProcessData().getSubjects().get(action.getSubjectID());
						if(action.getStateID() == subject.getInternalBehavior().getCurrentState() && subject.getInternalBehavior().isExecutable()){
							State currentState = subject.getInternalBehavior().getStatesMap().get(action.getStateID());
							String msg;
							boolean isEnd = false;
							switch(action.getStateType()){
							case "action":
								System.out.println("type: action");
								break;
							case "receive":
								String[] str = currentState.getTransitions().get(0).getText().split("(1)");
								String text = str[0].trim();
								msg = subject.getMessageFromSubjcetIDAndType(subject.getSubjectID(), text);
								System.out.println("type: receive");
								break;
							case "send":
								int messageID = 0;
								String from_subjectID = subject.getSubjectID();
								String target_subjectID = "0";
								String[] str1 = currentState.getTransitions().get(0).getText().split("to:");
								String sName = str1[str1.length-1].trim();
								String messageType = str1[0].trim();
								String msgContent = action.getActionData(0).getMessages(0).getMessageContent();
								System.out.println("msgContent: " + msgContent);
								Iterator it = pi.getProcessData().getSubjects().keySet().iterator();
								while(it.hasNext()){
									String subjectID = (String)it.next();
									String subjectName = pi.getProcessData().getSubjects().get(subjectID).getSubjectName();
									if(sName.equals(subjectName)){
										target_subjectID = subjectID;
									}
								}	
								SubjectToSubjectMessage stsmsg = new SubjectToSubjectMessage(messageID, userID, from_subjectID, target_subjectID, processInstanceID, messageType, msgContent);
								subject.addMessage(stsmsg);
								processManager.checkReceiveActions();
								System.out.println("type: send");
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
								System.out.println("type: end");
								break;
							default: System.out.println("wrong type");
							}
							Action cAction = processManager.getAction(processInstanceID, action.getSubjectID(), action.getStateID());
							processManager.removeAvailableActions(cAction);
							if(!isEnd){
								System.out.println("action count: " + action.getActionDataCount());
								int nextStateID = subject.getInternalBehavior().getNextStateIDNoBranch();
								System.out.println("nextStateID: " + nextStateID);
//								String transitonText = action.getActionData(0).getText();
//								int nextStateID = s.getInternalBehavior().getNextStateID(transitonText);
								if(nextStateID != -1){
									subject.getInternalBehavior().nextState(nextStateID);
									State state = subject.getInternalBehavior().getStatesMap().get(nextStateID);
									boolean executable = true;
									if(state.getStateType().equals(StateType.receive)){
										String[] str = state.getTransitions().get(0).getText().split("(1)");
										String text = str[0].trim();
										int num = subject.checkMessageNumberFromSubjectIDAndType(subject.getSubjectID(), text);
										if(num == 0){
											executable = false;
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
											MessageData.Builder msgBuilder = MessageData.newBuilder();
											String[] s = state.getTransitions().get(0).getText().split("(1)");
											String text1 = s[0].trim();
											int num = subject.checkMessageNumberFromSubjectIDAndType(subject.getSubjectID(), text1);
											if(num == 0){
												msgBuilder.setMessageContent("");
											}else{
												msgBuilder.setMessageContent(subject.getMessageFromSubjcetIDAndType(subject.getSubjectID(), text1));
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
