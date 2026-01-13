package com.acelink.etx.provisioning.enums

/**
 * @author gregho
 * @since 2018/10/3
 */
enum class ProvisioningState(val value: Int) {

  SUCCESS(0),
  FORMAT_ERROR(1),
  INVALID_COMMAND(2),
  CONNECT_TO_AP_FAILED(11),
  UNABLE_TO_RETRIEVE_IP(12),
  NO_INTERNET(13),
  CONNECT_TO_SERVER_FAILED(14),
  ACTIVITY_ID_NOT_FOUND(15),
  ALREADY_ACTIVATED(16),
  USER_ALREADY_DELETE(21),
  APP_NOT_VERIFY(22),
  CHECKING(99);

  override fun toString(): String {
    return when(this) {
      SUCCESS -> "SUCCESS"
      FORMAT_ERROR -> "FORMAT ERROR"
      INVALID_COMMAND -> "INVALID COMMAND"
      CONNECT_TO_AP_FAILED -> "CONNECT TO AP FAILED"
      UNABLE_TO_RETRIEVE_IP -> "UNABLE TO RETRIEVE IP"
      NO_INTERNET -> "NO INTERNET"
      CONNECT_TO_SERVER_FAILED -> "CONNECT TO SERVER FAILED"
      ACTIVITY_ID_NOT_FOUND -> "ACTIVITY ID NOT FOUND"
      ALREADY_ACTIVATED -> "ALREADY ACTIVATED"
      USER_ALREADY_DELETE -> "USER ALREADY DELETE"
      APP_NOT_VERIFY -> "APP NOT VERIFY"
      CHECKING -> "CHECKING"
    }
  }

  companion object Factory {

    fun fromValue(value: Int?): ProvisioningState {
      for (state in values()) {
        if (state.value == value) {
          return state
        }
      }

      return INVALID_COMMAND
    }
  }
}