package com.acelink.etx.provisioning.enums;

import androidx.annotation.NonNull;


public enum ProvisioningStatus {

  UNKNOWN(-1),
  SUCCESS(0),
  INVALID_PRIVATE_KEY(-100),
  INVALID_NONCE_SIZE(-101),
  GENERATE_SIGNATURE_FAILURE(-102),
  INVALID_PAYLOAD_SIZE(-103),
  INVALID_CERTIFICATE(-104),
  VERIFY_CERTIFICATE_FAILURE(-105),
  VERIFY_NONCE_FAILURE(-106),
  GENERATE_KEY_FAILURE(-107),
  PARSE_FAILURE(-108),
  INVALID_FORMAT(-109),
  ENCRYPT_DECRYPT_FAILURE(-110),
  CONNECTION_FAILED(-111),
  TIMEOUT(-112),
  TUNNEL_CLOSED(-113);

  private final int value;

  ProvisioningStatus(int status) {
    this.value = status;
  }

  @Override public String toString() {
    switch (this) {
      case UNKNOWN:
      default:
        return "UNKNOWN";

      case SUCCESS:
        return "SUCCESS";

      case INVALID_PRIVATE_KEY:
        return "INVALID PRIVATE KEY";

      case INVALID_NONCE_SIZE:
        return "INVALID NONCE SIZE";

      case GENERATE_SIGNATURE_FAILURE:
        return "GENERATE SIGNATURE FAILURE";

      case INVALID_PAYLOAD_SIZE:
        return "INVALID PAYLOAD SIZE";

      case INVALID_CERTIFICATE:
        return "INVALID CERTIFICATE";

      case VERIFY_CERTIFICATE_FAILURE:
        return "VERIFY CERTIFICATE FAILURE";

      case VERIFY_NONCE_FAILURE:
        return "VERIFY NONCE FAILURE";

      case GENERATE_KEY_FAILURE:
        return "GENERATE KEY FAILURE";

      case PARSE_FAILURE:
        return "PARSE FAILURE";

      case INVALID_FORMAT:
        return "INVALID FORMAT";

      case ENCRYPT_DECRYPT_FAILURE:
        return "ENCRYPT/DECRYPT FAILURE";

      case CONNECTION_FAILED:
        return "CONNECTION FAILED";

      case TIMEOUT:
        return "TIMEOUT";

      case TUNNEL_CLOSED:
        return "TUNNEL CLOSED";
    }
  }

  public int getValue() {
    return value;
  }

  @NonNull public static ProvisioningStatus fromValue(int value) {
    for (ProvisioningStatus status : values()) {
      if (status.value == value) {
        return status;
      }
    }

    return UNKNOWN;
  }
}
