package com.acelink.etx.totalsecurity.aggregate

import android.view.TextureView
import android.widget.ImageView
import com.acelink.cloud.tsc.RequestStatus
import com.acelink.cloud.tsc.listeners.ResponseListener
import com.acelink.cloud.tsc.v1.user.UserApi
import com.acelink.etx.EtxLogger
import com.acelink.etx.handle.api.cipher.EtxKeyPair
import com.acelink.etx.handle.synchronizer.Synchronizer
import com.acelink.etx.totalsecurity.FrameFPS
import com.acelink.etx.totalsecurity.TsDevice
import com.acelink.etx.totalsecurity.TsDeviceLANState
import com.acelink.etx.totalsecurity.TsDeviceState
import com.acelink.etx.totalsecurity.aggregate.enums.Dewrap_mode
import com.acelink.etx.totalsecurity.aggregate.enums.Mount_type
import com.acelink.etx.totalsecurity.enums.TsJob
import com.acelink.etx.totalsecurity.enums.TsJob.*

import com.acelink.etx.totalsecurity.enums.TsOutputAudioFormat
import com.acelink.etx.totalsecurity.enums.TsStatus
import com.acelink.etx.totalsecurity.enums.TsStatus.SUCCESS
import com.acelink.etx.totalsecurity.enums.TsStatus.TUNNEL_OPENED
import com.acelink.etx.totalsecurity.enums.TsStatus.TUNNEL_OPENING
import com.acelink.etx.totalsecurity.enums.TsTunnel
import com.acelink.etx.totalsecurity.enums.TsTunnel.Rule.RELAY_ONLY
import com.acelink.etx.totalsecurity.listener.SimpleTsResponseListener
import com.ns.greg.library.fasthook.BaseRunnable
import com.ns.greg.library.fasthook.BaseThreadManager
import com.ns.greg.library.fasthook.BaseThreadTask
import com.ns.greg.library.fasthook.ThreadExecutorFactory
import com.ns.greg.library.fasthook.functions.BaseRun
import org.json.JSONObject
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author gregho
 * @since 2018/10/25
 *
 * The plugin which handles all device connection state
 */
class AggregateTsgPlugin private constructor(
  private val securityGuard: AggregateSecurityGuard,
  val vendor: String,
  val project: String,
  private val autoConnect: Boolean,
  private val timeoutMs: Int
) : SimpleTsResponseListener {

  companion object Functions {

    private const val TAG = "AggregateTsgPlugin"
    private const val CONNECTION_TIMEOUT = 30_000
    private const val TIME_NEXT_CONNECT = 60_000L
    private const val TIME_RETRY_CONNECT = 5_000L
    private const val TIME_INTERNAL_CONNECT = 500L

    @JvmStatic
    @Volatile var v2PluginState = V2State.UNINITIALIZED
      private set
    @JvmStatic
    @Volatile
    private var instance: AggregateTsgPlugin? = null
    @JvmStatic
    private lateinit var userApi: UserApi
    @JvmStatic
    private lateinit var keyPair: EtxKeyPair

    @JvmStatic
    @JvmOverloads
    fun apply(
      securityGuard: AggregateSecurityGuard,
      vendor: String,
      project: String,
      autoConnect: Boolean = true,
      timeout: Int = CONNECTION_TIMEOUT
    ) {
      if (instance == null) {
        synchronized(this) {
          if (instance == null) {
            instance = AggregateTsgPlugin(securityGuard, vendor, project, autoConnect, timeout)
            v2PluginState =  V2State.INITIALIZED
          }
        }
      }
    }

    @Deprecated("Do not invoke this function (not stable)")
    @JvmStatic
    fun release() {
      if (instance != null) {
        synchronized(this) {
          if (instance != null) {
            instance!!.releaseImp()
            instance = null
            v2PluginState =  V2State.UNINITIALIZED
          }
        }
      }
    }

    @JvmStatic
    fun setUserApi(userApi: UserApi) {
      this.userApi = userApi
    }

    @JvmStatic
    fun setKeyPair(keyPair: EtxKeyPair) {
      this.keyPair = keyPair
    }

    @JvmStatic
    fun onResume() {
      with(precondition(true)) {
        setHandlingImp(true)
        connectAll()
      }
    }

    @JvmStatic
    fun onPause() {
      with(precondition(true)) {
        setHandlingImp(false)
        disconnectAll()
      }
    }

    @JvmStatic
    fun setHandling(enabled: Boolean) {
      precondition(true).setHandlingImp(enabled)
    }

    @JvmStatic
    fun subscribe(observer: SimpleTsResponseListener) {
      precondition(true).subscribeImp(observer)
    }

    @JvmStatic
    fun unsubscribe(observer: SimpleTsResponseListener) {
      precondition(true).unsubscribeImp(observer)
    }

    @JvmStatic
    fun unsubscribeAll() {
      precondition(true).unsubscribeAllImp()
    }

    /**
     * Synchronizes device list
     */
    @JvmStatic
    fun sync(syncList: List<String>) {
      precondition(true).syncImp(syncList)
    }

    /**
     * Adds device
     */
    @JvmStatic
    fun add(
      deviceId: String
    ) {
      precondition(true).addImp(deviceId)
    }

    /**
     * Remove devices
     */
    @JvmStatic
    fun remove(deviceId: String) {
      precondition(true).removeImp(deviceId)
    }



    @JvmStatic
    fun getDeviceState(deviceId: String): TsDeviceState? {
      return precondition(true).getDeviceStateImp(deviceId)
    }

    @JvmStatic
    fun getDeviceLANState(deviceId: String): TsDeviceLANState? {
      return precondition(true).getDeviceLANStateImp(deviceId)
    }


    //2023 demo Lan connect
    @JvmStatic
    fun connectAuto(deviceId: String) {
      precondition(true).apply {
        contains(deviceId)?.run {
          if (getState() == TsDeviceState.CLOSED)
            connectImp(this.deviceId, 0, rule = TsTunnel.Rule.AUTO)
          else
            EtxLogger.log(TAG, "CHECK STATE", "STATE: ${getState()}, id: $deviceId")
        } ?: EtxLogger.log(TAG, "CHECK STATE", "no such device: $deviceId to be checked")
      }
    }

    /*--------------------------------
   * Total security functions
   *-------------------------------*/
    @JvmStatic
    fun connect(deviceId: String) {
      precondition(true).checkState(deviceId)
    }

    @JvmStatic
    fun closeTunnel(deviceId: String) {
      precondition(true).disconnectImp(deviceId)
    }

    @JvmStatic
    fun snapshot(deviceId: String, snapshotPath: String) {
      precondition(true).snapshotImp(deviceId, snapshotPath)
    }

    @JvmStatic
    @JvmOverloads
    fun startStream(
      deviceId: String,
      textureView: TextureView,
      snapshot:ImageView?,
      path: String,
      port: Int,
      ip:String?,
      user:String?,
      pass:String?,
       fps: FrameFPS
    ) {
      precondition(true).startStreamImp(deviceId, textureView,snapshot, path, port,ip, user, pass,fps)
    }
    @JvmStatic
    fun stopSteam(deviceId: String) {
      precondition(true).stopSteamImp(deviceId)
    }

    @JvmStatic
    fun startAudio(
      deviceId:String,
      address:String,
      username:String,
      password:String
    ) {
      precondition(true).startAudioImp(deviceId,address,username,password)
    }

    @JvmStatic
    fun sendAudio(
      deviceId: String,
      format: TsOutputAudioFormat,
      videoServerChannel:String?,
      content: ByteArray
    ) {
      precondition(true).sendAudioImp(deviceId, format,videoServerChannel, content)
    }

    @JvmStatic
    fun stopSendAudio(
      deviceId: String,
      format: TsOutputAudioFormat
    ) {
      precondition(true).stopSendAudioImp(deviceId, format)
    }

    @JvmStatic
    fun switchTargetView(
      deviceId: String,
      target: TextureView
    ) {
      precondition(true).switchTargetViewImp(deviceId, target)
    }

    @JvmStatic
    fun setSpeakerState(
      deviceId: String,
      state: Boolean
    ) {
      precondition(true).setSpeakerStateImp(deviceId, state)
    }

    @JvmStatic
    fun getSpeakState(deviceId: String): Boolean {
      return precondition(true).getSpeakerStateImp(deviceId)
    }

    @JvmStatic
    fun getRawDataWidth(deviceId: String): Int {
      return precondition(true).getRawDataWidthImp(deviceId)
    }

    @JvmStatic
    fun getRawDataHeight(deviceId: String): Int {
      return precondition(true).getRawDataHeightImp(deviceId)
    }

    @JvmStatic
    fun customCommand(
      deviceId: String,
      customId: Int,
      postJson: String
    ) {
      precondition(true).getCustomCommandImp(deviceId, customId, postJson)
    }

    @JvmStatic
    fun getDewrapMode(deviceId: String): Dewrap_mode? {
      return precondition(true).getDewrapModeImp(deviceId)
    }

    @JvmStatic
    fun setMountType(deviceId: String, type: Mount_type) {
      return precondition(true).setMountTypeImp(deviceId,type)
    }

    @JvmStatic
    fun setDewrapMode1O(deviceId: String) {
      return precondition(true).setDewrapMode1OImp(deviceId)
    }

    @JvmStatic
    fun setDewrapMode1OToWall1R(deviceId: String) {
      return precondition(true).setDewrapMode1OToWall1RImp(deviceId)
    }

    @JvmStatic
    fun setDewrapMode1P(deviceId: String) {
      return precondition(true).setDewrapMode1PImp(deviceId)
    }



    @JvmStatic
    fun setDewrapMode1Oto1P(deviceId: String, img1: ImageView) {
      return precondition(true).setDewrapMode1Oto1PImp(deviceId, img1)
    }

    @JvmStatic
    fun setDewrapMode1OToWall1P(deviceId: String, img1: ImageView) {
      return precondition(true).setDewrapMode1OToWall1PImp(deviceId, img1)
    }

    @JvmStatic
    fun setDewrapMode1OTo1R(deviceId: String, img1: ImageView) {
      return precondition(true).setDewrapMode1OTo1RImp(deviceId, img1)
    }

    @JvmStatic
    fun setDewrapMode1OTo1R2(deviceId: String, img1: ImageView) {
      return precondition(true).setDewrapMode1OTo1R2Imp(deviceId, img1)
    }

    fun setDewrapMode1OTo4R(deviceId: String, img1: ImageView, img2:ImageView, img3:ImageView, img4:ImageView) {
      return precondition(true).setDewrapMode1OTo4RImp(deviceId, img1,img2,img3,img4)
    }

    fun getReceiveVideoRealFrameDiff(id: String):Long{
      return precondition(true).getReceiveVideoRealFrameDiffImp(id)
    }

    @JvmStatic
    fun isInitialied()= AggregateTsgPlugin.v2PluginState == V2State.INITIALIZED

    private fun precondition(checkState: Boolean): AggregateTsgPlugin {
      return instance?.let {
        if (checkState) {
          when (v2PluginState) {
            V2State.UNINITIALIZED -> throw IllegalStateException(
                "Pleas call AggregateSecurityGuard.apply() first."
            )
            V2State.INITIALIZED -> it
            else -> throw IllegalStateException(
              "AggregateSecurityGuard.apply() unknown state."
            )
          }
        } else {
          it
        }
      } ?: throw IllegalStateException("AggregateSecurityGuard has no configuration.")
    }
  }

  /*--------------------------------
   * Internal
   *-------------------------------*/

  private val pluginHook: BaseThreadManager<ThreadPoolExecutor> by lazy {
    PluginThreadManager()
  }
  private val deviceList = CopyOnWriteArrayList<TsDevice>()
  private val atomicHandling = AtomicBoolean()

  init {
    /* subscribe self */
    subscribeImp(this)
    /* start handling */
    setHandlingImp(true)
  }

  /*--------------------------------
   * SimpleTsResponseListener
   *-------------------------------*/

  override fun onReceiveCommand(
    deviceId: String,
    job: TsJob,
    customId: Int,
    content: ByteArray?,
    contentLength: Int,
    status: TsStatus?
  ) {
    EtxLogger.log(
        TAG, "ON RECEIVE COMMAND", "job: $job, customId: $customId, status: $status, id: $deviceId"
    )
    when (job) {
      CONNECT -> {
        var samelan=0
        if (contentLength>0){
          try {
            var json=JSONObject(String(content!!,0,contentLength))
            samelan=json.getInt("samelan")
            EtxLogger.log(TAG, "ON RECEIVE CONNECT", "samelan: $samelan json=${json}")
          }catch (ex:Exception){ex.printStackTrace()}
        }

        processConnect(deviceId, status,samelan)
      }
      START_RTSP, TsJob.LAN_START_RTSP -> processStartRtsp(deviceId, status,job)
      STOP_RTSP, TsJob.LAN_STOP_RTSP -> processStopRtsp(deviceId, status,job)
      DISCONNECT -> processDisconnect(deviceId)
      LAN_DISCONNECT->{
        contains(deviceId)?.also { device ->
          device.applyLANStreamClosed()
        }
      }
      else -> processOthers(deviceId, status)
    }
  }

  override fun onReceiveMessage(
    type: Int,
    message: String
  ) {
  }

  private fun setHandlingImp(enabled: Boolean) {
    atomicHandling.set(enabled)
  }

  private fun subscribeImp(observer: SimpleTsResponseListener) {
    securityGuard.subscribe(observer)
  }

  private fun unsubscribeImp(observer: SimpleTsResponseListener) {
    securityGuard.unsubscribe(observer)
  }

  private fun unsubscribeAllImp() {
    securityGuard.unsubscribeAll()
  }

  private fun syncImp(syncList: List<String>) {
    removeNonexistent(syncList)
  }

  private fun addImp(id: String) {
    if (contains(id) == null) {
      addItem(id)
    }

    if (autoConnect) {
      checkState(id)
    }
  }

  private fun removeImp(id: String) {
    contains(id)?.run {
      disconnectImp(id)
      removeItem(id)
    }
  }


  private fun removeNonexistent(syncList: List<String>) {
    Synchronizer.removeNonexistent(deviceList, syncList, { src, dst ->
      src.deviceId == dst
    }, {})
  }

  private fun clearItems() {
    synchronized(deviceList) {
      deviceList.clear()
    }
  }

  private fun addItem(deviceId: String) {
    synchronized(deviceList) {
      deviceList.add(TsDevice(deviceId))
    }
  }

  private fun removeItem(device: TsDevice) {
    synchronized(deviceList) {
      deviceList.remove(device)
    }
  }

  private fun removeItem(deviceId: String) {
    contains(deviceId)
        ?.also { device ->
          removeItem(device)
        }
  }

  private fun contains(deviceId: String): TsDevice? {
    synchronized(deviceList) {
      deviceList.forEach {
        if (it.deviceId == deviceId) {
          return it
        }
      }

      return null
    }
  }

  private fun releaseImp() {
    /* clear all observer first to prevent receive disconnect callback */
    securityGuard.unsubscribeAll()
    /* disconnect with each device */
    disconnectAll()
    /* release all rtsp handler */

    /* clear all device */
    clearItems()
    /* shut down took */
    pluginHook.shutdownAndAwaitTermination(0)
    /* release total security guard */
    securityGuard.release()
  }

  private fun getReceiveVideoRealFrameDiffImp(id: String)=securityGuard.getReceiveVideoRealFrameDiff(id)


  private fun checkState(deviceId: String) {
    contains(deviceId)?.run {
      if (getState() == TsDeviceState.CLOSED)
        connectImp(this.deviceId, 0)
      else
        EtxLogger.log(TAG, "CHECK STATE", "STATE: ${getState()}, id: $deviceId")
    } ?: EtxLogger.log(TAG, "CHECK STATE", "no such device: $deviceId to be checked")
  }

  private fun connectAll() {
    for (device in deviceList) {
      if (device.getState() != TsDeviceState.CONNECTED) {
        connectImp(device.deviceId)
      }
    }
  }

  private fun disconnectAll() {
    for (device in deviceList) {
      if (device.getState() != TsDeviceState.CLOSED||device.getLANState()!=TsDeviceLANState.CLOSED) {
        disconnectImp(device.deviceId)
        /* just set state to disconnected */
        device.applyAllClosedState()
      }
    }
  }

  fun getDeviceStateImp(deviceId: String): TsDeviceState? {
    return contains(deviceId)?.getState()
  }

  fun getDeviceLANStateImp(deviceId: String): TsDeviceLANState? {
    return contains(deviceId)?.getLANState()
  }

  private fun reconnect(device: TsDevice) {
    if (!atomicHandling.get()) {
      /* ignored when not handling */
      return
    }

    if (autoConnect) {
      connectImp(device.deviceId, TIME_NEXT_CONNECT)
    }
  }

  private fun connectImp(
    deviceId: String,
    delayTime: Long = 0L,
    rule: TsTunnel.Rule=RELAY_ONLY
  ) {
    userApi.getDeviceToken(deviceId, object : ResponseListener<String> {
      override fun onNotify(
        response: String?,
        requestStatus: RequestStatus,
        exception: Exception?
      ) {
        response?.also { token ->
          pluginHook.addTask(object : BaseRunnable<BaseRun>() {
            override fun runImp(): BaseRun? {
              contains(deviceId)?.also { device ->
                if (device.getState() == TsDeviceState.CLOSED) {
                  device.applyConnectingState()
                  securityGuard.connect(
                    userApi.deviceApiDns(),
                    keyPair.certPem,
                    keyPair.privateKeyPem,
                    deviceId,
                    token,
                    rule,
                    vendor,
                    project,
                    timeoutMs
                  )
                }
              }

              return null
            }
          })
              .addDelayTime(delayTime)
              .start()
        }
      }
    })
  }

  private fun disconnectImp(
    deviceId: String
  ) {
    securityGuard.disconnect(deviceId)
  }

  private fun snapshotImp(deviceId: String, snapshotPath: String) {
    securityGuard.snapshot(deviceId, snapshotPath)
  }

  private fun startStreamImp(
    deviceId: String,
    textureView: TextureView,
    snapshot:ImageView?,
    path: String,
    port: Int,
    ip:String?,
     user:String?,
     pass:String?,
     fps:FrameFPS

  ) {

    securityGuard.startStreaming(contains(deviceId)!!, path, port,  textureView,snapshot,ip,user,pass,contains(deviceId)?.getLAN()==1,fps)
  }

  private fun stopSteamImp(deviceId: String) {
    securityGuard.stopStreaming(deviceId)
    contains(deviceId)?.applyLANStreamClosed()
  }

  private fun startAudioImp( deviceId:String, address:String,  username:String,  password:String){
    securityGuard.startAudio(deviceId,address,username,password)
  }

  private fun sendAudioImp(
    deviceId: String,
    format: TsOutputAudioFormat,
    videoServerChannel:String?,
    content: ByteArray
  ) {
    securityGuard.sendAudio(deviceId, format,videoServerChannel, content)
  }

  private fun stopSendAudioImp(
    deviceId: String,
    format: TsOutputAudioFormat
  ) {
    securityGuard.stopSendAudio(deviceId, format)
  }

  private fun switchTargetViewImp(
    deviceId: String,
    target: TextureView
  ) {
    securityGuard.switchTargetView(deviceId, target)
  }

  private fun setSpeakerStateImp(
    deviceId: String,
    state: Boolean
  ) {
    securityGuard.setSpeakerState(deviceId, state)
  }

  private fun getSpeakerStateImp(deviceId: String): Boolean {
    return securityGuard.getSpeakerState(deviceId)
  }

  private fun getRawDataWidthImp(deviceId: String): Int {
    return securityGuard.getRawDataWidth(deviceId)
  }

  private fun getRawDataHeightImp(deviceId: String): Int {
    return securityGuard.getRawDataHeight(deviceId)
  }

  fun getDewrapModeImp(deviceId: String)=securityGuard.getDewrapMode(deviceId)

  fun setMountTypeImp(deviceId: String, type: Mount_type) {
    securityGuard.setMountType(deviceId,type)
  }

  fun setDewrapMode1OImp(deviceId: String) {
    securityGuard.setDewrapMode1O(deviceId)
  }

  fun setDewrapMode1OToWall1RImp(deviceId: String) {
    securityGuard.setDewrapMode1OToWall1R(deviceId)
  }

  fun setDewrapMode1PImp(deviceId: String) {
    securityGuard.setDewrapMode1P(deviceId)
  }



  fun setDewrapMode1Oto1PImp(deviceId: String, img1: ImageView) {
    securityGuard.setDewrapMode1OTo1P(deviceId,img1)
  }

  fun setDewrapMode1OToWall1PImp(deviceId: String, img1: ImageView) {
    securityGuard.setDewrapMode1OToWall1P(deviceId,img1)
  }

  fun setDewrapMode1OTo1RImp(deviceId: String, img1: ImageView) {
    securityGuard.setDewrapMode1OTo1R(deviceId,img1)
  }

  fun setDewrapMode1OTo1R2Imp(deviceId: String, img1: ImageView) {
    securityGuard.setDewrapMode1OTo1R2(deviceId,img1)
  }

  fun setDewrapMode1OTo4RImp(deviceId: String, img1: ImageView, img2:ImageView, img3:ImageView, img4:ImageView) {
    securityGuard.setDewrapMode1OTo4R(deviceId,img1, img2, img3, img4)
  }

  private fun getCustomCommandImp(
    deviceId: String,
    customId: Int,
    postJson: String
  ) {
    securityGuard.customCommand(deviceId, customId, postJson)
  }

  private fun processConnect(
    deviceId: String,
    status: TsStatus?,
    samelan:Int
  ) {
    contains(deviceId)
        ?.also { device ->
          when (status) {
            SUCCESS, TUNNEL_OPENED -> {
              device.applyConnectedState()
              device.setLAN(samelan)
            }
            TUNNEL_OPENING -> {
              /* TODO: retry? */
            }
            else -> {
              device.applyAllClosedState()
              reconnect(device)
            }
          }
        }
  }

  private fun processStartRtsp(
    deviceId: String,
    status: TsStatus?,
    job: TsJob
  ) {
    contains(deviceId)?.also { device ->
      when (status) {
        SUCCESS, TUNNEL_OPENED -> {
          if (job==START_RTSP) device.applyStreamingState()
          else if (job==TsJob.LAN_START_RTSP)device.applyLANStreaming()//LAN
        }
        TUNNEL_OPENING -> {
          /* TODO: retry? */
        }
        else -> {
          device.applyAllClosedState()
          reconnect(device)
        }
      }

    }
  }

  private fun processStopRtsp(
    deviceId: String,
    status: TsStatus?,
    job: TsJob
  ) {
    contains(deviceId)?.also { device ->
      when (status) {
        SUCCESS, TUNNEL_OPENED -> device.applyStopStreamingState()
        TUNNEL_OPENING -> {
          /* TODO: retry? */
        }
        else -> {
          device.applyAllClosedState()
        }
      }
      device.applyLANStreamClosed()//LAN
      EtxLogger.log(TAG, "processStopRtsp", "AggregateTsgPlugin processStopRtsp  deviceId=$deviceId ")
    }

  }

  private fun processDisconnect(deviceId: String) {
    contains(deviceId)?.also { device ->
      device.applyClosedState()
      reconnect(device)
    }
  }

  private fun processOthers(
    deviceId: String,
    status: TsStatus?
  ) {
    contains(deviceId)
        ?.also { device ->
          if ((status != SUCCESS) and (status != TUNNEL_OPENED) and (status != TUNNEL_OPENING)) {
            device.applyAllClosedState()
            reconnect(device)
          }
        }
  }

  enum class V2State {
    UNINITIALIZED(),
    INITIALIZED();
  }

/*--------------------------------
 * ThreadManager
 *-------------------------------*/

  private class PluginThreadManager : BaseThreadManager<ThreadPoolExecutor>() {

    init {
      setLog(false)
    }

    override fun createBaseThreadTask(job: BaseRunnable<*>?): BaseThreadTask {
      return BaseThreadTask(job)
    }

    override fun createThreadPool(): ThreadPoolExecutor {
      return ThreadExecutorFactory.newCoreSizeThreadPool()
    }
  }
}