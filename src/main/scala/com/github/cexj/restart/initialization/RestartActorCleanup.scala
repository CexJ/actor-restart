package com.github.cexj.restart.initialization

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import com.github.cexj.restart.initialization.JobManagerActor.JobManagerActorCommander._
import com.github.cexj.restart.initialization.JobManagerActor.{JobManagerActorCommander, initialization}
import com.github.cexj.restart.initialization.JobManagerGuardian.JobManagerGuardianCommander
import com.github.cexj.restart.initialization.JobManagerGuardian.JobManagerGuardianCommander._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

object RestartActorInitialization extends App {

  val system: ActorSystem = ActorSystem("edge")

  val actor: ActorRef[JobManagerActorCommander] = system.spawn(JobManagerGuardian(), Random.nextInt().toString).narrow[JobManagerActorCommander]

  actor ! Print("Hello")
  actor ! Die

  Thread.sleep(1000)

  actor ! Print("world")

  Thread.sleep(1000)

  actor ! Print("world")

  Thread.sleep(1000)

  actor ! Print("world")

  actor ! Print("Hello")
  actor ! Die

  Thread.sleep(1000)

  actor ! Print("world")

  Thread.sleep(1000)

  actor ! Print("world")

  Thread.sleep(1000)

  actor ! Print("world")

}

object JobManagerGuardian {

  trait JobManagerGuardianCommander
  object JobManagerGuardianCommander {
    case object Start extends JobManagerGuardianCommander
    case object Stop extends JobManagerGuardianCommander
  }


  def apply(): Behavior[JobManagerGuardianCommander] = init()
  private def init(): Behavior[JobManagerGuardianCommander] = Behaviors.setup { context =>
    context.pipeToSelf(initialization){
      case Success(_) => Start
      case Failure(_) => Stop
    }
    starting
  }

  private def watching(actor: ActorRef[JobManagerActorCommander]): Behavior[JobManagerGuardianCommander] =
    Behaviors.receiveMessage[JobManagerGuardianCommander]{
      case msg: JobManagerActorCommander =>
        actor ! msg
        Behaviors.same
      case _ =>
        Behaviors.same
    }.receiveSignal {
      case (context, Terminated(_)) =>
        context.pipeToSelf(initialization) {
          case Success(_) => Start
          case Failure(_) => Stop
        }
        starting
    }

  private def starting(): Behavior[JobManagerGuardianCommander] =
    Behaviors.receive { case (context,  msg) => msg match {
      case Start =>
        val actor = context.spawn(JobManagerActor(), "AGAIN")
        context.watch(actor)
        watching(actor)
      case Stop =>
        Behaviors.stopped
      case msg =>
        println(s"I won't execute $msg")
        Behaviors.same
    }}

}



object JobManagerActor {
  trait JobManagerActorCommander extends JobManagerGuardianCommander
  object JobManagerActorCommander {
    case class Print(name: String) extends JobManagerActorCommander
    case object Die extends JobManagerActorCommander
  }

  def initialization: Future[Unit] = Future {
    println("initialization start")
    Thread.sleep(2000)
    println("initialization end")
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
