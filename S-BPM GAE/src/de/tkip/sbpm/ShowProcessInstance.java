package de.tkip.sbpm;

import java.io.IOException;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import com.google.appengine.api.datastore.KeyFactory;

public class ShowProcessInstance extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<ProcessInstanceWrapper> processInstances = null;
		String url = req.getRequestURI();
		if (url.equals("/get") || url.equals("/get/")) {
			Query query = pm.newQuery(ProcessInstanceWrapper.class);
			processInstances = (List<ProcessInstanceWrapper>) query.execute();
			if (processInstances.isEmpty()) {
				resp.getWriter().println("There is no ProcessInstance.");
			} else {
				for (ProcessInstanceWrapper p : processInstances) {
					resp.getWriter().println(
							"id: " + p.getProcessInstance().getId());
					resp.getWriter().println(
							"processid: "
									+ p.getProcessInstance().getProcessId());
					resp.getWriter().println(
							"graph: " + p.getProcessInstance().getGraph());
					resp.getWriter().println(
							"isterminated "
									+ p.getProcessInstance().getIsTerminated());
					resp.getWriter().println(
							"history " + p.getProcessInstance().getHistory());
					resp.getWriter().println(
							"actionscount "
									+ p.getProcessInstance().getActionsCount());
					resp.getWriter().println();
				}
			}
		} else {
			String[] urls = url.split("/");
			int id = Integer.valueOf(urls[urls.length - 1]);
			Key key = KeyFactory.createKey(
					ProcessInstanceWrapper.class.getSimpleName(), id);
			try {
				ProcessInstanceWrapper p = pm.getObjectById(
						ProcessInstanceWrapper.class, key);
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
						"actionscount "
								+ p.getProcessInstance().getActionsCount());
			} catch (JDOObjectNotFoundException e) {
				resp.getWriter()
						.println(
								"The process instance of id: " + id
										+ " does not exist");
			}

		}

	}
}
