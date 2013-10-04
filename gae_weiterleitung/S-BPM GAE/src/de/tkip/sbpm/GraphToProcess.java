package de.tkip.sbpm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tkip.sbpm.State.StateType;
import de.tkip.sbpm.proto.GAEexecution.Graph;

/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

public class GraphToProcess extends HttpServlet {
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
			Graph graph = Graph.parseFrom(byteProto);
			int processID = graph.getProcessId();
			int subjectNum = graph.getSubjectsCount();
			Process process = new Process();
			process.setProcessID(processID);
			process.setDate(graph.getDate());
			//miss process name now
			for(int i = 0; i < subjectNum; i++){
				Subject subject = new Subject();
				subject.setProcessID(processID);
				subject.setSubjectID(graph.getSubjects(i).getId());
				subject.setSubjectName(graph.getSubjects(i).getName());
				subject.setSubjectType(graph.getSubjects(i).getSubjectType());
				subject.setDisabled(graph.getSubjects(i).getIsDisabled());
				subject.setStartSubject(graph.getSubjects(i).getIsStartSubject());
				subject.getInputPool().setMessageLimit(graph.getSubjects(i).getInputPool());
				subject.getInternalBehavior().setSubjectID(graph.getSubjects(i).getId());
				int graphNodeNum = graph.getSubjects(i).getMacros(0).getNodesCount();
				for(int j = 0; j < graphNodeNum; j++){
					State state = new State();
					state.setId(graph.getSubjects(i).getMacros(0).getNodes(j).getId());
					state.setText(graph.getSubjects(i).getMacros(0).getNodes(j).getText());
					state.setStartState(graph.getSubjects(i).getMacros(0).getNodes(j).getIsStart());
					state.setEndState(graph.getSubjects(i).getMacros(0).getNodes(j).getIsEnd());
					state.setStateType(StateType.valueOf(graph.getSubjects(i).getMacros(0).getNodes(j).getNodeType()));
					state.setDisabled(graph.getSubjects(i).getMacros(0).getNodes(j).getIsDisabled());
					state.setMajorStart(graph.getSubjects(i).getMacros(0).getNodes(j).getIsMajorStartNode());
					subject.getInternalBehavior().addState(state);
				}
				int graphEdgeNum = graph.getSubjects(i).getMacros(0).getEdgesCount();
				for(int j = 0; j < graphEdgeNum; j++){
					int stateID = graph.getSubjects(i).getMacros(0).getEdges(j).getStartNodeId();
					State state = subject.getInternalBehavior().getStateByID(stateID);
					Transition transition = new Transition();
					transition.setText(graph.getSubjects(i).getMacros(0).getEdges(j).getText());
					transition.setTransitionType(graph.getSubjects(i).getMacros(0).getEdges(j).getEdgeType());
					transition.setSuccessorID(graph.getSubjects(i).getMacros(0).getEdges(j).getEndNodeId());
					transition.setDisabled(graph.getSubjects(i).getMacros(0).getEdges(j).getIsDisabled());
					transition.setOptional(graph.getSubjects(i).getMacros(0).getEdges(j).getIsOptional());
					transition.setManualTimeout(graph.getSubjects(i).getMacros(0).getEdges(j).getManualTimeout());
					transition.setPriority(graph.getSubjects(i).getMacros(0).getEdges(j).getPriority());
					state.transitions.add(transition);
				}
				process.addSubject(subject);
			}
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				pm.currentTransaction().begin();
				Query query = pm.newQuery(ProcessManager.class);
				List<ProcessManager> processManagerList = (List<ProcessManager>) query.execute();
				if (processManagerList.isEmpty()) {
					ProcessManager processManager = new ProcessManager();
					processManager.addProcess(process);
					pm.makePersistent(processManager);
				}else{
					ProcessManager processManager = processManagerList.get(0);
					processManager.addProcess(process);
					pm.makePersistent(processManager);
				}
				pm.currentTransaction().commit();
			} catch (Exception e) {
				pm.currentTransaction().rollback();
				e.printStackTrace();
			}finally{
				pm.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			
		}
	}
}
