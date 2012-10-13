package com.github.etaoins.s5tool

import java.io.File
import java.util.concurrent.{Executors,ForkJoinPool}
import akka.dispatch.{ExecutionContext,Future}
import akka.util.duration._
import akka.dispatch.Await

object TargetStateCalculator {
  def apply(config : Config) : Map[String,UploadableLocalFile] = {
    /** Our fixed Cache-Control header */
    val cacheControlHeader = "max-age=" + config.maxAge.toString

    /** Execution context for I/O-bound tasks */
    val ioContext = ExecutionContext.fromExecutorService(
      Executors.newFixedThreadPool(4)
    )

    /** Execution context for CPU-bound tasks */
    val computeContext = ExecutionContext.fromExecutorService(
      new ForkJoinPool 
    )

    val localDirEnts = LocalDirectoryEnumerator(new File(config.filesystemRoot))

    val hashedFutures = localDirEnts.map { dirEnt =>
      for {
        read <- Future(LocalFileReader(dirEnt))(ioContext)
        encoded <- Future(LocalFileEncoder(read))(computeContext)
        hashed <- Future(LocalFileHasher(encoded))(computeContext)
      } yield hashed
    }

    // Wait for everything to finish and turn in to an UploadableLocalFile
    val uploadableFiles = hashedFutures.map { hashedFuture =>
      val hashed = Await.result(hashedFuture, 1 day)

      UploadableLocalFile(
        siteRelativePath=hashed.siteRelativePath,
        contentMd5=hashed.contentMd5,
        contentEncoding=hashed.contentEncoding,
        cacheControl=cacheControlHeader,
        body=hashed.body
      )
    }

    // Shut. Everything. Down.
    ioContext.shutdown()
    computeContext.shutdown()

    // Return a map
    uploadableFiles.map { uploadable => 
      (uploadable.siteRelativePath, uploadable)
    }.toMap
  }
}

