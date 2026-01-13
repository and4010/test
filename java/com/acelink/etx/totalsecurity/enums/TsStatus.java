package com.acelink.etx.totalsecurity.enums;

import androidx.annotation.NonNull;

import com.acelink.etx.totalsecurity.EdimaxLANGuard;

/**
 * @author gregho
 * @since 2018/10/11
 */
public enum TsStatus {

  UNKNOWN(-1),
  SUCCESS(0),
  INVALID_JOB(-2),
  INVALID_SIZE(-3),
  PARSE_ERROR(-5),
  INVALID_FORMAT(-6),
  DEVICE_NOT_FOUND(-10),
  CLOUD_NOT_FOUND(-11),
  CLOUD_NO_RESPONSE(-12),
  FW_TOO_OLD(-13),
  TUNNEL_CLOSE(-20),
  TUNNEL_OPENED(-21),
  TUNNEL_OPENING(-22),
  TUNNEL_CLOSING(-23),
  CONNECTION_FAILED(-24),
  LAN_TIMEOUT(-25),
  RELAY_CLOSED(-26),
  PASSWORD_ERROR(-30),
  COMMAND_NOT_SUPPORT(-31),
  COMMAND_TOO_MANY(-32),
  COMMAND_RUNNING(-33),
  PRIVACY_ERROR(-35),
  NETWORK_ISSUE_ERROR(-36),
  PROFILE_ERROR(-37),
  PATH_ERROR(-38),
  BITRATE_ERR(-39);

  private final int value;

  TsStatus(int value) {
    this.value = value;
  }

  @Override public String toString() {
    switch (this) {
      case UNKNOWN:
      default:
        return "UNKNOWN";

      case SUCCESS:
        return "SUCCESS";

      case INVALID_JOB:
        return "INVALID JOB";

      case INVALID_SIZE:
        return "INVALID SIZE";

      case PARSE_ERROR:
        return "PARSE ERROR";

      case INVALID_FORMAT:
        return "INVALID FORMAT";

      case DEVICE_NOT_FOUND:
        return "DEVICE NOT FOUND";

      case CLOUD_NOT_FOUND:
        return "CLOUD NOT FOUND";

      case CLOUD_NO_RESPONSE:

        return "CLOUD NO RESPONSE";

      case FW_TOO_OLD:
        return "FW TOO OLD";

      case TUNNEL_CLOSE:
        return "TUNNEL CLOSE";

      case TUNNEL_OPENED:
        return "TUNNEL OPENED";

      case TUNNEL_OPENING:
        return "TUNNEL OPENING";

      case TUNNEL_CLOSING:
        return "TUNNEL CLOSING";

      case CONNECTION_FAILED:
        return "CONNECTION FAILED";

      case LAN_TIMEOUT:
        return "LAN TIMEOUT";

      case RELAY_CLOSED:
        return "RELAY CLOSE";

      case COMMAND_NOT_SUPPORT:
        return "COMMAND NOT SUPPORT";

      case COMMAND_TOO_MANY:
        return "COMMAND TOO MANY";

      case COMMAND_RUNNING:
        return "COMMAND RUNNING";

      case PRIVACY_ERROR:
        return "PRIVACY ERROR";
      case BITRATE_ERR:
        return "BITRATE ERROR";
    }
  }

  public int getValue() {
    return value;
  }

  @NonNull public static TsStatus fromValue(int value) {
    for (TsStatus status : values()) {
      if (status.value == value) {
        return status;
      }
    }

    return UNKNOWN;
  }

  @NonNull public static TsStatus fromLANValue(int value) {
    if(value==EdimaxLANGuard.PT_ERR_SUCCESS){
      return TsStatus.SUCCESS;
    }else  if(value==EdimaxLANGuard.PT_ERR_SIZE){
      return TsStatus.INVALID_SIZE;
    }else  if(value==EdimaxLANGuard.PT_ERR_PARSER){
      return TsStatus.PARSE_ERROR;
    }else  if(value==EdimaxLANGuard.PT_ERR_FORMAT) {
      return TsStatus.INVALID_FORMAT;
    }else  if(value==EdimaxLANGuard.PT_ERR_MEMORY) {
      return TsStatus.INVALID_SIZE;
    }else  if(value==EdimaxLANGuard.PT_ERR_INIT) {
      return TsStatus.INVALID_SIZE;
    }else  if(value==EdimaxLANGuard.PT_ERR_INIT_VIDEO||value==EdimaxLANGuard.PT_ERR_INIT_AUDIO) {
      return TsStatus.NETWORK_ISSUE_ERROR;
    }else  if(value==EdimaxLANGuard.PT_ERR_UID) {
      return TsStatus.INVALID_FORMAT;
    }else  if(value==EdimaxLANGuard.PT_ERR_JSON) {
      return TsStatus.INVALID_FORMAT;
    }else  if(value==EdimaxLANGuard.PT_ERR_UID_NOT_FOUND) {
      return TsStatus.INVALID_FORMAT;
    }else  if(value==EdimaxLANGuard.PT_ERR_PARAM) {
      return TsStatus.INVALID_FORMAT;
    }else  if(value==EdimaxLANGuard.PT_ERR_SESSION_CLOSED) {
     return TsStatus.TUNNEL_CLOSE;
    }else  if(value==EdimaxLANGuard.PT_ERR_SESSION_OPENED) {
      return TsStatus.TUNNEL_OPENED;
    }else  if(value==EdimaxLANGuard.PT_ERR_PASSWORD) {
      return TsStatus.PASSWORD_ERROR;
    }else  if(value==EdimaxLANGuard.PT_ERR_PATH) {
      return TsStatus.PATH_ERROR;
    }else  if(value==EdimaxLANGuard.PT_ERR_TOO_MANY) {

    }else  if(value==EdimaxLANGuard.PT_ERR_PRIVACY) {
      return TsStatus.PRIVACY_ERROR;
    }else  if(value==EdimaxLANGuard.PT_ERR_STREAMING) {
      return TsStatus.BITRATE_ERR;
    }else  if(value==EdimaxLANGuard.PT_ERR_DISCONNECTED) {
      return TsStatus.RELAY_CLOSED;
    }else  if(value==EdimaxLANGuard.PT_ERR_CONNECT) {
      return TsStatus.RELAY_CLOSED;
    }


    return UNKNOWN;
  }


}
