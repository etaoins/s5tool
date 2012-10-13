package com.github.etaoins.s5tool

import java.io.File
import com.amazonaws.auth.AWSCredentials

object SiteSynchronizer {
  def apply(config : Config, awsCredentials : AWSCredentials) = {
    val targetState = TargetStateCalculator(config)

    // Print everything out for debugging
    for((relativePath, file) <- targetState) {
      val hexMd5 = file.contentMd5.map("%02x" format _).mkString
      println(file.siteRelativePath + ": " + hexMd5 + " (" + file.contentEncoding.getOrElse("identity") + ")")
    }
  }
}

