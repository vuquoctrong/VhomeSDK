package com.viettel.vht.sdk.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.viettel.vht.sdk.databinding.ActivitySdkVhomeTestBinding
import com.viettel.vht.sdk.funtionsdk.VHomeSDKAddCameraJFListener
import com.viettel.vht.sdk.funtionsdk.VHomeSDKManager
import com.viettel.vht.sdk.model.DeviceDataResponse
import com.viettel.vht.sdk.utils.DebugConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SDKVHomeTestActivity : AppCompatActivity()  {
    private lateinit var binding:ActivitySdkVhomeTestBinding
    @Inject
    lateinit var vHomeSDKManager: VHomeSDKManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySdkVhomeTestBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.btnOpenLib.setOnClickListener {
            vHomeSDKManager.openAddCameraJF(this, object : VHomeSDKAddCameraJFListener {
                override fun onFailed(messageError: String) {
                    DebugConfig.logd(message =messageError)
                }

                override fun onSuccess(data: DeviceDataResponse) {
                    val idCamera =  data.id
                    val serial = data.getSerialNumber()
                    val nameCamera = data.name
                    val model = data.getModelCamera()
                    DebugConfig.logd(message = idCamera)
                }

            })
        }
    }


}