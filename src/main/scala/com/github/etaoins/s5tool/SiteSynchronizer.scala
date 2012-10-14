package com.github.etaoins.s5tool

import java.io.File
import com.amazonaws.auth.AWSCredentials

object SiteSynchronizer {
  private def printFileInfo(file : SiteFile) {
    val hexMd5 = file.contentMd5.map("%02x" format _).mkString
    println(file.siteRelativePath + ": " + hexMd5 + " (" + file.contentEncoding.getOrElse("identity") + ")")
  }

  def apply(config : Config, awsCredentials : AWSCredentials) = {
    val targetState = TargetStateCalculator(config)

    println("Current local:")
    // Print everything out for debugging
    for((_, file) <- targetState) {
      printFileInfo(file)
    }
    
    println()
    println("Current remote:")
    val remoteState = RemoteStateCalculator(awsCredentials)(config.bucketName)
    for((_, file) <- remoteState) {
      printFileInfo(file)
    }
  }
}

