package de.tkip.sbpm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

class SerialCloneable implements Cloneable, Serializable{
    static final long serialVersionUID = -8084210473720589252L;
  public Object clone(){
      try{
          //save the object to a byte array
          ByteArrayOutputStream bout = new ByteArrayOutputStream();
          ObjectOutputStream out = new ObjectOutputStream(bout);
          out.writeObject(this);
          out.close();
          
          //read a clone of the object from byte array
          ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
          ObjectInputStream in = new ObjectInputStream(bin);
          Object ret = in.readObject();
          in.close();
          
          return ret;
      }catch(Exception e){
          return null;
      }
  }
}
