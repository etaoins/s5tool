package com.github.etaoins.s5tool

import scala.collection.JavaConversions._

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{S3ObjectSummary,ObjectMetadata}

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext,Future,Await}
import scala.concurrent.duration.Duration

object RemoteStateCalculator {
  def apply(s3Client : AmazonS3Client)(bucketName : String) : Map[String, RemoteFile] = {
    /** Execution context for HTTP operations */
    implicit val httpContext = FixedExecutionContext(6)

    // Get the raw S3 data
    val objectSummaries = s3Client.listObjects(bucketName).getObjectSummaries()

    val remoteFileFutures = objectSummaries.map { summary =>
      Future {
        val metadata = s3Client.getObjectMetadata(bucketName, summary.getKey())

        RemoteFile(
          siteRelativePath=summary.getKey(),
          contentMd5=metadata.getETag().sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte),
          contentEncoding=Option(metadata.getContentEncoding()),
          cacheControl=Option(metadata.getCacheControl())
        )
      }
    }

    // Await, convert to map
    val remoteState = remoteFileFutures.map { remoteFileFuture => 
      val remoteFile = Await.result(remoteFileFuture, Duration.Inf)

      (remoteFile.siteRelativePath, remoteFile)
    }.toMap

    // Shut down the HTTP execution context
    httpContext.shutdown()

    // Return the remote state
    remoteState
  }
}

