package de.tkip.test;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tkip.sbpm.PMF;
import de.tkip.sbpm.ProcessManager;

public class PMFTestDelete extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		String query = "select from " + PMFTest.class.getName();
		List<PMFTest> pmftest = (List<PMFTest>) pm.newQuery(query).execute();
		pm.deletePersistentAll(pmftest);
		Query query1 = pm.newQuery(ProcessManager.class);
		List<ProcessManager> processManagerList = (List<ProcessManager>) query1.execute();
		pm.deletePersistentAll(processManagerList);
		resp.getWriter().println("ok");
	}
}
