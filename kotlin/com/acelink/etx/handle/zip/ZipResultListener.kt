package com.acelink.etx.handle.zip

/**
 * @author gregho
 * @since 2018/11/30
 */
interface ZipResultListener {

  fun onZipped(
    successes: List<Result>,
    failures: List<Result>
  )

  fun onTimeout(
    successes: List<Result>,
    failures: List<Result>
  )
}