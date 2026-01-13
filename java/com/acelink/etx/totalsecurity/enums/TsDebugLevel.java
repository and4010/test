package com.acelink.etx.totalsecurity.enums;

/**
 * @author gregho
 * @since 2018/11/22
 */
public enum TsDebugLevel {

  NONE(0),
  ERROR(1),
  STATUS(2),
  STREAMING(3),
  ALL(4);

  private final int value;

  TsDebugLevel(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
