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

  private def fileToBytes(file : java.io.File) : Array[Byte] = {
    val randomAccessFile = new RandomAccessFile(file, "r")
    val readBuffer = new Array[Byte](randomAccessFile.length.toInt)
    
    randomAccessFile.read(readBuffer)

    return readBuffer
  }

  def apply(dirEntry : LocalDirectoryEntry) : EncodedLocalFile = {
    val uncompressedData = fileToBytes(dirEntry.file)
    val compressedData = gzipEncode(uncompressedData)

    if ((uncompressedData.length - compressedData.length) > gzipOverheadBytes) {
      // Compressed wins!
      EncodedLocalFile(
        siteRelativePath = dirEntry.siteRelativePath,
        contentEncoding = Some("gzip"),
        body = compressedData
      )
    }
    else {
      EncodedLocalFile(
        siteRelativePath = dirEntry.siteRelativePath,
        contentEncoding = None,
        body = uncompressedData
      )
    }
  }
}
