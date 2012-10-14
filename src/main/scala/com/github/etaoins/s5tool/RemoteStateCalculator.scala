package com.github.etaoins.s5tool

import scala.collection.JavaConversions._

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{S3ObjectSummary,ObjectMetadata}

import java.util.concurrent.Executors
import akka.dispatch.{ExecutionContext,Future,Await}
import akka.util.duration._

object RemoteStateCalculator {
  def apply(credentials : AWSCredentials)(bucketName : String) : Map[String, RemoteFile] = {
    /** Execution context for HTTP operations */
    implicit val httpContext = ExecutionContext.fromExecutorService(
      Executors.newFixedThreadPool(6)
    )

    // Get the raw S3 data
    val s3Client = new AmazonS3Client(credentials)
    val objectSummaries = s3Client.listObjects(bucketName).getObjectSummaries()

    val remoteFileFutures = objectSummaries.map { summary =>
      Future {
        val metadata = s3Client.getObjectMetadata(bucketName, summary.getKey())

        RemoteFile(
          siteRelativePath=summary.getKey(),
          contentMd5=metadata.getETag().sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte),
          contentEncoding=Option(metadata.getContentEncoding()),
          cacheControl=metadata.getCacheControl()
        )
      }
    }

    // Await, convert to map
    val remoteState = remoteFileFutures.map { remoteFileFuture => 
      val remoteFile = Await.result(remoteFileFuture, 1 day)

      (remoteFile.siteRelativePath, remoteFile)
    }.toMap

    // Shut down the HTTP execution context
    httpContext.shutdown()

    // Return the remote state
    remoteState
  }
}

