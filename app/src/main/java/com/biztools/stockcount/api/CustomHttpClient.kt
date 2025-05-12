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
            builder.addInterceptor {
                val b = it.request().newBuilder()
                b.addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                if (hasToken) {
                    b.addHeader("Authorization", "Bearer " + token!!)
                }
                if (headers.isNotEmpty()) {
                    for (h in headers) {
                        b.addHeader(h.key, h.value)
                    }
                }
                it.proceed(b.build())
            }
            builder.connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
            return builder.build()
        }
    }
}