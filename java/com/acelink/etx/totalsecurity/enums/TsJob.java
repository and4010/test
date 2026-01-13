package com.acelink.etx.totalsecurity.enums;

import org.jetbrains.annotations.Nullable;

/**
 * @author gregho
 * @since 2018/10/11
 */
public enum TsJob {

  NONE(0),
  CONNECT(1),
  DISCONNECT(2),
  RELEASE(3),
  CUSTOM_COMMAND(12),
  KEEP_ALIVE(91),
  SNAPSHOT(100),
  START_RTSP(101),
  STOP_RTSP(102),
  /* PAN_TILT(110), */
  /* START_SEND_AUDIO(150), */
  SEND_AUDIO(151),
  STOP_SEND_AUDIO(152),
  //LAN_DISCONNECT(1004),
  LAN_START_RTSP(1001),
  LAN_STOP_RTSP(1003),
  LAN_DISCONNECT(1004),
  GET_LOG(162);

  private final int value;

  TsJob(int value) {
    this.value = value;
  }

  @Override public String toString() {
    switch (this) {
      case NONE:
        return "None";

      case CONNECT:
        return "Connect";

      case DISCONNECT:
        return "Disconnect";

      case RELEASE:
        return "Release";

      case CUSTOM_COMMAND:
        return "Custom command";

      case KEEP_ALIVE:
        return "Keep alive";

      case SNAPSHOT:
        return "Snapshot";

      case START_RTSP:
        return "Start RTSP";

      case LAN_START_RTSP:
        return "LAN_START_RTSP";

      case STOP_RTSP:
        return "Stop RTSP";

      case LAN_STOP_RTSP:
        return "LAN_STOP_RTSP";

      case LAN_DISCONNECT:
        return "LAN_DISCONNECT";
      case SEND_AUDIO:
        return "Send audio";

      case STOP_SEND_AUDIO:
        return "Stop send audio";

      case GET_LOG:
        return "Get log";

      default:
        return "Unknown";
    }
  }

  public int getValue() {
    return value;
  }

  @Nullable public static TsJob fromValue(int value) {
    for (TsJob job : values()) {
      if (job.value == value) {
        return job;
      }
    }

    return null;
  }
}
