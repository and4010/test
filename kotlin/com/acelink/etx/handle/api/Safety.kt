package com.acelink.etx.handle.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.acelink.cloud.CloudLogger
import com.acelink.cloud.tsc.RequestStatus
import com.acelink.cloud.tsc.RequestStatus.OK
import com.acelink.cloud.tsc.ResponseData
import com.acelink.cloud.tsc.app.*
import com.acelink.cloud.tsc.app.data.AppGetData
import com.acelink.cloud.tsc.app.data.AppNewData
import com.acelink.cloud.tsc.app.data.EndpointData
import com.acelink.cloud.tsc.app.v2account.data.V2UserRegistData
import com.acelink.cloud.tsc.app.internal.AppJsonBeans
import com.acelink.cloud.tsc.listeners.NonResponseListener
import com.acelink.cloud.tsc.listeners.ResponseListener
import com.acelink.cloud.tsc.query.QueryApi
import com.acelink.cloud.tsc.query.QueryApiImp
import com.acelink.cloud.tsc.v1.user.UserApi
import com.acelink.cloud.tsc.v1.user.UserV1Api
import com.acelink.cloud.tsc.v1.user.UserApiImp
import com.acelink.cloud.tsc.v1.user.data.VerifyData
import com.acelink.cloud.tsc.v2.user.V2ConfigApi
import com.acelink.cloud.tsc.v2.user.V2MetaApi
import com.acelink.cloud.tsc.v2.user.V2UserApiGoImp

import com.acelink.cloud.tsc.v2.user.V2UserGoApi
import com.acelink.cloud.tsc.verify.VerifiedInformation
import com.acelink.cloud.tsc.verify.VerifiedInformation.Companion.FLAVOR_ACELINK
import com.acelink.cloud.tsc.verify.VerifiedInformation.Companion.FLAVOR_EDIGREEN
import com.acelink.cloud.tsc.verify.VerifiedInformation.Companion.FLAVOR_EDIMAX
import com.acelink.cloud.tsc.verify.VerifiedInformation.Companion.FLAVOR_SMARTVISION
import com.acelink.etx.EtxLogger
import com.acelink.etx.handle.api.SafetyFlavor.*
import com.acelink.etx.handle.api.SafetyState.*
import com.acelink.etx.handle.api.cipher.EtxCipher
import com.acelink.etx.handle.api.cipher.EtxKeyPair
import com.acelink.etx.handle.api.constants.EtxConstants
import com.acelink.etx.handle.api.constants.EtxConstants.CLIENT_CERT_2VERSION
import com.acelink.etx.handle.api.constants.EtxConstants.CLIENT_CERT_ACELINK_2VERSION
import com.acelink.etx.handle.api.constants.EtxConstants.CLIENT_CERT_SMARTVISION

import com.acelink.etx.handle.api.constants.EtxConstants.CLIENT_PRIVATE_KEY_2VERSION
import com.acelink.etx.handle.api.constants.EtxConstants.CLIENT_PRIVATE_KEY_ACELINK_2VERSION
import com.acelink.etx.handle.api.constants.EtxConstants.CLIENT_PRIVATE_KEY_SMARTVISION
import com.acelink.etx.handle.api.constants.EtxConstants.CLOUD_CA_V2


import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

/**
 * @author gregho
 * @since 2018/9/25
 */
class Safety private constructor(internal val configuration: Configuration) {

  companion object Plugin {
    private const val TAG = "Safety"
    private const val EC_PREFIX = "-----BEGIN EC PRIVATE KEY-----"
    private const val  SharedFile="Safety_SP"
    private const val  SharedSafetySaveTime="Safety_SP_SaveTime"
    private const val  SharedSafetyHash="Safety_SP_Hash"
    @JvmStatic
    @Volatile
    private var instance: Safety? = null

    @JvmStatic
    fun apply(builder: Configuration.Builder) {
      if (instance == null) {
        synchronized(this) {
          if (instance == null) {
            instance = Safety(builder.build()).also {
                it.endpointApiImp = EndpointImp(it.configuration.endpoint)
            }

          } else {
            EtxLogger.log(TAG, "APPLY", "already has configuration state=${getState()}")
          }
        }
      } else {
        EtxLogger.log(TAG, "APPLY", "already has configuration 2 state=${getState()}")
      }
    }

    @JvmStatic
    fun release( ) {
      if (instance != null) {
        synchronized(this) {
          if (instance != null) {
            instance!!.etxCipher.clearTscKeyPair()
          }
          EtxLogger.log(TAG, "RELEASE")
          instance = null
        }
      } else {
        EtxLogger.log(TAG, "RELEASE", "no instance needs to release")
      }
    }

    fun close(){
      if (instance != null) {
        EtxLogger.log(TAG, "Close")
        instance = null
      }
    }

    @JvmStatic
    fun getState(): SafetyState {
      return instance?.run {
        state
      } ?: UNINITIALIZED
    }


    fun getUserEndpoint()= getFlavor().endpointUser

    @JvmStatic
    fun getFlavor(): SafetyFlavor {
      if (instance != null) {
        instance.also {
          return if (instance!!.etxCipher != null && instance!!.etxCipher.getFlavor() != "")
            instance!!.checkFlavor(instance!!.etxCipher.getFlavor())
          else instance!!.configuration.flavor
        }
      } else {
        EtxLogger.log(TAG, "GET FLAVOR", "no instance needs to change")
        return PRODUCTION
      }
    }

    @JvmStatic
    fun changeFlavor(flavor: SafetyFlavor) {
      if (instance != null) {
        synchronized(this) {
          if (instance != null) {
            instance!!.isChangeFlavor = true
            instance!!.also {
              val newConfiguration = it.configuration.newBuilder()
                  .flavor(flavor)

              release()
              apply(newConfiguration)
              EtxLogger.log(TAG, "CHANGE FLAVOR =${instance?.configuration?.flavor}")
            }
          } else {
            EtxLogger.log(TAG, "CHANGE FLAVOR", "no instance needs to change")
          }
        }
      } else {
        EtxLogger.log(TAG, "CHANGE FLAVOR", "no instance needs to change")
      }
    }

    @JvmStatic
    fun getKeyPair(): EtxKeyPair {
      return precondition(true).etxKeyPair
    }

    @JvmStatic
    fun clearKeyPair() {
      precondition(true).clearKeyPairImp()
    }

    @JvmStatic
    fun getAppConfiguration(listener: ResponseListener<AppGetData>) {
      precondition(true).getAppConfigurationImp(listener)
    }

    @JvmStatic
    fun updatePushToken(
      pushToken: String,
      listener: ResponseListener<ResponseData>
    ) {
      precondition(true)
          .updatePushTokenImp(pushToken, listener)
    }

    @JvmStatic
    fun updateLanguage(
      language: String,
      listener: ResponseListener<ResponseData>
    ) {
      precondition(true)
          .updateLanguageImp(language, listener)
    }

    @JvmStatic
    fun updateProfile(
      profile: String,
      listener: ResponseListener<ResponseData>
    ) {
      precondition(true).updateProfileImp(profile, listener)
    }

    fun v2UserRegister(company: String, email: String, password:String, userProfile:V2UserRegistData.RegistProfile, listener: ResponseListener<ResponseData>)
    {
      precondition(true).v2UserRegister(company, email, password, userProfile, listener)
    }

    fun v2ForgetPassword(company: String,userId: String, listener: ResponseListener<VerifyData>)
    {
      precondition(true).v2ForgetPassword(company,userId,listener)
    }

    fun v2VerifyForgetPassword(company: String,userId: String, activityId: String, pin: String, listener: NonResponseListener)
    {
      precondition(true).v2VerifyForgetPassword(company,userId,activityId,pin,listener)
    }

    fun v2ChangePassword(company: String,userId: String, activityId: String, newPassword: String, listener: NonResponseListener)
    {
      precondition(true).v2ChangePassword(company,userId,activityId,newPassword,listener)
    }


    @JvmStatic
    fun userApi(): UserV1Api {
      return precondition(true).userApiImp as UserV1Api
    }

    @JvmStatic
    fun v2UserGoApi(): V2UserGoApi {
      return precondition(true).userApiImp as V2UserGoApi
    }

    @JvmStatic
    fun queryApi(): QueryApi {
      return precondition(true).queryApiImp
    }

    @JvmStatic
    fun endpointWebMap(): String {
      return precondition(true).endpointWebMap!!
    }

    //no use
   /* @JvmStatic
    fun refreshEndpoint() {
        precondition(true).getEndpoint({ newApp()})
    }*/

    private fun precondition(checkState: Boolean): Safety {
      return instance?.let {
        if (checkState) {
          when (it.state) {
            UNINITIALIZED -> throw IllegalStateException("Pleas call Safety.init() first.")
            INITIALIZING -> throw IllegalAccessException("Safety is initializing.")
            FAILED -> throw IllegalStateException("Safety initializing failed.")
            INITIALIZED -> it
          }
        } else {
          it
        }
      } ?: throw IllegalStateException("Safety has no configuration.")
    }

    @JvmStatic
    fun appStart(url: String, uuid: String, version: String, token: String,language: String ,listener: ResponseListener<String>){
      instance?.run {
        if(appStartApi==null)
           appStartApi=AppStartApiImp(VerifiedInformation(
               CLIENT_CERT_2VERSION
             , CLIENT_PRIVATE_KEY_2VERSION
            // , if (configuration.tscCloudVersion==2)CLOUD_V2_GO_CA2 else CLOUD_CA
             ,CLOUD_CA_V2
           )
           )
        appStartApi?.appStart(url,uuid, version, token, language, listener) ?: run {
          Log.e("123", "appStartApi not init")
        }
      }?:Log.e("123", "appStartApi Safety not init")
     // precondition(true).userApiImp.appStart(uuid, version, token, language, listener)
    }
  }

  @Volatile
  private var state: SafetyState = UNINITIALIZED
  private val etxCipher: EtxCipher by lazy {
    EtxCipher(configuration.context)
  }
  private lateinit var etxKeyPair: EtxKeyPair
  private lateinit var appAPIs: AppAPIs
  private lateinit var endpointApiImp: EndpointApi
  private lateinit var userApiImp: UserApi

  private lateinit var queryApiImp: QueryApi
  private var appStartApi: AppStartApi?=null
  private var endpointWebMap: String?=null
  private var isChangeFlavor: Boolean = false

  init {
    initImp()
  }

  private val sharedPreferences: SharedPreferences by lazy {
    configuration.context.getSharedPreferences(SharedFile, Context.MODE_PRIVATE)
  }

  private fun saveSafetyTime(time:Long){
    var editor=sharedPreferences.edit()
    editor.putLong(SharedSafetySaveTime, time)
    editor.apply()
  }

  private fun getSafetyTime():Long{
   return sharedPreferences.getLong(SharedSafetySaveTime, 0L)
  }

  private fun saveSafetyHash(md5:String){
    var editor=sharedPreferences.edit()
    editor.putString(SharedSafetyHash, md5)
    editor.apply()
  }

  private fun getSafetyHash():String{
    return sharedPreferences.getString(SharedSafetyHash, "")!!
  }

  /*--------------------------------
   * Private functions
   *-------------------------------*/

  private fun initImp() {
    EtxLogger.log(TAG, "initImp", "get flavor =  ${etxCipher.getFlavor()} config Favor=${configuration.flavor} ,first_time= ${configuration.first_time}")
    if(configuration.first_time){
      etxCipher.clearTscKeyPair()
    }
    if (etxCipher.getFlavor() == "" || isChangeFlavor) {
      EtxLogger.log(TAG, "initImp", "isChangeFlavor = $isChangeFlavor, save flavor =  ${configuration.flavor.name}")
      isChangeFlavor = false
      etxCipher.saveFlavor(configuration.flavor)
    }

   // EtxLogger.log(TAG, "PREPARING", "versions: ${BuildConfig.VERSION_NAME}")
    /* Etx */
    EtxLogger.log(message = "etx module loaded")
    /* Ble */
    try {
      /* just to test if the module is include or not */

      EtxLogger.log(message = "ble module loaded")
    } catch (e: LinkageError) {
      EtxLogger.log(message = "ble module missing")
    }
    /* socket */
    try {
      /* just to test if the module is include or not */

      EtxLogger.log(message = "socket module loaded")
    } catch (e: LinkageError) {
      EtxLogger.log(message = "socket module missing")
    }
    /* Cloud */
    try {
      /* just to test if the module is include or not */
      CloudLogger.log()
      EtxLogger.log(message = "cloud module loaded")
    } catch (e: LinkageError) {
      setState(FAILED)
      EtxLogger.log(message = "cloud module missing")
    }
      initTask()

  }

  private fun initTask() {
    if (isState(INITIALIZING) or isState(INITIALIZED)) {
      /* ignored when already processing or done */
      return
    }
   // appStartApi=AppStartApiImp(VerifiedInformation(CLIENT_CERT, CLIENT_PRIVATE_KEY, CLOUD_CA))
    setState(INITIALIZING)
    EtxLogger.log(TAG, "INITIALIZING configuration.endpoint= ${configuration.endpoint}")
    Thread {


      var keyPair=etxCipher.getTscKeyPair()
      val hash=getSafetyHash()
      val saveTime=getSafetyTime()
      val isLegal=(hash==computeHash(keyPair.toString())&&System.currentTimeMillis()-saveTime< TimeUnit.DAYS.toMillis(700))//2 year
      if(!isLegal){
        fromCloud()
      }else{
        keyPair?.run {
          when(getFlavor()){
            SafetyFlavor.V2_PROD,SafetyFlavor.V2_UAT2_AGGREGATE,V2_UAT3,SafetyFlavor.V2_UAT5->//no endpoint url
            {
              syncEndpoint()
              fromLocal(this)
            }
            else->
             {
                when(configuration.endpoint)
                {
                  FLAVOR_EDIGREEN->{//edigreen need get endpoint each time
                    getEndpoint {  fromLocal(this)  }
                  }
                  else -> {
                    syncEndpoint()
                    fromLocal(this)
                  }
                }
            }
          }
        } ?: fromCloud()
      }

    }.apply {
      priority=Thread.MAX_PRIORITY
      start()
    }

  }

  private fun fromLocal(keyPair: EtxKeyPair) {
    with(keyPair) {
      EtxLogger.log(TAG, "RETRIEVE KEY PAIR", "load succeeded from local database")
      /* create new verified information with new cert and key which received via AppNew path */
      val verifiedInformation = configuration.flavor.verifiedInformation.copy(
          certPem = certPem, privateKeyPem = privateKeyPem
      )

      with(configuration) {
        when(endpoint) {
          FLAVOR_ACELINK -> verifiedInformation.falvor = FLAVOR_ACELINK
          FLAVOR_SMARTVISION -> verifiedInformation.falvor = FLAVOR_SMARTVISION
          FLAVOR_EDIGREEN-> verifiedInformation.falvor = FLAVOR_EDIGREEN
          else -> verifiedInformation.falvor = FLAVOR_EDIMAX
        }
      }
      initApis(verifiedInformation)
      updateApp(object : ResponseListener<ResponseData> {
        override fun onNotify(
          response: ResponseData?,
          requestStatus: RequestStatus,
          exception: Exception?
        ) {
          /* doesn't care i guess? */
        }
      })
      this@Safety.etxKeyPair = this
    }
  }

  private fun fromCloud() {
    /* init `AppApi` with default cert first */
    when(getFlavor()){
      SafetyFlavor.V2_PROD,SafetyFlavor.V2_UAT2_AGGREGATE,V2_UAT3,SafetyFlavor.V2_UAT5->//skip getEndpoint
      {
        syncEndpoint()
        with(configuration) {
          appAPIs = AppAPIs(flavor.endpointUser,with(flavor.verifiedInformation){
            falvor = FLAVOR_EDIMAX
            copy(
              certPem =  CLIENT_CERT_2VERSION ,
              privateKeyPem = CLIENT_PRIVATE_KEY_2VERSION
            )
          },configuration.tscCloudVersion,configuration.appId)
        }
        newApp()
      }
      else->{
        endpointApiImp = EndpointImp(configuration.endpoint)
        getEndpoint({ newApp()})
      }
    }

  }

  private fun syncEndpoint() {
    EtxLogger.log(TAG, "syncEndpoint", "now app flavor = ${etxCipher.getFlavor()} getFlavor=${getFlavor()} configuration flavor= ${configuration.flavor}  isEmpty=${etxCipher.getFlavor().isEmpty()} ,configuration name=${configuration.flavor.name}")
    var flavor = etxCipher.getFlavor().also {
      var flavorName = it
      if (it == null||it.isEmpty()) flavorName = configuration.flavor.name
      flavorName
    }

    var enpoints=etxCipher.getUserEndpoint();
   // var userEndpoint = enpoints.devUser!!.userEndpoint!!
    //var portalEndpoint = enpoints.devUser!!.portalEndpoint!!
    var userEndpoint = getFlavor().endpointUser!!.replace("https://","")
    var portalEndpoint = getFlavor().gsEndpointUser!!.replace("https://","")

      when (flavor) {
        PRODUCTION.name -> {
          userEndpoint = enpoints.prodUser!!.userEndpoint!!
          portalEndpoint = enpoints.prodUser!!.portalEndpoint!!
        }
        PRODUCTION_EDI.name -> {
          userEndpoint = when(configuration.endpoint){
            FLAVOR_EDIGREEN->enpoints.prodUser!!.userEndpoint!!
            else ->enpoints.prodUserEdi!!.userEndpoint!!
          }
        }
        PRODUCTION_VG.name -> userEndpoint = enpoints.prodUserVg!!.userEndpoint!!//not use
        PRODUCTION_OR.name -> {
          userEndpoint = enpoints.prodUserOr!!.userEndpoint!!
          portalEndpoint = enpoints.prodUserOr!!.portalEndpoint!!
        }
        QA.name -> {
          userEndpoint =enpoints.qaUser!!.userEndpoint!!
          portalEndpoint = enpoints.qaUser!!.portalEndpoint!!
        }
        QA_EDI.name -> userEndpoint = when(configuration.endpoint){
          FLAVOR_EDIGREEN->enpoints.qaUser!!.userEndpoint!!
            else->{
              enpoints.qaUserEdi!!.userEndpoint!!.run {
                if(this.length>10) this else enpoints.qaUser!!.userEndpoint!!
              }
            }
        }
        QA_SG.name -> {
          if(configuration.tscCloudVersion==2){
            userEndpoint= enpoints.v2QaSGUser!!.userEndpoint!!

          }else{
            userEndpoint = enpoints.qaUserSg!!.userEndpoint!!
            portalEndpoint = enpoints.qaUserSg!!.portalEndpoint!!
          }

        }
        DEVELOP.name -> {
          userEndpoint = enpoints.devUser!!.userEndpoint!!
          portalEndpoint =enpoints.devUser!!.portalEndpoint!!
        }
        DEVELOP_SG.name -> {
          if(configuration.tscCloudVersion==2){
            userEndpoint= enpoints.v2DevSGUser!!.userEndpoint!!
          }else userEndpoint = enpoints.devUserSg!!.userEndpoint!!
        }
        DEMO.name -> userEndpoint = enpoints.demoUser!!.userEndpoint!!
        DEMO_SG.name -> userEndpoint = enpoints.demoUserSg!!.userEndpoint!!
        AppJsonBeans.V2_DEV_NAME->userEndpoint=enpoints.v2DevSGUser!!.userEndpoint!!
        AppJsonBeans.V2_QA_SG_NAME->userEndpoint=enpoints.v2QaSGUser!!.userEndpoint!!
        AppJsonBeans.V2_UAT_NAME->userEndpoint=enpoints.v2UATUser!!.userEndpoint!!
        else->{}
      }
     // EtxLogger.log(TAG, "endpoints userEndpoint=${userEndpoint} prodUser=${enpoints.prodUser?.userEndpoint?:""} prodUserVg=${enpoints.prodUserVg?.userEndpoint?:""}  qaUserSg=${enpoints.qaUserSg?.userEndpoint?:""} demoUserSg=${ enpoints.demoUserSg?.userEndpoint?:""}" )
      getFlavor().endpointUser = "https://$userEndpoint"
    getFlavor().gsEndpointUser = "https://$portalEndpoint/"
      if(configuration.tscCloudVersion!=2){
        when (configuration.endpoint) {
          "smartvision" -> getFlavor().gsEndpointUser += "android/"
          FLAVOR_EDIMAX-> getFlavor().gsEndpointUser += "v1/apps/"
          else -> getFlavor().gsEndpointUser += "v1/apps/"
        }
      }



  }

  private fun initApis(
    verifiedInformation: VerifiedInformation
  ) {
    EtxLogger.log(TAG, "initApis", "initApis   odm= ${configuration.odm},  flavor =  ${getFlavor()}")
    with(configuration) {
      // init flavor
      verifiedInformation.falvor = when(endpoint) {
        FLAVOR_ACELINK,FLAVOR_EDIGREEN -> FLAVOR_ACELINK
        FLAVOR_SMARTVISION -> FLAVOR_SMARTVISION
        else -> FLAVOR_EDIMAX
      }


      /* init `AppApi` */
      var tscFavor=getFlavor()
      appAPIs = AppAPIs(tscFavor.endpointUser, verifiedInformation,configuration.tscCloudVersion,configuration.appId)


      /* init `UserApi` */
      userApiImp = when(configuration.tscCloudVersion){
        1->  UserApiImp(
          tscFavor.endpointUser,
          tscFavor.gsEndpointUser,
          verifiedInformation,
          appId,
          etxCipher.getFlavor().also {
            var flavorName = it
            if (it == "") flavorName = tscFavor.name
            flavorName
          }
        )
        2->{
         // verifiedInformation.caPem= CLOUD_V2_GO_CA2

        //  FancyLogger.e(TAG, "v2Login endpoint=${endpoint} verifiedInformation.caPem=${verifiedInformation.caPem }\n certPem=${verifiedInformation.certPem } \n privateKeyPem=${verifiedInformation.privateKeyPem } falvor=${verifiedInformation.falvor } " )
          V2UserApiGoImp(
            tscFavor.endpointUser,
            tscFavor.gsEndpointUser,
            verifiedInformation,
            appId,
            etxCipher.getFlavor().also {
              var flavorName = it
              if (it == "") flavorName = tscFavor.name
              flavorName
            }
          )
        }
        else ->throw UnsupportedOperationException("no this TSC cloud version $configuration.tscCloudVersion")
      }

      EtxLogger.log(TAG, "initApis endpointUser=${tscFavor.endpointUser } gsEndpointUser=${tscFavor.gsEndpointUser } " )

      /* if needs to init `QueryApi` */
      odm?.also { odm ->
        var query: String?=null
        var webMap: String?=null
        //when (flavor) //need  change favor or query will fail
        when (getFlavor()) {
          PRODUCTION,
          PRODUCTION_EDI,
          PRODUCTION_VG,
          PRODUCTION_OR,
          DEMO,
          DEMO_SG-> {
            //query = EtxConstants.ENDPOINT_QUERY_PRODUCTION
            query = EtxConstants.ENDPOINT_QUERY_PRODUCTION_OPENSOURCE
            webMap = EtxConstants.ENDPOINT_WEB_MAP_PRODUCTION
          }
          QA,
          QA_EDI,
          QA_SG,
          DEVELOP,
          DEVELOP_SG-> {
           // query = EtxConstants.ENDPOINT_QUERY_QA //old
            query = EtxConstants.ENDPOINT_QUERY_QA_OPENSOURCE
            webMap = EtxConstants.ENDPOINT_WEB_MAP_QA
          }
          else->{
            //not support yet
          }
        }

        queryApiImp = QueryApiImp(query, verifiedInformation, odm)
        endpointWebMap = webMap
      }
    }

    setState(INITIALIZED)
    EtxLogger.log(TAG, "INITIALIZED")
  }

  private fun newApp() {
    with(configuration) {
      val listener=object : ResponseListener<AppNewData> {
        override fun onNotify(
          response: AppNewData?,
          requestStatus: RequestStatus,
          exception: Exception?
        ) {
          if (requestStatus == OK) {
            response!!.run {
              val certPem = certificate!!.replace(
                ',',
                '\n'
              )
              val keyPem = key!!.replace(
                ',',
                '\n'
              )
                .let { replace ->
                  replace.substring(
                    replace.indexOf(
                      EC_PREFIX
                    )
                  )
                }
              EtxLogger.log(TAG, "RETRIEVE KEY PAIR", "load succeeded from cloud")
              /* create new verified information with new cert and key which received via AppNew path */
              initApis(
                flavor.verifiedInformation.copy(
                  certPem = certPem,
                  privateKeyPem = keyPem
                )
              )
              etxKeyPair = EtxKeyPair(
                certPem,
                keyPem
              )
              etxCipher.saveTscKeyPair(etxKeyPair)
              saveSafetyHash(computeHash(etxKeyPair.toString())!!)
              saveSafetyTime(System.currentTimeMillis())
            }
          } else {
            setState(FAILED)
            EtxLogger.log(TAG, "RETRIEVE KEY PAIR", "load failed from cloud, reason: $exception")
          }
        }
      }
      if (appName.contains("Furuno",true))
      {
        appAPIs.appApiImp.appPrivilegeNew("1234" , appId, pushToken, language, profile, appName, listener)
      }else
      {
        appAPIs.appApiImp.appNew(
          "1234" /* FIXME: should generate otp */,
          appId, pushToken, language, profile, appName, listener)
      }

    }
  }

  private fun updateApp(listener: ResponseListener<ResponseData>) {
    with(configuration) {
      appAPIs.appApiImp.appSet(appId, pushToken, language, profile, appName, listener)
    }
  }

  private fun v2UserRegister(company: String, email: String, password:String, userProfile: V2UserRegistData.RegistProfile,
                             listener: ResponseListener<ResponseData>) {
    with(configuration) {
      appAPIs.v2GoApiImp.userRegister(company, email, password, userProfile, listener)
    }
  }

  private fun v2ForgetPassword(company: String,userId: String, listener: ResponseListener<VerifyData>) {
    with(configuration) {
      appAPIs.v2GoApiImp.forgetPassword(company,userId,  listener)
    }
  }

  private fun v2VerifyForgetPassword( company: String,userId: String, activityId: String, pin: String, listener: NonResponseListener) {
    with(configuration) {
      appAPIs.v2GoApiImp.verifyForgetPassword(company,userId,activityId,pin,  listener)
    }
  }

  private fun v2ChangePassword( company: String,userId: String, activityId: String, newPassword: String, listener: NonResponseListener) {
    with(configuration) {
      appAPIs.v2GoApiImp.changePassword(company,userId,activityId,newPassword,  listener)
    }
  }

  private fun setState(state: SafetyState) {
    synchronized(this) {
      this.state = state
    }
  }

  private fun isState(state: SafetyState): Boolean {
    synchronized(this) {
      return this.state == state
    }
  }

  private fun clearKeyPairImp() {
    etxCipher.clearTscKeyPair()
    EtxLogger.log(TAG, "CLEAR KEY PAIR")
  }

  private fun getAppConfigurationImp(listener: ResponseListener<AppGetData>) {
    appAPIs.appApiImp.appGet(configuration.appId, listener)
  }

  private fun updatePushTokenImp(pushToken: String, listener: ResponseListener<ResponseData>)
  {
    synchronized(this) {
      configuration.pushToken = pushToken
      updateApp(listener)
    }
  }

  private fun updateLanguageImp(
    language: String,
    listener: ResponseListener<ResponseData>
  ) {
    synchronized(this) {
      configuration.language = language
      updateApp(listener)
    }
  }

  private fun updateProfileImp(
    profile: String,
    listener: ResponseListener<ResponseData>
  ) {
    synchronized(this) {
      configuration.profile = profile
      updateApp(listener)
    }
  }


  private fun getEndpoint(block:()->Unit) {
    endpointApiImp.getEndpoint(object: ResponseListener<EndpointData>{
      override fun onNotify(response: EndpointData?, requestStatus: RequestStatus, exception: Exception?)
      {
        Log.e("123","getEndpoint -------------------------->")
        if (exception == null&&response!=null) {

          // Save endpoint to preference
          etxCipher.saveEndpoint(response!!)
          syncEndpoint()

          with(configuration) {
            // check flavor for default cert/private

           /* if (tscCloudVersion == 2){
              if(getFlavor()==QA_SG)
                flavor.endpointUser="https://sgqa-user.tscdev.editsc.com"
                else
                flavor.endpointUser="https://sgacegodev-user.acelinkgo.com"

            }*/
            EtxLogger.log(TAG, "syncEndpoint", "getFlavor=${getFlavor()} endpointUser = ${flavor.endpointUser}")
            appAPIs =
              AppAPIs(flavor.endpointUser, when(endpoint) {
                FLAVOR_EDIGREEN -> {
                  with(flavor.verifiedInformation){
                    falvor = FLAVOR_ACELINK
                    copy(
//                            certPem = CLIENT_CERT_ACELINK,
//                            privateKeyPem = CLIENT_PRIVATE_KEY_ACELINK
                      CLIENT_CERT_2VERSION ,
                    privateKeyPem = CLIENT_PRIVATE_KEY_2VERSION
                    )
                  }

                }
                FLAVOR_ACELINK -> {
                  with(flavor.verifiedInformation){
                    falvor = FLAVOR_ACELINK
                    copy(
                      certPem = CLIENT_CERT_ACELINK_2VERSION,
                      privateKeyPem = CLIENT_PRIVATE_KEY_ACELINK_2VERSION
                    )
                  }

                }
                FLAVOR_SMARTVISION -> {
                  with(flavor.verifiedInformation){
                    falvor = FLAVOR_SMARTVISION
                    copy(
                      certPem = CLIENT_CERT_SMARTVISION,
                      privateKeyPem = CLIENT_PRIVATE_KEY_SMARTVISION
                    )
                  }
                }
                else -> {
                  with(flavor.verifiedInformation){
                    falvor = FLAVOR_EDIMAX
                    copy(
                      certPem =  CLIENT_CERT_2VERSION ,
                      privateKeyPem = CLIENT_PRIVATE_KEY_2VERSION
                    )
                  }
                }
              },configuration.tscCloudVersion,configuration.appId)
          //  flavor.verifiedInformation.caPem= if (tscCloudVersion == 2) CLOUD_V2_GO_CA2 else CLOUD_CA
          }
          block.invoke()

        }else if(response==null){
         Log.e("123","getEndpoint Fail")
        }
        else{
          exception?.printStackTrace()
        }
      }
    })
  }

  private fun checkFlavor(flavor: String): SafetyFlavor {
    return when (flavor) {
      PRODUCTION.name -> PRODUCTION
      PRODUCTION_EDI.name -> PRODUCTION_EDI
      PRODUCTION_VG.name -> PRODUCTION_VG
      PRODUCTION_OR.name -> PRODUCTION_OR
      QA.name -> QA
      QA_EDI.name -> QA_EDI
      QA_SG.name -> if (configuration.tscCloudVersion==2)V2_QA_SG else QA_SG
      DEVELOP.name -> if (configuration.tscCloudVersion==2)V2_DEVELOP else DEVELOP
      DEVELOP_SG.name -> DEVELOP_SG
      DEMO.name -> DEMO
      DEMO_SG.name -> DEMO_SG
      V2_QA_SG.name ->V2_QA_SG
      V2_DEVELOP.name ->V2_DEVELOP
      V2_UAT.name ->V2_UAT
      V2_UAT2_AGGREGATE.name ->V2_UAT2_AGGREGATE
      V2_UAT3.name ->V2_UAT3
      V2_UAT5.name ->V2_UAT5
      V2_PROD.name ->V2_PROD
      else -> configuration.flavor
    }
  }


  @Throws(NoSuchAlgorithmException::class)
  fun computeHash(input: String): String? {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.reset()
    try {
      digest.update(input.toByteArray(charset("UTF-8")))
    } catch (e: UnsupportedEncodingException) {
      e.printStackTrace()
    }
    val byteData = digest.digest(input.toByteArray())
    val sb = StringBuffer()
    for (i in byteData.indices) {
      sb.append(Integer.toString((byteData[i].toInt() and 0xFF) + 0x100, 16).substring(1))
    }
    return sb.toString()
  }

}