package de.tkip.sbpm;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tkip.sbpm.State.StateType;
import de.tkip.sbpm.proto.GAEexecution.CreateProcessInstance;
import de.tkip.sbpm.proto.GAEexecution.Graph;
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
		try {
			int size = req.getContentLength();
			byte[] byteProto = new byte[size];
			is.read(byteProto);
			CreateProcessInstance cp = CreateProcessInstance.parseFrom(byteProto);
			int processID = cp.getProcessId();
			System.out.println("ProID:" + processID);
			ProcessInstance pi = new ProcessInstance();
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				Query query = pm.newQuery(ProcessManager.class);
				List<ProcessManager> processManagerList = (List<ProcessManager>) query.execute();
				if (processManagerList.isEmpty()) {
//					ProcessManager processManager = new ProcessManager();
//					pm.makePersistent(processManager);
					System.out.println("waiting for process manager initialization");
				}else{
					pm.currentTransaction().begin();
					ProcessManager processManager = processManagerList.get(0);
					System.out.println(processManager.processList.size());
					if(processManager.containsProcess(processID)){
						Process process = processManager.getProcess(processID);
						int processInstanceID = processManager.getProcessInstanceID();
						pi.setProcessInstanceID(processInstanceID) ;
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
								processManager.addAvailableActions(state, executable);
								System.out.println(executable);
							}
						}
						processManager.addProcessInstance(pi);
						System.out.println(pi.processInstanceID);
						System.out.println(pi.processData.processName);
//						JDOHelper.makeDirty(processManager, "processInstanceList");
						int t = processManager.getProcessInstanceID() +1;
						processManager.setProcessInstanceID(t);
//						pm.makePersistent(processManager);
						Date dt=new Date();
						SimpleDateFormat matter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						ProcessInstanceData.Builder pidbuilder = ProcessInstanceData.newBuilder();
						pidbuilder.setId(processInstanceID)
								  .setName("travel")
								  .setProcessId(processID)
								  .setProcessName(processManager.getProcess(processID).getProcessName())
								  .setIsTerminated(false)
								  .setDate(matter.format(dt))
								  .setOwner(0)
								  .setHistory("");
						Graph.Builder graphbuilder = Graph.newBuilder();
						graphbuilder.setDate(matter.format(dt));
						Graph graph = graphbuilder.build();
						pidbuilder.setGraph(graph);
						ProcessInstanceData pid = pidbuilder.build();
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
				System.out.println("finally");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}
