package com.acelink.etx.handle.zip

/**
 * @author gregho
 * @since 2018/11/30
 */
data class Result(
  val id: String,
  val function: String
) {

  override fun toString() = "id: $id, function: $function"
}