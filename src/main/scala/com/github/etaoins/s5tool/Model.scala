package com.github.etaoins.s5tool

import java.io.File

/** Abstract information needed to identify a file
  *
  * This is useful to perform comparisons between remote and local files 
  */
sealed trait SiteFile {
  def siteRelativePath : String
  def contentMd5 : Array[Byte]
  def contentEncoding : Option[String]
  def cacheControl : String

  def sameContentAs(that : SiteFile) : Boolean = {
    (this.siteRelativePath == that.siteRelativePath) &&
    (this.contentMd5.toSeq == that.contentMd5.toSeq) &&
    (this.contentEncoding == that.contentEncoding) &&
    (this.cacheControl == that.cacheControl)
  }
}

/** Local file that has been discovered but is unprocessed */
case class LocalDirectoryEntry(
  siteRelativePath : String,
  file : File
)

/** Local file that has been loaded from disk */
case class LoadedLocalFile(
  siteRelativePath : String,
  body : Array[Byte]
)

/** Local file that has had its body encoded */
case class EncodedLocalFile(
  siteRelativePath : String,
  contentEncoding : Option[String],
  body : Array[Byte]
)

/** Local file that has been hashed */
case class HashedLocalFile(
  siteRelativePath : String,
  contentEncoding : Option[String],
  contentMd5 : Array[Byte],
  body : Array[Byte]
)

/** Metadata and content of a local file eligible for uploading */
case class UploadableLocalFile(
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
