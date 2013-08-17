package de.tkip.sbpm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import de.tkip.sbpm.State.StateType;
import de.tkip.sbpm.proto.GAEexecution.CreateProcessInstance;
import de.tkip.sbpm.proto.GAEexecution.ProcessInstanceData;

public class CreateProcessInst extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Get ok.");
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		InputStream is = req.getInputStream();
		try {
			int size = req.getContentLength();
			byte[] byteProto = new byte[size];
			is.read(byteProto);
			CreateProcessInstance cp = CreateProcessInstance.parseFrom(byteProto);
			int processID = cp.getProcessId();
			ProcessInstance pi = new ProcessInstance();
//			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
//				Query query = pm.newQuery(ProcessManager.class);
//				List<ProcessManager> processManagerList = (List<ProcessManager>) query.execute();
//				if (processManagerList.isEmpty()) {
//					ProcessManager processManager = new ProcessManager();
//					pm.makePersistent(processManager);
//					System.out.println("waiting for process manager initialization");
//					resp.getWriter().println("try again");
//				}else{
//					pm.currentTransaction().begin();
//					ProcessManager processManager = processManagerList.get(0);
					ProcessManager processManager = new ProcessManager();
					System.out.println(processManager.processList.size());
					if(processManager.containsProcess(processID)){
						Process process = processManager.getProcess(processID);
						int processInstanceID = processManager.processInstanceID;
						pi.processInstanceID = processInstanceID;
						pi.setProcessData(process);
						if(!pi.processData.subjects.isEmpty()){
							Iterator it = pi.processData.subjects.keySet().iterator();
							while(it.hasNext()){
								int id = (int) it.next();
								Subject sub = pi.processData.subjects.get(id);
								sub.internalBehavior.setProcessInstanceIDofStates(processInstanceID);
								State state = sub.internalBehavior.statesMap.get(sub.internalBehavior.getStartState());
								boolean executable = true;
								if(state.stateType.equals(StateType.receive)){
									String[] s = state.transitions.get(0).text.split("(1)");
									String text = s[0].trim();
									int num = sub.checkMessageNumberFromSubjectIDAndType(sub.subjectID, text);
									if(num == 0){
										executable = false;
									}		
								}
								sub.internalBehavior.setExecutable(executable);
								processManager.addAvailbleActions(state, executable);
								System.out.println(executable);
							}
						}
						processManager.addProcessInstance(pi);
						System.out.println(pi.processInstanceID);
						System.out.println(pi.processData.processName);
//						JDOHelper.makeDirty(processManager, "processInstanceList");
						processManager.processInstanceID += 1;
//						pm.makePersistent(processManager);
//						pm.currentTransaction().commit();
					}else{
						System.out.println("no process");
						resp.getWriter().println("no process");
//						pm.currentTransaction().commit();
					}	
//				}
			} catch(Exception e){
				e.printStackTrace();
//				pm.currentTransaction().rollback();
			}finally {
//				pm.close();
				System.out.println("finally");
			}
		} catch (Exception e) {
			resp.getWriter().println("Post Error");
			e.printStackTrace();
		}	
	}
}
