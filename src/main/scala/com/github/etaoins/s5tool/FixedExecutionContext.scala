package com.github.etaoins.s5tool

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object FixedExecutionContext {
  def apply(poolSize : Int) = {
    ExecutionContext.fromExecutorService(
      Executors.newFixedThreadPool(poolSize)
    )
  }
}

