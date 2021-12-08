package com.github.cexj.restart.guardian

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import com.github.cexj.restart.guardian.JobManagerActor.JobManagerActorCommander
import com.github.cexj.restart.guardian.JobManagerActor.JobManagerActorCommander._

import scala.util.Random

object RestartActorGuardian extends App {

  val system: ActorSystem = ActorSystem("edge")

  val actor = system.spawn(JobManagerGuardian(), Random.nextInt().toString)

  actor ! Print("Hello")
  actor ! Die

  Thread.sleep(2000)

  actor ! Print("world")

}

object JobManagerGuardian {

  def apply(): Behavior[JobManagerActorCommander] = init()
  private def init(): Behavior[JobManagerActorCommander] = Behaviors.setup { context =>
    val actor = context.spawn(JobManagerActor(), "AGAIN")
    context.watch(actor)
    watching(actor)
  }

  private def watching(actor: ActorRef[JobManagerActorCommander]): Behavior[JobManagerActorCommander] =
    Behaviors.receiveMessage[JobManagerActorCommander]{
      case msg =>
        actor ! msg
        Behaviors.same
    }.receiveSignal {
      case (context, Terminated(_)) =>
        val actor = context.spawn(JobManagerActor(), "AGAIN")
        context.watch(actor)
        watching(actor)
    }


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
