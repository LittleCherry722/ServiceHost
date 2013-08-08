package de.tkip.sbpm;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PMFDelete extends HttpServlet{
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		String query = "select from " + ProcessInstanceWrapper.class.getName();
		List<ProcessInstanceWrapper> processInstances = (List<ProcessInstanceWrapper>) pm.newQuery(query).execute();
		for(ProcessInstanceWrapper p : processInstances){		
			pm.deletePersistent(p);			
		}
		pm.deletePersistentAll(processInstances);
		query = "select from " + ActionWrapper.class.getName();
		List<ActionWrapper> actions = (List<ActionWrapper>) pm.newQuery(query).execute();
		pm.deletePersistentAll(actions);
		pm.close();
		resp.setContentType("text/plain");
    	resp.getWriter().println("All Deleted.");
	}
}
