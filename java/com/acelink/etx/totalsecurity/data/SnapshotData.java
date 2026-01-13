package com.acelink.etx.totalsecurity.data;

import com.google.gson.annotations.SerializedName;

import static com.acelink.etx.totalsecurity.data.JsonBeans.CUSTOM_ID;
import static com.acelink.etx.totalsecurity.data.JsonBeans.PATH;

/**
 * @author gregho
 * @since 2018/10/11
 */
public class SnapshotData extends TotalSecurityData {

  @SerializedName(PATH) private final String snapshotPath;
  @SerializedName(CUSTOM_ID) private final int snapshotChannel;

  public SnapshotData(String deviceId, String snapshotPath) {
    super(deviceId);
    this.snapshotPath = snapshotPath;
    if (snapshotPath.length() == 1)
      this.snapshotChannel = Integer.parseInt(snapshotPath);
    else
      this.snapshotChannel = 0;
  }
}
