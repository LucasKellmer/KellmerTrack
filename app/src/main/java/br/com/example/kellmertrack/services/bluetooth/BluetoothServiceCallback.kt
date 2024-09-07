package br.com.example.kellmertrack.services.bluetooth

interface BluetoothServiceCallback {
    fun startBluetoothScanning()

    fun disconnectDevice()
}