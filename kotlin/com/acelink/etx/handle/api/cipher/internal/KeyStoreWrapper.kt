package com.acelink.etx.handle.api.cipher.internal

import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.util.Calendar
import javax.security.auth.x500.X500Principal

/**
 * @author Ray Huang
 * @since 2018/8/30
 */
/**
 * This class wraps [KeyStore] class apis with some additional possibilities.
 */
internal class KeyStoreWrapper(private val context: Context) {

  private val keyStore: KeyStore by lazy {
    KeyStore.getInstance("AndroidKeyStore")
        .also {
          it.load(null)
        }
  }

  fun getAndroidKeyStoreAsymmetricKeyPair(alias: String): KeyPair {
    return try {
      val publicKey = keyStore.getCertificate(alias)
          .publicKey
      val privateKey = keyStore.getKey(alias, null) as PrivateKey
      KeyPair(publicKey, privateKey)
    } catch (e: Exception) {
      createAndroidKeyStoreAsymmetricKey(alias)
    }
  }

  fun removeAndroidKeyStoreKey(alias: String) = keyStore.deleteEntry(alias)

  private fun createAndroidKeyStoreAsymmetricKey(alias: String): KeyPair {
    val generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
    initGeneratorWithKeyGenParameterSpec(generator, alias)
    return generator.generateKeyPair()
  }


  private fun initGeneratorWithKeyGenParameterSpec(
    generator: KeyPairGenerator,
    alias: String
  ) {
    val builder = KeyGenParameterSpec.Builder(
        alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
    generator.initialize(builder.build())
  }
}