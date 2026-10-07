package com.justmusic.app.data.repository

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AudioOutputRepository(private val context: Context) {

    val currentAudioDeviceName: Flow<String> = callbackFlow {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        fun updateDevice() {
            var deviceName = "Phone Speaker"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                for (device in devices) {
                    when (device.type) {
                        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                        AudioDeviceInfo.TYPE_BLE_HEADSET,
                        AudioDeviceInfo.TYPE_BLE_SPEAKER -> {
                            val productName = device.productName?.toString()
                            deviceName = if (!productName.isNullOrEmpty() && productName != "Bluetooth") {
                                productName
                            } else {
                                "Bluetooth Headset"
                            }
                            break
                        }
                        AudioDeviceInfo.TYPE_WIRED_HEADSET,
                        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                        AudioDeviceInfo.TYPE_USB_HEADSET -> {
                            deviceName = "Wired Headphones"
                        }
                    }
                }
            }
            trySend(deviceName)
        }

        updateDevice()

        val callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            object : android.media.AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                    updateDevice()
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    updateDevice()
                }
            }
        } else null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && callback != null) {
            audioManager.registerAudioDeviceCallback(callback, null)
        }

        awaitClose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && callback != null) {
                audioManager.unregisterAudioDeviceCallback(callback)
            }
        }
    }
}
