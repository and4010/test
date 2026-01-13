package com.acelink.etx.handle.api.cipher

/**
 * @author gregho
 * @since 2018/9/25
 */
data class EtxKeyPair(
  val certPem: String,
  val privateKeyPem: String
) {

  override fun toString(): String {
    return "cert: $certPem\nkey: $privateKeyPem"
  }
}