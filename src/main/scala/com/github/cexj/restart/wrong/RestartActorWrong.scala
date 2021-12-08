package com.github.cexj.restart.wrong

import akka.actor.ActorSystem
import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import com.github.cexj.restart.wrong.JobManagerActorW.JobManagerActorCommanderW
import com.github.cexj.restart.wrong.JobManagerActorW.JobManagerActorCommanderW._

import scala.util.Random

object RestartActorWrong extends App {

  def supervisedJobManagerActor: Behavior[JobManagerActorCommanderW] = Behaviors.supervise(JobManagerActorW()).onFailure(SupervisorStrategy.restart)

  val system: ActorSystem = ActorSystem("edge")

  val actor = system.spawn(supervisedJobManagerActor, Random.nextInt().toString)

  actor ! PrintW("Hello")
  actor ! DieW

  Thread.sleep(2000)

  actor ! PrintW("world")

}

object JobManagerActorW {
  trait JobManagerActorCommanderW
  object JobManagerActorCommanderW{
    case class PrintW(name: String) extends JobManagerActorCommanderW
    case object DieW extends JobManagerActorCommanderW
  }
  def apply(): Behavior[JobManagerActorCommanderW] = init()
  private def init(): Behavior[JobManagerActorCommanderW] = Behaviors.setup { context =>
    Behaviors.receiveMessage[JobManagerActorCommanderW] {
      case PrintW(name) =>
        println(s"$name")
        Behaviors.same
      case DieW =>
        Behaviors.stopped
    }
  }
}
