package com.acelink.etx.totalsecurity;

import android.util.Log;

import com.acelink.etx.totalsecurity.enums.TsDebugLevel;
import com.acelink.etx.totalsecurity.listener.EdimaxLANGuardListener;


public class EdimaxLANGuard {

  /* init defines, do not modify */
  //private static final String TAG = "EdimaxLANGuard";

  public static final int	LIFE_LAN_JOB_NONE        =         0;

  public static final int	PT_API_LAN_START_STREAMING	     =      1;
  public static final int PT_API_LAN_STOP_STREAMING   =    2;
  public static final int PT_API_LAN_API_COMMAND        =   3   ;

  public static final int PT_ERR_SUCCESS = 0;
  public static final int PT_ERR_SIZE = -1;
  public static final int PT_ERR_PARSER = -2;
  public static final int PT_ERR_FORMAT = -3;
  public static final int PT_ERR_MEMORY = -4;
  public static final int PT_ERR_INIT = -10;
  public static final int PT_ERR_INIT_VIDEO = -11;
  public static final int PT_ERR_INIT_AUDIO = -12;
  public static final int PT_ERR_UID = -13;
  public static final int PT_ERR_JSON = -14;
  public static final int PT_ERR_UID_NOT_FOUND = -15;
  public static final int PT_ERR_PARAM = -16;
  public static final int PT_ERR_SESSION_CLOSED = -17;
  public static final int PT_ERR_SESSION_OPENED = -18;
  public static final int PT_ERR_PASSWORD = -20;
  public static final int PT_ERR_PATH = -21;
  public static final int PT_ERR_TOO_MANY = -22;
  public static final int PT_ERR_PRIVACY = -23;
  public static final int PT_ERR_STREAMING = -24;
  public static final int PT_ERR_DISCONNECTED = -25;
  public static final int PT_ERR_CONNECT = -26;

  public static final int PT_VIDEO_FRAME_TYPE_VSPPS   = 0;
  public static final int PT_VIDEO_FRAME_TYPE_SEI   = 1;
  public static final int PT_VIDEO_FRAME_TYPE_I   = 2;
  public static final int PT_VIDEO_FRAME_TYPE_P   = 3;


  private static EdimaxLANGuard instance;

  static {
    try {
      System.loadLibrary("PTSCstreamAPI");
    } catch (UnsatisfiedLinkError e) {
      e.printStackTrace();
    }
  }

  private static EdimaxLANGuardListener listener;

  public static synchronized EdimaxLANGuard getInstance(EdimaxLANGuardListener listener, TsDebugLevel debugLevel) {
    if(instance == null ) {
      instance = new EdimaxLANGuard(listener,debugLevel);

    }
    return instance;
  }

  public static EdimaxLANGuard getGuild() {
    if (instance==null)throw new IllegalStateException("EdimaxLANGuard not instance yet");
    return instance;
  }

  static {
    try {
      System.loadLibrary("PTSCstreamAPI");
    } catch (UnsatisfiedLinkError e) {
      e.printStackTrace();
    }
  }



  private EdimaxLANGuard(EdimaxLANGuardListener listener, TsDebugLevel debugLevel) {
    this.listener=listener;
    var a = -1;
    a = psInit(
            "com.edimax.glplifesdk.PLifeManager",
            "LifeMainCallback",
            "LifeVideoCallback",
            "LifeAudioCallback",
            "LifeMsgCallback",
            debugLevel.getValue(),
            "Copyright 2020 (C) EDIMAX Technology Co., Ltd. All Rights Reserved."
    );
     Log.e("EdimaxLANGuard", "LifeManager init="+a);

  }

  public void releaseGuard(){

    instance=null;
  }


  /*--------------------------------
   * Callback functions
   *-------------------------------*/

  private void LifeMainCallback(String deviceId, int job, int customId, byte[] content,
      int contentLength,
      int status) {
    listener.LifeLanMainCallback(deviceId,job,customId,content,contentLength,status);

  }

  private void LifeVideoCallback(String deviceId, byte[] content, int contentLength, int format,
      int iFrame, int width, int height, long playTimeMs) {
    listener.LifeLanVideoCallback(deviceId,content,contentLength,format,iFrame,width,height,playTimeMs);
  }

  private void LifeAudioCallback(String deviceId, byte[] content, int contentLength, int format, long playTimeMs)
  {
    listener.LifeLanAudioCallback(deviceId,content,contentLength,format,playTimeMs);
  }

  private void LifeMsgCallback(int type, String message) {
    listener.LifeLanMsgCallback(type,message);
  }

  /*--------------------------------
   * Native functions
   *-------------------------------*/

  /**
   * must call init first.
   *
   * @param strMyClassPathName   input this class package name
   * @param strCallBackName      input job callback function name
   * @param strCallBackVideoName input video callback function name
   * @param strCallBackAudioName input audio callback function name
   * nDebugLevel // 0:None, 1:Err log, 2:connection log, 3:streaming log, 4: reserve, 5:status log, 6: all
   */
  private native int  psInit(
          String strMyClassPathName, String strCallBackName,
          String strCallBackVideoName, String strCallBackAudioName,String strCallBackMsgName, int nDebugLevel,
          String copyRight
  );

  /**
   * customer defined job
   *
   * const char strUuid[] = "204112D6-60E5-4496-BB3C-FB1B9BED74E2";
   * const char strIp[] = "192.168.2.3";
   * const double intPort = 8481;
   * const char strPT[] = "JSON";
   * const char strPC[] = "NA";
   * const int intSid = 3;
   * const char strContent[] = "{\"cmd\":\"get_lpr\""};
   * const int intContentLen = strlen(strContent);
   * //get_lpr
   * ps_Command(strUuid, strIp, intPort, intContentLen, strPT, strPC, intSid, strContent)
   */
   private native void psCommand(
           String strUuid,
           String strIp,
          int nPort,
           int nContentLen,
           String zpPT,
           String zpPC,
           int nSID,
           String zpContent
  );

  //native void connect(String strJSON);
  public  native void psStartStreaming(String ID ,String strJSON);
 public native void psStopStreaming(String uuid);
//{"type": 1, "addr": "192.168.8.22", "port": 56088, "username": "admin", "password": "1234", "cgi": "/cgi2/audio.cgi", "format": 0, "timeout": 30}
 public native void psStartAudioOut(String ID ,String strJSON);

  public native void psSendAudioOut(String ID ,byte[] data,int size);
  public native void psStopAudioOut(String ID );
}
