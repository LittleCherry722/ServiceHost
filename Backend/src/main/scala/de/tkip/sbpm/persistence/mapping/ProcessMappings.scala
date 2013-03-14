package de.tkip.sbpm.persistence.mapping
import de.tkip.sbpm.{ model => domainModel }

object ProcessMappings {
  def convert(p: (Process, Option[Int])): domainModel.Process =
    domainModel.Process(p._1.id,
      p._1.name,
      p._1.isCase,
      p._2)

  def convert(p: Option[(Process, Option[Int])]): Option[domainModel.Process] =
    if (p.isDefined) Some(convert(p.get))
    else None

  def convert(p: domainModel.Process): (Process, Option[Int]) =
    (Process(p.id, p.name, p.isCase), p.activeGraphId)
}