package com.vht.sdkcore.utils

data class ProtobufAppLogModel(
    var logMode: String = Constants.LOG_TCP_HEADER,
    var timeStamp: String = System.currentTimeMillis().toString(),
    var clientIp: String = Utils.getIpv4Address().toString(),
    var userId: String = Constants.LOG_DEFAULT,
    var phoneNumber: String = Constants.LOG_DEFAULT,
    var appAgent: String = Constants.LOG_APP_AGENT,
    var appVersion: String = Constants.LOG_DEFAULT,
    var screenId: String = Constants.LOG_DEFAULT,
    var actionId: String = Constants.LOG_DEFAULT,
    var deviceId: String = Constants.LOG_DEFAULT,
    var serverDomainIP: String = Constants.LOG_DEFAULT,
    var tcpMethod: String = Constants.LOG_DEFAULT,
    var tcpURL: String = Constants.LOG_DEFAULT,
    var tcpVersion: String = Constants.LOG_DEFAULT,
    var tcpResponseCode: String = Constants.LOG_DEFAULT,
    var tcpResponseBodyLength: String = Constants.LOG_DEFAULT,
    var tcpResponseTime: String = Constants.LOG_DEFAULT
)

data class HTTPAppLogModel(
    var logMode: String = Constants.LOG_HTTP_HEADER,
    var timeStamp: String = System.currentTimeMillis().toString(),
    var clientIp: String = Utils.getIpv4Address().toString(),
    var userId: String = Constants.LOG_DEFAULT,
    var phoneNumber: String = Constants.LOG_DEFAULT,
    var appAgent: String = Constants.LOG_APP_AGENT,
    var appVersion: String = Constants.LOG_DEFAULT,
    var screenId: String = Constants.LOG_DEFAULT,
    var actionId: String = Constants.LOG_DEFAULT,
    var deviceId: String = Constants.LOG_DEFAULT,
    var serverDomainIP: String = Constants.LOG_DEFAULT,
    var httpMethod: String = Constants.LOG_DEFAULT,
    var httpURL: String = Constants.LOG_DEFAULT,
    var httpVersion: String = Constants.LOG_DEFAULT,
    var httpResponseCode: String = Constants.LOG_DEFAULT,
    var httpResponseBodyLength: String = Constants.LOG_DEFAULT,
    var httpResponseTime: String = Constants.LOG_DEFAULT,
    var apiResponseCode: String = Constants.LOG_DEFAULT,
    var traceParent: String = Constants.LOG_DEFAULT
)

data class ActionAppLogModel(
    var logMode: String = Constants.LOG_HTTP_HEADER,
    var timeStamp: String = System.currentTimeMillis().toString(),
    var clientIp: String = Utils.getIpv4Address().toString(),
    var userId: String = Constants.LOG_DEFAULT,
    var phoneNumber: String = Constants.LOG_DEFAULT,
    var appAgent: String = Constants.LOG_APP_AGENT,
    var appVersion: String = Constants.LOG_DEFAULT,
    var screenId: String = Constants.LOG_DEFAULT,
    var actionId: String = Constants.LOG_DEFAULT,
    var deviceId: String = Constants.LOG_DEFAULT,
    var serverDomainIP: String = Constants.LOG_DEFAULT,
    var loadTime: String = "",
    var type: String = ""
)
