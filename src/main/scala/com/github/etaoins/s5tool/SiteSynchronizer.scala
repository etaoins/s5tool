package com.github.etaoins.s5tool

import java.io.File
import com.amazonaws.auth.AWSCredentials
import akka.dispatch.{Future,Await}
import akka.util.duration._

object SiteSynchronizer {
  def apply(config : Config, awsCredentials : AWSCredentials) = {
    implicit val executeContext = FixedExecutionContext(6)
    
    // Get our target and remote state concurrently
    val targetStateFuture = Future {
      TargetStateCalculator(config)
    }

    val remoteStateFuture = Future {
      RemoteStateCalculator(awsCredentials)(config.bucketName)
    }

   val targetState = Await.result(targetStateFuture, 1 day)
   val remoteState = Await.result(remoteStateFuture, 1 day)

    // Calculate the files to delete - this is easy
    val toDelete = remoteState.keys.toSet -- targetState.keys.toSet

    val toUpload = targetState.filter { case (relativePath, file) =>
      remoteState.get(relativePath).map(_.sameContentAs(file)).getOrElse(true)
    }
      
    executeContext.shutdown();
  }
}

