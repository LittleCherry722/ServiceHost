package de.tkip.sbpm;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ShowActions extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		PersistenceManager pm = PMF.get().getPersistenceManager();
		String query = "select from " + ActionWrapper.class.getName();
		List<ActionWrapper> actions = (List<ActionWrapper>) pm
				.newQuery(query).execute();
		if (actions.isEmpty()) {
			resp.getWriter().println("There is no actions.");
		} else {
			for (ActionWrapper a : actions) {
				resp.getWriter().println(
						"userid: " + a.getAction().getUserID());
				resp.getWriter().println(
						"processinstanceid: " + a.getAction().getProcessInstanceID());
				resp.getWriter().println(
						"sujectid: " + a.getAction().getSubjectID());
				resp.getWriter().println(
						"stateid " + a.getAction().getStateID());
				resp.getWriter().println(
						"statetext " + a.getAction().getStateText());
				resp.getWriter().println(
						"statetype " + a.getAction().getStateType());
			}
		}
	}
}
