package com.github.etaoins.s5tool

import java.io.File
import java.io.ByteArrayInputStream
import scala.concurrent.{Future,Await}
import scala.concurrent.duration.Duration

import java.net.FileNameMap
import java.net.URLConnection

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.internal.Mimetypes

object SiteSynchronizer {
  def apply(config : Config, awsCredentials : AWSCredentials) = {
    implicit val executeContext = FixedExecutionContext(6)
    
    // Create an S3 client
    val s3Client = new AmazonS3Client(awsCredentials)
    
    // Get our target and remote state concurrently
    val targetStateFuture = Future {
      TargetStateCalculator(config)
    }

    val remoteStateFuture = Future {
      RemoteStateCalculator(s3Client)(config.bucketName)
    }

    val targetState = Await.result(targetStateFuture, Duration.Inf)
    val remoteState = Await.result(remoteStateFuture, Duration.Inf)

    // Upload any new or changed files
    targetState.filter { case (relativePath, file) =>
      remoteState.get(relativePath).map(!_.sameContentAs(file)).getOrElse(true)
    }.map { case (key, file) =>
      val metadata = new ObjectMetadata

      // Guess the content type
      val contentType = Mimetypes.getInstance().getMimetype(key)
      metadata.setContentType(contentType)

      // Set all the metadata we previously calculated
      file.contentEncoding.map { contentEncoding =>
        metadata.setContentEncoding(contentEncoding)
      }

      file.cacheControl.map { cacheControl =>
        metadata.setCacheControl(cacheControl)
      }

      metadata.setContentLength(file.body.length)

      println("Uploading " + key)

      val istream = new ByteArrayInputStream(file.body)
      s3Client.putObject(config.bucketName, key, istream, metadata)
    } 
    
    // Delete any extra files
    (remoteState.keys.toSet -- targetState.keys.toSet).map { key =>
      println("Deleting " + key)
      s3Client.deleteObject(config.bucketName, key)
    }
      
    executeContext.shutdown();
  }
}

