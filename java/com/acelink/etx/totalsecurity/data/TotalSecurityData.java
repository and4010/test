package com.acelink.etx.totalsecurity.data;

import com.google.gson.annotations.SerializedName;

import static com.acelink.etx.totalsecurity.data.JsonBeans.DEVICE_ID;

/**
 * @author gregho
 * @since 2018/10/11
 */
public abstract class TotalSecurityData {

  @SerializedName(DEVICE_ID) private final String deviceId;

  TotalSecurityData(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getDeviceId() {
    return deviceId;
  }
}
