package de.tkip.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tkip.sbpm.Subject;
import de.tkip.sbpm.SubjectToSubjectMessage;

public class SubjectTest extends HttpServlet {
	// public void doPost(HttpServletRequest req, HttpServletResponse resp)
	// throws IOException {}
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		int subjectNumber = 10;
		Map<Integer,Subject> subjects = new HashMap<Integer,Subject>();
		for (int i = 0; i < subjectNumber; i++) {
			Subject s = new Subject();
			s.setSubjectID(i);
			subjects.put(i,s);
		}
		Random r = new Random();
		for (int i = 0; i < 100; i++) {
			int from = r.nextInt(subjectNumber);
			int to = r.nextInt(subjectNumber);
			while (from == to) {
				to = r.nextInt(subjectNumber);
			}
			SubjectToSubjectMessage msg = new SubjectToSubjectMessage();
			msg.setFrom_subjectID(from);
			msg.setTarget_subjectID(to);
			msg.setMessageContent("This message is from ID" + from + " to ID"
					+ to);
			Subject s = subjects.get(to);
			
			s.addMessage(msg);
			subjects.put(to, s);
		}
		Iterator it = subjects.values().iterator();
		while (it.hasNext()) {
			Subject s = (Subject) it.next();
			resp.getWriter().println(
					"Subject " + s.getSubjectID() + " : "
							+ s.getMessageNumber() + " messages");
		}
		resp.getWriter().println();
		resp.getWriter().println();
		it = subjects.values().iterator();
		while(it.hasNext()){
			Subject s = (Subject) it.next();
			resp.getWriter().println("Subject " + s.getSubjectID() + "'s messages:");
			resp.getWriter().println();
		}
	}
}
