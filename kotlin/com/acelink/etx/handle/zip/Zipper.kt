package com.acelink.etx.handle.zip

import com.acelink.etx.EtxLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author gregho
 * @since 2018/11/30
 */
class Zipper private constructor(
  private val size: Int,
  private val listener: ZipResultListener,
  timeout: Long
) {

  private val successes = ArrayList<Result>(size)
  private val failures = ArrayList<Result>(size)
  private val blockUntilReceived = timeout == -1L
  private val endedTime = System.currentTimeMillis() + timeout

  companion object Functions {

    private const val TAG = "Zipper"

    @JvmStatic
    fun with(
      size: Int,
      listener: ZipResultListener,
      timeout: Long = -1L
    ): Zipper {
      return Zipper(size, listener, timeout)
    }
  }

  fun add(
    result: Boolean,
    id: String = "",
    function: String = ""
  ) {
    synchronized(this) {
      if ((blockUntilReceived or isNotTimeout()) and (count() < size)) {
        if (result) {
          successes.add(Result(id, function))
        } else {
          failures.add(Result(id, function))
        }
      }
    }
  }

  fun zip() {
    GlobalScope.launch {
      while (blockUntilReceived or isNotTimeout()) {
        if (count() == size) {
          dumpSuccesses()
          dumpFailures()
          listener.onZipped(successes, failures)
          break
        }

        /* check again after 100 millisecond */
        delay(100)
      }

      if (count() != size) {
        dumpSuccesses()
        dumpFailures()
        listener.onTimeout(successes, failures)
      }
    }
  }

  private fun isNotTimeout(): Boolean {
    return System.currentTimeMillis() <= endedTime
  }

  private fun count(): Int {
    return successes.size + failures.size
  }

  private fun dumpSuccesses() {
    EtxLogger.log(TAG, "ZIP", "succeeded")
    for (success in successes) {
      EtxLogger.log(message = success.toString())
    }
  }

  private fun dumpFailures() {
    EtxLogger.log(TAG, "ZIP", "failed")
    for (failure in failures) {
      EtxLogger.log(message = failure.toString())
    }
  }
}