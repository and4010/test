package com.acelink.etx.handle.api

import android.content.Context
import android.util.Log
import com.acelink.etx.handle.api.SafetyFlavor.DEVELOP

/**
 * @author gregho
 * @since 2018/9/25
 */
class Configuration(
  val context: Context,
  var first_time:Boolean,
  var flavor: SafetyFlavor,
  val appId: String,
  var pushToken: String,
  var language: String,
  var profile: String,
  val appName: String,
  val odm: Array<String>? = null, /* for query api */
  val endpoint: String,
  val tscCloudVersion:Int
) {

  fun newBuilder()= Builder(this)

  class Builder(private val context: Context)
  {

    constructor(configuration: Configuration) : this(configuration.context)
    {
      this.flavor = configuration.flavor
      this.appId = configuration.appId
      this.pushToken = configuration.pushToken
      this.language = configuration.language
      this.profile = configuration.profile
      this.appName = configuration.appName
      this.odm = configuration.odm
      this.endpoint = configuration.endpoint
      this.first_time = configuration.first_time
      this.tscCloudVersion =configuration.tscCloudVersion
    }

    private var flavor: SafetyFlavor = DEVELOP
    private var first_time: Boolean = false
    private var appId: String? = null
    private var pushToken: String? = null
    private var language: String? = null
    private var profile: String = ""
    private var appName: String = ""
    private var odm: Array<String>? = null
    private var endpoint: String = ""
    private var tscCloudVersion=1

    fun flavor(flavor: SafetyFlavor): Builder {
      this.flavor = flavor

      return this
    }

    fun firstTime(first_time: Boolean): Builder {
      this.first_time = first_time
      return this
    }
    fun appId(appId: String): Builder {
      this.appId = appId
      return this
    }

    fun pushToken(pushToken: String): Builder {
      this.pushToken = pushToken
      return this
    }

    fun language(language: String): Builder {
      this.language = language
      return this
    }

    fun profile(profile: String): Builder {
      this.profile = profile
      return this
    }

    fun appName(appName: String): Builder {
      this.appName = appName
      return this
    }

    fun odm(odm: Array<String>): Builder {
      this.odm = odm
      return this
    }

    fun endpoint(endpoint: String): Builder {
      this.endpoint = endpoint
      return this
    }

    fun tscCloudVersion(version: Int): Builder {
      this.tscCloudVersion = version
      return this
    }

    fun build(): Configuration {

      return Configuration(
          context,first_time, flavor, appId!!, pushToken!!, language!!, profile, appName, odm, endpoint,tscCloudVersion
      )
    }
  }
}