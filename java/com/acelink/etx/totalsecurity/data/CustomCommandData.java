package com.acelink.etx.totalsecurity.data;

import com.google.gson.annotations.SerializedName;

import static com.acelink.etx.totalsecurity.data.JsonBeans.CUSTOM_ID;

/**
 * @author gregho
 * @since 2018/10/11
 */
public class CustomCommandData extends TotalSecurityData {

  @SerializedName(CUSTOM_ID) private final Integer customId;

  public CustomCommandData(String deviceId, Integer customId) {
    super(deviceId);
    this.customId = customId;
  }
}
