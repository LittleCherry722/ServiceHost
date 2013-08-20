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

import de.tkip.sbpm.proto.GAEexecution.ListProcesses;
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
											  .setProcessId(pi.getProcessData().getProcessID());
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
				Map<State, Boolean> availbleActions = processManager.getAvailbleActions();
				Iterator it = availbleActions.keySet().iterator();
				while (it.hasNext()) {
					State state = (State) it.next();
					boolean executable = availbleActions.get(state);
					System.out.println(
							"Process Instance ID: " + state.processInstanceID
									+ "   State: " + state.text + "   "
									+ executable);
					Iterator it1 = state.getTransitions().iterator();
					while (it1.hasNext()) {
						Transition transition = (Transition) it1.next();
						System.out.println(transition.text);
					}
					System.out.println(
							"-------------------------------------------");
				}
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
						for (int i = 0; i < pi.getProcessData().getSubjects().size(); i++) {
							System.out.println("Subject " + i + ": " + pi.getProcessData().getSubjects().get(i).getSubjectName());
							System.out.println("current state: " + pi.getProcessData().getSubjects().get(i).getInternalBehavior().getCurrentState());
							Iterator it1 = pi.getProcessData().getSubjects().get(i).getInternalBehavior().getStatesMap()
									.get(pi.getProcessData().getSubjects().get(i).getInternalBehavior().getCurrentState()).getTransitions()
									.iterator();
							System.out.println("available actions: ");
							while (it1.hasNext()) {
								Transition transition = (Transition) it1.next();
								System.out.println("        " + transition.text);
							}
							System.out.println("----------------------------------------------------------------");
						}

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
