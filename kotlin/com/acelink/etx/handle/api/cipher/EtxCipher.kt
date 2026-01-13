package com.acelink.etx.handle.api.cipher

import android.content.Context
import android.content.SharedPreferences
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import com.acelink.cloud.tsc.app.data.EndpointData
import com.acelink.cloud.tsc.app.data.UserEnpointData
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.DEMO_SG
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.DEV_PORTAL
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.DEV_SG
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.DEV_SG_PORTAL
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.PROD_EDI
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.PROD_EDI_PORTAL
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.PROD_PORTAL
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.PROD_VG
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.PROD_OR
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.PROD_OR_PORTAL
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.PROD_VG_PORTAL
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.QA_EDI
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.QA_PORTAL
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.QA_SG
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.V2_DEV_NAME
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.V2_QA_SG_NAME
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.V2_UAT_NAME
import com.acelink.etx.EtxLogger
import com.acelink.etx.handle.api.SafetyFlavor
import com.acelink.etx.handle.api.cipher.internal.CipherWrapper
import com.acelink.etx.handle.api.cipher.internal.KeyStoreWrapper

/**
 * @author Ray Huang
 * @since 2018/8/30
 */
@RequiresApi(VERSION_CODES.JELLY_BEAN_MR2)
class EtxCipher(private val context: Context) {

  private companion object Constants {

    const val TAG = "EtxCipher"
    const val PREFS_FILENAME = "edimax.p1"
    const val ENCODE_MAX_SIZE = 245
    const val KEY_PREFIX = "tsKey"
    const val ALIAS = "MASTER_KEY"
    const val FLAVOR = "flavor"
    const val PROD = "prod"
    const val QA = "qa"
    const val DEV = "dev"
    const val DEMO = "demo"
  }

  private val sharedPreferences: SharedPreferences by lazy {
    context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
  }
  private val keyPair by lazy {
    keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair(
      ALIAS
    )
  }
  private val keyStoreWrapper by lazy {
    KeyStoreWrapper(context)
  }
  private val cipherWrapper by lazy {
    CipherWrapper()
  }

  /*--------------------------------
   * Public functions
   *-------------------------------*/

  @WorkerThread
  fun saveTscKeyPair(
    etxKeyPair: EtxKeyPair
  ) {
    val key = etxKeyPair.privateKeyPem
    val indexCount = getStringSplitSize(etxKeyPair.certPem.length)
    parsePemEncode(etxKeyPair.certPem, 0)
    parsePemEncode(key, indexCount + 1)
    EtxLogger.log(TAG, "saveTscKeyPair", "key pair saved")
    EtxLogger.log(TAG, "saveTscKeyPair", "get flavor = ${getFlavor()}")
  }

  @WorkerThread
  fun getTscKeyPair(): EtxKeyPair? {
    val indexCount = sharedPreferences.getInt("${KEY_PREFIX}0", 0)
    try {
      return if (indexCount > 1) {
        var certPem = parsePemDecode(0, indexCount)
        val keyIndex = indexCount + 1
        val key = "$KEY_PREFIX$keyIndex"
        val size = sharedPreferences.getInt(key, 0)
        var keyPem = parsePemDecode(keyIndex, size)
        /*
       //expired cert
     certPem="-----BEGIN CERTIFICATE-----\n" +
               "MIIDjjCCAzSgAwIBAgIUHL826EBPiZcRq0ny8BuYZpNUlLEwCgYIKoZIzj0EAwIw\n" +
               "QjELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRQw\n" +
               "EgYDVQQDEwtFZGltYXhBcHBDQTAeFw0xOTAxMDkwMzEzMDBaFw0yMTAxMDgwMzEz\n" +
               "MDBaMF8xCzAJBgNVBAYTAlRXMQ4wDAYDVQQHEwVQcm9DMTEPMA0GA1UEChMGRWRp\n" +
               "bWF4MQ0wCwYDVQQLEwRUZXN0MSAwHgYDVQQDDBdbdGVzdF0gRWRpbWF4IFRlc3Qg\n" +
               "VEVTVDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABPVJx+aYg4ZpKlN3Cc23LDix\n" +
               "EjlNg+Go59BnRuUBYhmeowbrv6yMrZO89uFtipc9sJbEsAi+2D1piqh1NsYI+u2j\n" +
               "ggHpMIIB5TAOBgNVHQ8BAf8EBAMCA8gwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsG\n" +
               "AQUFBwMEMAwGA1UdEwEB/wQCMAAwHQYDVR0OBBYEFI6oJ0JXUxMNulV7iFC7z3B2\n" +
               "LdKLMB8GA1UdIwQYMBaAFIcLHg5VTH4po+m86j5T5jxdXOnFMH0GCCsGAQUFBwEB\n" +
               "BHEwbzAwBggrBgEFBQcwAYYkaHR0cDovL2VkaXByb2Qtb2NzcC1hcHBjYS5lZGl0\n" +
               "c2MuY29tMDsGCCsGAQUFBzAChi9odHRwOi8vZWRpcHJvZC1jcnQuZWRpdHNjLmNv\n" +
               "bS9lZGltYXhfYXBwX2NhLnBlbTCBrAYDVR0RBIGkMIGhgg90ZXMxLmVkaXRzYy5j\n" +
               "b22CD3RlczIuZWRpdHNjLmNvbYIPdGVzMy5lZGl0c2MuY29tgglQMjAxODA5MDmB\n" +
               "EXRlc3QxQHRlc3QuY29tLnR3gRF0ZXN0MkB0ZXN0LmNvbS50d4cEAQEBAYcEAgIC\n" +
               "AocQIAENuIWjAAAAAIouA3BzNIYdaHR0cHM6Ly93d3cuZ29vZ2xlLmNvbS9zZWFy\n" +
               "Y2gwOAYDVR0fBDEwLzAtoCugKYYnaHR0cDovL2VkaXByb2QtY3JsLmVkaXRzYy5j\n" +
               "b20vYXBwY2EuY3JsMAoGCCqGSM49BAMCA0gAMEUCIQDgQMI2YtCg0y6a/XK1TH/V\n" +
               "XxGhKVI8YPFjyZ8rZVEAHQIgU+U/eHYLEy3FCzsan8tnRxvxViZ2V3toqm6j6jMU\n" +
               "MbA=\n" +
               "-----END CERTIFICATE-----\n" +
               "-----BEGIN CERTIFICATE-----\n" +
               "MIICpTCCAkygAwIBAgIUchcCmfuWJiRr2sWOyjTe2vYcv7wwCgYIKoZIzj0EAwIw\n" +
               "QzELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRUw\n" +
               "EwYDVQQDEwxFZGltYXhSb290Q0EwHhcNMTgxMjI2MDgyMDAwWhcNMjgxMjIzMDgy\n" +
               "MDAwWjBCMQswCQYDVQQGEwJUVzEPMA0GA1UEChMGRWRpbWF4MQwwCgYDVQQLEwNJ\n" +
               "b1QxFDASBgNVBAMTC0VkaW1heEFwcENBMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcD\n" +
               "QgAE2maa/yTE5EJsgL+7Tk3o26MrAHO/7eQcJudSJDWyRiSg+ZWuGP1otOJjRsqZ\n" +
               "PAQSVEnWk8NtJEKopyn4BGIoS6OCAR0wggEZMA4GA1UdDwEB/wQEAwIBhjASBgNV\n" +
               "HRMBAf8ECDAGAQH/AgEAMB0GA1UdDgQWBBSHCx4OVUx+KaPpvOo+U+Y8XVzpxTAf\n" +
               "BgNVHSMEGDAWgBT+pR2/kXIno8z89M7VJxLSwff/YDB4BggrBgEFBQcBAQRsMGow\n" +
               "MQYIKwYBBQUHMAGGJWh0dHA6Ly9lZGlwcm9kLW9jc3Atcm9vdGNhLmVkaXRzYy5j\n" +
               "b20wNQYIKwYBBQUHMAKGKWh0dHA6Ly9lZGlwcm9kLWNydC5lZGl0c2MuY29tL3Jv\n" +
               "b3RfY2EucGVtMDkGA1UdHwQyMDAwLqAsoCqGKGh0dHA6Ly9lZGlwcm9kLWNybC5l\n" +
               "ZGl0c2MuY29tL3Jvb3RjYS5jcmwwCgYIKoZIzj0EAwIDRwAwRAIgK+MMcPhKmOXs\n" +
               "2ICmohCnhNPmNuSF+TwSKYYcYFZkFeACIBOLrN9M8sb1opKePvmpR0g28ApPmDVr\n" +
               "ZvVNQtiQhjFV\n" +
               "-----END CERTIFICATE-----\n"
       keyPem="-----BEGIN EC PRIVATE KEY-----\n" +
               "MHcCAQEEIFWhAP5gRi4k39YIFoX05SXeCmf45GtDStzL3lphTNKCoAoGCCqGSM49\n" +
               "AwEHoUQDQgAE9UnH5piDhmkqU3cJzbcsOLESOU2D4ajn0GdG5QFiGZ6jBuu/rIyt\n" +
               "k7z24W2Klz2wlsSwCL7YPWmKqHU2xgj67Q==\n" +
               "-----END EC PRIVATE KEY-----\n"
      */
        EtxKeyPair(certPem, keyPem)
      } else {
        return null
      }
    } catch (e: Exception) {
      return null
    }
  }

  @WorkerThread
  fun saveFlavor(flavor: SafetyFlavor) {
    saveString2Preferences(FLAVOR, flavor.name)
  }

  @WorkerThread
  fun getFlavor(): String {
    return sharedPreferences.getString(FLAVOR, "")!!
  }

  @WorkerThread
  fun saveEndpoint(endpointData: EndpointData) {
  //  EtxLogger.log("enpoint", "saveEndpoint ${TsIotJsonParser.create(endpointData)} ")
    var encodeEndpoint: String
    /* Production 3 endpoint */
    if (endpointData.prodUser != null) {
      encodeEndpoint = endpointData.prodUser!!.userEndpoint!!
      saveString2Preferences(PROD, encodeEndpoint)

      if (endpointData.prodUser!!.portalEndpoint != null) {
        encodeEndpoint = endpointData.prodUser!!.portalEndpoint!!
        saveString2Preferences(PROD_PORTAL, encodeEndpoint)
      }
    }

    if (endpointData.prodUserEdi != null) {
      encodeEndpoint = endpointData.prodUserEdi!!.userEndpoint!!
      saveString2Preferences(PROD_EDI, encodeEndpoint)
      if (endpointData.prodUserEdi!!.portalEndpoint != null) {
        encodeEndpoint = endpointData.prodUserEdi!!.portalEndpoint!!
        saveString2Preferences(PROD_EDI_PORTAL, encodeEndpoint)
      }
    }

    if (endpointData.prodUserVg != null) {
      encodeEndpoint = endpointData.prodUserVg!!.userEndpoint!!
      saveString2Preferences(PROD_VG, encodeEndpoint)
      if (endpointData.prodUserVg!!.portalEndpoint != null) {
        encodeEndpoint = endpointData.prodUserVg!!.portalEndpoint!!
        saveString2Preferences(PROD_VG_PORTAL, encodeEndpoint)
      }
    }

    if (endpointData.prodUserOr != null) {
      encodeEndpoint = endpointData.prodUserOr!!.userEndpoint!!
      saveString2Preferences(PROD_OR, encodeEndpoint)
      if (endpointData.prodUserOr!!.portalEndpoint != null) {
        encodeEndpoint = endpointData.prodUserOr!!.portalEndpoint!!
        saveString2Preferences(PROD_OR_PORTAL, encodeEndpoint)
      }
    }

    /* QA 3 endpoint */
    if (endpointData.qaUser != null) {
      encodeEndpoint = endpointData.qaUser!!.userEndpoint!!
      saveString2Preferences(QA, encodeEndpoint)
      /* portal endpoint */
      if (endpointData.qaUser!!.portalEndpoint != null) {
        encodeEndpoint = endpointData.qaUser!!.portalEndpoint!!
        saveString2Preferences(QA_PORTAL, encodeEndpoint)
      }
    }
    if (endpointData.qaUserEdi != null) {
      encodeEndpoint = endpointData.qaUserEdi!!.userEndpoint!!
      saveString2Preferences(QA_EDI, encodeEndpoint)
    }
    if (endpointData.qaUserSg != null) {
      encodeEndpoint = endpointData.qaUserSg!!.userEndpoint!!
      saveString2Preferences(QA_SG, encodeEndpoint)
      /* portal endpoint */
      if (endpointData.qaUserSg!!.portalEndpoint != null) {
        encodeEndpoint = endpointData.qaUserSg!!.portalEndpoint!!
        saveString2Preferences(QA_PORTAL, encodeEndpoint)
      }
    }
    /* Develop 2 endpoint */
    if (endpointData.devUser != null) {
      encodeEndpoint = endpointData.devUser!!.userEndpoint!!
      saveString2Preferences(DEV, encodeEndpoint)
      /* portal endpoint */
      if (endpointData.devUser!!.portalEndpoint != null) {
        encodeEndpoint = endpointData.devUser!!.portalEndpoint!!
        saveString2Preferences(DEV_PORTAL, encodeEndpoint)
      }
    }
    if (endpointData.devUserSg != null) {
      encodeEndpoint = endpointData.devUserSg!!.userEndpoint!!
      saveString2Preferences(DEV_SG, encodeEndpoint)
      if (endpointData.devUserSg!!.portalEndpoint != null) {
        encodeEndpoint = endpointData.devUserSg!!.portalEndpoint!!
        saveString2Preferences(DEV_SG_PORTAL, encodeEndpoint)
      }
    }
    /* Demo 2 endpoint */
    if (endpointData.demoUser != null) {
      encodeEndpoint = endpointData.demoUser!!.userEndpoint!!
      saveString2Preferences(DEMO, encodeEndpoint)
    }
    if (endpointData.demoUserSg != null) {
      encodeEndpoint = endpointData.demoUserSg!!.userEndpoint!!
      saveString2Preferences(DEMO_SG, encodeEndpoint)
     /* if (endpointData.demoUserSg!!.portalEndpoint != null) {
        encodeEndpoint = endpointData.demoUserSg!!.portalEndpoint!!
        saveString2Preferences(DEMO_SG_PORTAL, encodeEndpoint)
      }*/
    }

    //V2
    if (endpointData.v2QaSGUser != null) {
      encodeEndpoint = endpointData.v2QaSGUser!!.userEndpoint!!
      saveString2Preferences(V2_QA_SG_NAME, encodeEndpoint)
    }
    if (endpointData.v2DevSGUser != null) {
      encodeEndpoint = endpointData.v2DevSGUser!!.userEndpoint!!
      saveString2Preferences(V2_DEV_NAME, encodeEndpoint)
    }
    if (endpointData.v2UATUser != null) {
      encodeEndpoint = endpointData.v2UATUser!!.userEndpoint!!
      saveString2Preferences(V2_UAT_NAME, encodeEndpoint)
    }
  }

  @WorkerThread
  fun getUserEndpoint(): EndpointData {
    return EndpointData(
      UserEnpointData(sharedPreferences.getString(PROD, ""), sharedPreferences.getString(PROD_PORTAL, "")),
      UserEnpointData(sharedPreferences.getString(PROD_EDI, ""), sharedPreferences.getString(PROD_EDI_PORTAL, "")),
      UserEnpointData(sharedPreferences.getString(PROD_VG, ""),sharedPreferences.getString(PROD_VG_PORTAL, "")),
       UserEnpointData(sharedPreferences.getString(PROD_OR, ""),sharedPreferences.getString(PROD_OR_PORTAL, "")),
      UserEnpointData(sharedPreferences.getString(QA, ""), sharedPreferences.getString(QA_PORTAL, "")),
      UserEnpointData(sharedPreferences.getString(QA_EDI, "")),
      UserEnpointData(sharedPreferences.getString(QA_SG, ""), sharedPreferences.getString(QA_PORTAL, "")),
      UserEnpointData(sharedPreferences.getString(DEV, ""), sharedPreferences.getString(DEV_PORTAL, "")),
      UserEnpointData(sharedPreferences.getString(DEV_SG, "")),
      UserEnpointData(sharedPreferences.getString(DEMO, "")),
      demoUserSg = UserEnpointData(sharedPreferences.getString(DEMO_SG, "")),
      v2DevSGUser = UserEnpointData(sharedPreferences.getString(V2_DEV_NAME, "")),
      v2QaSGUser = UserEnpointData(sharedPreferences.getString(V2_QA_SG_NAME, "")),
      v2UATUser = UserEnpointData(sharedPreferences.getString(V2_UAT_NAME, "")),
    )
  }

  fun clearTscKeyPair() {
    sharedPreferences.edit()
      .clear()
      .apply()
  }

  /*--------------------------------
   * Private functions
   *-------------------------------*/

  private fun getStringSplitSize(size: Int): Int {
    return if (size % ENCODE_MAX_SIZE != 0) {
      size / ENCODE_MAX_SIZE + 1
    } else {
      size / ENCODE_MAX_SIZE
    }
  }

  @WorkerThread
  private fun parsePemEncode(
    pemString: String,
    start: Int
  ) {
    val indexCount = getStringSplitSize(pemString.length)
    var index = 1
    if (indexCount > 0) {
      sharedPreferences.edit()
        .putInt("$KEY_PREFIX$start", indexCount)
        .apply()
      while (indexCount >= index) {
        val sI = (index - 1) * ENCODE_MAX_SIZE
        val eI = if (index == indexCount) {
          pemString.length
        } else {
          index * ENCODE_MAX_SIZE
        }

        val text = pemString.substring(sI, eI)
        val encodeText = cipherWrapper.encrypt(text, keyPair.public)
        val key = "$KEY_PREFIX${start + index}"
        sharedPreferences.edit()
          .putString(key, encodeText)
          .apply()
        index++
      }
    }
  }

  @WorkerThread
  private fun parsePemDecode(
    startIndex: Int,
    size: Int
  ): String {
    var pemString = ""
    var loopIndex = 1
    while (loopIndex <= size) {
      val key = "$KEY_PREFIX${startIndex + loopIndex}"
      pemString += cipherWrapper.decrypt(sharedPreferences.getString(key, "")!!, keyPair.private)
      loopIndex++
    }

    return pemString
  }

  @WorkerThread
  private fun saveString2Preferences(key: String, encodeText: String) {
    sharedPreferences.edit()
      .putString(key, encodeText)
      .apply()
  }
}

