package com.acelink.etx.totalsecurity.data;

import static com.acelink.etx.totalsecurity.data.JsonBeans.PATH;
import static com.acelink.etx.totalsecurity.data.JsonBeans.PORT;

import com.google.gson.annotations.SerializedName;

/**
 * @author gregho
 * @since 2018/10/11
 */
public class MicTwoWayLANData {
  @SerializedName("addr") private final String address;
  @SerializedName("cgi") private final String cgi="/cgi2/audio.cgi";
  @SerializedName(PORT) private int port=56088;

  @SerializedName("username") private String username = "admin";

  @SerializedName("password") private String password = "1234";
  @SerializedName("format") int format = 0;
  @SerializedName("type") int type = 1;

  @SerializedName("timeout") int timeout = 30;
  public MicTwoWayLANData(String address, String username, String password) {
    this.address = address;
    this.username=username;
    this.password=password;
  }
}
