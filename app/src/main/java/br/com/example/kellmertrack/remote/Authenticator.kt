package br.com.example.kellmertrack.remote

import okhttp3.Interceptor
import okhttp3.Response

class Authenticator: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder()
            .header("Content-Type", "application/json").build()
        return chain.proceed(authenticatedRequest)
    }
}