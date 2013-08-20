package de.tkip.test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import de.tkip.sbpm.PMF;

public class PMFTestServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Key key = KeyFactory.createKey(PMFTest.class.getSimpleName(), "Unique");	
		try {
			pm.currentTransaction().begin();
			PMFTest pmftest = pm.getObjectById(PMFTest.class, key);
			if (pmftest == null) {
				PMFTest p = new PMFTest();
				p.setKey(key);
				pm.makePersistent(p);
				pm.currentTransaction().commit();
				resp.getWriter().println("empty");
			} else {
//				for (PMFTest pt : pmftest) {
					resp.getWriter().println(pmftest.num);
//					pmftest.setNum(pmftest.num+1);
					pmftest.num++;
					resp.getWriter().println(pmftest.test);
					resp.getWriter().println(pmftest.getSct().getNum());
					int t = pmftest.getSct().getNum()+1;
					pmftest.sct.setNum(t);
//					SerializationClassTest sct = new SerializationClassTest();
//					sct = pmftest.getSct();
//					sct.setNum(sct.num+1);
//					System.out.println(sct.getNum());
//					pmftest.setSct(sct);
//					JDOHelper.makeDirty(pmftest.sct, "num");
//					JDOHelper.makeDirty(pmftest, "sct");
					resp.getWriter().println(pmftest.sct.name);
					resp.getWriter().println(pmftest.sct.nn.get(0));
					resp.getWriter().println();
//					for(Field f : pmftest.getClass().getDeclaredFields()){
//						resp.getWriter().println(f.getName());
//					}					
//					resp.getWriter().println(pt.processList.get(0).processID);
//					pt.processList.get(0).processID += 1;
					resp.getWriter().println();				
					pm.makePersistent(pmftest);
					pm.currentTransaction().commit();
//				}
			}
//			pm.deletePersistentAll(pmftest);
		} catch (JDOObjectNotFoundException e) {
			PMFTest p = new PMFTest();
			p.setKey(key);
			pm.makePersistent(p);
			pm.currentTransaction().commit();
			resp.getWriter().println("empty");
		}catch(Exception e){
			pm.currentTransaction().rollback();
			e.printStackTrace();
		}finally {
			pm.close();
		}
	}
}
