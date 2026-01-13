package com.acelink.etx.totalsecurity.data;

import com.google.gson.annotations.SerializedName;

import static com.acelink.etx.totalsecurity.data.JsonBeans.ENDPOINT;
import static com.acelink.etx.totalsecurity.data.JsonBeans.PROJECT;
import static com.acelink.etx.totalsecurity.data.JsonBeans.TIMEOUT;
import static com.acelink.etx.totalsecurity.data.JsonBeans.TOKEN;
import static com.acelink.etx.totalsecurity.data.JsonBeans.TUNNEL_RULE;
import static com.acelink.etx.totalsecurity.data.JsonBeans.VENDOR;

/**
 * @author gregho
 * @since 2018/10/11
 */
public class ConnectData extends TotalSecurityData {

  @SerializedName(ENDPOINT) private final String endPoint;
  @SerializedName(TOKEN) private final String token;
  @SerializedName(VENDOR) private final String vendor;
  @SerializedName(PROJECT) private final String project;
  @SerializedName(TUNNEL_RULE) private final Integer rule;
  @SerializedName(TIMEOUT) private final Integer timeout;

  public ConnectData(String deviceId) {
    this(deviceId, null, null, null, null, null, null);
  }

  public ConnectData(String deviceId, String endPoint, String token, String vendor, String project,
                     Integer rule,Integer timeout) {
    super(deviceId);
    this.endPoint = endPoint;
    this.token = token;
    this.vendor = vendor;
    this.project = project;
    this.rule = rule;
    this.timeout = timeout;
  }
}
