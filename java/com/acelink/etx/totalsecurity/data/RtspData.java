package com.acelink.etx.totalsecurity.data;

import com.google.gson.annotations.SerializedName;

import static com.acelink.etx.totalsecurity.data.JsonBeans.PATH;
import static com.acelink.etx.totalsecurity.data.JsonBeans.PORT;

/**
 * @author gregho
 * @since 2018/10/11
 */
public class RtspData extends TotalSecurityData {

  @SerializedName(PATH) private final String path;
  @SerializedName(PORT) private final Integer port;

  public RtspData(String uid) {
    this(uid, null, null);
  }

  public RtspData(String deviceId, String path, Integer port) {
    super(deviceId);
    this.path = path;
    this.port = port;
  }
}
