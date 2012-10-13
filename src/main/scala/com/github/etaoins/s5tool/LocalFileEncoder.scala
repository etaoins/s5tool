package com.github.etaoins.s5tool

import java.io._
import java.util.zip._

object LocalFileEncoder {
  // The Content-Encoding declaration takes up space 
  private val gzipOverheadBytes = "Content-Encoding: gzip\r\n".length

  private def gzipEncode(input : Array[Byte]) : Array[Byte] = {
    val byteArrayOutputStream = new ByteArrayOutputStream()
    val gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)

    gzipOutputStream.write(input)
    gzipOutputStream.close()

    byteArrayOutputStream.toByteArray()
  }

  def apply(loadedFile : LoadedLocalFile) : EncodedLocalFile = {
    val compressedData = gzipEncode(loadedFile.body)

    if ((loadedFile.body.length - compressedData.length) > gzipOverheadBytes) {
      // Compressed wins!
      EncodedLocalFile(
        siteRelativePath = loadedFile.siteRelativePath,
        contentEncoding = Some("gzip"),
        body = compressedData
      )
    }
    else {
      EncodedLocalFile(
        siteRelativePath = loadedFile.siteRelativePath,
        contentEncoding = None,
        body = loadedFile.body
      )
    }
  }
}
