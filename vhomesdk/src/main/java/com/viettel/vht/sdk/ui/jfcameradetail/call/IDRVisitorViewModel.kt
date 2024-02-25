package com.viettel.vht.sdk.ui.jfcameradetail.call

import android.os.Message
import com.alibaba.fastjson.JSON
import com.basic.G
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.MsgContent
import com.vht.sdkcore.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class IDRVisitorViewModel @Inject constructor() : BaseViewModel(), IFunSDKResult{

    val SUPPORT_VOICE_REPLY = "SUPPORT_VOICE_REPLY"
    var devId: String? = null
    set(value) {
        field = value
        initData()
    }
    private var userId = 0

    init {
        userId = FunSDK.GetId(userId, this)
    }

    private fun initData() {
        //每次都获取能力集 需要判断设备密码的
//        val isSupport: Boolean = SPUtil.getInstance(iVisitorView.getContext())
//            .getSettingParam(
//                com.xm.device.idr.ui.visitor.presenter.VisitorPresenter.SUPPORT_VOICE_REPLY + devId,
//                false
//            )
//        if (isSupport) {
//            getAudioList()
//            if (iVisitorView != null) {
//                iVisitorView.onShowVoiceReplyView()
//            }
//        } else {
//            FunSDK.DevGetConfigByJson(userId, devId, "SystemFunction", 1024, -1, 5000, 0)
//        }

        FunSDK.DevGetConfigByJson(userId, devId, "SystemFunction", 1024, -1, 5000, 0)
    }

    override fun OnFunSDKResult(message: Message?, msgContent: MsgContent?): Int {
        try {
            if (message!!.arg1 < 0) {
                return 0
            }
            when (message.what) {
                EUIMSG.DEV_GET_JSON -> {
                    if (msgContent!!.pData == null) {
                        Timber.d("initialization failed")
                        return 0
                    }
                    if ("SystemFunction" == msgContent.str) {
                        val jsonData = G.ToString(msgContent.pData)
                        if (jsonData != null) {
                            var jsonObject = JSON.parseObject(jsonData)
                            if (jsonObject != null && jsonObject.containsKey("SystemFunction")) {
                                jsonObject = jsonObject.getJSONObject("SystemFunction")
                                if (jsonObject != null && jsonObject.containsKey("OtherFunction")) {
                                    jsonObject = jsonObject.getJSONObject("OtherFunction")
                                    if (jsonObject != null && jsonObject.containsKey("SupportQuickReply")) {
                                        val result = jsonObject["SupportQuickReply"]
                                        if (result is Boolean) {
                                            if (result) {
                                                Timber.d("initialization success")
//                                                SPUtil.getInstance(iVisitorView.getContext())
//                                                    .setSettingParam(
//                                                        com.xm.device.idr.ui.visitor.presenter.VisitorPresenter.SUPPORT_VOICE_REPLY + devId,
//                                                        true
//                                                    )
//                                                getAudioList()
//                                                if (iVisitorView != null) {
//                                                    iVisitorView.onShowVoiceReplyView()
//                                                }
                                                return 0
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Timber.d("initialization failed")
                    }
                }
                /*EUIMSG.DEV_CMD_EN -> {
                    if (msgContent!!.pData == null) {
                        Toast.makeText(
                            iVisitorView.getContext(),
                            FunSDK.TS("TR_Init_Failed"),
                            Toast.LENGTH_LONG
                        ).show()
                        return 0
                    }
                    val jsonData = G.ToString(msgContent!!.pData)
                    if (jsonData != null) {
                        val `object` = JSON.parseObject(jsonData)
                        if (`object` != null && `object`.containsKey(VoiceReplyBean.JSON_NAME)) {
                            var result = `object`[VoiceReplyBean.JSON_NAME]
                            if (result == null) {
                                Toast.makeText(
                                    iVisitorView.getContext(),
                                    FunSDK.TS("TR_Init_Failed"),
                                    Toast.LENGTH_LONG
                                ).show()
                                break
                            }
                            if (result.javaClass.name == "com.alibaba.fastjson.JSONArray") {
                                val json = `object`.getString(VoiceReplyBean.JSON_NAME)
                                if (json != null) {
                                    try {
                                        result = JSON.parseArray(json, VoiceReplyBean::class.java)
                                        if (result is List<*>) {
                                            voiceReplyBeans = result as List<VoiceReplyBean?>
                                            break
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(
                            iVisitorView.getContext(),
                            FunSDK.TS("TR_Init_Failed"),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }*/
                else -> {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }
}