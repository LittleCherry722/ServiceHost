package de.tkip.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SerializationClassTest implements Serializable {
	public String name;
	public int num;
	public List<String> nn = new ArrayList<String>();
	
	public SerializationClassTest(){
		name = "test";
		num = 10000;
		nn.add("1");
		nn.add("2");
	}

	public SerializationClassTest(int num){
		name = "test";
		this.num = num;
		nn.add("1");
		nn.add("2");
	}
	
	public void setNum(int num) {
		this.num = num;
	}

	public int getNum() {
		return num;
	}
}
