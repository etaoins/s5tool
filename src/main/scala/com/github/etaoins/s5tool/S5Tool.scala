package com.github.etaoins.s5tool

import scopt.immutable._
import java.io.File

object S5Tool extends App {
  val parser = new OptionParser[Config]("s5tool", "0.0,1") {
    def options = Seq(
      intOpt("m", "max-age", "maximum time in seconds to allow HTTP clients to cache site files. Defaults to 300") { (v: Int, c: Config) => c.copy(maxAge = v) },
      arg("<source>", "source directory") { (v: String, c: Config) => c.copy(filesystemRoot = v) },
      arg("<bucket>", "S3 bucket name") { (v: String, c: Config) => c.copy(bucketName = v) }
    )
  }

  parser.parse(args, Config("", "", 300)) map { config =>
    SiteSynchronizer(config)
  }
}
