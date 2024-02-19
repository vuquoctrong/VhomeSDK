package com.viettel.vht.core.base

import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log

private const val TAG = "HeadsetPlugReceiver"

class HeadsetPlugReceiver : BroadcastReceiver() {
    lateinit var audioManager: AudioManager

    override fun onReceive(context: Context?, intent: Intent?) {
        audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (intent?.action != null) {
            when (intent.action) {
                Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1)
//                    for basic headsets
                    if (state == 0) {
                        Log.e(
                            TAG, "ACTION_HEADSET_UNPLUG"
                        )
                    } else if (state == 1) {
                        Log.e(TAG, "ACTION_HEADSET_PLUG")
                    }

                }
                BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1)

                    if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                        Log.e(
                            TAG, "BluetoothHeadset: STATE_AUDIO_DISCONNECTED"
                        )
                    } else if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                        Log.e(
                            TAG, "BluetoothHeadset: STATE_AUDIO_CONNECTED"
                        )
                    }
                }
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1)
                    if (state == BluetoothHeadset.STATE_CONNECTED) {
                        Log.e(TAG, "BluetoothHeadset.STATE_CONNECTED")
//                        Log.e(TAG, "onReceive: Starting BluetoothScoOn")

                        audioManager.isBluetoothScoOn = true
//                        need audio mode MODE_IN_COMMUNICATION to switch between speaker and headset
                        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                        audioManager.startBluetoothSco()

                    } else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
                        Log.e(TAG, "BluetoothHeadset.STATE_DISCONNECTED")
                        audioManager.mode = AudioManager.MODE_CURRENT
//                        audioManager.isBluetoothScoOn = false
//                        audioManager.stopBluetoothSco()
                        audioManager.isSpeakerphoneOn = true

                    }

                }
            }
        }
    }
}