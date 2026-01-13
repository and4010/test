package com.acelink.etx.handle.api

import com.acelink.cloud.tsc.app.internal.AppJsonBeans.V2_DEV_NAME
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.V2_QA_SG_NAME
import com.acelink.cloud.tsc.app.internal.AppJsonBeans.V2_UAT_NAME
import com.acelink.cloud.tsc.verify.VerifiedInformation
import com.acelink.etx.handle.api.constants.EtxConstants.CLIENT_CERT_2VERSION
import com.acelink.etx.handle.api.constants.EtxConstants.CLIENT_PRIVATE_KEY_2VERSION
import com.acelink.etx.handle.api.constants.EtxConstants.CLOUD_CA_V2
import com.acelink.etx.handle.api.constants.EtxConstants.ENDPOINT_USER_DEMO_GS
import com.acelink.etx.handle.api.constants.EtxConstants.ENDPOINT_USER_DEVELOP
import com.acelink.etx.handle.api.constants.EtxConstants.ENDPOINT_USER_DEVELOP_GS
import com.acelink.etx.handle.api.constants.EtxConstants.ENDPOINT_USER_LOW_TESTING
import com.acelink.etx.handle.api.constants.EtxConstants.ENDPOINT_USER_PRODUCTION_GS
import com.acelink.etx.handle.api.constants.EtxConstants.ENDPOINT_USER_QA
import com.acelink.etx.handle.api.constants.EtxConstants.ENDPOINT_USER_QA_GS
import com.acelink.etx.handle.api.constants.EtxConstants.ENDPOINT_USER_VIRGINIA
import com.acelink.etx.handle.api.constants.EtxConstants.ENDPOINT_V2GO_USER_DEVELOP
import com.acelink.etx.handle.api.constants.EtxConstants.ENDPOINT_V2GO_USER_PROD
import com.acelink.etx.handle.api.constants.EtxConstants.ENDPOINT_V2GO_USER_SGQA
import com.acelink.etx.handle.api.constants.EtxConstants.ENDPOINT_V2GO_USER_UAT

/**
 * @author gregho
 * @since 2018/11/14
 */
enum class SafetyFlavor(
  internal var endpointUser: String,
  internal var gsEndpointUser: String,
  internal val verifiedInformation: VerifiedInformation = VerifiedInformation(
      CLIENT_CERT_2VERSION, CLIENT_PRIVATE_KEY_2VERSION, CLOUD_CA_V2
  )
) {
  PRODUCTION(ENDPOINT_USER_VIRGINIA, ENDPOINT_USER_PRODUCTION_GS),
  PRODUCTION_EDI(ENDPOINT_USER_VIRGINIA, ENDPOINT_USER_PRODUCTION_GS),
  PRODUCTION_VG(ENDPOINT_USER_VIRGINIA, ENDPOINT_USER_PRODUCTION_GS),
  PRODUCTION_OR(ENDPOINT_USER_VIRGINIA, ENDPOINT_USER_PRODUCTION_GS),
  QA(ENDPOINT_USER_QA, ENDPOINT_USER_QA_GS),
  QA_EDI(ENDPOINT_USER_QA, ENDPOINT_USER_QA_GS),
  QA_SG(ENDPOINT_USER_QA, ENDPOINT_USER_QA_GS),
  DEVELOP(ENDPOINT_USER_DEVELOP, ENDPOINT_USER_DEVELOP_GS),
  DEVELOP_SG(ENDPOINT_USER_DEVELOP, ENDPOINT_USER_DEVELOP_GS),
  DEMO(ENDPOINT_USER_LOW_TESTING, ENDPOINT_USER_DEMO_GS),
  DEMO_SG(ENDPOINT_USER_LOW_TESTING, ENDPOINT_USER_DEMO_GS),
  V2_DEVELOP(ENDPOINT_V2GO_USER_DEVELOP, ""),
  V2_QA_SG(ENDPOINT_V2GO_USER_SGQA, ""),
  V2_UAT(ENDPOINT_V2GO_USER_UAT, ""),
  V2_UAT2_AGGREGATE("sgacegouat2-user.acelinkgo.com", ""),
  V2_UAT3("sgacegouat3-user.acelinkgo.com", ""),
  V2_UAT5("sgacegouat5-user.acelinkgo.com", ""),
  V2_PROD(ENDPOINT_V2GO_USER_PROD, ""),;


    fun endpointName():String {
      return when(this){
        V2_DEVELOP->V2_DEV_NAME
        V2_QA_SG->V2_QA_SG_NAME
        V2_UAT->V2_UAT_NAME
        V2_UAT2_AGGREGATE->"SGAceGoUAT2"
        V2_UAT3->"SGAceGoUAT3"//BS VS8
        V2_UAT5->"SGAceGoUAT5"//no use
        V2_PROD->"V2_PROD"
        else->super.name
      }
    }

  }