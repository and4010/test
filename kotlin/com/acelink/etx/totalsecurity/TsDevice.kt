package com.acelink.etx.totalsecurity

import com.acelink.etx.totalsecurity.TsDeviceState.CLOSED
import com.acelink.etx.totalsecurity.TsDeviceState.CONNECTED
import com.acelink.etx.totalsecurity.TsDeviceState.CONNECTING
import com.acelink.etx.totalsecurity.TsDeviceState.STREAMING


internal class TsDevice(val deviceId: String) {

  @Volatile private var state: TsDeviceState = CLOSED
  @Volatile private var lanState: TsDeviceLANState = TsDeviceLANState.CLOSED
  @Volatile private var samelan= 0 //0 relay 1:LAN
  fun getState(): TsDeviceState {
    synchronized(this) {
      return state
    }
  }



  fun applyAllClosedState() {
    synchronized(this) {
      state = CLOSED
      lanState=TsDeviceLANState.CLOSED
    }
  }

  fun applyClosedState() {
    synchronized(this) {
      state = CLOSED
    }
  }



  fun applyConnectingState() {
    synchronized(this) {
      state = CONNECTING
    }
  }

  fun applyConnectedState() {
    synchronized(this) {
      if (state != STREAMING) {
        /* ignored when streaming */
        state = CONNECTED
      }
    }
  }

  fun applyStreamingState() {
    synchronized(this) {
      if (state == CONNECTED) {
        state = STREAMING
      } else {
        /* ignored when closed/disconnected */
      }
    }
  }

  fun applyStopStreamingState() {
    synchronized(this) {
      if (state == STREAMING) {
        state = CONNECTED
      } else {
        /* ignored when not streaming */
      }
    }
  }

  fun getLANState(): TsDeviceLANState {
    synchronized(this) {
      return lanState
    }
  }



  fun applyLANStreamClosed() {

    synchronized(this) {
      if (lanState != TsDeviceLANState.STREAMING) {
        lanState = TsDeviceLANState.CLOSED
      } else {
        /* ignored when not streaming */
      }
    }
  }

  fun applyLANStartStream() {
    synchronized(this) {
      if (lanState != TsDeviceLANState.CLOSED) {
        lanState = TsDeviceLANState.STARTSTREAM
      } else {
        /* ignored when closed/disconnected */
      }
    }
  }

  fun applyLANStreaming() {
    synchronized(this) {
      if (lanState != TsDeviceLANState.STREAMING) {
        lanState = TsDeviceLANState.STREAMING
      } else {
        /* ignored when closed/disconnected */
      }
    }
  }
  fun getLAN()=samelan

  fun setLAN(lan:Int){
    samelan=lan
  }
}
