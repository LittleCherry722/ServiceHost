package de.tkip.sbpm;

import java.io.IOException;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.*;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import de.tkip.sbpm.proto.GAEexecution.ListProcesses;

@SuppressWarnings("serial")
public class S_BPM_GAEServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		String query = "select from " + ProcessInstanceWrapper.class.getName();
		List<ProcessInstanceWrapper> processInstances = (List<ProcessInstanceWrapper>) pm
				.newQuery(query).execute();
		if (processInstances.isEmpty()) {
			resp.setContentType("text/plain");
			resp.getWriter().println("There is no ProcessInstance.");
		} else {
			for (ProcessInstanceWrapper p : processInstances) {
				resp.setContentType("text/plain");
				resp.getWriter().println(
						"id: " + p.getProcessInstance().getId());
				resp.getWriter().println(
						"processid: " + p.getProcessInstance().getProcessId());
				resp.getWriter().println(
						"graph: " + p.getProcessInstance().getGraph());
				resp.getWriter().println(
						"isterminated "
								+ p.getProcessInstance().getIsTerminated());
				resp.getWriter().println(
						"history " + p.getProcessInstance().getHistory());
				resp.getWriter().println(
						"actionscount " + p.getProcessInstance().getActionsCount());
			}
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Key key = null;
		String execution = req.getParameter("execution");
		if (execution != null) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			switch (execution) {
			case "cp":
				int id = Integer.parseInt(req.getParameter("id"));
				int processID = Integer.parseInt(req.getParameter("processid"));
				String graph = req.getParameter("graph");
				boolean isTerminated;
				if (req.getParameter("isterminated").equals("y")) {
					isTerminated = true;
				} else {
					isTerminated = false;
				}
				String history = req.getParameter("history");
				ProcessInstanceWrapper processInstance = new ProcessInstanceWrapper(
						id, processID, graph, isTerminated, history);
				key = KeyFactory.createKey(ProcessInstanceWrapper.class.getSimpleName(), id);
				processInstance.setKey(key);
				try {
					pm.makePersistent(processInstance);
					resp.setContentType("text/plain");
					resp.getWriter().println("ProcessInstance created.");
				} finally {
					pm.close();
				}
				break;
			case "ca":
				int userID = Integer.parseInt(req.getParameter("userid"));
				int processInstanceID = Integer.parseInt(req
						.getParameter("processinstanceid"));
				int subjectID = Integer.parseInt(req.getParameter("subjectid"));
				int stateID = Integer.parseInt(req.getParameter("stateid"));
				String stateText = req.getParameter("statetext");
				String stateType = req.getParameter("statetype");
				ActionWrapper action = new ActionWrapper(userID,
						processInstanceID, subjectID, stateID, stateText,
						stateType);
				try {
					key = KeyFactory.createKey(ProcessInstanceWrapper.class.getSimpleName(), processInstanceID);
					ProcessInstanceWrapper processInstance1 = pm.getObjectById(ProcessInstanceWrapper.class,key);
					processInstance1.addAction(action.getAction());
					pm.makePersistent(action);
					resp.setContentType("text/plain");
					resp.getWriter().println("action created.");
				} catch(JDOObjectNotFoundException e){
					resp.setContentType("text/plain");
					resp.getWriter().println("The processinstance not exist. Try again.");
				}finally {
					pm.close();
				}
			}
		}
	}

	public void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

	}
}
