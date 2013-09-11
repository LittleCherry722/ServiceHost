package de.tkip.test;
//package de.tkip.sbpm;
//
//import java.io.IOException;
//import java.util.List;
//
//import javax.jdo.JDOHelper;
//import javax.jdo.PersistenceManager;
//import javax.jdo.Query;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import de.tkip.sbpm.State.StateType;
//
//public class TravalRequestProcess extends HttpServlet{
//	Subject applicant = new Subject();
//	Subject supervisor = new Subject();
//	Subject administration = new Subject();
//	private String tt = "Exit Condition";
//	
//	public void doGet(HttpServletRequest req, HttpServletResponse resp)
//			throws IOException {
//		resp.setContentType("text/plain");
//		Process pi = new Process();
//		pi.processID = 4;
//		pi.processName = "Travel Request";
//		initApplicant();
//		initSupervisor();
//		initAdministration();
//		pi.subjects.put(applicant.getSubjectID(), applicant);
//		pi.subjects.put(supervisor.getSubjectID(), supervisor);
//		pi.subjects.put(administration.getSubjectID(),administration);
//		PersistenceManager pm = PMF.get().getPersistenceManager();
//		try{
//			pm.currentTransaction().begin();
//			Query query = pm.newQuery(ProcessManager.class);
//			List<ProcessManager> processManagerList = (List<ProcessManager>) query.execute();
//			if (processManagerList.isEmpty()) {
//				ProcessManager processManager = new ProcessManager();
//				processManager.addProcess(pi);
//				JDOHelper.makeDirty(processManager, processManager.getClass().getDeclaredFields()[2].getName());
//				pm.makePersistent(processManager);
//			}else{
//				ProcessManager processManager = processManagerList.get(0);
//				processManager.addProcess(pi);
//				JDOHelper.makeDirty(processManager, processManager.getClass().getDeclaredFields()[2].getName());
//				pm.makePersistent(processManager);
//			}
//			pm.currentTransaction().commit();
//			resp.getWriter().println("process created");
//		}catch(Exception e){
//			e.printStackTrace();
//			pm.currentTransaction().rollback();
//		}finally{
//			pm.close();
//		}
//	}
//
//	private void initApplicant(){
//		applicant.setProcessID(0);
//		applicant.setSubjectID("0");
//		applicant.setSubjectName("Applicant");
//		applicant.internalBehavior.setSubjectID("0");
//		
//		State s0 = new State(0,"Prepare Travel Applicantion",StateType.action,true);
//		s0.setSubjectID("0");
//		s0.transitions.add(new Transition(tt,"Done",1));
//		applicant.internalBehavior.addState(s0);
//		
//		State s1 = new State(1,"Send",StateType.send,false);
//		s1.setSubjectID("0");
//		s1.transitions.add(new Transition(tt,"Travel Applicantion to: Superviosr",2));
//		applicant.internalBehavior.addState(s1);
//		
//		State s2 = new State(2,"Receive",StateType.receive,false);
//		s2.setSubjectID("0");
//		s2.transitions.add(new Transition(tt,"Permission denied(1) from: Supervisor",3));
//		s2.transitions.add(new Transition(tt,"Permission granted(1) from: Supervisor",4));
//		applicant.internalBehavior.addState(s2);
//		
//		State s3 = new State(3,"Decide whether filing again",StateType.action,false);
//		s3.setSubjectID("0");
//		s3.transitions.add(new Transition(tt,"Redo Travel Application",0));
//		s3.transitions.add(new Transition(tt,"Denial accepted",5));
//		applicant.internalBehavior.addState(s3);
//		
//		State s4 = new State(4,"Make travel",StateType.action,false);
//		s4.setSubjectID("0");
//		s4.transitions.add(new Transition(tt,"",6));
//		applicant.internalBehavior.addState(s4);
//		
//		State s5 = new State(5,"Send",StateType.send,false);
//		s5.setSubjectID("0");
//		s5.transitions.add(new Transition(tt,"No further Travel Application to: Supervisor",6));
//		applicant.internalBehavior.addState(s5);
//		
//		State s6 = new State(6,"End",StateType.end,false);
//		s6.setSubjectID("0");
//		applicant.internalBehavior.addState(s6);
//	}
//	
//	private void initSupervisor(){
//		supervisor.setProcessID(0);
//		supervisor.setSubjectID("1");
//		supervisor.setSubjectName("Supervisor");
//		supervisor.internalBehavior.setSubjectID("1");
//		
//		State s0 = new State(0,"Receive",StateType.receive,true);
//		s0.setSubjectID("1");
//		s0.transitions.add(new Transition(tt,"Travel Applicantion(1) from: Applicant",1));
//		supervisor.internalBehavior.addState(s0);
//		
//		State s1 = new State(1,"Check Travel Application",StateType.action,false);
//		s1.setSubjectID("1");
//		s1.transitions.add(new Transition(tt,"Grant Permission",2));
//		s1.transitions.add(new Transition(tt,"Deny Permission",3));
//		supervisor.internalBehavior.addState(s1);
//		
//		State s2 = new State(2,"Send",StateType.send,false);
//		s2.setSubjectID("1");
//		s2.transitions.add(new Transition(tt,"Permission granted to: Applicant",4));
//		supervisor.internalBehavior.addState(s2);
//		
//		State s3 = new State(3,"Send",StateType.send,false);
//		s3.setSubjectID("1");
//		s3.transitions.add(new Transition(tt,"Permission denied to: Applicant",5));
//		supervisor.internalBehavior.addState(s3);
//		
//		State s4 = new State(4,"Send",StateType.send,false);
//		s4.setSubjectID("1");
//		s4.transitions.add(new Transition(tt,"Approved Travel Application to: Administration",6));
//		supervisor.internalBehavior.addState(s4);
//		
//		State s5 = new State(5,"Receive",StateType.receive,false);
//		s5.setSubjectID("1");
//		s5.transitions.add(new Transition(tt,"Travel Application(1) from: Applicant",1));
//		s5.transitions.add(new Transition(tt,"No further Travel Application(1) from: Applicant",6));
//		supervisor.internalBehavior.addState(s5);
//		
//		State s6 = new State(6,"End",StateType.end,false);
//		s6.setSubjectID("1");
//		supervisor.internalBehavior.addState(s6);
//	}
//	
//	private void initAdministration(){
//		administration.setProcessID(0);
//		administration.setSubjectID("2");
//		administration.setSubjectName("Administration");
//		administration.internalBehavior.setSubjectID("2");
//		
//		State s0 = new State(0,"Receive",StateType.receive,true);
//		s0.setSubjectID("2");
//		s0.transitions.add(new Transition(tt,"Approved Travel Application(1) from: Supervisor",1));
//		administration.internalBehavior.addState(s0);
//		
//		State s1 = new State(1,"Handle Travel Application",StateType.action,false);
//		s1.setSubjectID("2");
//		s1.transitions.add(new Transition(tt,"Travel Application filed",2));
//		administration.internalBehavior.addState(s1);
//		
//		State s2 = new State(2,"End",StateType.action,false);
//		s2.setSubjectID("2");
//		administration.internalBehavior.addState(s2);
//	}
//}
