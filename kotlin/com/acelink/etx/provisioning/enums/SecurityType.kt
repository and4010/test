package com.acelink.etx.provisioning.enums

/**
 * @author gregho
 * @since 2018/10/3
 */
enum class SecurityType(val value: Int) {

  OPEN(0),
  WEP_PSK(1),
  WPA_TKIP(2),
  WPA_AES(3),
  WPA2_TKIP(4),
  WPA2_AES(5),
  WPA2_MIXED(6),
  UNKNOWN(7);

  override fun toString(): String {
    return when (this) {
      OPEN -> "OPEN"
      WEP_PSK -> "WEP"
      WPA_TKIP -> "WPA TKIP"
      WPA_AES -> "WPA AES"
      WPA2_TKIP -> "WAP2 TKIP"
      WPA2_AES -> "WPA2 AES"
      WPA2_MIXED -> "WPA2 MIXED"
      else -> "UNKNOWN"
    }
  }

  companion object Factory {

    fun fromValue(value: Int): SecurityType {
      for (securityType in values()) {
        if (securityType.value == value) {
          return securityType
        }
      }

      return UNKNOWN
    }

    fun fromCapabilities(capabilities: String): SecurityType {
      return when {
        capabilities.contains("PSK") -> {
          val isAes = capabilities.contains("CCMP")
          val isTkip = capabilities.contains("TKIP")
          when {
            capabilities.contains("WEP") -> WEP_PSK
            capabilities.contains("WPA2") -> when {
              isAes && isTkip -> WPA2_MIXED
              isAes -> WPA2_AES
              isTkip -> WPA2_TKIP
              else -> UNKNOWN
            }
            capabilities.contains("WPA") -> when {
              isAes -> WPA_AES
              isTkip -> WPA_TKIP
              else -> UNKNOWN
            }
            else -> UNKNOWN
          }
        }
        capabilities.contains("ESS") -> OPEN
        else -> UNKNOWN
      }
    }
  }
}