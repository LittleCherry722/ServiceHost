package de.tkip.sbpm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
