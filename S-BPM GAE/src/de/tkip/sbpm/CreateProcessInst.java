package de.tkip.sbpm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

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
//			System.out.println(byteProto.toString());
			ProcessInstanceData pi = ProcessInstanceData.parseFrom(byteProto);
			System.out.println(pi.getId());
			System.out.println(pi.getProcessId());
			System.out.println(pi.getGraph());
			System.out.println(pi.getIsTerminated());
			System.out.println(pi.getHistory());
			ProcessInstanceWrapper processInstance = new ProcessInstanceWrapper(pi);
			Key key = KeyFactory.createKey(ProcessInstanceWrapper.class.getSimpleName(), pi.getId());
			processInstance.setKey(key);
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				pm.makePersistent(processInstance);
				resp.getWriter().println("ProcessInstance created.");
			} catch(Exception e){
				System.out.println(e.toString());
			}finally {
				pm.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
		resp.getWriter().println("Post ok.");
	}
}
