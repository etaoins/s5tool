package com.github.etaoins.s5tool

/** Abstract information needed to identify a file
  *
  * This is useful to perform comparisons between remote and local files 
  */
sealed trait SiteFile {
  def siteRelativePath : String
  def contentMd5 : Array[Byte]
  def contentEncoding : Option[String]
  def cacheControl : String
}

/** Metadata and content of a local file eligible for uploading */
case class LocalFile(
  siteRelativePath : String,
  contentMd5 : Array[Byte],
  contentEncoding : Option[String],
  cacheControl : String,
  body : Array[Byte]
) extends SiteFile

/** Metadata of a file remotely located on S3 */ 
case class RemoteFile(
  siteRelativePath : String,
  contentMd5 : Array[Byte],
  contentEncoding : Option[String],
  cacheControl : String
) extends SiteFile
