package com.acelink.etx

import android.util.Log

import com.acelink.cloud.CloudLogger

import java.io.OutputStreamWriter

/**
 * @author gregho
 * @since 2018/9/12
 */
class EtxLogger {

  companion object {

    private const val TAG = "EtxLogger"
    private const val LEFT_BRACKET = "["
    private const val RIGHT_BRACKET = "]"
    private const val SIGN = "-->"
    private const val LINK = "└--"

    private var debug = false
    private var dumper: ((message: String) -> Unit)? = null

    var logAndroidStream: OutputStreamWriter?=null

    private fun writeLogAndroid(className: String = "", functionName: String = "", message: String = ""){
      try {
        if (logAndroidStream!=null){
          synchronized(logAndroidStream!!){
            logAndroidStream?.apply {
              write("${className}:${functionName}:${message}\n")
              flush()
            }
          }

        }
      }catch (e:Exception){}
    }

    @JvmStatic
    fun dumper(dumper: ((message: String) -> Unit)?) {
      this.dumper = dumper
    }

    @JvmStatic
    fun enableLogger() {
      debug = true


      try {
        CloudLogger.enableLogger()
      } catch (ignored: NoClassDefFoundError) {
      }
    }

    @JvmStatic
    fun disableLogger() {
      debug = false


      try {
        CloudLogger.disableLogger()
      } catch (ignored: NoClassDefFoundError) {
      }
    }

    @JvmStatic
    @JvmOverloads
    fun log(
      className: String = "",
      functionName: String = "",
      message: String = ""
    ) {
      if (debug) {
        if (className.isNotEmpty()) {
          val dump = "$LEFT_BRACKET$className$RIGHT_BRACKET"
          Log.d(TAG, dump)
          dumper?.invoke(dump)
        }

        if (functionName.isNotEmpty()) {
          val dump = "$SIGN $functionName"
          Log.d(TAG, dump)
          dumper?.invoke(dump)
        }

        if (message.isNotEmpty()) {
          val dump = "$LINK $message"
          Log.d(TAG, dump)
          dumper?.invoke(dump)
        }
        writeLogAndroid(className,functionName,message)
      }
    }

    @JvmStatic
    @JvmOverloads
    fun logE(
            className: String = "",
            functionName: String = "",
            message: String = ""
    ) {
      if (debug) {
        if (className.isNotEmpty()) {
          val dump = "$LEFT_BRACKET$className$RIGHT_BRACKET"
          Log.e(TAG, dump)
          dumper?.invoke(dump)
        }

        if (functionName.isNotEmpty()) {
          val dump = "$SIGN $functionName"
          Log.e(TAG, dump)
          dumper?.invoke(dump)
        }

        if (message.isNotEmpty()) {
          val dump = "$LINK $message"
          Log.e(TAG, dump)
          dumper?.invoke(dump)
        }
        writeLogAndroid(className,functionName,message)
      }
    }
  }
}