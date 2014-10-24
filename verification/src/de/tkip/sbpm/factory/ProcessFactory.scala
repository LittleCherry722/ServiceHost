package de.tkip.sbpm.factory

import de.tkip.sbpm.newmodel._
import de.tkip.sbpm.newmodel.ProcessModelTypes._
import de.tkip.sbpm.newmodel.StateTypes._
import de.tkip.sbpm.newmodel.Operation._
import de.tkip.sbpm.newmodel.NoServiceParams

object ProcessFactory {
  /**
   * Example Folie 2
   */
  def createExampleProcess(): ProcessModel =
    ProcessModel(
      -1,
      "Example Process",
      Set(Subject("E", "Employee", true, false, 10, 0, Set(
        State(0, "Fill out Travel Request Application",
          Act,
          Set(Transition(ActParam("Done"), -2, 1)),
          NoServiceParams),
        State(1, "",
          Send,
          Set(
            Transition(
              CommunicationParams("Travel Request", None, "M", Number(1), Number(1), None, None),
              1, 2)),
          NoServiceParams),
        State(2, "",
          Receive,
          Set(
            Transition(
              CommunicationParams("Approved", None, "M", Number(1), Number(1), None, None),
              1, 3),
            Transition(
              CommunicationParams("Disapproved", None, "M", Number(1), Number(1), None, None),
              1, 4)),
          NoServiceParams),
        State(3, "Make Vacation",
          Act,
          Set(Transition(ActParam("Done"), -2, 4)),
          NoServiceParams),
        State(4, "", End, Set(), NoServiceParams)),
        Set()),
        Subject("M", "Manager", true, false, 10, 0, Set(
          State(0, "",
            Receive,
            Set(
              Transition(
                CommunicationParams("Travel Request", None, "E", Number(1), Number(1), None, None),
                1, 1)),
            NoServiceParams),
          State(1, "Check Travel Request",
            Act,
            Set(
              Transition(ActParam("Approved"), -2, 2),
              Transition(ActParam("Disapproved"), -2, 3)),
            NoServiceParams),
          State(2, "",
            Send,
            Set(
              Transition(
                CommunicationParams("Approved", None, "E", Number(1), Number(1), None, None),
                1, 4)),
            NoServiceParams),
          State(3, "",
            Send,
            Set(
              Transition(
                CommunicationParams("Disapproved", None, "E", Number(1), Number(1), None, None),
                1, 4)),
            NoServiceParams),
          State(4, "", End, Set(), NoServiceParams)),
          Set())))

  def createExampleProcessInstantInterface(): ProcessModel =
    ProcessModel(
      -1,
      "Example Process",
      Set(InstantInterface("E", "Employee"),
        Subject("M", "Manager", true, false, 10, 0, Set(
          State(0, "",
            Receive,
            Set(
              Transition(
                CommunicationParams("Travel Request", None, "E", Number(1), Number(1), None, None),
                1, 1)),
            NoServiceParams),
          State(1, "Check Travel Request",
            Act,
            Set(
              Transition(ActParam("Approved"), -2, 2),
              Transition(ActParam("Disapproved"), -2, 3)),
            NoServiceParams),
          State(2, "",
            Send,
            Set(
              Transition(
                CommunicationParams("Approved", None, "E", Number(1), Number(1), None, None),
                1, 4)),
            NoServiceParams),
          State(3, "",
            Send,
            Set(
              Transition(
                CommunicationParams("Disapproved", None, "E", Number(1), Number(1), None, None),
                1, 4)),
            NoServiceParams),
          State(4, "", End, Set(), NoServiceParams)),
          Set())))
  /**
   * Example Folie 26
   */
  def createSimpleProcess(): ProcessModel =
    ProcessModel(
      0,
      "simpleProcess",
      Set(
        Subject("S1", "Sendsubject", true, false, 10, 1,
          Set(
            State(1,
              "actstate",
              Act,
              Set(
                Transition(ActParam("act"), -2, 2)),
              NoServiceParams),
            State(2,
              "",
              Send,
              Set(
                Transition(
                  CommunicationParams("a", None, "S2", Number(1), Number(1), None, None),
                  1, 3)),
              NoServiceParams),
            State(3,
              "",
              Send,
              Set(
                Transition(
                  CommunicationParams("b", None, "S2", Number(1), Number(1), None, None),
                  1, 4)),
              NoServiceParams),
            State(4, "", End, Set(), NoServiceParams)),
          Set()),
        Subject("S2", "Receivesubject", false, false, 10, 5,
          Set(
            State(5,
              "",
              Receive,
              Set(
                Transition(
                  CommunicationParams("a", None, "S1", Number(1), Number(1), None, None),
                  1, 6)),
              NoServiceParams),
            State(6,
              "",
              Receive,
              Set(
                Transition(
                  CommunicationParams("b", None, "S1", Number(1), Number(1), None, None),
                  1, 7)),
              NoServiceParams),
            State(7, "", End, Set(), NoServiceParams)),
          Set())))

  def createSimpleProcessBreakUpTimeout(): ProcessModel =
    ProcessModel(
      0,
      "simpleProcess",
      Set(
        Subject("S1", "Sendsubject", true, false, 10, 1,
          Set(
            State(1,
              "actstate",
              Act,
              Set(
                Transition(ActParam("act"), -2, 2)),
              NoServiceParams),
            State(2,
              "",
              Send,
              Set(
                Transition(
                  CommunicationParams("a", None, "S2", Number(1), Number(1), None, None),
                  1, 3),
                Transition(BreakUpParam, 10, 4)),
              NoServiceParams),
            State(3,
              "",
              Send,
              Set(
                Transition(
                  CommunicationParams("b", None, "S2", Number(1), Number(1), None, None),
                  1, 4)),
              NoServiceParams),
            State(4, "", End, Set(), NoServiceParams)),
          Set()),
        Subject("S2", "Receivesubject", false, false, 10, 5,
          Set(
            State(5,
              "",
              Receive,
              Set(
                Transition(
                  CommunicationParams("a", None, "S1", Number(1), Number(1), None, None),
                  1, 6)),
              NoServiceParams),
            State(6,
              "",
              Receive,
              Set(
                Transition(
                  CommunicationParams("b", None, "S1", Number(1), Number(1), None, None),
                  1, 7),
                Transition(TimeoutParam(10), -1, 8)),
              NoServiceParams),
            State(8,
              "Beschweren",
              Act,
              Set(Transition(ActParam("Er sendet nun"), 1, 6)),
              NoServiceParams),
            State(7, "", End, Set(), NoServiceParams)),
          Set())))

  def createSimpleTravelRequest(): ProcessModel =
    ProcessModel(
      2,
      "Travel Request",
      Set(
        Subject("App", "Applicant", true, false, 100, 0,
          Set(
            State(0, "Fill out Travel Request", Act,
              Set(Transition(ActParam("Done"), 1, 1)),
              NoServiceParams),
            State(1, "", Send,
              Set(Transition(
                CommunicationParams("Travel Application", None, "Sup", Number(1), Number(1), None, None), 1, 2)),
              NoServiceParams),
            State(2, "", Receive,
              Set(Transition(
                CommunicationParams("Permission granted", None, "Sup", Number(1), Number(1), None, None), 1, 3),
                Transition(
                  CommunicationParams("Permission denied", None, "Sup", Number(1), Number(1), None, None), 1, 4)),
              NoServiceParams),
            State(3, "Make Travel", Act,
              Set(Transition(ActParam("Travel Ended"), 1, 6)),
              NoServiceParams),
            State(4, "Decide wheter filling again", Act,
              Set(
                Transition(ActParam("Redo Travel Application"), 1, 0),
                Transition(ActParam("Denial Accepted"), 1, 5)),
              NoServiceParams),
            State(5, "", Send,
              Set(Transition(
                CommunicationParams("No further Travel Application", None, "Sup", Number(1), Number(1), None, None), 1, 6)),
              NoServiceParams),
            State(6, "", End, Set(), NoServiceParams)),
          Set()),
        Subject("Sup", "Supervisor", false, false, 100, 0,
          Set(
            State(0, "", Receive,
              Set(Transition(
                CommunicationParams("Travel Application", None, "App", Number(1), Number(1), None, None), 1, 1)),
              NoServiceParams),
            State(1, "Check Travel Application", Act,
              Set(Transition(ActParam("Grant Permission"), 1, 2),
                Transition(ActParam("Deny Permission"), 1, 3)),
              NoServiceParams),
            State(2, "", Send,
              Set(Transition(
                CommunicationParams("Permission granted", None, "App", Number(1), Number(1), None, None), 1, 5)),
              NoServiceParams),
            State(3, "", Send,
              Set(Transition(
                CommunicationParams("Permission denied", None, "App", Number(1), Number(1), None, None), 1, 4)),
              NoServiceParams),
            State(4, "", Receive,
              Set(Transition(
                CommunicationParams("Travel Application", None, "App", Number(1), Number(1), None, None), 1, 1),
                Transition(
                  CommunicationParams("No further Travel Application", None, "App", Number(1), Number(1), None, None), 1, 5)),
              NoServiceParams),
            State(5, "", End, Set(), NoServiceParams)),
          Set())))

  def createTravelRequest(): ProcessModel =
    ProcessModel(
      2,
      "Travel Request",
      Set(
        Subject("App", "Applicant", true, false, 100, 0,
          Set(
            State(0, "Fill out Travel Request", Act,
              Set(Transition(ActParam("Done"), -2, 1),
                  Transition(TimeoutParam(1), -1, 6)),
              NoServiceParams),
            State(1, "", Send,
              Set(Transition(
                CommunicationParams("Travel Application", None, "Sup", Number(1), Number(1), None, None), 1, 2)),
              NoServiceParams),
            State(2, "", Receive,
              Set(Transition(
                CommunicationParams("Permission granted", None, "Sup", Number(1), Number(1), None, None), 1, 3),
                Transition(
                  CommunicationParams("Permission denied", None, "Sup", Number(1), Number(1), None, None), 1, 4)),
              NoServiceParams),
            State(3, "Make Travel", Act,
              Set(Transition(ActParam("Travel ended"), 1, 6)),
              NoServiceParams),
            State(4, "Decide wheter filling again", Act,
              Set(
                Transition(ActParam("Redo Travel Application"), 1, 0),
                Transition(ActParam("Denial Accepted"), 1, 5)),
              NoServiceParams),
            State(5, "", Send,
              Set(Transition(
                CommunicationParams("No further Travel Application", None, "Sup", Number(1), Number(1), None, None), 1, 6)),
              NoServiceParams),
            State(6, "", End, Set(), NoServiceParams)),
          Set()),
        Subject("Sup", "Supervisor", false, false, 100, 0,
          Set(
            State(0, "", Receive,
              Set(Transition(
                CommunicationParams("Travel Application", None, "App", Number(1), Number(1), None, None), 1, 1)),
              NoServiceParams),
            State(1, "Check Travel Application", Act,
              Set(Transition(ActParam("Grant Permission"), 1, 2),
                Transition(ActParam("Deny Permission"), 1, 3)),
              NoServiceParams),
            State(2, "", Send,
              Set(Transition(
                CommunicationParams("Permission granted", None, "App", Number(1), Number(1), None, None), 1, 6)),
              NoServiceParams),
            State(6, "", Send,
              Set(Transition(
                CommunicationParams("Approved Travel Application", None, "Adm", Number(1), Number(1), None, None), 1, 5)),
              NoServiceParams),
            State(3, "", Send,
              Set(Transition(
                CommunicationParams("Permission denied", None, "App", Number(1), Number(1), None, None), 1, 4)),
              NoServiceParams),
            State(4, "", Receive,
              Set(Transition(
                CommunicationParams("Travel Application", None, "App", Number(1), Number(1), None, None), 1, 1),
                Transition(
                  CommunicationParams("No further Travel Application", None, "App", Number(1), Number(1), None, None), 1, 5)),
              NoServiceParams),
            State(5, "", End, Set(), NoServiceParams)),
          Set()),
        Subject("Adm", "Administration", false, false, 100, 0, Set(
          State(0, "", Receive,
            Set(Transition(
              CommunicationParams("Approved Travel Application", None, "Sup", Number(1), Number(1), None, None), 1, 1)),
            NoServiceParams),
          State(1, "Fill out Travel Application", Act,
            Set(Transition(ActParam("Travel Application filed"), 1, 2)),
            NoServiceParams),
          State(2, "", End, Set(), NoServiceParams)),
          Set())))

  def createTravelRequestInterface(interfaceId: String): ProcessModel = {
    val p = createTravelRequest()

    val subjects = p.subjects.filterNot(_.id == interfaceId) + InstantInterface(interfaceId, interfaceId)
    val nSubjects =
      if (interfaceId == "App") {
        val supSub = subjects.find(_.id == "Sup").get.asInstanceOf[Subject]
        val newSupSub = supSub.copy(startSubject = true)
        subjects - supSub + newSupSub
      } else {
        subjects
      }

    p.copy(subjects = nSubjects)
  }

  /**
   * Example Folie 32
   */
  def createMultiSubjectExample(): ProcessModel =
    ProcessModel(
      3,
      "Multisubjects Example",
      Set(
        Subject("S1",
          "Principal",
          true,
          false,
          2,
          -1,
          Set(
            State(-1, "", Function, Set(Transition(NoExitParams, 1, 0)),
              NewSubjectInstances("S2", Number(25), Number(25), "all")),
            State(0, "", Send, Set(Transition(
              CommunicationParams("inquiry", None, "S2", AllUser, AllUser, Some("all"), None), 1, 1)),
              NoServiceParams),
            State(1, "", Receive, Set(Transition(
              CommunicationParams("offer", None, "S2", Number(1), Number(1), None, Some("ofs")), 1, 2)),
              NoServiceParams),
            State(2, "", Send, Set(Transition(
              CommunicationParams("order", None, "S2", AllUser, AllUser, Some("ofs"), None), 1, 3)),
              NoServiceParams),
            State(3, "", Receive, Set(Transition(
              CommunicationParams("offer", None, "S2", Number(24), Number(24), None, Some("ofs")), 1, 4)),
              NoServiceParams),
            State(4, "", Send, Set(Transition(
              CommunicationParams("cancel", None, "S2", AllUser, AllUser, Some("ofs"), None), 1, 5)),
              NoServiceParams),
            State(5, "", End, Set(), NoServiceParams)),
          Set()),
        Subject("S2",
          "Contractor",
          false,
          true,
          2,
          0,
          Set(
            State(0, "", Receive, Set(Transition(
              CommunicationParams("inquiry", None, "S1", Number(1), Number(1), None, None), 1, 1)),
              NoServiceParams),
            State(1, "Do Something", Act, Set(Transition(ActParam("done"), 1, 2)),
              NoServiceParams),
            State(2, "", Send, Set(Transition(
              CommunicationParams("offer", None, "S1", Number(1), Number(1), None, None), 1, 3)),
              NoServiceParams),
            State(3, "", Receive, Set(
              Transition(
                CommunicationParams("order", None, "S1", Number(1), Number(1), None, None), 1, 4),
              Transition(
                CommunicationParams("cancel", None, "S1", Number(1), Number(1), None, None), 1, 5)),
              NoServiceParams),
            State(4, "Carry out order", Act, Set(Transition(ActParam("carry out order"), 1, 5)), NoServiceParams),
            State(5, "", End, Set(), NoServiceParams)),
          Set())))
  /**
   * Vorheriges Example mit weniger Subjecten
   */
  def createFirstMultiSubjectExample(count: Int = 2): ProcessModel = {
    ProcessModel(
      3,
      "Multisubjects Example",
      Set(
        Subject("S1",
          "Principal",
          true,
          false,
          count,
          -1,
          Set(
            State(-1, "", Function, Set(Transition(NoExitParams, 1, 0)),
              NewSubjectInstances("S2", Number(count), Number(count), "all")),
            State(0, "", Send, Set(Transition(
              CommunicationParams("inquiry", None, "S2", AllUser, AllUser, Some("all"), None), 1, 1)),
              NoServiceParams),
            State(1, "", Receive, Set(Transition(
              CommunicationParams("offer", None, "S2", Number(1), Number(1), None, Some("ofs")), 1, 2)),
              NoServiceParams),
            State(2, "", Send, Set(Transition(
              CommunicationParams("order", None, "S2", AllUser, AllUser, Some("ofs"), None), 1, 3)),
              NoServiceParams),
            State(3, "", Receive, Set(Transition(
              CommunicationParams("offer", None, "S2", Number(count - 1), Number(count - 1), None, Some("ofs")), 1, 4)),
              NoServiceParams),
            State(4, "", Send, Set(Transition(
              CommunicationParams("cancel", None, "S2", AllUser, AllUser, Some("ofs"), None), 1, 5)),
              NoServiceParams),
            State(5, "", End, Set(), NoServiceParams)),
          Set()),
        Subject("S2",
          "Contractor",
          false,
          true,
          2,
          0,
          Set(
            State(0, "", Receive, Set(Transition(
              CommunicationParams("inquiry", None, "S1", Number(1), Number(1), None, None), 1, 1)),
              NoServiceParams),
            State(1, "Do Something", Act, Set(Transition(ActParam("done"), 1, 2)),
              NoServiceParams),
            State(2, "", Send, Set(Transition(
              CommunicationParams("offer", None, "S1", Number(1), Number(1), None, None), 1, 3)),
              NoServiceParams),
            State(3, "", Receive, Set(
              Transition(
                CommunicationParams("order", None, "S1", Number(1), Number(1), None, None), 1, 4),
              Transition(
                CommunicationParams("cancel", None, "S1", Number(1), Number(1), None, None), 1, 5)),
              NoServiceParams),
            State(4, "Carry out order", Act, Set(Transition(ActParam("carry out order"), 1, 5)), NoServiceParams),
            State(5, "", End, Set(), NoServiceParams)),
          Set())))
  }

  def createMacroExample(): ProcessModel =
    ProcessModel(
      7,
      "Macro Example",
      Set(
        Subject("S1", "S1", true, false, 10, 1, Set(
          State(1, "", Function,
            Set(Transition(NoExitParams, 1, 2)),
            ExecuteMacro("send-receive")),
          State(2, "", End, Set(), NoServiceParams)),
          Set(Macro("send-receive", 1, Set(
            State(1, "", Send, Set(Transition(
              CommunicationParams("a", None, "S2", Number(1), Number(1), None, None), 1, 2)),
              NoServiceParams),
            State(2, "", Receive, Set(Transition(
              CommunicationParams("b", None, "S2", Number(1), Number(1), None, None), 1, 3)),
              NoServiceParams),
            State(3, "", End, Set(), NoServiceParams))))),
        Subject("S2", "S2", false, false, 10, 1, Set(
          State(1, "", Receive, Set(Transition(
            CommunicationParams("a", None, "S1", Number(1), Number(1), None, None), 1, 2)),
            NoServiceParams),
          State(2, "", Send, Set(Transition(
            CommunicationParams("b", None, "S1", Number(1), Number(1), None, None), 1, 3)),
            NoServiceParams),
          State(3, "", End, Set(), NoServiceParams)), Set())),
      Map("a" -> NoContentType, "b" -> NoContentType))

  /**
   * Example Folie 53
   */
  def createModalSplitExample(): ProcessModel =
    ProcessModel(
      4,
      "Beispiel Modal Split / Modal Join",
      Set(Subject("S1", "S1", true, false, 10, 1, Set(
        State(1, "",
          Send,
          Set(
            Transition(
              CommunicationParams("a", None, "S2", Number(1), Number(1), None, None),
              1, 2)),
          NoServiceParams),
        State(2, "", End, Set(), NoServiceParams)),
        Set()),
        Subject("S2", "S2", false, false, 10, 3, Set(
          State(3, "",
            Receive,
            Set(
              Transition(
                CommunicationParams("a", None, "S1", Number(1), Number(1), None, None),
                1, 4)),
            NoServiceParams),
          State(4, "",
            Split,
            Set(
              Transition(NoExitParams, 0, 5),
              Transition(NoExitParams, 0, 7)),
            NoServiceParams),
          State(5, "",
            Send,
            Set(
              Transition(
                CommunicationParams("b", None, "S3", Number(1), Number(1), None, None),
                1, 6)),
            NoServiceParams),
          State(6, "",
            Receive,
            Set(
              Transition(
                CommunicationParams("c", None, "S3", Number(1), Number(1), None, None),
                1, 10)),
            NoServiceParams),
          State(7, "",
            SplitGuard,
            Set(
              Transition(NoExitParams, 1, 8),
              Transition(ImplicitTransitionParam, 1, 10)),
            NoServiceParams),
          State(8, "",
            Send,
            Set(
              Transition(
                CommunicationParams("d", None, "S4", Number(1), Number(1), None, None),
                1, 9)),
            NoServiceParams),
          State(9, "",
            Receive,
            Set(
              Transition(
                CommunicationParams("e", None, "S4", Number(1), Number(1), None, None),
                1, 10)),
            NoServiceParams),
          State(10, "",
            Join,
            Set(
              Transition(NoExitParams, 1, 11)),
            NoServiceParams),
          State(11, "", End, Set(), NoServiceParams)),
          Set()),
        Subject("S3", "S3", false, false, 10, 13,
          Set(
            State(13, "",
              Receive,
              Set(
                Transition(
                  CommunicationParams("b", None, "S2", Number(1), Number(1), None, None),
                  1, 14)),
              NoServiceParams),
            State(14, "",
              Send,
              Set(
                Transition(
                  CommunicationParams("c", None, "S2", Number(1), Number(1), None, None),
                  1, 15)),
              NoServiceParams),
            State(15, "", End, Set(), NoServiceParams)),
          Set()),
        Subject("S4", "S4", false, false, 10, 16,
          Set(
            State(16, "",
              Receive,
              Set(
                Transition(
                  CommunicationParams("d", None, "S2", Number(1), Number(1), None, None),
                  1, 17)),
              NoServiceParams),
            State(17, "",
              Send,
              Set(
                Transition(
                  CommunicationParams("e", None, "S2", Number(1), Number(1), None, None),
                  1, 18)),
              NoServiceParams),
            State(18, "", End, Set(), NoServiceParams)),
          Set())))

  /**
   * Example Folie 59
   */
  def createObserverExample(): ProcessModel =
    ProcessModel(
      5,
      "Beispiel Observer als ExceptionHandler",
      Set(Subject("S1", "S1", true, false, 10, 0, Set(
        State(0, "Start Subject",
          Act,
          Set(
            Transition(ActParam("Done"), 1, 1)),
          NoServiceParams),
        State(1, "",
          Function,
          Set(Transition(NoExitParams, 1, 2)),
          ActivateState(7)),
        State(2, "New Costumer?",
          Act,
          Set(
            Transition(ActParam("Yes"), 1, 3),
            Transition(ActParam("No"), 1, 4)),
          NoServiceParams),
        State(3, "change costumer data",
          Act,
          Set(
            Transition(ActParam("Done"), 1, 8)),
          NoServiceParams),
        State(4, "capture costumer data",
          Act,
          Set(
            Transition(ActParam("Done"), 1, 8)),
          NoServiceParams),
        // Close the InputPool for the Messages
        State(8, "",
          Function,
          Set(Transition(NoExitParams, 1, 9)),
          CloseIP(Some("modify request"), Some("costumer"))),
        State(9, "",
          Function,
          Set(Transition(NoExitParams, 1, 5)),
          CloseIP(Some("delete request"), Some("costumer"))),
        State(5, "",
          Function,
          Set(Transition(NoExitParams, 1, 6)),
          DeactivateState(7)),
        State(6, "", End, Set(), NoServiceParams),
        State(7, "",
          Observer,
          Set(
            Transition(
              CommunicationParams("modify request", None, "costumer", Number(1), Number(1), None, None),
              1, 1),
            Transition(
              CommunicationParams("delete request", None, "costumer", Number(1), Number(1), None, None),
              1, 6)),
          NoServiceParams)),
        Set()),
        Subject("costumer", "Costumer", true, false, 1, 0, Set(
          State(0, "Change Request?",
            Act,
            Set(
              Transition(ActParam("Do Nothing"), 1, 3),
              Transition(ActParam("modify"), 1, 1),
              Transition(ActParam("delete"), 1, 2)),
            NoServiceParams),
          State(1, "",
            Send,
            Set(
              Transition(
                CommunicationParams("modify request", None, "S1", Number(1), Number(1), None, None),
                1, 3)),
            NoServiceParams),
          State(2, "",
            Send,
            Set(
              Transition(
                CommunicationParams("delete request", None, "S1", Number(1), Number(1), None, None),
                1, 3)),
            NoServiceParams),
          State(3, "", End, Set(), NoServiceParams)),
          Set())))

  def createObserverAsCorIdExample(): ProcessModel = {
    val (min, max) = (1, 1)
    ProcessModel(
      7,
      "Beispiel Observer als ExceptionHandler",
      Set(Subject("S1", "S1", true, false, 6, -1, Set(
        State(-1, "", Function, Set(Transition(NoExitParams, 1, 0)),
          NewSubjectInstances("S2", Number(min), Number(max), "all")),
        State(0, "",
          Send,
          Set(
            Transition(
              CommunicationParams("Antrag", None, "S2", AllUser, AllUser, Some("all"), None),
              1, 1)),
          NoServiceParams),
        State(1, "",
          Act,
          Set(Transition(ActParam("Warte auf Deadline"), 1, 2)),
          NoServiceParams),
        State(2, "",
          Send,
          Set(
            Transition(
              CommunicationParams("Deadline", None, "S2", AllUser, AllUser, Some("all"), None),
              1, 3)),
          NoServiceParams),
        State(3, "",
          Receive,
          Set(
            Transition(
              CommunicationParams("Antwort", None, "S2", AllMessages, AllMessages, None, Some("answer")),
              1, 4)),
          NoServiceParams),
        State(4, "",
          Act,
          Set(
            Transition(ActParam("Gute Antworten"), 1, 6),
            Transition(ActParam("Keine guten Antworten"), 1, 5)),
          NoServiceParams),
        State(5, "",
          Send,
          Set(
            Transition(
              CommunicationParams("Redo", None, "S2", AllUser, AllUser, Some("all"), None),
              1, 1)),
          NoServiceParams),
        State(6, "",
          Function,
          Set(Transition(NoExitParams, 1, 7)),
          VariableManipulation("answer", Selection, None, "good")),
        State(7, "",
          Function,
          Set(Transition(NoExitParams, 1, 8)),
          VariableManipulation("all", Difference, Some("good"), "cancel")),
        State(8, "",
          Send,
          Set(
            Transition(
              CommunicationParams("Angenommen", None, "S2", AllUser, AllUser, Some("good"), None),
              1, 9)),
          NoServiceParams),
        State(9, "",
          Send,
          Set(
            Transition(
              CommunicationParams("cancel", None, "S2", AllUser, AllUser, Some("cancel"), None),
              1, 10)),
          NoServiceParams),
        State(10, "", End, Set(), NoServiceParams)),
        Set()),
        Subject("S2", "S2", false, true, 3, 0, Set(
          State(0, "",
            Receive,
            Set(
              Transition(
                CommunicationParams("Antrag", None, "S1", Number(1), Number(1), None, None),
                1, 1)),
            NoServiceParams),
          State(1, "",
            Function,
            Set(Transition(NoExitParams, 1, 2)),
            ActivateState(4)),
          State(2, "",
            Send,
            Set(
              Transition(
                CommunicationParams("Antwort", None, "S1", Number(1), Number(1), None, None),
                1, 3)),
            NoServiceParams),
          State(3, "Warte",
            Act,
            Set(Transition(ActParam("warte"), 1, 3)),
            NoServiceParams),
          State(4, "",
            Observer,
            Set(
              Transition(
                CommunicationParams("Deadline", None, "S1", Number(1), Number(1), None, None),
                1, 5)),
            NoServiceParams),
          State(5, "",
            Receive,
            Set(
              Transition(
                CommunicationParams("Redo", None, "S1", Number(1), Number(1), None, None),
                2, 1),
              Transition(
                CommunicationParams("cancel", None, "S1", Number(1), Number(1), None, None),
                1, 7),
              Transition(
                CommunicationParams("Angenommen", None, "S1", Number(1), Number(1), None, None),
                1, 6)),
            NoServiceParams),
          State(6, "Do something usefull",
            Act,
            Set(Transition(ActParam("Done"), 1, 7)),
            NoServiceParams),
          State(7, "", End, Set(), NoServiceParams)),
          Set())))
  }
}
