package com.acelink.etx.totalsecurity

import android.view.TextureView
import com.acelink.cloud.tsc.RequestStatus
import com.acelink.cloud.tsc.listeners.ResponseListener
import com.acelink.cloud.tsc.v1.user.UserApi
import com.acelink.etx.EtxLogger
import com.acelink.etx.handle.api.cipher.EtxKeyPair
import com.acelink.etx.handle.synchronizer.Synchronizer
import com.acelink.etx.totalsecurity.TsDeviceState.CONNECTED

import com.acelink.etx.totalsecurity.TsgPlugin.State.INITIALIZED
import com.acelink.etx.totalsecurity.TsgPlugin.State.UNINITIALIZED
import com.acelink.etx.totalsecurity.enums.TsJob
import com.acelink.etx.totalsecurity.enums.TsJob.CONNECT
import com.acelink.etx.totalsecurity.enums.TsJob.DISCONNECT
import com.acelink.etx.totalsecurity.enums.TsJob.START_RTSP
import com.acelink.etx.totalsecurity.enums.TsJob.STOP_RTSP
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
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author gregho
 * @since 2018/10/25
 *
 * The plugin which handles all device connection state
 */
class TsgPlugin private constructor(
    private val totalSecurityGuard: TotalSecurityGuard,
    val vendor: String,
    val project: String,
    private val autoConnect: Boolean,
    private val timeoutMs: Int
) : SimpleTsResponseListener {

    companion object Functions {

        private const val TAG = "TsgPlugin"
        private const val CONNECTION_TIMEOUT = 30_000
        private const val TIME_NEXT_CONNECT = 60_000L
        private const val TIME_RETRY_CONNECT = 5_000L
        private const val TIME_INTERNAL_CONNECT = 500L

        @JvmStatic
        @Volatile var pluginState = UNINITIALIZED
            private set
        @JvmStatic
        @Volatile
        private var instance: TsgPlugin? = null
        @JvmStatic
        private lateinit var userApi: UserApi
        @JvmStatic
        private lateinit var keyPair: EtxKeyPair

        @JvmStatic
        @JvmOverloads
        fun apply(
            totalSecurityGuard: TotalSecurityGuard,
            vendor: String,
            project: String,
            autoConnect: Boolean = true,
            timeout: Int = CONNECTION_TIMEOUT
        ) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = TsgPlugin(totalSecurityGuard, vendor, project, autoConnect, timeout)
                        pluginState = INITIALIZED
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
                        pluginState = UNINITIALIZED
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
            path: String,
            port: Int
        ) {
            precondition(true).startStreamImp(deviceId, textureView, path, port)
        }
        @JvmStatic
        fun stopSteam(deviceId: String) {
            precondition(true).stopSteamImp(deviceId)
        }

        @JvmStatic
        fun sendAudio(
            deviceId: String,
            format: TsOutputAudioFormat,
            content: ByteArray
        ) {
            precondition(true).sendAudioImp(deviceId, format, content)
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
        fun isInitialied()=pluginState==INITIALIZED

        private fun precondition(checkState: Boolean): TsgPlugin {
            return instance?.let {
                if (checkState) {
                    when (pluginState) {
                        UNINITIALIZED -> throw IllegalStateException(
                            "Pleas call TotalSecurityPlugin.apply() first."
                        )
                        INITIALIZED -> it
                        else -> throw IllegalStateException(
                            "TotalSecurityPlugin.apply() unknown state."
                        )
                    }
                } else {
                    it
                }
            } ?: throw IllegalStateException("TotalSecurityPlugin has no configuration.")
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
            CONNECT -> processConnect(deviceId, status)
            START_RTSP -> processStartRtsp(deviceId, status)
            STOP_RTSP -> processStopRtsp(deviceId, status)
            DISCONNECT -> processDisconnect(deviceId)
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
        totalSecurityGuard.subscribe(observer)
    }

    private fun unsubscribeImp(observer: SimpleTsResponseListener) {
        totalSecurityGuard.unsubscribe(observer)
    }

    private fun unsubscribeAllImp() {
        totalSecurityGuard.unsubscribeAll()
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
        totalSecurityGuard.clearObservers()
        /* disconnect with each device */
        disconnectAll()
        /* release all rtsp handler */
        totalSecurityGuard.releaseRtspPlayer()
        /* clear all device */
        clearItems()
        /* shut down took */
        pluginHook.shutdownAndAwaitTermination(0)
        /* release total security guard */
        totalSecurityGuard.release()
    }

    private fun checkState(deviceId: String) {
        contains(deviceId)?.run {
            if (getState() ==  TsDeviceState.CLOSED)
                connectImp(this.deviceId, 0)
            else
                EtxLogger.log(TAG, "CHECK STATE", "STATE: ${getState()}, id: $deviceId")
        } ?: EtxLogger.log(TAG, "CHECK STATE", "no such device: $deviceId to be checked")
    }

    private fun connectAll() {
        for (device in deviceList) {
            if (device.getState() != CONNECTED) {
                connectImp(device.deviceId)
            }
        }
    }

    private fun disconnectAll() {
        for (device in deviceList) {
            if (device.getState() !=  TsDeviceState.CLOSED) {
                disconnectImp(device.deviceId)
                /* just set state to disconnected */
                device.applyAllClosedState()
            }
        }
    }

    private fun getDeviceStateImp(deviceId: String): TsDeviceState? {
        return contains(deviceId)?.getState()
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
                                if (device.getState() ==  TsDeviceState.CLOSED) {
                                    device.applyConnectingState()
                                    totalSecurityGuard.connect(
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
        totalSecurityGuard.disconnect(deviceId)
    }

    private fun snapshotImp(deviceId: String, snapshotPath: String) {
        totalSecurityGuard.snapshot(deviceId, snapshotPath)
    }

    private fun startStreamImp(
        deviceId: String,
        textureView: TextureView,
        path: String,
        port: Int

    ) {
        totalSecurityGuard.startStreaming(deviceId, path, port,  textureView)
    }

    private fun stopSteamImp(deviceId: String) {
        totalSecurityGuard.stopStreaming(deviceId)
    }

    private fun sendAudioImp(
        deviceId: String,
        format: TsOutputAudioFormat,
        content: ByteArray
    ) {
        totalSecurityGuard.sendAudio(deviceId, format, content)
    }

    private fun stopSendAudioImp(
        deviceId: String,
        format: TsOutputAudioFormat
    ) {
        totalSecurityGuard.stopSendAudio(deviceId, format)
    }

    private fun switchTargetViewImp(
        deviceId: String,
        target: TextureView
    ) {
        totalSecurityGuard.switchTargetView(deviceId, target)
    }

    private fun setSpeakerStateImp(
        deviceId: String,
        state: Boolean
    ) {
        totalSecurityGuard.setSpeakerState(deviceId, state)
    }

    private fun getSpeakerStateImp(deviceId: String): Boolean {
        return totalSecurityGuard.getSpeakerState(deviceId)
    }

    private fun getRawDataWidthImp(deviceId: String): Int {
        return totalSecurityGuard.getRawDataWidth(deviceId)
    }

    private fun getRawDataHeightImp(deviceId: String): Int {
        return totalSecurityGuard.getRawDataHeight(deviceId)
    }

    private fun getCustomCommandImp(
        deviceId: String,
        customId: Int,
        postJson: String
    ) {
        totalSecurityGuard.customCommand(deviceId, customId, postJson)
    }

    private fun processConnect(
        deviceId: String,
        status: TsStatus?
    ) {
        contains(deviceId)
            ?.also { device ->
                when (status) {
                    SUCCESS, TUNNEL_OPENED -> device.applyConnectedState()
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
        status: TsStatus?
    ) {
        contains(deviceId)?.also { device ->
            when (status) {
                SUCCESS, TUNNEL_OPENED -> device.applyStreamingState()
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
        status: TsStatus?
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
        }
    }

    private fun processDisconnect(deviceId: String) {
        contains(deviceId)?.also { device ->
            device.applyAllClosedState()
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

    enum class State {
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