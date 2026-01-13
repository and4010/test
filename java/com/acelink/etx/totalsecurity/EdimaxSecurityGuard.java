package com.acelink.etx.totalsecurity;

import com.acelink.etx.totalsecurity.enums.TsDebugLevel;

import com.acelink.etx.totalsecurity.listener.EdimaxSecurityGuardListener;


public class EdimaxSecurityGuard {

  /* init defines, do not modify */
  //private static final String TAG = "EdimaxSecurityGuard";
  private static final String COMMAND_CALLBACK_FUNCTION_NAME = "tsCommandCallback";
  private static final String VIDEO_CALLBACK_FUNCTION_NAME = "tsVideoCallback";
  private static final String AUDIO_CALLBACK_FUNCTION_NAME = "tsAudioCallback";
  private static final String MESSAGE_CALLBACK_FUNCTION_NAME = "tsMessageCallback";
  public static final int	TSL_LOG_TYPE_NA        =         0;
  public static final int	TSL_LOG_TYPE_LATENCY	     =      1;
  public static final int TSL_LOG_TYPE_CONNECTION_TIME   =    2;
  public static final int TSL_LOG_TYPE_DEVICE_LIVE        =   3   ;
  public static final int TSL_LOG_TYPE_APP_LIVE          =    4  ;

  public static final int TSL_VIDEO_FRAME_TYPE_SEI=0;

  public static final int TSL_VIDEO_FRAME_TYPE_P=1;

  public static final int TSL_VIDEO_FRAME_TYPE_VSPPS=2;// VPS/SPS or PPS

  public static final int TSL_VIDEO_FRAME_TYPE_I=3;


  private static EdimaxSecurityGuard instance;

  private static EdimaxSecurityGuardListener listener;

  public static synchronized EdimaxSecurityGuard getInstance(EdimaxSecurityGuardListener listener,TsDebugLevel debugLevel) {
    if(instance == null ) {
      instance = new EdimaxSecurityGuard(listener,debugLevel);

    }
    return instance;
  }

  public static  EdimaxSecurityGuard getGuild() {
    if (instance==null)throw new IllegalStateException("EdimaxSecurityGuard not instance yet");
    return instance;
  }

  static {
    try {
      System.loadLibrary("EdimaxSecurityGuard");
    } catch (UnsatisfiedLinkError e) {
      e.printStackTrace();
    }
  }



  private EdimaxSecurityGuard(EdimaxSecurityGuardListener listener, TsDebugLevel debugLevel) {
    this.listener=listener;
    tslInit(COMMAND_CALLBACK_FUNCTION_NAME, VIDEO_CALLBACK_FUNCTION_NAME,
        AUDIO_CALLBACK_FUNCTION_NAME, MESSAGE_CALLBACK_FUNCTION_NAME, debugLevel.getValue());
  }

  public void releaseGuard(){
    tslRelease();
    instance=null;
  }


  /*--------------------------------
   * Callback functions
   *-------------------------------*/

  private void tsCommandCallback(String deviceId, int job, int customId, byte[] content,
      int contentLength,
      int status) {
    listener.tsCommandCallback(deviceId,job,customId,content,contentLength,status);

  }

  private void tsVideoCallback(String deviceId, byte[] content, int contentLength, int format,
      int iFrame, int width, int height, long playTimeMs) {
    listener.tsVideoCallback(deviceId,content,contentLength,format,iFrame,width,height,playTimeMs);
  }

  private void tsAudioCallback(String deviceId, byte[] content, int contentLength, int format, long playTimeMs)
  {
    listener.tsAudioCallback(deviceId,content,contentLength,format,playTimeMs);
  }

  private void tsMessageCallback(int type, String message) {
    listener.tsMessageCallback(type,message);
  }

  /*--------------------------------
   * Native functions
   *-------------------------------*/

  private native void tslInit(String commandCallbackFunctionName, String videoCallbackFunctionName,
      String audioCallbackFunctionName, String messageCallbackFunctionName, int debugLevel);

  private native void tslRelease();

  public native void tslConnect(String tsJson, String caPem, String certPem, String keyPem);

  public native void tslDisconnect(String tsJson);

  public native void tslGetSnapshot(String tsJson);

  public native void tslStartLiveStreaming(String tsJson);

  public native void tslStopLiveStreaming(String tsJson);

  public native void tslSendAudio(String tsJson, byte[] rawAudio, int size);

  public native void tslStopSendAudio(String tsJson);

  public native void tslCustomCommand(String tsJson, String postJson);

   public native int tslGetTunnelState(String tsJson);
}
