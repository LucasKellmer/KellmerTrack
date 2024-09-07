package br.com.example.kellmertrack.services.location

import android.location.Location

interface LocationClientCallBack {
    fun onLocationChanged(location : Location?)
}