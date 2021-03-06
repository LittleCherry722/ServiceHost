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

package de.tkip.sbpm.persistence.testdata

import de.tkip.sbpm.model._
import akka.actor.ActorRef
import akka.pattern._
import scala.concurrent.duration._
import scala.concurrent.Future

import com.github.t3hnar.bcrypt._
import scala.concurrent.ExecutionContext
import de.tkip.sbpm.application.miscellaneous.RoleMapper
import de.tkip.sbpm.persistence.query._
import de.tkip.sbpm.rest.GraphJsonProtocol._
import spray.json.JsonParser
import de.tkip.sbpm.model._

/**
 * Provides test data for the database.
 */
object Entities {
  val groups = List(
    Group(None, "Gro\u00dfunternehmen", true),
    Group(None, """Staples""", true),
    Group(None, """Zulieferer""", true),

    Group(None, """_SAME_""", true),
    Group(None, """_ANY_""", true),

    Group(None, """Employees""", true),
    Group(None, """Supervisors""", true),
    Group(None, """Human Resources""", true),

    Group(None, """Warehouse""", true),
    Group(None, """Purchasing""", true)
  )

  val roles = List(
    Role(None, "Gro\u00dfunternehmen", true),
    Role(None, """Staples""", true),
    Role(None, """Kunde""", true),
    Role(None, """Verarbeitung""", true),
    Role(None, """Zulieferer""", true),

    Role(None, """default""", true),
    Role(None, """assess_travel_requests""", true),
    Role(None, """recieve_approved_travel_requests""", true),

    Role(None, """execute_orders""", true),
    Role(None, """handle_purchases""", true),
    Role(None, """manage_cost_center""", true)
  )

  // users and one default identity with password for login
  val users = List(
    (User(None, """Superuser""", true, 8, "test@gmail.com"), ("sbpm", "superuser@sbpm.com", "s1234".bcrypt)),
    (User(None, "Gro\u00dfunternehmen Mitarbeiter", true, 8), ("sbpm", "unternehmer@sbpm.com", "u1234".bcrypt)),
    (User(None, """Staples Mitarbeiter""", true, 8), ("sbpm", "staples@sbpm.com", "s1234".bcrypt)),
    (User(None, """TSP Mitarbeiter""", true, 8), ("sbpm", "tsp@sbpm.com", "t1234".bcrypt)),

    (User(None, """Google App Engine""", true, 8), ("sbpm", "google@sbpm.com", "g1234".bcrypt)),
    (User(None, """Beyer""", true, 8), ("sbpm", "beyer@sbpm.com", "b1234".bcrypt)),
    (User(None, """Link""", true, 8), ("sbpm", "link@sbpm.com", "l1234".bcrypt)),
    (User(None, """Woehnl""", true, 8), ("sbpm", "woehnl@sbpm.com", "w1234".bcrypt)),
    (User(None, """Borgert""", true, 8), ("sbpm", "borgert@sbpm.com", "b1234".bcrypt)),
    (User(None, """Roeder""", true, 8), ("sbpm", "roeder@sbpm.com", "r1234".bcrypt)),
    (User(None, """Hartwig""", true, 8), ("sbpm", "hartwig@sbpm.com", "h1234".bcrypt)),
    (User(None, """Stein""", true, 8), ("sbpm", "stein@sbpm.com", "s1234".bcrypt)))

  // process with one active graph loaded from corresponding json file
  val processes = List[(Process, String)](
//    (Process(None, None, false, "Grossunternehmen", false) -> loadJson("grossunternehmen")),
//    (Process(None, None, false, """Transportdienstleister""", false) -> loadJson("lieferant")),
//
//    (Process(None, None, false, "Grossunternehmen Dreieck", false) -> loadJson("grossunternehmen_dreieck")),
//    (Process(None, None, false, """Staples Dreieck""", false) -> loadJson("staples_dreieck")),
//    (Process(None, None, false, """Transportdienstleister Dreieck""", false) -> loadJson("lieferant_dreieck")),

    (Process(None, None, false, """Travel Request""", false) -> loadJson("travel_request")), //only process to use roles Supervisor and HR_Data_Access
//    (Process(None, None, false, """Order""", false) -> loadJson("order")), //only process to use roles Cost_Center_Manager, Purchase_Requisitions and Warehouse
 //   (Process(None, None, false, """IP Test""", false) -> loadJson("ip_test")),
//    (Process(None, None, false, """IP Test Open Close Wildcard""", false) -> loadJson("ip_test_open_close_wildcard")),
//    (Process(None, None, false, """IP Test Open Close Wildcard With Timeout""", false) -> loadJson("ip_test_open_close_wildcard_with_timeout")),
//    (Process(None, None, false, """Modal Split Example""", false) -> loadJson("modalsplit_example")),
 //   (Process(None, None, false, """Modal Split Guard Example""", false) -> loadJson("modalsplitguard_example")),
//    (Process(None, None, false, """Macro Example""", false) -> loadJson("macro_example")),
//    (Process(None, None, false, """Nested Modal Split Example""", false) -> loadJson("nested_modal_split_example")),
//    (Process(None, None, false, """Nested Modal Split Guard Example""", false) -> loadJson("nested_modal_split_guard_example")),
//    (Process(None, None, false, """Projektleiter""", false) -> loadJson("projektleiter")),
//    (Process(None, None, false, """Projekt Team""", false) -> loadJson("projekt_team")),
//    (Process(None, None, false, """Fortgeschritten Bestellung""", false) -> loadJson("fortgeschritten_bestellung")),
//    (Process(None, None, false, """Fortgeschritten Lieferung""", false) -> loadJson("fortgeschritten_lieferung")),
//    (Process(None, None, false, """Fortgeschritten Rechnung""", false) -> loadJson("fortgeschritten_rechnung")),
   (Process(None, None, false, """Simple Observer Example""", false) -> loadJson("simple_observer_example")),
//    (Process(None, None, false, """Shared IP Test""", false) -> loadJson("shared_ip_test")),
//
//    (Process(None, None, false, """VASEC Router Stub""", false) -> loadJson("vasec_router_stub")),
//
////  blackbox process
//    (Process(None, None, false, """Blackbox Provider""", false) -> loadJson("blackbox_provider")),
//    (Process(None, None, false, """Blackbox Consumer""", false) -> loadJson("blackbox_consumer")),
//
////  variables processes
//    (Process(None, None, false, """Variables to subjects local""", false) -> loadJson("variables_to_subjects_local")),
//    (Process(None, None, false, """Variables to subjects external""", false) -> loadJson("variables_to_subjects_external")),
//    (Process(None, None, false, """Variables to variables external""", false) -> loadJson("variables_to_variables")),
//    (Process(None, None, false, """Variables to variables and extraction external""", false) -> loadJson("variables_to_variables_extraction")),
//
////  distributed processes
//    (Process(None, None, false, """test8080""", false) -> loadJson("test8080")),
//    (Process(None, None, false, """RatioDrink""", false) -> loadJson("ratiodrink")),
//
////  external subject processes
//    (Process(None, None, false, """ExternalSubject Simple - Sender""", false) -> loadJson("externalsubject_simple_sender")),           // 37
//    (Process(None, None, false, """ExternalSubject Simple - Receiver""", false) -> loadJson("externalsubject_simple_receiver")),       // 38
//    (Process(None, None, false, """ExternalSubject Dreieck - Kunde""", false) -> loadJson("externalsubject_dreieck_kunde")),           // 39
//    (Process(None, None, false, """ExternalSubject Dreieck - Hersteller""", false) -> loadJson("externalsubject_dreieck_hersteller")), // 40
//    (Process(None, None, false, """ExternalSubject Dreieck - Lieferant""", false) -> loadJson("externalsubject_dreieck_lieferant")),    // 41
//    (Process(None, None, false, """OnlineShopping""", false) -> loadJson("OnlineShopping")),
//    (Process(None, None, false, """ServiceTest""", false) -> loadJson("ServiceTest")),
//    (Process(None, None, false, """Advance_ServiceTest""", false) -> loadJson("Advance_ServiceTest")),
//    (Process(None, None, false, """VariableTest""", false) -> loadJson("VariableTest")),
//    (Process(None, None, false, """AgentMaptest""", false) -> loadJson("AgentMapTest")),
//    (Process(None, None, false, """AdvanceVariableTest""", false) -> loadJson("AdvanceVariableTest")),
//    (Process(None, None, false, """IPTestInServiceHost""", false) -> loadJson("IPTestInServiceHost")),
//    (Process(None, None, false, """CorrelationTest""", false) -> loadJson("CorrelationTest")),
//    (Process(None, None, false, """correlationId_In_ServiceHost""", false) -> loadJson("correlationId_In_ServiceHost")),
//   //(Process(None, None, false, """CloseIP""", false) -> loadJson("CloseIP")),
//    (Process(None, None, false, """CloseIP_In_ServiceHost""", false) -> loadJson("CloseIP_In_ServiceHost")),
//    (Process(None, None, false, """CloseIP_OpenIP""", false) -> loadJson("CloseIP_OpenIP")),
//    (Process(None, None, false, """TimeOUT""", false) -> loadJson("TimeOUT")),
//    (Process(None, None, false, """TimeOut_ServiceHost""", false) -> loadJson("TimeOut_ServiceHost")),
    (Process(None, None, false, """Observer_In_ServiceHost""", false) -> loadJson("Observer_In_ServiceHost")),
    //(Process(None, None, false, """ObserverTest""", false) -> loadJson("ObserverTest")),
    //(Process(None, None, false, """ParallelService""", false) -> loadJson("ParallelService"))
    (Process(None, None, false, """ParaService""", false) -> loadJson("paraService"))

   )


  // group -> role mappings
  // _1 = index in groups list, _2 = index in roles list
  // ids are not known a priori
  val groupRoles = List(
    // showcase matchings
    (0, 0),
    (0, 1),
    (1, 2),
    (1, 3),
    (2, 4),

    // other matchings
    (4, 5),
    (4, 6),
    (5, 5),
    (6, 6),
    (7, 7),

    (8, 8),
    (9, 9),
    (6, 10)
  )

  // group -> user mappings
  // _1 = index in groups list, _2 = index in users list
  // ids are not known a priori
  val groupUsers = List(
    // showcase matchings
    (0, 1),
    (1, 2),
    (2, 3),

//    // other matchings
    (4, 0),
    (4, 3),
    (0, 0),
    (1, 0),
    (2, 0),
    (3, 0),
    (4, 0),
    (5, 0),
    (6, 0),
    (7, 0),
    (8, 0),
    (9, 0)
  )

  implicit val timeout = akka.util.Timeout(100 seconds)

  /**
   * Load a resource json file as string.
   * A file in the current package folder with name "name.json"
   * must exist.
   */
  def loadJson(name: String) = {
    val inStream = getClass.getResourceAsStream(name + ".json")
	val str = scala.io.Source.fromInputStream(inStream, "UTF-8").mkString
	inStream.close()
	str
  }

  /**
   * Send all test data to the persistence actor to be inserted into the database.
   */
  def insert(persistenceActor: ActorRef)(implicit executionContext: ExecutionContext): Future[Any] = {
    // insert groups
    val groupsFuture = (persistenceActor ? Groups.Save(groups: _*)).mapTo[Seq[Option[Int]]]

    // insert users
    val usersFuture = (persistenceActor ? Users.Save(users.map(_._1): _*)).mapTo[Seq[Option[Int]]]

    // insert roles
    val rolesFuture = (persistenceActor ? Roles.Save(roles: _*)).mapTo[Seq[Option[Int]]]

    // combine futures and wait until groups/users/roles are
    // inserted, then insert the different associations using
    // the generated ids
    val groupAssocFuture = for {
      g <- groupsFuture
      u <- usersFuture
      // save user identities for generated user ids
      ui <- Future.sequence(users.indices.map { i =>
        val ident = users(i)._2
        (persistenceActor ? Users.Save.Identity(u(i).get, ident._1, ident._2, Some(ident._3)))
      })
      // save group -> user mappings with generated ids
      gu <- (persistenceActor ? GroupsUsers.Save(groupUsers.map(gu => GroupUser(g(gu._1).get, u(gu._2).get)): _*))
      r <- rolesFuture
      // save group -> role mappings with generated ids
      gr <- (persistenceActor ? GroupsRoles.Save(groupRoles.map(gr => GroupRole(g(gr._1).get, r(gr._2).get)): _*))
    } yield (r, gu, gr)

    // insert processes
    val processesFuture = (persistenceActor ? Processes.Save(processes.map(_._1): _*)).mapTo[Seq[Option[Int]]]

    // wait until group mappings and processes are inserted
    // then parse and insert graphs
    val result =
      for {
        ga <- groupAssocFuture
        p <- processesFuture
        // convert roles to name -> role mapping (necessary for parsing json)
        rls <- Future(ga._1.zip(roles).map(t => (t._2.name -> t._2.copy(t._1))).toMap)
        // parse graph jsons and insert graphs
        g <- (persistenceActor ? Graphs.Save(processes.indices.map { i =>
          // use slicks' json parser to convert graph from string to domain model
          JsonParser(processes(i)._2).asJsObject.convertTo[Graph](graphJsonFormat(RoleMapper.createRoleMapper(rls))).copy(processId = p(i))
        }: _*)).mapTo[Seq[Option[Int]]]
        // update processes' active graph property with graph ids of
        // recently inserted graphs
        pg <- persistenceActor ? Processes.Save(p.zip(processes).map(t => t._2._1.copy(id = t._1)).zip(g).map(t => t._1.copy(activeGraphId = t._2)).toSeq: _*)
      } yield (ga, p, g, pg)
    result.onComplete(a => println("Entering Testdata result was:\n" + a))
    result
  }
}
