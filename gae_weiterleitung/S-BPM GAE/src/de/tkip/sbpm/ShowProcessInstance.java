package de.tkip.sbpm;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import com.google.appengine.api.datastore.KeyFactory;

import de.tkip.sbpm.proto.GAEexecution.Action;
import de.tkip.sbpm.proto.GAEexecution.ActionData;
import de.tkip.sbpm.proto.GAEexecution.ListActions;
import de.tkip.sbpm.proto.GAEexecution.ListProcesses;
import de.tkip.sbpm.proto.GAEexecution.ProcessInstanceData;
import de.tkip.sbpm.proto.GAEexecution.ListProcesses.ProcessInfo;

public class ShowProcessInstance extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(ProcessManager.class);
			List<ProcessManager> processManagerList = (List<ProcessManager>) query
					.execute();
			String url = req.getRequestURI();
			System.out.println("get:");
			if (url.equals("/get") || url.equals("/get/")) {
				if (processManagerList.isEmpty()) {
					System.out.println("Try again later.");
				} else {
					ProcessManager processManager = processManagerList.get(0);
					if (processManager.getProcessInstanceList().isEmpty()) {
						System.out.println("There is no process instance.");
					} else {
						ListProcesses.Builder listProcessesBuilder = ListProcesses.newBuilder();
						Iterator it = processManager.getProcessInstanceList().iterator();
						while (it.hasNext()) {
							ProcessInstance pi = (ProcessInstance)it.next();
							int id = pi.getProcessInstanceID();
							String name = pi.getProcessData().getProcessName();
							System.out.println("process id: " + id + "   process name: " + name);
							ProcessInfo.Builder processInfoBuilder = ProcessInfo.newBuilder();
							processInfoBuilder.setId(pi.getProcessInstanceID())
											  .setProcessId(pi.getProcessData().getProcessID())
											  .setName(name);
							ProcessInfo processInfo = processInfoBuilder.build();
							listProcessesBuilder.addProcesses(processInfo);
						}
						ListProcesses listProcesses = listProcessesBuilder.build();
						resp.getOutputStream().write(listProcesses.toByteArray());
				        resp.getOutputStream().flush();
				        resp.getOutputStream().close();
					}
				}
			} else if (url.equals("/get/action") || url.equals("/get/action/")) {
				ProcessManager processManager = processManagerList.get(0);
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
			} else {
				String[] urls = url.split("/");
				int id = Integer.valueOf(urls[urls.length - 1]);
				ProcessManager processManager = processManagerList.get(0);
				if (processManager.getProcessInstanceList().isEmpty()) {
					System.out.println("There is no process instance.");
				} else {
					if (processManager.containsProcessInstance(id)) {
						ProcessInstance pi = processManager
								.getProcessInstance(id);
						String name = pi.getProcessData().getProcessName();
						System.out.println(
								"process id: " + id + "   process name: "
										+ name);
						System.out.println();
						ProcessInstanceData.Builder pidbuilder = ProcessInstanceData.newBuilder();
						pidbuilder.setId(id)
								  .setName(name)
								  .setProcessId(pi.getProcessData().getProcessID())
								  .setProcessName(pi.getProcessData().getProcessName())
								  .setIsTerminated(pi.isTerminated())
								  .setDate(pi.getProcessData().date)
								  .setOwner(0)
								  .setHistory("")
								  .setGraph(processManager.getGraph(pi.getProcessData().getProcessID()));
						ProcessInstanceData pid = pidbuilder.build();
						resp.getOutputStream().write(pid.toByteArray());
			            resp.getOutputStream().flush();
			            resp.getOutputStream().close();
					} else {
						System.out.println("There is no process instance.");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pm.close();
		}
	}
}
