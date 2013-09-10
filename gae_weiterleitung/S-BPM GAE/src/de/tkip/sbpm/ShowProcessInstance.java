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
			if (url.equals("/get")) {
				if (processManagerList.isEmpty()) {
					ProcessManager processManager = new ProcessManager();
					pm.makePersistent(processManager);
					ListProcesses.Builder listProcessesBuilder = ListProcesses.newBuilder();
					ListProcesses listProcesses = listProcessesBuilder.build();
					resp.getOutputStream().write(listProcesses.toByteArray());
			        resp.getOutputStream().flush();
			        resp.getOutputStream().close();
					System.out.println("Try again later.");
				} else {
					ProcessManager processManager = processManagerList.get(0);
					if (processManager.getProcessInstanceList().isEmpty()) {
						System.out.println("There is no process instance.");
					} else {
						ListProcesses.Builder listProcessesBuilder = ListProcesses.newBuilder();
						Iterator it = processManager.getProcessInstanceList().iterator();
//						System.out.println("processInstanceList:");
						while (it.hasNext()) {
							ProcessInstance pi = (ProcessInstance)it.next();
							int id = pi.getProcessInstanceID();
							String name = pi.getName();
//							System.out.println("process id: " + id + "   process name: " + name);
							ProcessInfo.Builder processInfoBuilder = ProcessInfo.newBuilder();
							processInfoBuilder.setId(pi.getProcessInstanceID())
											  .setProcessId(pi.getProcessData().getProcessID())
											  .setName(name);
							ProcessInfo processInfo = processInfoBuilder.build();
							listProcessesBuilder.addProcesses(processInfo);
//							System.out.println("processInstance:" + id);
						}
						ListProcesses listProcesses = listProcessesBuilder.build();
						resp.getOutputStream().write(listProcesses.toByteArray());
				        resp.getOutputStream().flush();
				        resp.getOutputStream().close();
					}
				}
			} else if (url.equals("/get/action") || url.equals("/get/action/")) {
				if (processManagerList.isEmpty()) {
					ProcessManager processManager = new ProcessManager();
					pm.makePersistent(processManager);
					ListActions.Builder listActionsBuilder = ListActions.newBuilder();
					ListActions listActions = listActionsBuilder.build();
					resp.getOutputStream().write(listActions.toByteArray());
			        resp.getOutputStream().flush();
			        resp.getOutputStream().close();
					System.out.println("Try again later.");
				} else {
					ProcessManager processManager = processManagerList.get(0);
					ListActions.Builder listActionsBuilder = ListActions.newBuilder();
					Iterator it = processManager.getAvailableActionsList().iterator();
//					System.out.println("Action number: " + processManager.getAvailableActionsList().size());
					while(it.hasNext()){
						Action action = (Action) it.next();
						listActionsBuilder.addActions(action);
					}
					ListActions listActions = listActionsBuilder.build();
					resp.getOutputStream().write(listActions.toByteArray());
		            resp.getOutputStream().flush();
		            resp.getOutputStream().close();
				}
			} else {
				if (processManagerList.isEmpty()) {
					ProcessManager processManager = new ProcessManager();
					pm.makePersistent(processManager);
//					ListActions.Builder listActionsBuilder = ListActions.newBuilder();
//					ListActions listActions = listActionsBuilder.build();
//					resp.getOutputStream().write(listActions.toByteArray());
//			        resp.getOutputStream().flush();
//			        resp.getOutputStream().close();
					System.out.println("Try again later.");
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
							System.out.println(pi.getProcessData().getProcessID());
							pidbuilder.setId(id)
									  .setName(name)
									  .setProcessId(pi.getProcessData().getProcessID())
									  .setProcessName(pi.getProcessData().getProcessName())
									  .setIsTerminated(pi.isTerminated())
									  .setDate(pi.getProcessData().date)
									  .setOwner(0)
									  .setHistory("")
									  .setGraph(processManager.getGraphFromProcessID(pi.getProcessData().getProcessID()));
							Iterator it = processManager.getAvailableActionsList().iterator();
							while(it.hasNext()){
								Action action = (Action) it.next();
								if(action.getProcessInstanceID() == id){
									pidbuilder.addActions(action);
								}
							}
							ProcessInstanceData pid = pidbuilder.build();
							resp.getOutputStream().write(pid.toByteArray());
				            resp.getOutputStream().flush();
				            resp.getOutputStream().close();
						} else {
							System.out.println("There is no process instance.");
						}
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
