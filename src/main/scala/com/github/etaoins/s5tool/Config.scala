package com.github.etaoins.s5tool

import java.io.File

case class Config(
  filesystemRoot : Option[File] = None,
  bucketName : String = "",
  maxAge : Option[Int] = None
)

