package de.tkip.test;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

import de.tkip.sbpm.Process;
import de.tkip.sbpm.ProcessInstance;
import de.tkip.sbpm.Subject;

@PersistenceCapable
public class PMFTest {
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
	
	@Persistent
	public String test = "ttt";
	@Persistent
	public int num;
	@Persistent(serialized = "true")
	public SerializationClassTest sct;
//	@Persistent(serialized = "true")
//	public List<Process> processList = new ArrayList<Process>();
//	@Persistent(serialized = "true")
//	public List<ProcessInstance> processInstanceList = new ArrayList<ProcessInstance>();
	
	public PMFTest(){
		num = 10000;
		Process p = new Process();
		p.processID = 0;
		Subject s = new Subject();
		s.processID = 20;
		s.processInstanceID = 20;
		s.subjectID = 20;
		p.addSubject(0, s);
		sct = new SerializationClassTest();
//		processList.add(p);
		ProcessInstance pi = new ProcessInstance();
		pi.processInstanceID = 1;
		pi.processData = p;
//		processInstanceList.add(pi);
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public Key getKey() {
		return key;
	}
	public void setKey(Key key) {
		this.key = key;
	}
}
