package com.biztools.stockcount.api

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class Header(val key: String, val value: String)
class CustomHttpClient {
    companion object {
        @SuppressLint("CustomX509TrustManager")
        class UnsafeTrust : X509TrustManager {

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }

        class UnsafeHostVerifier : HostnameVerifier {
            @SuppressLint("BadHostnameVerifier")
            override fun verify(hostname: String?, session: SSLSession?): Boolean {
                return true
            }
        }

        fun unsafeSslClient(
            token: String? = null,
            headers: List<Header> = listOf()
//            refreshToken: String? = null,
//            deviceId: String? = null,
//            forRefresh: Boolean = false
        ): OkHttpClient {
            val trusts = arrayOf<TrustManager>(
                UnsafeTrust()
            )
            val sslCtx = SSLContext.getInstance("SSL")
            sslCtx.init(null, trusts, java.security.SecureRandom())
            val sslFactory = sslCtx.socketFactory
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslFactory, UnsafeTrust())
            builder.hostnameVerifier(UnsafeHostVerifier())
            val hasToken = !token.isNullOrBlank() && token.isNotEmpty() && token != "no-token"
//            val hasRefresh = !refreshToken.isNullOrBlank() && refreshToken.isNotEmpty()
//            val hasDevice = !deviceId.isNullOrBlank() && deviceId.isNotEmpty()
            builder.addInterceptor {
                val b = it.request().newBuilder()
                b.addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
//                    .addHeader("conical-api-key", "s24eBL77FCuik/d8BsD9vk+0xnmGK8mNxrWRQT9JfQ0=")
//                if (hasToken || hasRefresh || hasDevice) {
                if (hasToken) {
                    b.addHeader("Authorization", "Bearer " + token!!)
//                    b.addHeader("conical-device-id", deviceId!!)
//                    when (forRefresh) {
//                        true -> b.addHeader("Authorization", "Bearer " + refreshToken!!)
//                            .addHeader("conical-refresh-token", token!!)
//
//                        false -> b.addHeader("Authorization", "Bearer " + token!!)
//                            .addHeader("conical-refresh-token", refreshToken!!)
//                    }
                }
                if (headers.isNotEmpty()) {
                    for (h in headers) {
                        b.addHeader(h.key, h.value)
                    }
                }
                it.proceed(b.build())
            }
            builder.connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
            return builder.build()
        }
    }
}