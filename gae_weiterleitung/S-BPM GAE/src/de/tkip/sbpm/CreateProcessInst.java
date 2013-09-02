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
import de.tkip.sbpm.proto.GAEexecution.Graph;
import de.tkip.sbpm.proto.GAEexecution.ListActions;
import de.tkip.sbpm.proto.GAEexecution.MessageData;
import de.tkip.sbpm.proto.GAEexecution.ProcessInstanceData;

public class CreateProcessInst extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Get ok.");
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		InputStream is = req.getInputStream();
		String url = req.getRequestURI();
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
					if (url.equals("/post") || url.equals("/post/")){
						pm.currentTransaction().begin();
						ProcessManager processManager = processManagerList.get(0);
//						System.out.println(processManager.processList.size());
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
//								System.out.println("msgID: " + msgID + " msg: " + msg);
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
									state.transitions.add(transition);
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
							pi.setDate(cp.getName());
							pi.setProcessData(process);
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
										actionBuilder.setUserID(7)
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
											if(!state.getTransitions().get(i).getRelatedSubject().equals("")){
												actionDataBuilder.setRelatedSubject(state.getTransitions().get(i).getRelatedSubject());
											}
											if(state.getStateType().equals(StateType.receive)){
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
//							System.out.println(pi.processInstanceID);
//							System.out.println(pi.processData.processName);
							int t = processManager.getProcessInstanceID() +1;
							processManager.setProcessInstanceID(t);
							
							ProcessInstanceData.Builder pidbuilder = ProcessInstanceData.newBuilder();
							pidbuilder.setId(processInstanceID)
									  .setName("travel")
									  .setProcessId(processID)
									  .setProcessName(processManager.getProcess(processID).getProcessName())
									  .setIsTerminated(false)
									  .setDate(date)
									  .setOwner(0)
									  .setHistory("");
							pidbuilder.setGraph(cp.getGraph());
							ProcessInstanceData pid = pidbuilder.build();
							resp.getOutputStream().write(pid.toByteArray());
				            resp.getOutputStream().flush();
				            resp.getOutputStream().close();
							pm.currentTransaction().commit();
						}else{
							System.out.println("no process");
							pm.currentTransaction().commit();
						}	
					}else {
						String[] urls = url.split("/");
						int piid = Integer.valueOf(urls[urls.length - 1]);
						pm.currentTransaction().begin();
						ProcessManager processManager = processManagerList.get(0);
						ListActions.Builder listActionsBuilder = ListActions.newBuilder();
						Iterator it = processManager.getAvailableActionsList().iterator();
						while(it.hasNext()){
							Action action = (Action) it.next();						
							if(action.getProcessInstanceID() == piid){
								listActionsBuilder.addActions(action);
							}			
						}
						ListActions listActions = listActionsBuilder.build();
						resp.getOutputStream().write(listActions.toByteArray());
			            resp.getOutputStream().flush();
			            resp.getOutputStream().close();
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
	}
}
