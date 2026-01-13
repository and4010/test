package com.acelink.etx.totalsecurity.data;

import com.google.gson.annotations.SerializedName;

import static com.acelink.etx.totalsecurity.data.JsonBeans.AUDIO_FORMAT;

/**
 * @author gregho
 * @since 2018/10/11
 */
public class AudioData extends TotalSecurityData {

  @SerializedName(AUDIO_FORMAT) private final Integer format;

  //VS8 Channel
  @SerializedName("channel") private  String channel;

  public AudioData(String deviceId, Integer format,String channel) {
    super(deviceId);
    this.format = format;
    this.channel = channel;
  }

  public AudioData(String deviceId, Integer format) {
    super(deviceId);
    this.format = format;
  }
}
