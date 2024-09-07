package br.com.example.kellmertrack.remote.service

import com.google.firebase.messaging.FirebaseMessagingService

open class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}