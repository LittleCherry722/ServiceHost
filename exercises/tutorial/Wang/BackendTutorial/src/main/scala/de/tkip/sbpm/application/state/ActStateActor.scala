package de.tkip.sbpm.application.state

import de.tkip.sbpm.model.State
import de.tkip.sbpm.application._

class ActStateActor(s: State) extends AbstractBeviorStateActor(s) {
  def receive = {
    case ExecuteAction(_, succ) if (s.transitions contains succ) => {
      // FIXME We dont always want to go the first transition
      // change the program: go to the state given in succ
      for(elem <- s.transitions){
        if(elem == succ){
          context.parent ! ChangeState(elem)
        }
      }
//      context.parent ! ChangeState(s.transitions(0))
    }
  }
}
