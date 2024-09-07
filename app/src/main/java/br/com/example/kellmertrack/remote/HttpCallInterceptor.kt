package br.com.example.kellmertrack.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HttpCallInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request=chain.request()
        return try {
            chain.proceed(request)
        }catch (e: Exception){
            val contingenciaUrl = if(request.url.encodedPath == "/mineracaotracksocket")
                "http://192.168.0.7:8080"
            else
                "http://192.168.0.7:8080" + request.url.encodedPath
            val newRequest=request.newBuilder()
                .url(contingenciaUrl )
                .build()
            println(e.message)
            chain.proceed(newRequest)
        }
    }
}