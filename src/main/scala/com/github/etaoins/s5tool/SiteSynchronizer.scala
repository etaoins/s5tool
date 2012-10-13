package com.github.etaoins.s5tool

import java.io.File
import java.util.concurrent.{Executors, ForkJoinPool}
import akka.dispatch.{ExecutionContext, Future}
import akka.util.duration._
import akka.dispatch.Await

object SiteSynchronizer {
  def apply(config : Config) = {
    /** Execution context for I/O-bound tasks */
    val ioContext = ExecutionContext.fromExecutorService(
      Executors.newFixedThreadPool(4)
    )

    /** Execution context for CPU-bound tasks */
    val computeContext = ExecutionContext.fromExecutorService(
      new ForkJoinPool 
    )

    val localDirEnts = LocalDirectoryEnumerator(new File(config.filesystemRoot))

    val fileFutures = localDirEnts.map { dirEnt =>
      for {
        read <- Future(LocalFileReader(dirEnt))(ioContext)
        encoded <- Future(LocalFileEncoder(read))(computeContext)
      } yield encoded
    }

    // Wait for everything to finish
    val fileResults = fileFutures.map { fileFuture =>
      Await.result(fileFuture, 1 day)
    }

    // Print everything out for debugging
    fileResults.map( file => 
      println(file.siteRelativePath + ": " + file.contentEncoding.getOrElse("identity"))
    )

    // Shut. Everything. Down.
    ioContext.shutdown()
    computeContext.shutdown()
  }
}

