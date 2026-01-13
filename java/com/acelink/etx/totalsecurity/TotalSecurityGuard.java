package com.acelink.etx.totalsecurity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.acelink.etx.EtxLogger;
import com.acelink.etx.totalsecurity.data.AudioData;
import com.acelink.etx.totalsecurity.data.CustomCommandData;
import com.acelink.etx.totalsecurity.data.ConnectData;
import com.acelink.etx.totalsecurity.data.RtspData;
import com.acelink.etx.totalsecurity.data.SnapshotData;
import com.acelink.etx.totalsecurity.enums.TsDebugLevel;
import com.acelink.etx.totalsecurity.enums.TsJob;
import com.acelink.etx.totalsecurity.enums.TsOutputAudioFormat;
import com.acelink.etx.totalsecurity.enums.TsRtpFormat;
import com.acelink.etx.totalsecurity.enums.TsStatus;
import com.acelink.etx.totalsecurity.enums.TsTunnel;
import com.acelink.etx.totalsecurity.listener.EdimaxLANGuardListener;
import com.acelink.etx.totalsecurity.listener.EdimaxSecurityGuardListener;
import com.acelink.etx.totalsecurity.listener.SimpleTsResponseListener;
import com.acelink.etx.totalsecurity.listener.TsMessageListener;
import com.acelink.etx.totalsecurity.listener.TsResponseListener;
import com.acelink.etx.totalsecurity.media.codec.CodecFormat;
import com.acelink.etx.totalsecurity.media.rtsp.RtspPlayer;
import com.acelink.etx.utils.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.acelink.etx.handle.api.constants.EtxConstants.CLOUD_CA;
import static com.acelink.etx.handle.api.constants.EtxConstants.CLOUD_CA_V2;
import static com.acelink.etx.totalsecurity.enums.TsJob.START_RTSP;
import static com.acelink.etx.totalsecurity.enums.TsJob.STOP_RTSP;


public class TotalSecurityGuard implements EdimaxSecurityGuardListener {

  /* init defines, do not modify */
  private static final String TAG = "TotalSecurityGuard";




  private final Gson tsGson;
  private final Map<String, RtspPlayer> rtspPlayerMap;
  private final List<SimpleTsResponseListener> listeners;
  private int cloudVersion=1;
  public TotalSecurityGuard(int cloudVersion) {
    this(TsDebugLevel.ALL,cloudVersion);
  }

  public TotalSecurityGuard(TsDebugLevel debugLevel,int cloudVersion) {
    this.cloudVersion=cloudVersion;
    this.tsGson = new GsonBuilder().create();
    this.rtspPlayerMap = new ConcurrentHashMap<>();
    this.listeners = new CopyOnWriteArrayList<>();
     EdimaxSecurityGuard.getInstance(this,debugLevel);
  }

  /*--------------------------------
   * Public functions
   *-------------------------------*/

  public void subscribe(@NonNull SimpleTsResponseListener observer) {
    synchronized (listeners) {
      if (listeners.contains(observer)) {
        return;
      }

      listeners.add(observer);
    }
  }

  public void unsubscribe(@NonNull SimpleTsResponseListener observer) {
    synchronized (listeners) {
      listeners.remove(observer);
    }
  }

  public void unsubscribeAll() {
    synchronized (listeners) {
      for (SimpleTsResponseListener li : listeners){
        if(!(li instanceof TsgPlugin))listeners.remove(li);
      }
    }
  }

  public void clearObservers() {
    synchronized (listeners) {
      listeners.clear();
    }
  }

  public void releaseRtspPlayer() {
    synchronized (rtspPlayerMap) {
      rtspPlayerMap.clear();
    }
  }

  public void connect(@NonNull String serverDns, @NonNull String certPem,
      @NonNull String privateKeyPem, @NonNull String deviceId, @NonNull String token,
      TsTunnel.Rule rule, String vendor, String project, int timeoutMs) {
    String json = toJson(new ConnectData(deviceId, serverDns, token, vendor, project, rule.getValue(), timeoutMs));
   // EdimaxSecurityGuard.getGuild().tslConnect(json, cloudVersion==2? CLOUD_V2_GO_CA2 : CLOUD_CA, certPem, privateKeyPem);
    EdimaxSecurityGuard.getGuild().tslConnect(json,  CLOUD_CA_V2, certPem, privateKeyPem);
    EtxLogger.log(TAG, "CONNECT", "CONNECT json="+json);

  }

  public void disconnect(@NonNull String deviceId) {
    String json = toJson(new ConnectData(deviceId));
    EdimaxSecurityGuard.getGuild().tslDisconnect(json);
    EtxLogger.log(TAG, "DISCONNECT", json);
  }

  public void snapshot(@NonNull String deviceId, String path) {
    String json = toJson(new SnapshotData(deviceId, path));
    EdimaxSecurityGuard.getGuild().tslGetSnapshot(json);
    EtxLogger.log(TAG, "SNAPSHOT", json);
  }



  public void startStreaming(@NonNull String deviceId, @NonNull String path, int port, TextureView textureView) {
    RtspPlayer rtspPlayer = getRtspPlayer(deviceId);
    if (rtspPlayer == null //|| rtspPlayer.formatChanged(videoFormat, audioFormat)
    ) {
      /* this is safe to be invoked either rtsp player is existed or not */
      //removeRtspPlayer(deviceId);
      rtspPlayer = new RtspPlayer();//new RtspPlayer(videoFormat, audioFormat);
      addRtspPlayer(deviceId, rtspPlayer);
    }

    /* handler can only decode one device */
    if (!rtspPlayer.isStreaming()) {
      EtxLogger.log(TAG, "tsVideoCallback ", "startStreaming deviceId="+deviceId);
      rtspPlayer.startStreaming();
      if (textureView != null) {
        rtspPlayer.setTextureView(textureView);
      }else{
        Log.e( "START STREAMING", deviceId + " surface is null");

      }

      String json = toJson(new RtspData(deviceId, path, port));
      EdimaxSecurityGuard.getGuild().tslStartLiveStreaming(json);
      EtxLogger.log(TAG, "START STREAMING", json);
    } else {
      Log.e( "START STREAMING", deviceId + " already streaming");
    }
  }

  public void stopStreaming(@NonNull String deviceId) {
    releaseRtspPlayer(deviceId);
    String json = toJson(new RtspData(deviceId));
    EdimaxSecurityGuard.getGuild().tslStopLiveStreaming(json);
    EtxLogger.log(TAG, "STOP STREAMING", json);
  }

  public int getRawDataWidth(@NonNull String deviceId) {
    return getRtspPlayer(deviceId).getRawDataWidth();
  }

  public int getRawDataHeight(@NonNull String deviceId) {
    return getRtspPlayer(deviceId).getRawDataHeight();
  }

  public void sendAudio(@NonNull String deviceId, TsOutputAudioFormat format, @NonNull byte[] rawAudio) {
    String json = toJson(new AudioData(deviceId, format.getValue()));
    EdimaxSecurityGuard.getGuild().tslSendAudio(json, rawAudio, rawAudio.length);
    EtxLogger.log(TAG, "SEND OLD AUDIO", json);


  }

  public void stopSendAudio(@NonNull String deviceId, TsOutputAudioFormat format) {
    String json = toJson(new AudioData(deviceId, format.getValue()));
    EdimaxSecurityGuard.getGuild().tslStopSendAudio(json);
    EtxLogger.log(TAG, "STOP SEND AUDIO", json);
  }

  public boolean getSpeakerState(@NonNull String deviceId) {
    try {
      return Preconditions.requireNonNull(getRtspPlayer(deviceId),
          "get speaker state failure, no such RTSP handler")
          .getSpeakerState();
    } catch (NullPointerException e) {
      return false;
    }
  }

  public void setSpeakerState(@NonNull String deviceId, boolean state) {
    try {
      Preconditions.requireNonNull(getRtspPlayer(deviceId),
          "set speaker state failure, no such RTSP handler")
          .setSpeakerState(state);
    } catch (NullPointerException e) {
    }
  }

  public void switchTargetView(@NonNull String deviceId, TextureView target) {
    try {
      Preconditions.requireNonNull(getRtspPlayer(deviceId),
          "switch target view failure, no such RTSP handler")
          .switchTargetView(target);
    } catch (NullPointerException e) {
    }
  }

  public void customCommand(@NonNull String deviceId, int customId, @NonNull String postJson) {
    String json = toJson(new CustomCommandData(deviceId, customId));
    EdimaxSecurityGuard.getGuild().tslCustomCommand(json, postJson);
    EtxLogger.log(TAG, "CUSTOM COMMAND", json);
    EtxLogger.log("", "", postJson);
  }

  public void getConnectionState(@NonNull String deviceId) {
    String json = toJson(new ConnectData(deviceId));
    EdimaxSecurityGuard.getGuild().tslGetTunnelState(json);
    EtxLogger.log(TAG, "GET CONNECTION STATE", json);
  }

  public void release() {
    EdimaxSecurityGuard.getGuild().releaseGuard();
    EtxLogger.log(TAG, "RELEASE");
  }

  /*--------------------------------
   * RTSP handler functions
   *-------------------------------*/

  private void addRtspPlayer(String deviceId, RtspPlayer rtspPlayer) {
    synchronized (rtspPlayerMap) {
      rtspPlayerMap.put(deviceId, rtspPlayer);
    }
  }

  private RtspPlayer getRtspPlayer(String deviceId) {
    synchronized (rtspPlayerMap) {
      return rtspPlayerMap.get(deviceId);
    }
  }

  private void removeRtspPlayer(String deviceId) {
    synchronized (rtspPlayerMap) {
      rtspPlayerMap.remove(deviceId);
    }
  }

  private void releaseRtspPlayer(String deviceId) {
    EtxLogger.log(TAG, "tsVideoCallback ", "releaseRtspPlayer deviceId="+deviceId);
    try {
      RtspPlayer rtspPlayer = Preconditions.requireNonNull(getRtspPlayer(deviceId));
      rtspPlayer.stop();;
    } catch (Exception e) {
    }
  }

  /*--------------------------------
   * Private functions
   *-------------------------------*/

  private String toJson(Object object) {
    return tsGson.toJson(object);
  }

  private boolean isSuccessful(TsStatus tsStatus) {
    return tsStatus == TsStatus.SUCCESS || tsStatus == TsStatus.TUNNEL_OPENED;
  }

  private void processStartRtsp(String deviceId, TsStatus tsStatus, byte[] data, int size) {
    EtxLogger.log(TAG, "processStartRtsp", "processStartRtsp callback deviceId ="+deviceId+",tsStatus="+tsStatus);
    try {

      //RtspPlayer rtspPlayer = Preconditions.requireNonNull(getRtspPlayer(deviceId), "start rtsp failure, no such RTSP player");
      if (isSuccessful(tsStatus)) {
        boolean h265=false;
       /* if(size>5){
          for(int i=3;i<size;i++){
            if(data[i]==66&&data[i-1]==1&&data[i-2]==0&&data[i-3]==0&&data[i-4]==0){
              h265=true;
              break;
            }
          }
        }*/
      // if(size>=5&&data[4]==66)
        if(size>=5&&data[4] >> 1 ==32)
        {
         h265=true;
       }
        RtspPlayer rtspPlayer = getRtspPlayer(deviceId);
        if (rtspPlayer == null || h265?rtspPlayer.getVideoFormat()!=CodecFormat.VIDEO_HEVC : rtspPlayer.getVideoFormat()!=CodecFormat.VIDEO_AVC)
        {
          TextureView oldTextureView = null;
          /* this is safe to be invoked either rtsp player is existed or not */
          if(rtspPlayer!=null){
            oldTextureView=rtspPlayer.getTextureView();
            rtspPlayer.stop();
            rtspPlayer.setTextureView(null);
          }
          removeRtspPlayer(deviceId);
          rtspPlayer = new RtspPlayer(h265?CodecFormat.VIDEO_HEVC:CodecFormat.VIDEO_AVC, CodecFormat.AUDIO_AAC_LC);
          if(oldTextureView!=null)
            rtspPlayer.setTextureView(oldTextureView);
          if (!rtspPlayer.isStreaming())
            rtspPlayer.startStreaming();
          addRtspPlayer(deviceId, rtspPlayer);
        }
        EtxLogger.log(TAG, "processStartRtsp", "processStartRtsp callback h265="+h265);
          rtspPlayer.prepareVideoDecoder(data, size);


      } else {
        /* if failed, release rtsp player */
        releaseRtspPlayer(deviceId);
      }
    } catch (NullPointerException e) {
    }
  }

  private void processStopRtsp(String deviceId, TsStatus tsStatus) {
    /* release rtsp player if is not release */
    releaseRtspPlayer(deviceId);
  }

  private void processSendAudio(TsStatus tsStatus) {
    /* TODO: some handles */
  }

  private void processStopSendAudio(TsStatus tsStatus) {
    /* TODO: some handles */
  }

  private void processDisconnect(String deviceId, TsStatus tsStatus) {
    /* stop handling stream when receive disconnect */
    releaseRtspPlayer(deviceId);
  }

  private void processOthers(String deviceId, TsStatus tsStatus) {
    /* stop handling stream when received others command failed */
    if (!isSuccessful(tsStatus) && tsStatus != TsStatus.TUNNEL_OPENING) {
      releaseRtspPlayer(deviceId);
    }
  }

  private void notifyObserversOnReceivedCommand(@NonNull String deviceId, @NonNull TsJob job,
      int customId, @Nullable byte[] content, int contentLength, TsStatus status) {
    synchronized (listeners) {
      Iterator<SimpleTsResponseListener> iterator = listeners.iterator();
      while (iterator.hasNext()) {
        iterator.next().onReceiveCommand(deviceId, job, customId, content, contentLength, status);
      }
    }
  }

  private void notifyObserversOnReceivedVideo(@NonNull String deviceId, @Nullable byte[] content,
      int contentLength, TsRtpFormat.VideoFormat format, int iFrame, int width, int height,
      long playTimeMs) {
    synchronized (listeners) {
      Iterator<SimpleTsResponseListener> iterator = listeners.iterator();
      while (iterator.hasNext()) {
        try {
          Preconditions.isInstanceOf(TsResponseListener.class, iterator.next())
              .onReceiveVideo(deviceId, content, contentLength, format,
                  iFrame, width, height, playTimeMs);
        } catch (IllegalArgumentException ignored) {
        }
      }
    }
  }

  private void notifyObserversOnReceivedAudio(@NonNull String deviceId, @Nullable byte[] content,
      int contentLength, TsRtpFormat.AudioFormat format, long playTimeMs) {
    synchronized (listeners) {
      Iterator<SimpleTsResponseListener> iterator = listeners.iterator();
      while (iterator.hasNext()) {
        try {
          Preconditions.isInstanceOf(TsResponseListener.class, iterator.next())
              .onReceiveAudio(deviceId, content, contentLength, format,
                  playTimeMs);
        } catch (IllegalArgumentException ignored) {
        }
      }
    }
  }

  private void notifyObserversOnReceivedMessage(int type,String message) {
    synchronized (listeners) {
      Iterator<SimpleTsResponseListener> iterator = listeners.iterator();
      while (iterator.hasNext()) {
        try {
          Preconditions.isInstanceOf(TsMessageListener.class, iterator.next()).onReceiveMessage(type, message);
        } catch (IllegalArgumentException ignored) {
        }
      }
    }
  }

  /*--------------------------------
   * Callback functions
   *-------------------------------*/

  public void tsCommandCallback(String deviceId, int job, int customId, byte[] content,
      int contentLength,
      int status) {
    TsJob tsJob = TsJob.fromValue(job);
    TsStatus tsStatus = TsStatus.fromValue(status);
    if (Preconditions.isNull(tsJob)) {
      EtxLogger.log(TAG, "COMMAND CALLBACK", "no such job");
      /* ignored unknown job */
      return;
    }

    /* process the callback when the render options is not null
     * and the device id matches to the render options */
    if (getRtspPlayer(deviceId) != null) {
      switch (tsJob) {
        case START_RTSP:
          processStartRtsp(deviceId, tsStatus, content, contentLength);
          break;

        case STOP_RTSP:
          /* stop rtsp might be invoked by device automatically stop */
          processStopRtsp(deviceId, tsStatus);
          break;

        case SEND_AUDIO:
          processSendAudio(tsStatus);
          break;

        case STOP_SEND_AUDIO:
          processStopSendAudio(tsStatus);
          break;

        case DISCONNECT:
          processDisconnect(deviceId, tsStatus);
          break;

        default:
          processOthers(deviceId, tsStatus);
          break;
      }
    }

    notifyObserversOnReceivedCommand(deviceId, tsJob, customId, content, contentLength, tsStatus);
  }

  public void tsVideoCallback(String deviceId, byte[] content, int contentLength, int format,
      int iFrame, int width, int height, long playTimeMs) {
    try {
      TsRtpFormat.VideoFormat videoFormat = TsRtpFormat.VideoFormat.fromValue(format);
      RtspPlayer rtspPlayer = Preconditions.requireNonNull(getRtspPlayer(deviceId));
      if (!rtspPlayer.isStreaming()) {
        notifyObserversOnReceivedVideo(deviceId, content, contentLength, videoFormat, iFrame, width,
            height,
            playTimeMs);
      } else {
        rtspPlayer.decodeVideo(content, contentLength, iFrame/*, width, height, playTimeMs*/);
      }
    } catch (NullPointerException e) {
    }
  }

  public void tsAudioCallback(String deviceId, byte[] content, int contentLength, int format, long playTimeMs)
  {
    try {
      TsRtpFormat.AudioFormat audioFormat = TsRtpFormat.AudioFormat.fromValue(format);
      RtspPlayer rtspPlayer = Preconditions.requireNonNull(getRtspPlayer(deviceId));
      if (!rtspPlayer.isStreaming()) {
        notifyObserversOnReceivedAudio(deviceId, content, contentLength, audioFormat, playTimeMs);
      } else {
       // EtxLogger.log(TAG, "tsAudioCallback",  "TS tsAudioCallback audioFormat="+audioFormat);
        rtspPlayer.decodeAudio(content, contentLength, audioFormat, playTimeMs);
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  public void tsMessageCallback(int type, String message) {
    if(type<EdimaxSecurityGuard.TSL_LOG_TYPE_DEVICE_LIVE)
    EtxLogger.log(TAG, "TS MESSAGE CALLBACK type="+type,  message);
    else {
        notifyObserversOnReceivedMessage(type,message);

    }
  }



}
