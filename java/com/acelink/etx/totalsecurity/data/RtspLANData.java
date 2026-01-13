package com.acelink.etx.totalsecurity.data;

import static com.acelink.etx.totalsecurity.data.JsonBeans.PATH;
import static com.acelink.etx.totalsecurity.data.JsonBeans.PORT;

import com.google.gson.annotations.SerializedName;

/**
 * @author gregho
 * @since 2018/10/11
 */
public class RtspLANData  {
  @SerializedName("addr") private final String address;
  @SerializedName(PATH) private final String path;
  @SerializedName(PORT) private final Integer port;

  @SerializedName("username") private String username = "admin";

  @SerializedName("password") private String password = "1234";
  @SerializedName("type") int type = 0;


  public RtspLANData(String address, String path, Integer port,String username,String password) {
    this.address = address;
    this.path = path;
    this.port = port;
    this.username=username;
    this.password=password;
  }
}
