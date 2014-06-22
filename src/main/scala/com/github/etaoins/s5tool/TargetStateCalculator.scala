package com.github.etaoins.s5tool

import java.io.File
import java.util.concurrent.{Executors,ForkJoinPool}
import scala.concurrent.{ExecutionContext,Future,Await}
import scala.concurrent.duration._

object TargetStateCalculator {
  def apply(config : Config) : Map[String,UploadableLocalFile] = {
    /** Our fixed Cache-Control header */
    val cacheControlHeader = config.maxAge.map( "max-age=" + _.toString)

    /** Execution context for CPU-bound tasks */
    implicit val executeContext = ExecutionContext.fromExecutorService(
      new ForkJoinPool 
    )

    val localDirEnts = LocalDirectoryEnumerator(config.filesystemRoot.get)

    val hashedFutures = localDirEnts.map { dirEnt =>
      for {
        read <- Future(LocalFileReader(dirEnt))
        encoded <- Future(LocalFileEncoder(read))
        hashed <- Future(LocalFileHasher(encoded))
      } yield hashed
    }

    // Wait for everything to finish and turn in to an UploadableLocalFile
    val uploadableFiles = hashedFutures.map { hashedFuture =>
      val hashed = Await.result(hashedFuture, Duration.Inf)

      UploadableLocalFile(
        siteRelativePath=hashed.siteRelativePath,
        contentMd5=hashed.contentMd5,
        contentEncoding=hashed.contentEncoding,
        cacheControl=cacheControlHeader,
        body=hashed.body
      )
    }

    // Shut. Everything. Down.
    executeContext.shutdown()

    // Return a map
    uploadableFiles.map { uploadable => 
      (uploadable.siteRelativePath, uploadable)
    }.toMap
  }
}

