package com.github.etaoins.s5tool

import scopt.immutable._
import java.io.File

case class Config(
  filesystemRoot : String,
  bucketName : String
)

object S5Tool extends App {
  val parser = new OptionParser[Config]("s5tool", "0.0,1") {
    def options = Seq(
      arg("<source>", "source directory") { (v: String, c: Config) => c.copy(filesystemRoot = v) },
      arg("<bucket>", "S3 bucket name") { (v: String, c: Config) => c.copy(bucketName = v) }
    )
  }

  parser.parse(args, Config("", "")) map { config =>
    val localDirents = LocalDirectoryEnumerator(new File(config.filesystemRoot))
    val encodedFiles = localDirents.par.map(LocalFileEncoder(_)) 

    for(encoded <- encodedFiles) {
      println(encoded.siteRelativePath + ": " + encoded.contentEncoding.getOrElse("identity"))
    }
  }
}
