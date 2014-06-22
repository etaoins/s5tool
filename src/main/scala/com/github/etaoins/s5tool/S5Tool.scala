package com.github.etaoins.s5tool

import java.io.{File,FileNotFoundException}
import org.ini4j.Wini
import com.amazonaws.auth.BasicAWSCredentials

object S5Tool extends App {
  val parser = new scopt.OptionParser[Config]("s5tool") {
    opt[Int]('m', "max-age") action { (v, c) =>
      c.copy(maxAge = Some(v))
    } text("maximum time in seconds to allow HTTP clients to cache site files")

    arg[File]("source") action { (path, c) =>
      c.copy(filesystemRoot=Some(path))
    } text("source directory")

    arg[String]("bucket") action { (v, c) =>
      c.copy(bucketName = v)
    } text("S3 bucket name")
  }

  // Parse our arguments first
  parser.parse(args, Config()) map { config =>
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
