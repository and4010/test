package com.acelink.etx.handle.api.constants

/**
 * @author gregho
 * @since 2018/11/15
 */
internal object EtxConstants {

  /*--------------------------------
   * Cloud endpoints
   *-------------------------------*/
  const val ENDPOINT_USER_PRODUCTION = "https://ediprod-user.editsc.com"
  const val ENDPOINT_USER_QA = "https://ediqa-user.editsc.com"
  const val ENDPOINT_USER_DEVELOP = "https://emxdev-api.myedimax.com"
  const val ENDPOINT_USER_LOW_TESTING = "https://ohioprod-user.editsc.com"
  const val ENDPOINT_USER_VIRGINIA = "https://vgprod-user.editsc.com"
  const val ENDPOINT_QUERY_PRODUCTION = "https://ediprod-airbox-query.editsc.com"
  const val ENDPOINT_QUERY_PRODUCTION_OPENSOURCE = "https://ediprod-aria-app.editsc.com"
  const val ENDPOINT_QUERY_QA = "https://ediqa-airbox-query.editsc.com"
  const val ENDPOINT_QUERY_QA_OPENSOURCE = "https://ediqa-aria-app.editsc.com"
  const val ENDPOINT_WEB_MAP_PRODUCTION = "https://airbox-app.edimaxcloud.com/entoss"
  const val ENDPOINT_WEB_MAP_QA = "https://ab3-app.edimaxcloud.com/entoss"
  const val ENDPOINT_USER_PRODUCTION_GS = "https://fg8s5l0muk.execute-api.us-east-2.amazonaws.com"
  const val ENDPOINT_USER_QA_GS = "https://fg8s5l0muk.execute-api.us-east-2.amazonaws.com"
  const val ENDPOINT_USER_DEVELOP_GS = "https://fg8s5l0muk.execute-api.us-east-2.amazonaws.com"
  const val ENDPOINT_USER_DEMO_GS = "https://fg8s5l0muk.execute-api.us-east-2.amazonaws.com"

  const val ENDPOINT_V2GO_USER_DEVELOP = "https://sgacegodev-user.acelinkgo.com"
  const val ENDPOINT_V2GO_USER_SGQA = "https://sgacegoqa-user.acelinkgo.com"
  const val ENDPOINT_V2GO_USER_UAT = "https://sgacegouat-user.acelinkgo.com"
  const val ENDPOINT_V2GO_USER_PROD = "https://oracegoprod-user.acelinkgo.com"
  /*--------------------------------
   * Edimax cloud verification
   *-------------------------------*/
  const val CLIENT_CERT = "-----BEGIN CERTIFICATE-----\n" +
    "MIICtzCCAlygAwIBAgIUWDd40Qnm7xO0n3EPkQoWhCI8Me0wCgYIKoZIzj0EAwIw\n" +
    "QjELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRQw\n" +
    "EgYDVQQDEwtFZGltYXhBcHBDQTAgFw0xODEyMjcwNzQ1MDBaGA8yMTE3MTIwMzA3\n" +
    "NDUwMFowPjEMMAoGA1UEBxMDVFNDMQ8wDQYDVQQKEwZFZGltYXgxDDAKBgNVBAsT\n" +
    "A0FwcDEPMA0GA1UEAxMGYXBwbmV3MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE\n" +
    "vzQDHissMeFLvSQQIQPmOWETIVwgSGlHUQq2DRE6ZeuBAX8fs4uP2R+eVzai1tZc\n" +
    "Q+Txpi6T5ISU4RIcy+mo2qOCATAwggEsMA4GA1UdDwEB/wQEAwIDiDATBgNVHSUE\n" +
    "DDAKBggrBgEFBQcDAjAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBSzgIBovQZhS1Zm\n" +
    "l4K6VDOaWe74nTAfBgNVHSMEGDAWgBSHCx4OVUx+KaPpvOo+U+Y8XVzpxTB9Bggr\n" +
    "BgEFBQcBAQRxMG8wMAYIKwYBBQUHMAGGJGh0dHA6Ly9lZGlwcm9kLW9jc3AtYXBw\n" +
    "Y2EuZWRpdHNjLmNvbTA7BggrBgEFBQcwAoYvaHR0cDovL2VkaXByb2QtY3J0LmVk\n" +
    "aXRzYy5jb20vZWRpbWF4X2FwcF9jYS5wZW0wOAYDVR0fBDEwLzAtoCugKYYnaHR0\n" +
    "cDovL2VkaXByb2QtY3JsLmVkaXRzYy5jb20vYXBwY2EuY3JsMAoGCCqGSM49BAMC\n" +
    "A0kAMEYCIQDs38NpsppxNUQ/I2FTOKRrzvELBUYOJQ+Y2gHJpbv3wAIhAKvIhfHC\n" +
    "JsaN90nVlqt4SfliCydjfs7bBmazaD2kBP1B\n" +
    "-----END CERTIFICATE-----"
  const val CLIENT_PRIVATE_KEY = "-----BEGIN EC PRIVATE KEY-----\n" +
    "MHcCAQEEINeyvOQ7KgYigF9IKScDl4gQuz8tqTzt9o4mwqPRdwW0oAoGCCqGSM49\n" +
    "AwEHoUQDQgAEvzQDHissMeFLvSQQIQPmOWETIVwgSGlHUQq2DRE6ZeuBAX8fs4uP\n" +
    "2R+eVzai1tZcQ+Txpi6T5ISU4RIcy+mo2g==\n" +
    "-----END EC PRIVATE KEY-----"
  const val CLOUD_CA = "-----BEGIN CERTIFICATE-----\n" +
      "MIIByTCCAXCgAwIBAgIUc0mq2ZwRVCyFur3kAkaor/M7VY8wCgYIKoZIzj0EAwIw\n" +
      "QzELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRUw\n" +
      "EwYDVQQDEwxFZGltYXhSb290Q0EwHhcNMTgxMjE0MDc1NjAwWhcNMjgxMjExMDc1\n" +
      "NjAwWjBDMQswCQYDVQQGEwJUVzEPMA0GA1UEChMGRWRpbWF4MQwwCgYDVQQLEwNJ\n" +
      "b1QxFTATBgNVBAMTDEVkaW1heFJvb3RDQTBZMBMGByqGSM49AgEGCCqGSM49AwEH\n" +
      "A0IABFX2iG20iW9fxLjIrtAQWxuD58ywjPTkrAZfcdVbLwlQIE9ghbeDM16RK8ti\n" +
      "6GDkoTINUawS1GNsgQWW4ScGWdSjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNVHRMB\n" +
      "Af8EBTADAQH/MB0GA1UdDgQWBBT+pR2/kXIno8z89M7VJxLSwff/YDAKBggqhkjO\n" +
      "PQQDAgNHADBEAiBDFHUZ51J6AfQYsKvf9q4iZLoRu9axl/2D52gsGNQTkQIgewMt\n" +
      "yhWA7AcvGw6wFbZaauUbqiHCZ9nAZ/Y31s3+fco=\n" +
      "-----END CERTIFICATE-----"

  /*--------------------------------
   * Edimax cloud verification
   *-------------------------------*/
  const val CLIENT_CERT_2VERSION = "-----BEGIN CERTIFICATE-----\n" +
  "MIICtTCCAlygAwIBAgIUXgNZjp/S6VfCzaQ1Vi8Rr7ZWxfswCgYIKoZIzj0EAwIw\n" +
  "QjELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRQw\n" +
  "EgYDVQQDEwtFZGltYXhBcHBDQTAgFw0yMDExMjAxNTQ0MDBaGA8yMTE5MTAyODE1\n" +
  "NDQwMFowPjEMMAoGA1UEBxMDVFNDMQ8wDQYDVQQKEwZFZGltYXgxDDAKBgNVBAsT\n" +
  "A0FwcDEPMA0GA1UEAxMGYXBwbmV3MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE\n" +
  "1RKbDoMDs4L8L2J4aGjPwhrjo1wWkj5lliVmLpVoHLdk95w7p7GY9wZCXrgo/0wv\n" +
  "/yKeFIBUQ7MNCg/utsvKn6OCATAwggEsMA4GA1UdDwEB/wQEAwIDiDATBgNVHSUE\n" +
  "DDAKBggrBgEFBQcDAjAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBSvhzEXEQsq6kxu\n" +
  "vl1VCaR2mcZpKTAfBgNVHSMEGDAWgBSHCx4OVUx+KaPpvOo+U+Y8XVzpxTB9Bggr\n" +
  "BgEFBQcBAQRxMG8wMAYIKwYBBQUHMAGGJGh0dHA6Ly9lZGlwcm9kLW9jc3AtYXBw\n" +
  "Y2EuZWRpdHNjLmNvbTA7BggrBgEFBQcwAoYvaHR0cDovL2VkaXByb2QtY3J0LmVk\n" +
  "aXRzYy5jb20vZWRpbWF4X2FwcF9jYS5wZW0wOAYDVR0fBDEwLzAtoCugKYYnaHR0\n" +
  "cDovL2VkaXByb2QtY3JsLmVkaXRzYy5jb20vYXBwY2EuY3JsMAoGCCqGSM49BAMC\n" +
  "A0cAMEQCIHHnZlY9vu5n/E6vEdf0rfw4+KNx+y43iYOUpmGJwOJgAiAfCbjS0z2f\n" +
  "89j1/4sgbc/6m46mxyxBqa+xEQ+OK3uxfQ==\n" +
  "-----END CERTIFICATE-----\n" +
  "-----BEGIN CERTIFICATE-----\n" +
  "MIICpTCCAkygAwIBAgIUchcCmfuWJiRr2sWOyjTe2vYcv7wwCgYIKoZIzj0EAwIw\n" +
  "QzELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRUw\n" +
  "EwYDVQQDEwxFZGltYXhSb290Q0EwHhcNMTgxMjI2MDgyMDAwWhcNMjgxMjIzMDgy\n" +
  "MDAwWjBCMQswCQYDVQQGEwJUVzEPMA0GA1UEChMGRWRpbWF4MQwwCgYDVQQLEwNJ\n" +
  "b1QxFDASBgNVBAMTC0VkaW1heEFwcENBMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcD\n" +
  "QgAE2maa/yTE5EJsgL+7Tk3o26MrAHO/7eQcJudSJDWyRiSg+ZWuGP1otOJjRsqZ\n" +
  "PAQSVEnWk8NtJEKopyn4BGIoS6OCAR0wggEZMA4GA1UdDwEB/wQEAwIBhjASBgNV\n" +
  "HRMBAf8ECDAGAQH/AgEAMB0GA1UdDgQWBBSHCx4OVUx+KaPpvOo+U+Y8XVzpxTAf\n" +
  "BgNVHSMEGDAWgBT+pR2/kXIno8z89M7VJxLSwff/YDB4BggrBgEFBQcBAQRsMGow\n" +
  "MQYIKwYBBQUHMAGGJWh0dHA6Ly9lZGlwcm9kLW9jc3Atcm9vdGNhLmVkaXRzYy5j\n" +
  "b20wNQYIKwYBBQUHMAKGKWh0dHA6Ly9lZGlwcm9kLWNydC5lZGl0c2MuY29tL3Jv\n" +
  "b3RfY2EucGVtMDkGA1UdHwQyMDAwLqAsoCqGKGh0dHA6Ly9lZGlwcm9kLWNybC5l\n" +
  "ZGl0c2MuY29tL3Jvb3RjYS5jcmwwCgYIKoZIzj0EAwIDRwAwRAIgK+MMcPhKmOXs\n" +
  "2ICmohCnhNPmNuSF+TwSKYYcYFZkFeACIBOLrN9M8sb1opKePvmpR0g28ApPmDVr\n" +
  "ZvVNQtiQhjFV\n" +
  "-----END CERTIFICATE-----"

  const val CLIENT_PRIVATE_KEY_2VERSION = "-----BEGIN EC PRIVATE KEY-----\n" +
        "MHcCAQEEIC7AusDwO0Rad4JOauFyM9cj890BMyhGhAYBOdMsnRKAoAoGCCqGSM49\n" +
        "AwEHoUQDQgAE1RKbDoMDs4L8L2J4aGjPwhrjo1wWkj5lliVmLpVoHLdk95w7p7GY\n" +
        "9wZCXrgo/0wv/yKeFIBUQ7MNCg/utsvKnw==\n" +
        "-----END EC PRIVATE KEY-----"

//Cloud not work
  const val CLOUD_CA_V2="-----BEGIN CERTIFICATE-----\n" +
        "MIIBzDCCAXKgAwIBAgIUc0mq2ZwRVCyFur3kAkaor/M7VY8wCgYIKoZIzj0EAwIw\n" +
        "QzELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRUw\n" +
        "EwYDVQQDEwxFZGltYXhSb290Q0EwIBcNMjMwODI1MDMzMDM0WhgPMjEyMzA4MDEw\n" +
        "MzMwMzRaMEMxCzAJBgNVBAYTAlRXMQ8wDQYDVQQKEwZFZGltYXgxDDAKBgNVBAsT\n" +
        "A0lvVDEVMBMGA1UEAxMMRWRpbWF4Um9vdENBMFkwEwYHKoZIzj0CAQYIKoZIzj0D\n" +
        "AQcDQgAEVfaIbbSJb1/EuMiu0BBbG4PnzLCM9OSsBl9x1VsvCVAgT2CFt4MzXpEr\n" +
        "y2LoYOShMg1RrBLUY2yBBZbhJwZZ1KNCMEAwDgYDVR0PAQH/BAQDAgEGMA8GA1Ud\n" +
        "EwEB/wQFMAMBAf8wHQYDVR0OBBYEFP6lHb+RciejzPz0ztUnEtLB9/9gMAoGCCqG\n" +
        "SM49BAMCA0gAMEUCIQCx/pHiZdj+H+YSjZ1I668H5PgELxZNO5a6RNcHXNxhzwIg\n" +
        "Fok2dM9S1mI/nnuh2UUobhH7FC53HMhKqs2x3BfN4NY=\n" +
        "-----END CERTIFICATE-----"
  /*--------------------------------
   * Acelink cloud verification
   *-------------------------------*/
  const val CLIENT_CERT_ACELINK = "-----BEGIN CERTIFICATE-----\n" +
    "MIICtzCCAl2gAwIBAgIUUgpHdnp4oW89JNRJA2Ba/Zc4zg8wCgYIKoZIzj0EAwIw\n" +
    "QjELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRQw\n" +
    "EgYDVQQDEwtFZGltYXhBcHBDQTAgFw0xOTA5MjAwOTMzMDBaGA8yMTE4MDgyNzA5\n" +
    "MzMwMFowPzEMMAoGA1UEBxMDVFNDMRAwDgYDVQQKEwdBY2VsaW5rMQwwCgYDVQQL\n" +
    "EwNBcHAxDzANBgNVBAMTBmFwcG5ldzBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IA\n" +
    "BGT04TJIXAypegjS6z650ZQm87YgMtKfD4nA1CTyF7zpFAZwBpOJRMEVKfh2VCtO\n" +
    "spfjKLDUwQcAxbMdgGUNPpejggEwMIIBLDAOBgNVHQ8BAf8EBAMCA4gwEwYDVR0l\n" +
    "BAwwCgYIKwYBBQUHAwIwDAYDVR0TAQH/BAIwADAdBgNVHQ4EFgQUfe2ehmXO5q3J\n" +
    "5vHlSHKmflO7n1AwHwYDVR0jBBgwFoAUhwseDlVMfimj6bzqPlPmPF1c6cUwfQYI\n" +
    "KwYBBQUHAQEEcTBvMDAGCCsGAQUFBzABhiRodHRwOi8vZWRpcHJvZC1vY3NwLWFw\n" +
    "cGNhLmVkaXRzYy5jb20wOwYIKwYBBQUHMAKGL2h0dHA6Ly9lZGlwcm9kLWNydC5l\n" +
    "ZGl0c2MuY29tL2VkaW1heF9hcHBfY2EucGVtMDgGA1UdHwQxMC8wLaAroCmGJ2h0\n" +
    "dHA6Ly9lZGlwcm9kLWNybC5lZGl0c2MuY29tL2FwcGNhLmNybDAKBggqhkjOPQQD\n" +
    "AgNIADBFAiEAgq9/c4G7jFwLKBe828u1wm/C8rwAsWPoakCMVkWWiDkCIAkynb2J\n" +
    "wvtZS7Whz6V0keyPkMUACGhbYmUc5aufA0C3\n" +
    "-----END CERTIFICATE-----\n" +
    "-----BEGIN CERTIFICATE-----\n" +
    "MIICpTCCAkygAwIBAgIUchcCmfuWJiRr2sWOyjTe2vYcv7wwCgYIKoZIzj0EAwIw\n" +
    "QzELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRUw\n" +
    "EwYDVQQDEwxFZGltYXhSb290Q0EwHhcNMTgxMjI2MDgyMDAwWhcNMjgxMjIzMDgy\n" +
    "MDAwWjBCMQswCQYDVQQGEwJUVzEPMA0GA1UEChMGRWRpbWF4MQwwCgYDVQQLEwNJ\n" +
    "b1QxFDASBgNVBAMTC0VkaW1heEFwcENBMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcD\n" +
    "QgAE2maa/yTE5EJsgL+7Tk3o26MrAHO/7eQcJudSJDWyRiSg+ZWuGP1otOJjRsqZ\n" +
    "PAQSVEnWk8NtJEKopyn4BGIoS6OCAR0wggEZMA4GA1UdDwEB/wQEAwIBhjASBgNV\n" +
    "HRMBAf8ECDAGAQH/AgEAMB0GA1UdDgQWBBSHCx4OVUx+KaPpvOo+U+Y8XVzpxTAf\n" +
    "BgNVHSMEGDAWgBT+pR2/kXIno8z89M7VJxLSwff/YDB4BggrBgEFBQcBAQRsMGow\n" +
    "MQYIKwYBBQUHMAGGJWh0dHA6Ly9lZGlwcm9kLW9jc3Atcm9vdGNhLmVkaXRzYy5j\n" +
    "b20wNQYIKwYBBQUHMAKGKWh0dHA6Ly9lZGlwcm9kLWNydC5lZGl0c2MuY29tL3Jv\n" +
    "b3RfY2EucGVtMDkGA1UdHwQyMDAwLqAsoCqGKGh0dHA6Ly9lZGlwcm9kLWNybC5l\n" +
    "ZGl0c2MuY29tL3Jvb3RjYS5jcmwwCgYIKoZIzj0EAwIDRwAwRAIgK+MMcPhKmOXs\n" +
    "2ICmohCnhNPmNuSF+TwSKYYcYFZkFeACIBOLrN9M8sb1opKePvmpR0g28ApPmDVr\n" +
    "ZvVNQtiQhjFV\n" +
    "-----END CERTIFICATE-----"
  const val CLIENT_PRIVATE_KEY_ACELINK = "-----BEGIN EC PRIVATE KEY-----\n" +
    "MHcCAQEEIOJq18ksiiKQavokp4eeSO3bOxT0mQJzA2WM0t/UIJeioAoGCCqGSM49\n" +
    "AwEHoUQDQgAEZPThMkhcDKl6CNLrPrnRlCbztiAy0p8PicDUJPIXvOkUBnAGk4lE\n" +
    "wRUp+HZUK06yl+MosNTBBwDFsx2AZQ0+lw==\n" +
    "-----END EC PRIVATE KEY-----"

  /*--------------------------------
   * Acelink cloud verification
   *-------------------------------*/

  const val CLIENT_CERT_ACELINK_2VERSION = "-----BEGIN CERTIFICATE-----\n" +
          "MIICtzCCAl2gAwIBAgIUC8cBFKFhKf/7y162PCcFc/9cqZMwCgYIKoZIzj0EAwIw\n" +
          "QjELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRQw\n" +
          "EgYDVQQDEwtFZGltYXhBcHBDQTAgFw0yMDExMjAxNTQyMDBaGA8yMTE5MTAyODE1\n" +
          "NDIwMFowPzEMMAoGA1UEBxMDVFNDMRAwDgYDVQQKEwdBY2VsaW5rMQwwCgYDVQQL\n" +
          "EwNBcHAxDzANBgNVBAMTBmFwcG5ldzBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IA\n" +
          "BOh7WPKxaB3mcbTUVXLYkjOf+Sbq3+7t0fxLWSFR0AbhriAq9eDyVr57LWbxd0os\n" +
          "QQPWqgdFjfs9VxhdoMSf9jqjggEwMIIBLDAOBgNVHQ8BAf8EBAMCA4gwEwYDVR0l\n" +
          "BAwwCgYIKwYBBQUHAwIwDAYDVR0TAQH/BAIwADAdBgNVHQ4EFgQUom6JUOu/IWh3\n" +
          "Hx7xoEa9+zVuLxUwHwYDVR0jBBgwFoAUhwseDlVMfimj6bzqPlPmPF1c6cUwfQYI\n" +
          "KwYBBQUHAQEEcTBvMDAGCCsGAQUFBzABhiRodHRwOi8vZWRpcHJvZC1vY3NwLWFw\n" +
          "cGNhLmVkaXRzYy5jb20wOwYIKwYBBQUHMAKGL2h0dHA6Ly9lZGlwcm9kLWNydC5l\n" +
          "ZGl0c2MuY29tL2VkaW1heF9hcHBfY2EucGVtMDgGA1UdHwQxMC8wLaAroCmGJ2h0\n" +
          "dHA6Ly9lZGlwcm9kLWNybC5lZGl0c2MuY29tL2FwcGNhLmNybDAKBggqhkjOPQQD\n" +
          "AgNIADBFAiEAkvwQ+IMnl39hKNVD20KXfPADzP1LFctszRNZJkkHY8wCIAMnmvWp\n" +
          "7qDvEbDs9+sN1YJVkW+Zp38MklGXvMmKbNB6\n" +
          "-----END CERTIFICATE-----\n" +
          "-----BEGIN CERTIFICATE-----\n" +
          "MIICpTCCAkygAwIBAgIUchcCmfuWJiRr2sWOyjTe2vYcv7wwCgYIKoZIzj0EAwIw\n" +
          "QzELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRUw\n" +
          "EwYDVQQDEwxFZGltYXhSb290Q0EwHhcNMTgxMjI2MDgyMDAwWhcNMjgxMjIzMDgy\n" +
           "MDAwWjBCMQswCQYDVQQGEwJUVzEPMA0GA1UEChMGRWRpbWF4MQwwCgYDVQQLEwNJ\n" +
          "b1QxFDASBgNVBAMTC0VkaW1heEFwcENBMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcD\n" +
          "QgAE2maa/yTE5EJsgL+7Tk3o26MrAHO/7eQcJudSJDWyRiSg+ZWuGP1otOJjRsqZ\n" +
           "PAQSVEnWk8NtJEKopyn4BGIoS6OCAR0wggEZMA4GA1UdDwEB/wQEAwIBhjASBgNV\n" +
           "HRMBAf8ECDAGAQH/AgEAMB0GA1UdDgQWBBSHCx4OVUx+KaPpvOo+U+Y8XVzpxTAf\n" +
           "BgNVHSMEGDAWgBT+pR2/kXIno8z89M7VJxLSwff/YDB4BggrBgEFBQcBAQRsMGow\n" +
           "MQYIKwYBBQUHMAGGJWh0dHA6Ly9lZGlwcm9kLW9jc3Atcm9vdGNhLmVkaXRzYy5j\n" +
           "b20wNQYIKwYBBQUHMAKGKWh0dHA6Ly9lZGlwcm9kLWNydC5lZGl0c2MuY29tL3Jv\n" +
          "b3RfY2EucGVtMDkGA1UdHwQyMDAwLqAsoCqGKGh0dHA6Ly9lZGlwcm9kLWNybC5l\n" +
         "ZGl0c2MuY29tL3Jvb3RjYS5jcmwwCgYIKoZIzj0EAwIDRwAwRAIgK+MMcPhKmOXs\n" +
         "2ICmohCnhNPmNuSF+TwSKYYcYFZkFeACIBOLrN9M8sb1opKePvmpR0g28ApPmDVr\n" +
          "ZvVNQtiQhjFV\n" +
          "-----END CERTIFICATE-----"





  const val CLIENT_PRIVATE_KEY_ACELINK_2VERSION = "-----BEGIN EC PRIVATE KEY-----\n" +
          "MHcCAQEEIHkcJj9UlVvOMVFkbrE3vKlCbwo2OlbwnQwNxdboppxhoAoGCCqGSM49\n" +
          "AwEHoUQDQgAE6HtY8rFoHeZxtNRVctiSM5/5Jurf7u3R/EtZIVHQBuGuICr14PJW\n" +
          "vnstZvF3SixBA9aqB0WN+z1XGF2gxJ/2Og==\n" +
          "-----END EC PRIVATE KEY-----"

  /*--------------------------------
   * Galaxy cloud verification
   *-------------------------------*/
  const val CLIENT_CERT_SMARTVISION = "-----BEGIN CERTIFICATE-----\n" +
    "MIICtjCCAlygAwIBAgIUdUxPPKI9ajUgv6L/XXRQ9yjAQrIwCgYIKoZIzj0EAwIw\n" +
    "QjELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRQw\n" +
    "EgYDVQQDEwtFZGltYXhBcHBDQTAgFw0xOTA5MjAwODAxMDBaGA8yMTE4MDgyNzA4\n" +
    "MDEwMFowPjEMMAoGA1UEBxMDVFNDMQ8wDQYDVQQKEwZHYWxheHkxDDAKBgNVBAsT\n" +
    "A0FwcDEPMA0GA1UEAxMGYXBwbmV3MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE\n" +
    "+SpbniykQkPGq2DvVxeYTVb183eJ3UWrGi6WqQN97CcvQv0d33706/4t/Y0bl62T\n" +
    "HK5kK3PpPlx4XwyW7MytdaOCATAwggEsMA4GA1UdDwEB/wQEAwIDiDATBgNVHSUE\n" +
    "DDAKBggrBgEFBQcDAjAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBQpwXfTmCehUw4+\n" +
    "Et7haDSeM8UdSjAfBgNVHSMEGDAWgBSHCx4OVUx+KaPpvOo+U+Y8XVzpxTB9Bggr\n" +
    "BgEFBQcBAQRxMG8wMAYIKwYBBQUHMAGGJGh0dHA6Ly9lZGlwcm9kLW9jc3AtYXBw\n" +
    "Y2EuZWRpdHNjLmNvbTA7BggrBgEFBQcwAoYvaHR0cDovL2VkaXByb2QtY3J0LmVk\n" +
    "aXRzYy5jb20vZWRpbWF4X2FwcF9jYS5wZW0wOAYDVR0fBDEwLzAtoCugKYYnaHR0\n" +
    "cDovL2VkaXByb2QtY3JsLmVkaXRzYy5jb20vYXBwY2EuY3JsMAoGCCqGSM49BAMC\n" +
    "A0gAMEUCIQDbbHp7k1lMQRs0s19Z9qqCdvVkhj45OEVA546A6Z6sPgIga+/EW3d8\n" +
    "HEtW3TLGLj0O2xnFax7eZUsPiW0pQtjiE6U=\n" +
    "-----END CERTIFICATE-----\n" +
    "-----BEGIN CERTIFICATE-----\n" +
    "MIICpTCCAkygAwIBAgIUchcCmfuWJiRr2sWOyjTe2vYcv7wwCgYIKoZIzj0EAwIw\n" +
    "QzELMAkGA1UEBhMCVFcxDzANBgNVBAoTBkVkaW1heDEMMAoGA1UECxMDSW9UMRUw\n" +
    "EwYDVQQDEwxFZGltYXhSb290Q0EwHhcNMTgxMjI2MDgyMDAwWhcNMjgxMjIzMDgy\n" +
    "MDAwWjBCMQswCQYDVQQGEwJUVzEPMA0GA1UEChMGRWRpbWF4MQwwCgYDVQQLEwNJ\n" +
    "b1QxFDASBgNVBAMTC0VkaW1heEFwcENBMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcD\n" +
    "QgAE2maa/yTE5EJsgL+7Tk3o26MrAHO/7eQcJudSJDWyRiSg+ZWuGP1otOJjRsqZ\n" +
    "PAQSVEnWk8NtJEKopyn4BGIoS6OCAR0wggEZMA4GA1UdDwEB/wQEAwIBhjASBgNV\n" +
    "HRMBAf8ECDAGAQH/AgEAMB0GA1UdDgQWBBSHCx4OVUx+KaPpvOo+U+Y8XVzpxTAf\n" +
    "BgNVHSMEGDAWgBT+pR2/kXIno8z89M7VJxLSwff/YDB4BggrBgEFBQcBAQRsMGow\n" +
    "MQYIKwYBBQUHMAGGJWh0dHA6Ly9lZGlwcm9kLW9jc3Atcm9vdGNhLmVkaXRzYy5j\n" +
    "b20wNQYIKwYBBQUHMAKGKWh0dHA6Ly9lZGlwcm9kLWNydC5lZGl0c2MuY29tL3Jv\n" +
    "b3RfY2EucGVtMDkGA1UdHwQyMDAwLqAsoCqGKGh0dHA6Ly9lZGlwcm9kLWNybC5l\n" +
    "ZGl0c2MuY29tL3Jvb3RjYS5jcmwwCgYIKoZIzj0EAwIDRwAwRAIgK+MMcPhKmOXs\n" +
    "2ICmohCnhNPmNuSF+TwSKYYcYFZkFeACIBOLrN9M8sb1opKePvmpR0g28ApPmDVr\n" +
    "ZvVNQtiQhjFV\n" +
    "-----END CERTIFICATE-----"
  const val CLIENT_PRIVATE_KEY_SMARTVISION = "-----BEGIN EC PRIVATE KEY-----\n" +
    "MHcCAQEEIPk6e8jqgKUGOLq9pLb054PusXdpq+a/+fryd7NKMlY7oAoGCCqGSM49\n" +
    "AwEHoUQDQgAE+SpbniykQkPGq2DvVxeYTVb183eJ3UWrGi6WqQN97CcvQv0d3370\n" +
    "6/4t/Y0bl62THK5kK3PpPlx4XwyW7MytdQ==\n" +
    "-----END EC PRIVATE KEY-----"
}