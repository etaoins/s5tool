package com.github.etaoins.s5tool

import scopt.immutable._
import java.io.{File,FileNotFoundException}
import org.ini4j.Wini
import com.amazonaws.auth.BasicAWSCredentials

object S5Tool extends App {
  val parser = new OptionParser[Config]("s5tool", "0.0,1") {
    def options = Seq(
      intOpt("m", "max-age", "maximum time in seconds to allow HTTP clients to cache site files. Defaults to 300") { (v: Int, c: Config) => c.copy(maxAge = v) },
      arg("<source>", "source directory") { (v: String, c: Config) => c.copy(filesystemRoot = v) },
      arg("<bucket>", "S3 bucket name") { (v: String, c: Config) => c.copy(bucketName = v) }
    )
  }

  // Parse our arguments first
  parser.parse(args, Config("", "", 300)) map { config =>
    // Now read .s3cfg
    val homeDir = System.getProperty("user.home")

    val s3cfg = try {
      new Wini( new File(homeDir + "/.s3cfg"))
    } catch {
      case _ : FileNotFoundException =>
        throw new Exception("Unable to open ~/.s3cfg. Please create it manually or by using s3cmd --configure")
    }

    // Use the default config
    val defaultConfig = s3cfg.get("default")

    // Use it to build our credentials
    val awsCredentials = new BasicAWSCredentials(
      defaultConfig.get("access_key"),
      defaultConfig.get("secret_key")
    )

    SiteSynchronizer(config, awsCredentials)
  }
}
