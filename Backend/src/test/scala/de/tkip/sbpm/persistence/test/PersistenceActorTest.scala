package de.tkip.sbpm.persistence.test

import org.junit._
import Assert._
import akka.pattern._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.ActorSystem._
import akka.actor.Props
import akka.actor.ActorSystem
import de.tkip.sbpm.persistence.PersistenceActor
import akka.util.Timeout
import de.tkip.sbpm.persistence._

class PersistenceActorTest {
  
  import PersistenceActorTest._

  implicit val timeout = Timeout(10 seconds)
  implicit val executionContext = scala.concurrent.ExecutionContext.global

  @Test
  def configuration() {
    type M = model.Configuration
    val id1 = "config1"
    val id2 = "config2"
    actor ! SaveConfiguration(id1, "Config 1", "xxx", "String")
    actor ! SaveConfiguration(id2, "Config 2", "yyy", "Integer")
    val allFuture = actor ? GetConfiguration()
    val oneFuture1 = actor ? GetConfiguration(Some(id1))
    val oneFuture2 = actor ? GetConfiguration(Some(id2))
    val all = Await.result(allFuture.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all.length)
    val one1 = Await.result(oneFuture1.mapTo[Option[M]], timeout.duration)
    assertEquals(id1, one1.get.key)
    val one2 = Await.result(oneFuture2.mapTo[Option[M]], timeout.duration)
    assertEquals("Config 2", one2.get.label)
    actor ! SaveConfiguration(id2, "Config 3", "yyy", "Integer")
    val oneFuture3 = actor ? GetConfiguration(Some(id2))
    val one3 = Await.result(oneFuture3.mapTo[Option[M]], timeout.duration)
    assertEquals("Config 3", one3.get.label)
    actor ! DeleteConfiguration(id1)
    actor ! DeleteConfiguration(id2)
    val allFuture2 = actor ? GetConfiguration()
    val all2 = Await.result(allFuture2.mapTo[Seq[M]], timeout.duration)
    assertEquals(0, all2.length)
  }

  @Test
  def graph() {
    type M = model.Graph

    val idFuture1 = actor ? SaveGraph(None, "{}", DatabaseAccess.currentTimestamp, 1)
    val idFuture2 = actor ? SaveGraph(None, "{}", DatabaseAccess.currentTimestamp, 2)
    val id1 = Await.result(idFuture1.mapTo[Int], timeout.duration)
    val id2 = Await.result(idFuture2.mapTo[Int], timeout.duration)
    val allFuture = actor ? GetGraph()
    val oneFuture1 = actor ? GetGraph(Some(id1))
    val oneFuture2 = actor ? GetGraph(Some(id2))
    val all = Await.result(allFuture.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all.length)
    val one1 = Await.result(oneFuture1.mapTo[Option[M]], timeout.duration)
    assertEquals(Some(id1), one1.get.id)
    val one2 = Await.result(oneFuture2.mapTo[Option[M]], timeout.duration)
    assertEquals(2, one2.get.processId)
    actor ! SaveGraph(Some(id2), "{}", DatabaseAccess.currentTimestamp, 3)
    val oneFuture3 = actor ? GetGraph(Some(id2))
    val one3 = Await.result(oneFuture3.mapTo[Option[M]], timeout.duration)
    assertEquals(3, one3.get.processId)
    actor ! DeleteGraph(id1)
    actor ! DeleteGraph(id2)
    val allFuture2 = actor ? GetGraph()
    val all2 = Await.result(allFuture2.mapTo[Seq[M]], timeout.duration)
    assertEquals(0, all2.length)
  }

  @Test
  def group() {
    type M = model.Group

    val idFuture1 = actor ? SaveGroup(None, "Group 1")
    val idFuture2 = actor ? SaveGroup(None, "Group 2")
    val id1 = Await.result(idFuture1.mapTo[Int], timeout.duration)
    val id2 = Await.result(idFuture2.mapTo[Int], timeout.duration)
    val allFuture = actor ? GetGroup()
    val oneFuture1 = actor ? GetGroup(Some(id1))
    val oneFuture2 = actor ? GetGroup(Some(id2))
    val all = Await.result(allFuture.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all.length)
    val one1 = Await.result(oneFuture1.mapTo[Option[M]], timeout.duration)
    assertEquals(Some(id1), one1.get.id)
    val one2 = Await.result(oneFuture2.mapTo[Option[M]], timeout.duration)
    assertTrue(one2.get.isActive)
    actor ! SaveGroup(Some(id2), "{}", false)
    val oneFuture3 = actor ? GetGroup(Some(id2))
    val one3 = Await.result(oneFuture3.mapTo[Option[M]], timeout.duration)
    assertFalse(one3.get.isActive)
    actor ! DeleteGroup(id1)
    actor ! DeleteGroup(id2)
    val allFuture2 = actor ? GetGroup()
    val all2 = Await.result(allFuture2.mapTo[Seq[M]], timeout.duration)
    assertEquals(0, all2.length)
  }

  @Test
  def groupRole() {
    type M = model.GroupRole

    actor ! SaveGroupRole(1, 2)
    actor ! SaveGroupRole(3, 4)
    val allFuture = actor ? GetGroupRole()
    val all = Await.result(allFuture.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all.length)
    actor ! SaveGroupRole(3, 4, false)
    val allFuture2 = actor ? GetGroupRole()
    val all2 = Await.result(allFuture2.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all2.length)
    actor ! DeleteGroupRole(1, 2)
    actor ! DeleteGroupRole(3, 4)
    val allFuture3 = actor ? GetGroupRole()
    val all3 = Await.result(allFuture3.mapTo[Seq[M]], timeout.duration)
    assertEquals(0, all3.length)
  }

  @Test
  def groupUser() {
    type M = model.GroupUser

    actor ! SaveGroupUser(1, 2)
    actor ! SaveGroupUser(3, 4)
    val allFuture = actor ? GetGroupUser()
    val all = Await.result(allFuture.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all.length)
    actor ! SaveGroupUser(3, 4, false)
    val allFuture2 = actor ? GetGroupUser()
    val all2 = Await.result(allFuture2.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all2.length)
    actor ! DeleteGroupUser(1, 2)
    actor ! DeleteGroupUser(3, 4)
    val allFuture3 = actor ? GetGroupUser()
    val all3 = Await.result(allFuture3.mapTo[Seq[M]], timeout.duration)
    assertEquals(0, all3.length)
  }

  @Test
  def message() {
    type M = model.Message
    val time = DatabaseAccess.currentTimestamp
    time.setNanos(0)
    val idFuture1 = actor ? SaveMessage(None, 1, 2, 1, true, "Message 1", DatabaseAccess.currentTimestamp)
    val idFuture2 = actor ? SaveMessage(None, 3, 4, 1250, false, "Message 2", time)
    val id1 = Await.result(idFuture1.mapTo[Int], timeout.duration)
    val id2 = Await.result(idFuture2.mapTo[Int], timeout.duration)
    val allFuture = actor ? GetMessage()
    val oneFuture1 = actor ? GetMessage(Some(id1))
    val oneFuture2 = actor ? GetMessage(Some(id2))
    val all = Await.result(allFuture.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all.length)
    val one1 = Await.result(oneFuture1.mapTo[Option[M]], timeout.duration)
    assertEquals(Some(id1), one1.get.id)
    val one2 = Await.result(oneFuture2.mapTo[Option[M]], timeout.duration)
    assertEquals(time, one2.get.date)
    actor ! SaveMessage(Some(id2), 10, 2, 1250, false, "Message 2.5", time)
    val oneFuture3 = actor ? GetMessage(Some(id2))
    val one3 = Await.result(oneFuture3.mapTo[Option[M]], timeout.duration)
    assertEquals("Message 2.5", one3.get.data)
    actor ! DeleteMessage(id1)
    actor ! DeleteMessage(id2)
    val allFuture2 = actor ? GetMessage()
    val all2 = Await.result(allFuture2.mapTo[Seq[M]], timeout.duration)
    assertEquals(0, all2.length)
  }

  @Test
  def processInstance() {
    type M = model.ProcessInstance

    val idFuture1 = actor ? SaveProcessInstance(None, 1, 2, "{}", "ProcessInstance 1")
    val idFuture2 = actor ? SaveProcessInstance(None, 3, 4, "{}", "ProcessInstance 2")
    val id1 = Await.result(idFuture1.mapTo[Int], timeout.duration)
    val id2 = Await.result(idFuture2.mapTo[Int], timeout.duration)
    val allFuture = actor ? GetProcessInstance()
    val oneFuture1 = actor ? GetProcessInstance(Some(id1))
    val oneFuture2 = actor ? GetProcessInstance(Some(id2))
    val all = Await.result(allFuture.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all.length)
    val one1 = Await.result(oneFuture1.mapTo[Option[M]], timeout.duration)
    assertEquals(Some(id1), one1.get.id)
    val one2 = Await.result(oneFuture2.mapTo[Option[M]], timeout.duration)
    assertEquals(3, one2.get.processId)
    actor ! SaveProcessInstance(Some(id2), 10, 2, "{}", "")
    val oneFuture3 = actor ? GetProcessInstance(Some(id2))
    val one3 = Await.result(oneFuture3.mapTo[Option[M]], timeout.duration)
    assertEquals(10, one3.get.processId)
    actor ! DeleteProcessInstance(id1)
    actor ! DeleteProcessInstance(id2)
    val allFuture2 = actor ? GetProcessInstance()
    val all2 = Await.result(allFuture2.mapTo[Seq[M]], timeout.duration)
    assertEquals(0, all2.length)
  }

  @Test
  def relation() {
    type M = model.Relation
    actor ! SaveRelation(1, 2, 3, 4)
    actor ! SaveRelation(5, 6, 7, 8)
    val allFuture = actor ? GetRelation()
    val all = Await.result(allFuture.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all.length)
    actor ! SaveRelation(1, 2, 3, 4)
    val allFuture2 = actor ? GetRelation()
    val all2 = Await.result(allFuture2.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all2.length)
    actor ! DeleteRelation(1, 2, 3, 4)
    actor ! DeleteRelation(5, 6, 7, 8)
    val allFuture3 = actor ? GetRelation()
    val all3 = Await.result(allFuture3.mapTo[Seq[M]], timeout.duration)
    assertEquals(0, all3.length)
  }

  @Test
  def role() {
    type M = model.Role

    val idFuture1 = actor ? SaveRole(None, "Role 1")
    val idFuture2 = actor ? SaveRole(None, "Role 2")
    val id1 = Await.result(idFuture1.mapTo[Int], timeout.duration)
    val id2 = Await.result(idFuture2.mapTo[Int], timeout.duration)
    val allFuture = actor ? GetRole()
    val oneFuture1 = actor ? GetRole(Some(id1))
    val oneFuture2 = actor ? GetRole(Some(id2))
    val all = Await.result(allFuture.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all.length)
    val one1 = Await.result(oneFuture1.mapTo[Option[M]], timeout.duration)
    assertEquals(Some(id1), one1.get.id)
    val one2 = Await.result(oneFuture2.mapTo[Option[M]], timeout.duration)
    assertTrue(one2.get.isActive)
    actor ! SaveRole(Some(id2), "{}", false)
    val oneFuture3 = actor ? GetRole(Some(id2))
    val one3 = Await.result(oneFuture3.mapTo[Option[M]], timeout.duration)
    assertFalse(one3.get.isActive)
    actor ! DeleteRole(id1)
    actor ! DeleteRole(id2)
    val allFuture2 = actor ? GetRole()
    val all2 = Await.result(allFuture2.mapTo[Seq[M]], timeout.duration)
    assertEquals(0, all2.length)
  }

  @Test
  def user() {
    type M = model.User

    val idFuture1 = actor ? SaveUser(None, "User 1")
    val idFuture2 = actor ? SaveUser(None, "User 2", true, 12)
    val id1 = Await.result(idFuture1.mapTo[Int], timeout.duration)
    val id2 = Await.result(idFuture2.mapTo[Int], timeout.duration)
    val allFuture = actor ? GetUser()
    val oneFuture1 = actor ? GetUser(Some(id1))
    val oneFuture2 = actor ? GetUser(Some(id2))
    val all = Await.result(allFuture.mapTo[Seq[M]], timeout.duration)
    assertEquals(2, all.length)
    val one1 = Await.result(oneFuture1.mapTo[Option[M]], timeout.duration)
    assertEquals(Some(id1), one1.get.id)
    val one2 = Await.result(oneFuture2.mapTo[Option[M]], timeout.duration)
    assertEquals(12, one2.get.inputPoolSize)
    actor ! SaveUser(Some(id2), "{}", false, 23)
    val oneFuture3 = actor ? GetUser(Some(id2))
    val one3 = Await.result(oneFuture3.mapTo[Option[M]], timeout.duration)
    assertEquals(23, one3.get.inputPoolSize)
    actor ! DeleteUser(id1)
    actor ! DeleteUser(id2)
    val allFuture2 = actor ? GetUser()
    val all2 = Await.result(allFuture2.mapTo[Seq[M]], timeout.duration)
    assertEquals(0, all2.length)
  }
}

object PersistenceActorTest {
  val sys = ActorSystem()
  val actor = sys.actorOf(Props[PersistenceActor])
  
  @BeforeClass
  def init() {
    actor ! InitDatabase
  }
}