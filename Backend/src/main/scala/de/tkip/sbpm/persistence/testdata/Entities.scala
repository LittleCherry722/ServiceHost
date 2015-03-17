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

import java.util.UUID
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
    (Process(None, Some(UUID.fromString("e1499cbb-faba-4389-972d-ed9e3100cce8")), None, false, "Grossunternehmen", false) -> loadJson("grossunternehmen")), // 1
    (Process(None, Some(UUID.fromString("024e3686-1cb0-465c-8d5f-ba11c632c917")), None, false, "Service Host", false) -> loadJson("servicehost")), // 2
    (Process(None, Some(UUID.fromString("bfb40ae5-f4a6-4114-9d2e-86a02b713868")), None, false, """Transportdienstleister""", false) -> loadJson("lieferant")), // 3
    (Process(None, Some(UUID.fromString("1d8abed1-604c-47d6-9fc6-9ede2340eae2")), None, false, "Grossunternehmen Dreieck", false) -> loadJson("grossunternehmen_dreieck")), // 4
    (Process(None, Some(UUID.fromString("31ce2aec-75a2-4a82-a13c-3e2dc2c85fe4")), None, false, """Staples Dreieck""", false) -> loadJson("staples_dreieck")), // 5
    (Process(None, Some(UUID.fromString("fcc33399-225b-4b0e-b7b5-bed37157418b")), None, false, """Transportdienstleister Dreieck""", false) -> loadJson("lieferant_dreieck")), // 6

    (Process(None, Some(UUID.fromString("7dbd31ba-1d77-4fc7-9b2c-9e85ffdde851")), None, false, """Travel Request""", false) -> loadJson("travel_request")), //only process to use roles Supervisor and HR_Data_Access // 7
    (Process(None, Some(UUID.fromString("250190a5-7b78-4a91-ac01-4a37a15715c0")), None, false, """Order""", false) -> loadJson("order")), //only process to use roles Cost_Center_Manager, Purchase_Requisitions and Warehouse // process to use roles Supervisor and HR_Data_Access // 8
    (Process(None, Some(UUID.fromString("be8a820a-d756-48da-8f08-bf4c0e843a6d")), None, false, """IP Test""", false) -> loadJson("ip_test")), // 9
    (Process(None, Some(UUID.fromString("e9aa359a-3d25-4b13-98f1-40736ec7cb6e")), None, false, """IP Test Open Close Wildcard""", false) -> loadJson("ip_test_open_close_wildcard")), // 10
    (Process(None, Some(UUID.fromString("bbfb5f19-2412-4f2b-8c9e-ca246d6cc80e")), None, false, """IP Test Open Close Wildcard With Timeout""", false) -> loadJson("ip_test_open_close_wildcard_with_timeout")), // 11
    (Process(None, Some(UUID.fromString("f1a2b02f-c887-4cb2-8c40-e5890bb86853")), None, false, """Modal Split Example""", false) -> loadJson("modalsplit_example")), // 12
    (Process(None, Some(UUID.fromString("263fbea9-9983-4225-b011-e69f6d668702")), None, false, """Modal Split Guard Example""", false) -> loadJson("modalsplitguard_example")), // 13
    (Process(None, Some(UUID.fromString("b8a5e218-39cb-4dfc-945e-0531201904d2")), None, false, """Macro Example""", false) -> loadJson("macro_example")), // 14
    (Process(None, Some(UUID.fromString("08ad3c10-d0c2-481c-9ff2-a645f8ef0bd5")), None, false, """Nested Modal Split Example""", false) -> loadJson("nested_modal_split_example")), // 15
    (Process(None, Some(UUID.fromString("c01e6be8-6a88-4738-ac80-e90bcc73d829")), None, false, """Nested Modal Split Guard Example""", false) -> loadJson("nested_modal_split_guard_example")), // 16
    (Process(None, Some(UUID.fromString("340c386e-e0bc-4ce2-9512-e4a57e993ce5")), None, false, """Projektleiter""", false) -> loadJson("projektleiter")), // 17
    (Process(None, Some(UUID.fromString("bc1608ea-cf1a-40bd-8831-cabed5b64b3e")), None, false, """Projekt Team""", false) -> loadJson("projekt_team")), // 18
    (Process(None, Some(UUID.fromString("fd939ceb-7630-485f-89c6-c203776e6995")), None, false, """Fortgeschritten Bestellung""", false) -> loadJson("fortgeschritten_bestellung")), // 19
    (Process(None, Some(UUID.fromString("dbcf1b2e-a737-4b58-863e-8bd317786186")), None, false, """Fortgeschritten Lieferung""", false) -> loadJson("fortgeschritten_lieferung")), // 20
    (Process(None, Some(UUID.fromString("b7cfc814-2d64-4fa6-a7fc-bdb3d8622187")), None, false, """Fortgeschritten Rechnung""", false) -> loadJson("fortgeschritten_rechnung")), // 21
    (Process(None, Some(UUID.fromString("aca4463d-4973-477a-890e-85101d0daecc")), None, false, """Simple Observer Example""", false) -> loadJson("simple_observer_example")), // 22
    (Process(None, Some(UUID.fromString("13d392d6-2a39-48b1-9572-1ba7d974d7d3")), None, false, """Shared IP Test""", false) -> loadJson("shared_ip_test")), // 23
    (Process(None, Some(UUID.fromString("d348adca-f031-4926-bce8-3b3b6b8cd699")), None, false, """Service Host Test""", false) -> loadJson("service_host_test")), // 24
    (Process(None, Some(UUID.fromString("ce54421f-17d7-4e00-8899-36b5a7cb61db")), None, false, """Service Host Two""", false) -> loadJson("service_host_two")), // 25
    (Process(None, Some(UUID.fromString("90ac7ce6-dbe1-4e48-8187-b2b3e58b448e")), None, false, """ServiceToService Test""", false) -> loadJson("ServiceToService")), // 26
    (Process(None, Some(UUID.fromString("614a8918-9f37-4c83-a0a7-b5ef8305c3d4")), None, false, """Service Host Three""", false) -> loadJson("service_host_three")), // 27
//    (Process(None, Some(UUID.fromString("cb279c51-33df-480d-847a-92ebbc9df519")), None, false, """Service Host Four""", false) -> loadJson("service_host_four")),
    (Process(None, Some(UUID.fromString("bc1f1bac-d026-458a-8372-8498dd7e430c")), None, false, """VASEC Router Stub""", false) -> loadJson("vasec_router_stub")), // 28

//  blackbox process
    (Process(None, Some(UUID.fromString("13cc5c43-6968-4203-8fa0-6d38a03983e7")), None, false, """Blackbox Provider""", false) -> loadJson("blackbox_provider")), // 29
    (Process(None, Some(UUID.fromString("de09122d-6b4e-4ae7-9cce-3b8f21225606")), None, false, """Blackbox Consumer""", false) -> loadJson("blackbox_consumer")), // 30

//  variables processes
    (Process(None, Some(UUID.fromString("9cd3b942-917c-4683-aac0-46606fcb01dc")), None, false, """Variables to subjects local""", false) -> loadJson("variables_to_subjects_local")), // 31
    (Process(None, Some(UUID.fromString("402d294e-4e5e-44ce-ac96-6e33f04858f3")), None, false, """Variables to subjects external""", false) -> loadJson("variables_to_subjects_external")), // 32
    (Process(None, Some(UUID.fromString("2063256b-6d7f-4020-8f70-03f7ef05f064")), None, false, """Variables to variables external""", false) -> loadJson("variables_to_variables")), // 33
    (Process(None, Some(UUID.fromString("11de4b3c-22eb-43b2-93cd-9ce82427ef52")), None, false, """Variables to variables and extraction external""", false) -> loadJson("variables_to_variables_extraction")), // 34

//  distributed processes
    (Process(None, Some(UUID.fromString("abd2a44b-1d33-4ad4-b765-a997de024239")), None, false, """test8080""", false) -> loadJson("test8080")), // 35
    (Process(None, Some(UUID.fromString("6217f2d6-9ced-457f-88ca-d5fbbdc6d40e")), None, false, """RatioDrink""", false) -> loadJson("ratiodrink")), // 36

//  external subject processes
    (Process(None, Some(UUID.fromString("2385bb87-a17e-42e9-8de6-52db68cd4b95")), None, false, """ExternalSubject Simple - Sender""", false) -> loadJson("externalsubject_simple_sender")),           // 37
    (Process(None, Some(UUID.fromString("07c7a48f-3c75-4d64-addd-c4f1aeaf0898")), None, false, """ExternalSubject Simple - Receiver""", false) -> loadJson("externalsubject_simple_receiver")),       // 38
    (Process(None, Some(UUID.fromString("4ddf2373-8cae-4094-b886-8f396382d75f")), None, false, """ExternalSubject Dreieck - Kunde""", false) -> loadJson("externalsubject_dreieck_kunde")),           // 39
    (Process(None, Some(UUID.fromString("66a87e58-43ce-48de-b3f0-72c5c3885c15")), None, false, """ExternalSubject Dreieck - Hersteller""", false) -> loadJson("externalsubject_dreieck_hersteller")), // 40
    (Process(None, Some(UUID.fromString("ea50b302-e483-420b-b332-4548fe4ac7d9")), None, false, """ExternalSubject Dreieck - Lieferant""", false) -> loadJson("externalsubject_dreieck_lieferant"))    // 41
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
          // TODO conversion fails..
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
