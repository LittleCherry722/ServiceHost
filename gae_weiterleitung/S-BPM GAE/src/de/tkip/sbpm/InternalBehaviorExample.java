package de.tkip.sbpm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.tkip.sbpm.State.StateType;
import de.tkip.sbpm.proto.GAEexecution.Action;
import de.tkip.sbpm.proto.GAEexecution.ActionData;

public class InternalBehaviorExample extends HttpServlet {
	private static Subject audiBestellung = new Subject();
	private static Subject staples = new Subject();
	private static String availableAction;
	private static boolean executable;
	
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		InputStream is = req.getInputStream();
		try {
			int size = req.getContentLength();
			byte[] byteProto = new byte[size];
			is.read(byteProto);
			Action action = Action.parseFrom(byteProto);
			// List<ActionData> actionDateList = action.getActionDataList();
			if (action.getStateID() == audiBestellung.internalBehavior.getCurrentState()&&action.getStateType().equals("action")) {
				int nextStateID = audiBestellung.internalBehavior.getStatesMap().get(action.getStateID()).getTransitions().get(0).successorID;
				audiBestellung.internalBehavior.nextState(nextStateID);
				State state = audiBestellung.internalBehavior.getStatesMap().get(nextStateID);
				executable = true;
				if(state.stateType.name().equals("receive")){
					executable = false;
				}
				availableAction = state.transitions.get(0).text;
			}else if(action.getStateID() == audiBestellung.internalBehavior.getCurrentState()&&action.getStateType().equals("send")){
				ActionData actionData = action.getActionData(0);
				String messageContent = actionData.getMessages(0).getMessageContent();
				SubjectToSubjectMessage msg = new SubjectToSubjectMessage();
				msg.setFrom_subjectID(0);
				msg.setMessageContent(messageContent);
				msg.setTarget_subjectID(1);
				msg.setMessageType("Bestellung");
				System.out.println(actionData.getText());
				System.out.println(messageContent);
				staples.addMessage(msg);
				int nextStateID = audiBestellung.internalBehavior.getStatesMap().get(action.getStateID()).transitions.get(0).successorID;
				audiBestellung.internalBehavior.nextState(nextStateID);		
				State state = audiBestellung.internalBehavior.getStatesMap().get(nextStateID);
				executable = true;
				if(state.stateType.name().equals("receive")){
					executable = false;
				}
				availableAction = state.transitions.get(0).text;
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		if(audiBestellung.getSubjectName() == null){
			staples.setSubjectID(1);
			staples.setSubjectName("staples");
			
			audiBestellung.setSubjectID(0);
			audiBestellung.setSubjectName("Bestellung");
			audiBestellung.internalBehavior.setSubjectID(0);

			State s0 = new State(0, "Bestellformular ausfuellen", StateType.action,true);
			s0.transitions.add(new Transition("Exit Condition", "Erledigt", 1));
			audiBestellung.internalBehavior.addState(s0);

			State s1 = new State(1, "Pruefe Bestellung", StateType.action, false);
			s1.transitions.add(new Transition("Exit Condition", "Akzeptiert", 2));
			audiBestellung.internalBehavior.addState(s1);

			State s2 = new State(2, "Send", StateType.send, false);
			s2.transitions.add(new Transition("Exit Condition", "Bestellung", 3));
			audiBestellung.internalBehavior.addState(s2);

			State s3 = new State(3, "Receice", StateType.receive, false);
			s3.transitions.add(new Transition("Exit Condition", "Lieferdatum", 4));
			audiBestellung.internalBehavior.addState(s3);

			State s4 = new State(4, "End", StateType.end, false);
			audiBestellung.internalBehavior.addState(s4);
			
			State state = audiBestellung.internalBehavior.getStatesMap().get(audiBestellung.internalBehavior.getStartState());
			executable = true;
			if(state.stateType.name().equals("receive")){
				executable = false;
			}
			availableAction = state.transitions.get(0).text;
			
			SubjectToSubjectMessage msg = new SubjectToSubjectMessage();
			msg.setFrom_subjectID(1);
			msg.setMessageContent("bestellung test1");
			msg.setTarget_subjectID(0);
			msg.setMessageType("Lieferdatum");
			audiBestellung.addMessage(msg);
			msg = new SubjectToSubjectMessage();
			msg.setFrom_subjectID(1);
			msg.setMessageContent("bestellung test2");
			msg.setTarget_subjectID(0);
			msg.setMessageType("Lieferdatum");
			audiBestellung.addMessage(msg);
		}
		resp.getWriter().println("Subject: " + audiBestellung.getSubjectName());
		Iterator it = audiBestellung.internalBehavior.getStatesMap().keySet()
				.iterator();
		while (it.hasNext()) {
			int s = (int) it.next();
			State state = audiBestellung.internalBehavior.getStatesMap().get(s);
			resp.getWriter().println("State type: " + state.stateType.toString());
			resp.getWriter().println("State name: " + state.text);
		}
		resp.getWriter().println("----------------------------------------");
		resp.getWriter().println("Current State: ");
		State currentState = audiBestellung.internalBehavior.getStatesMap()
				.get(audiBestellung.internalBehavior.getCurrentState());
		resp.getWriter().println(currentState.text.toString());
		resp.getWriter().println("Available Action: ");
		resp.getWriter().println(availableAction);
		resp.getWriter().println("Executable: ");
		resp.getWriter().println(executable);
		resp.getWriter().println("InputPool of Bestellung: " + audiBestellung.getMessageLimit());
		resp.getWriter().println("----------------------------------------");
		resp.getWriter().println("Subject: " + staples.getSubjectName());
		resp.getWriter().println("InputPool of Staples: " + staples.getMessageLimit());
		resp.getWriter().println("Message Type:  " + staples.getMessageTypeFromSubjcetID(0));
		resp.getWriter().println("Message: " + staples.getMessageFromSubjcetIDAndType(0, "Bestellung"));
	}
}
