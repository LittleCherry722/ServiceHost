package de.tkip.sbpm;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tkip.test.ActionWrapper;
import de.tkip.test.PMFTest;
import de.tkip.test.ProcessInstanceWrapper;

public class PMFDelete extends HttpServlet{
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(ProcessManager.class);
		List<ProcessManager> processManagerList = (List<ProcessManager>) query.execute();
		pm.deletePersistentAll(processManagerList);
		resp.getWriter().println("ok");
	}
}
