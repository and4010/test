package com.acelink.etx.handle.api.cipher.internal

import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import android.util.Base64
import java.security.Key
import javax.crypto.Cipher

/**
 * @author Ray Huang
 * @since 2018/8/30
 */
/**
 * This class wraps [Cipher] class apis with some additional possibilities.
 */
@RequiresApi(VERSION_CODES.JELLY_BEAN_MR2)
internal class CipherWrapper {

  private val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

  fun encrypt(
    data: String,
    key: Key?
  ): String {
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val bytes = cipher.doFinal(data.toByteArray())
    return Base64.encodeToString(bytes, Base64.DEFAULT)
  }

  fun decrypt(
    data: String,
    key: Key?
  ): String {
    cipher.init(Cipher.DECRYPT_MODE, key)
    val encryptedData = Base64.decode(data, Base64.DEFAULT)
    val decodedData = cipher.doFinal(encryptedData)
    return String(decodedData)
  }
}