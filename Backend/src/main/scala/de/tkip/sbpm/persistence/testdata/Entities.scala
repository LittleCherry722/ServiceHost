package de.tkip.sbpm.persistence.testdata

import de.tkip.sbpm.model._
import akka.actor.ActorRef
import akka.pattern._
import scala.concurrent.duration._
import scala.concurrent.Future
import ua.t3hnar.bcrypt._
import scala.concurrent.ExecutionContext
import de.tkip.sbpm.persistence.query._
import de.tkip.sbpm.rest.GraphJsonProtocol._
import spray.json.JsonParser
import de.tkip.sbpm.model._
import java.io.ByteArrayOutputStream

/**
 * Provides test data for the database.
 */
object Entities {
  val groups = List(
    Group(None, """_SAME_""", true),
    Group(None, """_ANY_""", true),
    Group(None, """SBPM_Ltd""", true),
    Group(None, """SBPM_Ltd_DE""", true),
    Group(None, """SBPM_Ltd_DE_Accounting""", true),
    Group(None, """SBPM_Ltd_DE_Procurement""", true),
    Group(None, """SBPM_Ltd_DE_Human_Resources""", true),
    Group(None, """SBPM_Ltd_DE_Warehouse""", true),
    Group(None, """SBPM_Ltd_DE_Board""", true),
    Group(None, """SBPM_Ltd_UK""", true),
    Group(None, """SBPM_Ltd_UK_Accounting""", true),
    Group(None, """SBPM_Ltd_UK_Procurement""", true),
    Group(None, """SBPM_Ltd_UK_Human_Resources""", true),
    Group(None, """SBPM_Ltd_UK_Warehouse""", true),
    Group(None, """SBPM_Ltd_UK_Board""", true),
    Group(None, """Manager""", true),
    Group(None, """Teamleader""", true),
    Group(None, """Head_of_Department""", true),
    Group(None, """IT-Stuff""", true),
    Group(None, """External""", true))

  val roles = List(
    Role(None, """Employee""", true),
    Role(None, """Employee_DE""", true),
    Role(None, """Employee_UK""", true),
    Role(None, """Accounting""", true),
    Role(None, """Procurement""", true),
    Role(None, """HR_Data_Access""", true),
    Role(None, """Salary_Statement_DE""", true),
    Role(None, """Salary_Statement_UK""", true),
    Role(None, """Warehouse""", true),
    Role(None, """Purchase_Requisitions""", true),
    Role(None, """Board_Member""", true),
    Role(None, """Supervisor""", true),
    Role(None, """Cost_Center_Manager""", true))

  val users = List(
    (User(None, """Superuser""", true, 8), ("sbpm", "superuser@sbpm.com", "s1234".bcrypt)),
    (User(None, """Beyer""", true, 8), ("sbpm", "beyer@sbpm.com", "b1234".bcrypt)),
    (User(None, """Link""", true, 8), ("sbpm", "link@sbpm.com", "l1234".bcrypt)),
    (User(None, """Woehnl""", true, 8), ("sbpm", "woehnl@sbpm.com", "w1234".bcrypt)),
    (User(None, """Borgert""", true, 8), ("sbpm", "borgert@sbpm.com", "b1234".bcrypt)),
    (User(None, """Roeder""", true, 8), ("sbpm", "roeder@sbpm.com", "r1234".bcrypt)),
    (User(None, """Hartwig""", true, 8), ("sbpm", "hartwig@sbpm.com", "h1234".bcrypt)))

  val processes = List(
    (Process(None, """Travel Request""", false) -> loadJson("travel_request")),
    (Process(None, """Travel Request No Loop""", false) -> loadJson("travel_request_no_loop")),
    (Process(None, """Travel Request Timeout""", false) -> loadJson("travel_request_timeout")),
    (Process(None, """Order""", false) -> loadJson("order")),
    (Process(None, """Supplier (E)""", false) -> loadJson("supplier")))

  val groupRoles = List(
    (0, 0),
    (1, 1),
    (2, 3),
    (2, 6),
    (3, 4),
    (3, 8),
    (3, 9),
    (4, 5),
    (4, 6),
    (5, 8),
    (5, 9),
    (6, 10),
    (7, 2),
    (8, 3),
    (8, 7),
    (9, 4),
    (9, 8),
    (9, 9),
    (10, 5),
    (10, 7),
    (11, 8),
    (11, 9),
    (13, 11),
    (13, 12),
    (14, 11),
    (15, 11))

  // _1 = group, _2 = user index in groups/users list, _3 = isActive
  val groupUsers = List(
    (0, 0),
    (0, 1),
    (0, 2),
    (0, 3),
    (0, 4),
    (0, 5),
    (0, 6),
    (1, 1),
    (1, 3),
    (1, 5),
    (2, 1),
    (4, 3),
    (6, 5),
    (7, 2),
    (7, 6),
    (9, 2),
    (12, 6),
    (13, 2),
    (13, 4),
    (13, 5),
    (13, 6),
    (14, 1),
    (15, 4),
    (2, 3))

  implicit val timeout = akka.util.Timeout(100 seconds)

  def loadJson(name: String) = {
    val inStream = getClass.getResourceAsStream(name + ".json")
    val outStream = new ByteArrayOutputStream
    try {
      var reading = true
      while (reading) {
        inStream.read() match {
          case -1 => reading = false
          case c  => outStream.write(c)
        }
      }
      outStream.flush()
    } finally {
      inStream.close()
    }
    new String(outStream.toByteArray())
  }

  /**
   * Send all test data to the persistence actor to be inserted into the database.
   */
  def insert(persistenceActor: ActorRef)(implicit executionContext: ExecutionContext): Future[Any] = {
    val groupsFuture = (persistenceActor ? Groups.Save(groups: _*)).mapTo[Seq[Option[Int]]]

    val usersFuture = (persistenceActor ? Users.Save(users.map(_._1): _*)).mapTo[Seq[Option[Int]]]

    val rolesFuture = (persistenceActor ? Roles.Save(roles: _*)).mapTo[Seq[Option[Int]]]

    // combine futures and wait until groups/users/roles are
    // inserted, then insert the different associations using
    // the generated ids
    val groupAssocFuture = for {
      g <- groupsFuture
      u <- usersFuture
      r <- rolesFuture
      gu <- (persistenceActor ? GroupsUsers.Save(groupUsers.map(gu => GroupUser(g(gu._1).get, u(gu._2).get)): _*))
      gr <- (persistenceActor ? GroupsRoles.Save(groupRoles.map(gr => GroupRole(g(gr._1).get, r(gr._2).get)): _*))
      ui <- Future.sequence(users.indices.map { i =>
        val ident = users(i)._2
        (persistenceActor ? Users.Save.Identity(u(i).get, ident._1, ident._2, Some(ident._3)))
      })
    } yield (r, gu, gr, ui)

    val processesFuture = (persistenceActor ? Processes.Save(processes.map(_._1): _*)).mapTo[Seq[Option[Int]]]

    for {
      ga <- groupAssocFuture
      p <- processesFuture
      rls <- Future(ga._1.zip(roles).map(t => (t._2.name -> t._2.copy(t._1))).toMap)
      g <- (persistenceActor ? Graphs.Save(processes.indices.map { i =>
        JsonParser(processes(i)._2).asJsObject.convertTo[Graph](graphJsonFormat(rls)).copy(processId = p(i))
      }: _*)).mapTo[Seq[Option[Int]]]
      pg <- persistenceActor ? Processes.Save(p.zip(processes).map(t => t._2._1.copy(id = t._1)).zip(g).map(t => t._1.copy(activeGraphId = t._2)).toSeq: _*)
    } yield (ga, p, g, pg)
  }
}