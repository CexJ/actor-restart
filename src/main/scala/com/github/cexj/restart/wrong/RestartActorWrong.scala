package com.github.cexj.restart.wrong

import akka.actor.ActorSystem
import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import com.github.cexj.restart.wrong.JobManagerActor.JobManagerActorCommander
import com.github.cexj.restart.wrong.JobManagerActor.JobManagerActorCommander._

import scala.util.Random

object RestartActorWrong extends App {

  def supervisedJobManagerActor: Behavior[JobManagerActorCommander] = Behaviors.supervise(JobManagerActor()).onFailure(SupervisorStrategy.restart)

  val system: ActorSystem = ActorSystem("edge")

  val actor = system.spawn(supervisedJobManagerActor, Random.nextInt().toString)

  actor ! Print("Hello")
  actor ! Die

  Thread.sleep(2000)

  actor ! Print("world")

}

object JobManagerActor {
  trait JobManagerActorCommander
  object JobManagerActorCommander{
    case class Print(name: String) extends JobManagerActorCommander
    case object Die extends JobManagerActorCommander
  }
  def apply(): Behavior[JobManagerActorCommander] = init()
  private def init(): Behavior[JobManagerActorCommander] = Behaviors.setup { context =>
    Behaviors.receiveMessage[JobManagerActorCommander] {
      case Print(name) =>
        println(s"$name")
        Behaviors.same
      case Die =>
        Behaviors.stopped
    }
  }
}
