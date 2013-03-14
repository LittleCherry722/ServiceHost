package de.tkip.sbpm.persistence

import akka.actor.Actor
import de.tkip.sbpm.model._
import akka.actor.Props
import scala.slick.lifted
import akka.pattern._
import scala.concurrent._

/**
 * Handles database connection for "Process" entities using slick.
 */
private[persistence] class ProcessPersistenceActor extends GraphPersistenceActor
  with DatabaseAccess with schema.ProcessesSchema with schema.ProcessActiveGraphsSchema {
  import driver.simple._
  import query.Processes._
  import mapping.ProcessMappings._

  def joinQuery(baseQuery: driver.simple.Query[Processes.type, mapping.Process] = Query(Processes)) = for {
    (p, pag) <- baseQuery.leftJoin(Query(ProcessActiveGraphs)).on(_.id === _.processId)
  } yield (p, pag.graphId.?)

  override def receive = {
    // get all processes ordered by id
    case Read.All => answerProcessed { implicit session: Session =>
      joinQuery().list
    }(_.map(convert))
    // get process with given id
    case Read.ById(id) => answerOptionProcessed { implicit session: Session =>
      joinQuery(Query(Processes).where(_.id === id)).firstOption
    }(convert)
    // get process with given name
    case Read.ByName(name) => answerOptionProcessed { implicit session: Session =>
      joinQuery(Query(Processes).where(_.name === name)).firstOption
    }(convert)
    // create new process
    case Save.Entity(ps @ _*) => answer { implicit session =>
      ps.map {
        case p @ Process(None, _, _, _) => Some(insert(p))
        case p @ Process(id, _, _, _) => update(id, p)
      }
    }
    // create new process with a corresponding graph
    case Save.WithGraph(p: Process, g) => saveProcessWithGraph(p, g)
    // delete process with given id
    case Delete.ById(id) => answer { session =>
      Processes.where(_.id === id).delete(session)
    }
  }

  private def insert(p: Process)(implicit session: Session) = {
    val entities = convert(p)
    val id = Processes.autoInc.insert(entities._1)
    if (entities._2.isDefined)
      ProcessActiveGraphs.insert(mapping.ProcessActiveGraph(id, entities._2.get))
    id
  }

  // update entity or throw exception if it does not exist
  private def update(id: Option[Int], p: Process) = answer { implicit session =>
    val entities = convert(p)
    val res = Processes.where(_.id === id).update(entities._1)
    val pgaQuery = ProcessActiveGraphs.where(_.processId === id)
    if (entities._2.isDefined)
      pgaQuery.update(mapping.ProcessActiveGraph(id.get, entities._2.get))
    else
      pgaQuery.delete

    if (res == 0)
      throw new EntityNotFoundException("Process with id %d does not exist.", id.get)
    None
  }

  /**
   * Saves a process with the corresponding graph to the database.
   */
  private def saveProcessWithGraph(p: Process, g: Graph) = answer { implicit session =>
    var graph = g.copy(id = None)
    var process = p.copy(activeGraphId = None)
    var resultId = process.id
    // if id not defined -> save new process
    if (!resultId.isDefined) {
      resultId = Some(insert(process))
      process = process.copy(id = resultId)
    } else {
      if (!Processes.where(_.id === process.id).firstOption.isDefined)
        throw new EntityNotFoundException("Process with id %d does not exist.", process.id.get)
      resultId = None
    }
    // set process id in graph
    graph = graph.copy(processId = process.id)
    val gId = save(graph)
    (resultId, gId)
  }
}

