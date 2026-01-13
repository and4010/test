package com.acelink.etx.totalsecurity.aggregate;

import static com.acelink.etx.handle.api.constants.EtxConstants.CLOUD_CA_V2;
import static com.acelink.etx.totalsecurity.enums.TsJob.LAN_DISCONNECT;
import static com.acelink.etx.totalsecurity.enums.TsJob.LAN_START_RTSP;
import static com.acelink.etx.totalsecurity.enums.TsJob.LAN_STOP_RTSP;

import android.util.Log;
import android.view.TextureView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.acelink.etx.EtxLogger;
import com.acelink.etx.totalsecurity.EdimaxLANGuard;
import com.acelink.etx.totalsecurity.EdimaxSecurityGuard;

import com.acelink.etx.totalsecurity.FrameFPS;
import com.acelink.etx.totalsecurity.TsDevice;
import com.acelink.etx.totalsecurity.aggregate.enums.Dewrap_mode;
import com.acelink.etx.totalsecurity.aggregate.enums.Mount_type;
import com.acelink.etx.totalsecurity.aggregate.rtsp.AggregateRtspPlayer;
import com.acelink.etx.totalsecurity.data.AudioData;
import com.acelink.etx.totalsecurity.data.ConnectData;
import com.acelink.etx.totalsecurity.data.CustomCommandData;
import com.acelink.etx.totalsecurity.data.MicTwoWayLANData;
import com.acelink.etx.totalsecurity.data.RtspData;
import com.acelink.etx.totalsecurity.data.RtspLANData;
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
import com.acelink.etx.utils.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ns.greg.library.fancy_logger.FancyLogger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class AggregateSecurityGuard implements EdimaxSecurityGuardListener , EdimaxLANGuardListener {

  /* init defines, do not modify */
  private static final String TAG = "AggregateSecurityGuard";



  private final Gson tsGson;
  private final Map<String, AggregateRtspPlayer> rtspPlayerMap;
  private final List<SimpleTsResponseListener> listeners;

  private int cloudVersion=1;
  public AggregateSecurityGuard(int cloudVersion) {

    this(TsDebugLevel.ALL,cloudVersion);
  }

  public AggregateSecurityGuard(TsDebugLevel debugLevel,int cloudVersion) {
    this.cloudVersion=cloudVersion;
    this.tsGson = new GsonBuilder().create();
    this.rtspPlayerMap = new ConcurrentHashMap<>();
    this.listeners = new CopyOnWriteArrayList<>();
    EdimaxSecurityGuard.getInstance(this,debugLevel);
    EdimaxLANGuard.getInstance(this,debugLevel);

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
        if(!(li instanceof AggregateTsgPlugin))listeners.remove(li);
      }
    }
  }




  public void connect(@NonNull String serverDns, @NonNull String certPem,
      @NonNull String privateKeyPem, @NonNull String deviceId, @NonNull String token,
      TsTunnel.Rule rule, String vendor, String project, int timeoutMs) {
    String json = toJson(new ConnectData(deviceId, serverDns, token, vendor, project, rule.getValue(), timeoutMs));
    EtxLogger.log(TAG, "tsl Connect", json);
   // EdimaxSecurityGuard.getGuild().tslConnect(json, cloudVersion==2? CLOUD_V2_GO_CA2 : CLOUD_CA, certPem, privateKeyPem);
    EdimaxSecurityGuard.getGuild().tslConnect(json, CLOUD_CA_V2, certPem, privateKeyPem);
  }

  public void disconnect(@NonNull String deviceId) {
    String json = toJson(new ConnectData(deviceId));
    EdimaxSecurityGuard.getGuild().tslDisconnect(json);
    EdimaxLANGuard.getGuild().psStopStreaming(deviceId);
    EtxLogger.log(TAG, "DISCONNECT", json);
  }


  public void snapshot(@NonNull String deviceId, String path) {
    String json = toJson(new SnapshotData(deviceId, path));
    EdimaxSecurityGuard.getGuild().tslGetSnapshot(json);
    EtxLogger.log(TAG, "SNAPSHOT", json);
  }


  public void startStreaming(
          @NonNull TsDevice device,
           @NonNull String path, int port,
          TextureView textureView, ImageView snapshot,
           String ip, String  user, String pass,
          boolean lan, FrameFPS fps)
  {
    AggregateRtspPlayer rtspPlayer = getRtspPlayer(device.getDeviceId());
    if (rtspPlayer == null //|| rtspPlayer.formatChanged(videoFormat, audioFormat)
    ) {
      /* this is safe to be invoked either rtsp player is existed or not */
      //removeRtspPlayer(deviceId);
      rtspPlayer = new AggregateRtspPlayer();//new RtspPlayer(videoFormat, audioFormat);
      addRtspPlayer(device.getDeviceId(), rtspPlayer);
    }

    /* handler can only decode one device */
    if (!rtspPlayer.isStreaming()) {
      EtxLogger.log(TAG, "tsVideoCallback ", "startStreaming deviceId="+device.getDeviceId()+",lan="+lan+",listeners.size="+listeners.size());
      rtspPlayer.startStreaming();
      rtspPlayer.setFPS(fps);
      if (textureView != null) {
        rtspPlayer.setTextureView(textureView);
      }else{
        Log.e( "START STREAMING", device.getDeviceId() + " surface is null");
      }
      rtspPlayer.setSnapShotImage(snapshot);
      if(lan&&ip!=null&&user!=null&&pass!=null){
        rtspPlayer.setTypeLAN(true);
        device.applyLANStartStream();
        startLANStreaming(device.getDeviceId(),ip, path, port,user,pass  );
      }else {
        rtspPlayer.setTypeLAN(false);
        startRelayStream(device.getDeviceId(), path, port);
      }

    } else {
      Log.e( "START STREAMING", device.getDeviceId() + " already streaming");
    }
  }


  private void startRelayStream(@NonNull String deviceId, @NonNull String path, int port){
    String json = toJson(new RtspData(deviceId, path, port));
    EdimaxSecurityGuard.getGuild().tslStartLiveStreaming(json);
    EtxLogger.log(TAG, "START RELAY STREAMING", json);
  }

  public void stopStreaming(@NonNull String deviceId) {
    releaseRtspPlayer(deviceId);
    String json = toJson(new RtspData(deviceId));
    EdimaxSecurityGuard.getGuild().tslStopLiveStreaming(json);
    EdimaxLANGuard.getGuild().psStopStreaming(deviceId);
    FancyLogger.e(TAG, "STOP STREAMING "+ json);
  }

  public int getRawDataWidth(@NonNull String deviceId) {
    return getRtspPlayer(deviceId).getRawDataWidth();
  }

  public int getRawDataHeight(@NonNull String deviceId) {
    return getRtspPlayer(deviceId).getRawDataHeight();
  }

  public Dewrap_mode getDewrapMode(@NonNull String deviceId){
    AggregateRtspPlayer rtsp=getRtspPlayer(deviceId);
    return rtsp==null?null : getRtspPlayer(deviceId).getDewrapMode();

  }

  public void setMountType(@NonNull String deviceId,Mount_type type){
    AggregateRtspPlayer player=getRtspPlayer(deviceId);
    if(player!=null)player.setMountType(type);
  }

  public void setDewrapMode1O(@NonNull String deviceId){
    AggregateRtspPlayer player=getRtspPlayer(deviceId);
    if(player!=null)player.setDewrapMode1O();
  }

  public void setDewrapMode1OToWall1R(@NonNull String deviceId){
    AggregateRtspPlayer player=getRtspPlayer(deviceId);
    if(player!=null)player.setDewrapMode1OToWall1R();
  }

  public void setDewrapMode1P(@NonNull String deviceId){
    getRtspPlayer(deviceId).setDewrapMode1P();
  }




  public void setDewrapMode1OTo1P(@NonNull String deviceId, ImageView img1){
    getRtspPlayer(deviceId).setDewrapMode1OTo1P(img1);
  }

  public void setDewrapMode1OToWall1P(@NonNull String deviceId, ImageView img1){
    getRtspPlayer(deviceId).setDewrapMode1OToWall1P(img1);
  }

  public void setDewrapMode1OTo1R(@NonNull String deviceId, ImageView img1){
    getRtspPlayer(deviceId).setDewrapMode1OTo1R(img1);
  }

  public void setDewrapMode1OTo1R2(@NonNull String deviceId, ImageView img1){
    getRtspPlayer(deviceId).setDewrapMode1OTo1R2(img1);
  }
  public void setDewrapMode1OTo4R(@NonNull String deviceId, ImageView img1, ImageView img2, ImageView img3, ImageView img4){
    getRtspPlayer(deviceId).setDewrapMode1OTo4R(img1,img2,img3,img4);
  }

  //use relay to send audio
  public void startAudio(String deviceId,String address, String username, String password)//not work,need encrypt password
  {
    /* AggregateRtspPlayer player=getRtspPlayer(deviceId);
    if(player.getTypeLAN()){

      MicTwoWayLANData twoway=new MicTwoWayLANData(address,username,password);
      EtxLogger.log(TAG, "startAudio json="+toJson(twoway)+"========================================================>");
      EdimaxLANGuard.getGuild().psStartAudioOut(deviceId,toJson(twoway));
    }else {
      EtxLogger.log(TAG, "startAudio not in LAN");
    }*/
  }

  public void sendAudio(@NonNull String deviceId, TsOutputAudioFormat format,String videoServerChannel, @NonNull byte[] rawAudio) {
     /*AggregateRtspPlayer player=getRtspPlayer(deviceId);
      if(player.getTypeLAN()){
      EdimaxLANGuard.getGuild().psSendAudioOut(deviceId,rawAudio,rawAudio.length);
      EtxLogger.log(TAG, "SEND LAN AUDIO");
    }else {*/
    String json = toJson(new AudioData(deviceId, format.getValue(),videoServerChannel));
      EdimaxSecurityGuard.getGuild().tslSendAudio(json, rawAudio, rawAudio.length);
      EtxLogger.log(TAG, "SEND RELAY AUDIO", json);
   // }

  }

  public void stopSendAudio(@NonNull String deviceId, TsOutputAudioFormat format) {
    AggregateRtspPlayer player=getRtspPlayer(deviceId);
  /*  if(player.getTypeLAN()){
      EdimaxLANGuard.getGuild().psStopAudioOut(deviceId);
      EtxLogger.log(TAG, "STOP LAN SEND AUDIO");
    }else {*/
      String json = toJson(new AudioData(deviceId, format.getValue()));
      EdimaxSecurityGuard.getGuild().tslStopSendAudio(json);
      EtxLogger.log(TAG, "STOP SEND AUDIO", json);
   // }

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

  public int getConnectionState(@NonNull String deviceId) {
    String json = toJson(new ConnectData(deviceId));
    EtxLogger.log(TAG, "GET CONNECTION STATE", json);
    return EdimaxSecurityGuard.getGuild().tslGetTunnelState(json);
  }

  public void release() {
    synchronized (rtspPlayerMap) {
      for (var  set:rtspPlayerMap.entrySet()){
        set.getValue().stop();
      }
      rtspPlayerMap.clear();
    }
    EdimaxSecurityGuard.getGuild().releaseGuard();
    EdimaxLANGuard.getGuild().releaseGuard();
    EtxLogger.log(TAG, "RELEASE");
  }

  public long getReceiveVideoRealFrameDiff(String id){
    return getRtspPlayer(id).getReceiveVideoRealFrameDiff();
  }

  /*--------------------------------
   * RTSP handler functions
   *-------------------------------*/

  private void addRtspPlayer(String deviceId, AggregateRtspPlayer rtspPlayer) {
    synchronized (rtspPlayerMap) {
      rtspPlayerMap.put(deviceId, rtspPlayer);
    }
  }

  private AggregateRtspPlayer getRtspPlayer(String deviceId) {
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
    EtxLogger.log(TAG, "tsVideoCallback ", "AggregateSecurityGuard releaseRtspPlayer deviceId="+deviceId);
    try {
      AggregateRtspPlayer rtspPlayer = Preconditions.requireNonNull(getRtspPlayer(deviceId));
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
    EtxLogger.log(TAG, "processStartRtsp", "AggregateSecurityGuard processStartRtsp callback deviceId ="+deviceId+",listeners.size="+listeners.size()+",tsStatus="+tsStatus+",size="+size);
    if (listeners.size()==1)Log.e("123","processStartRtsp listeners="+listeners.get(0).getClass().getSimpleName());
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
          EtxLogger.log(TAG, "processStartRtsp", "processStartRtsp h265 ");
       }
        AggregateRtspPlayer rtspPlayer = getRtspPlayer(deviceId);
        if (rtspPlayer == null||rtspPlayer.getVideoFormat()==null|| h265?rtspPlayer.getVideoFormat()!=CodecFormat.VIDEO_HEVC : rtspPlayer.getVideoFormat()!=CodecFormat.VIDEO_AVC)
        {
          TextureView oldTextureView = null;
          /* this is safe to be invoked either rtsp player is existed or not */
          if(rtspPlayer!=null){
            oldTextureView=rtspPlayer.getTextureView();
            rtspPlayer.stop();
            rtspPlayer.setTextureView(null);
          }
          removeRtspPlayer(deviceId);
          rtspPlayer = new AggregateRtspPlayer(h265?CodecFormat.VIDEO_HEVC:CodecFormat.VIDEO_AVC, CodecFormat.AUDIO_AAC_LC);
          if(oldTextureView!=null)
            rtspPlayer.setTextureView(oldTextureView);
          if (!rtspPlayer.isStreaming())
            rtspPlayer.startStreaming();
          addRtspPlayer(deviceId, rtspPlayer);
          EtxLogger.log(TAG, "processStartRtsp", "create addRtspPlayer deviceId= "+deviceId);
        }
        EtxLogger.log(TAG, "processStartRtsp", "processStartRtsp callback h265="+h265+",getTypeLAN="+rtspPlayer.getTypeLAN());
          rtspPlayer.prepareVideoDecoder(data, size);


      } else {
        /* if failed, release rtsp player */
        releaseRtspPlayer(deviceId);
        EtxLogger.log(TAG, "processStartRtsp", "AggregateSecurityGuard processStartRtsp releaseRtspPlayer");
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  private void processStopRtsp(String deviceId, TsStatus tsStatus) {
    /* release rtsp player if is not release */
    EtxLogger.log(TAG, "AggregateSecurityGuard", "AggregateSecurityGuard processStopRtsp");
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
    AggregateRtspPlayer player=getRtspPlayer(deviceId);
    if (player != null) {
      switch (tsJob) {
        case START_RTSP:
          processStartRtsp(deviceId, tsStatus, content, contentLength);
          break;

        case STOP_RTSP:
          /* stop rtsp might be invoked by device automatically stop */
          if(!player.getTypeLAN())processStopRtsp(deviceId, tsStatus);
          break;

        case SEND_AUDIO:
          processSendAudio(tsStatus);
          break;

        case STOP_SEND_AUDIO:
          processStopSendAudio(tsStatus);
          break;

        case DISCONNECT:
          if(!player.getTypeLAN())processDisconnect(deviceId, tsStatus);
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
      AggregateRtspPlayer rtspPlayer = Preconditions.requireNonNull(getRtspPlayer(deviceId));
      if (!rtspPlayer.isStreaming()) {
        notifyObserversOnReceivedVideo(deviceId, content, contentLength, videoFormat, iFrame, width,
            height,
            playTimeMs);
      } else {
        rtspPlayer.decodeVideo(content, contentLength, iFrame,playTimeMs/*, width, height, playTimeMs*/);
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  public void tsAudioCallback(String deviceId, byte[] content, int contentLength, int format, long playTimeMs)
  {
    try {
      TsRtpFormat.AudioFormat audioFormat = TsRtpFormat.AudioFormat.fromValue(format);
      AggregateRtspPlayer rtspPlayer = Preconditions.requireNonNull(getRtspPlayer(deviceId));
      if (!rtspPlayer.isStreaming()) {
        notifyObserversOnReceivedAudio(deviceId, content, contentLength, audioFormat, playTimeMs);
      } else {
       // EtxLogger.log(TAG, "tsAudioCallback",  "TS tsAudioCallback audioFormat="+audioFormat+",contentLength="+contentLength);
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


//LAN stream

  public boolean isLan(String id){
    AggregateRtspPlayer player=getRtspPlayer(id);
    if(player!=null){
      return player.getTypeLAN();
    }
    return false;
  }

 void startLANStreaming(@NonNull  String deviceID,@NonNull String address, @NonNull String path, int port,String user,String pass)
 {
//aggregate path "stream1-3"
  String json = toJson(new RtspLANData(address, path, port, user,pass));
   EtxLogger.log(TAG, "START LAN STREAMING", "ID="+deviceID+"json="+json);

  EdimaxLANGuard.getGuild().psStartStreaming(deviceID,json);

}

@Override
public void LifeLanMainCallback(String deviceId, int job, int customId, byte[] content, int contentLength, int status) {

  if(job==EdimaxLANGuard.PT_API_LAN_START_STREAMING){
    TsJob tsJob = TsJob.fromValue(LAN_START_RTSP.getValue());//transfer  to relay job
    TsStatus tsStatus = TsStatus.fromLANValue(status);
    if (Preconditions.isNull(tsJob)) {
      EtxLogger.log(TAG, "COMMAND LNA CALLBACK", "no such job");
      /* ignored unknown job */
      return;
    }
    processStartRtsp(deviceId, tsStatus, content, contentLength);
    notifyObserversOnReceivedCommand(deviceId, tsJob, customId, content, contentLength, tsStatus);
  }else if(job==EdimaxLANGuard.PT_API_LAN_STOP_STREAMING){
    TsJob tsJob = TsJob.fromValue(LAN_STOP_RTSP.getValue());
    TsStatus tsStatus = TsStatus.fromLANValue(status);//transfer  to relay job
    if (Preconditions.isNull(tsJob)) {
      EtxLogger.log(TAG, "COMMAND Lan CALLBACK", "no such job");
      /* ignored unknown job */
      return;
    }
    processStopRtsp(deviceId, tsStatus);
    notifyObserversOnReceivedCommand(deviceId, tsJob, customId, content, contentLength, tsStatus);
  }else if(job==EdimaxLANGuard.PT_ERR_DISCONNECTED){
    TsJob tsJob = TsJob.fromValue(LAN_DISCONNECT.getValue());
    TsStatus tsStatus = TsStatus.fromLANValue(status);//transfer  to relay job
    if (Preconditions.isNull(tsJob)) {
      EtxLogger.log(TAG, "COMMAND Lan CALLBACK", "no such job");
      /* ignored unknown job */
      return;
    }
    notifyObserversOnReceivedCommand(deviceId, tsJob, customId, content, contentLength, tsStatus);
  }

}

  @Override
  public void LifeLanVideoCallback(String deviceId, byte[] content, int contentLength, int format, int iFrame, int width, int height, long playTimeMs) {
    try {
        if(iFrame==EdimaxLANGuard.PT_VIDEO_FRAME_TYPE_VSPPS)
        {
          iFrame=EdimaxSecurityGuard.TSL_VIDEO_FRAME_TYPE_VSPPS;
        }else if(iFrame==EdimaxLANGuard.PT_VIDEO_FRAME_TYPE_SEI)
        {
          iFrame=EdimaxSecurityGuard.TSL_VIDEO_FRAME_TYPE_SEI;
        }else if(iFrame==EdimaxLANGuard.PT_VIDEO_FRAME_TYPE_I)
        {
          iFrame=EdimaxSecurityGuard.TSL_VIDEO_FRAME_TYPE_I;
        }else if(iFrame==EdimaxLANGuard.PT_VIDEO_FRAME_TYPE_P)
        {
          iFrame=EdimaxSecurityGuard.TSL_VIDEO_FRAME_TYPE_P;
        }
      TsRtpFormat.VideoFormat videoFormat = TsRtpFormat.VideoFormat.fromValue(format);
      AggregateRtspPlayer rtspPlayer = Preconditions.requireNonNull(getRtspPlayer(deviceId));
      if (!rtspPlayer.isStreaming()) {
        FancyLogger.e( "AggregateSecurityGuard","LifeLanVideoCallback LAN rtspPlayer not isStreaming");
        notifyObserversOnReceivedVideo(deviceId, content, contentLength, videoFormat, iFrame, width,
                height,
                playTimeMs);
      } else {
        rtspPlayer.decodeVideo(content, contentLength, iFrame,playTimeMs/*, width, height*/);
        if(iFrame==EdimaxLANGuard.PT_VIDEO_FRAME_TYPE_I)rtspPlayer.unVisableSnapshotForLan();//safety method
      }
    } catch (NullPointerException e) {
      Log.d("AggregateSecurityGuard","LifeLanVideoCallback NullPointerException");
    //  e.printStackTrace();
    }
  }

  @Override
  public void LifeLanAudioCallback(String deviceId, byte[] content, int contentLength, int format, long playTimeMs) {
    try {
      TsRtpFormat.AudioFormat audioFormat = TsRtpFormat.AudioFormat.fromValue(format);
      AggregateRtspPlayer rtspPlayer = Preconditions.requireNonNull(getRtspPlayer(deviceId));
      if (!rtspPlayer.isStreaming()) {
        notifyObserversOnReceivedAudio(deviceId, content, contentLength, audioFormat, playTimeMs);
      } else {
       //  EtxLogger.log(TAG, "LifeLanAudioCallback",  "TS LifeLanAudioCallback audioFormat="+audioFormat+",contentLength="+contentLength);

       // rtspPlayer.decodeAudio(content, contentLength, audioFormat, playTimeMs);
        if(contentLength>7){//delete adps
          byte[] newContent=new byte[contentLength];
          System.arraycopy(content,7,newContent,0,contentLength-7);
          rtspPlayer.decodeAudio(newContent, contentLength-7, audioFormat, playTimeMs);
        }

      }
    } catch (NullPointerException e) {
      //e.printStackTrace();
      Log.d("AggregateSecurityGuard","LifeLanAudioCallback NullPointerException");
    }
  }

  @Override
  public void LifeLanMsgCallback(int type, String message) {
    if(type<EdimaxSecurityGuard.TSL_LOG_TYPE_DEVICE_LIVE)
      EtxLogger.log("LifeLanMsgCallback", "LifeLanMsgCallback type="+type,  message);
    else {
     // notifyObserversOnReceivedMessage(type,"LifeLanMsgCallback "+message);
      EtxLogger.log("LifeLanMsgCallback", "LifeLanMsgCallback type="+type,  message);
    }
  }



}
