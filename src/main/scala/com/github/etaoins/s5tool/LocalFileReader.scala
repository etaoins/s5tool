package com.github.etaoins.s5tool

import java.io.{File,RandomAccessFile}

object LocalFileReader {
  def apply(dirEnt : LocalDirectoryEntry) : LoadedLocalFile = {
    val randomAccessFile = new RandomAccessFile(dirEnt.file, "r")
    val readBuffer = new Array[Byte](randomAccessFile.length.toInt)
    
    randomAccessFile.read(readBuffer)

    LoadedLocalFile(
      siteRelativePath=dirEnt.siteRelativePath,
      body=readBuffer
    )
  }
}
