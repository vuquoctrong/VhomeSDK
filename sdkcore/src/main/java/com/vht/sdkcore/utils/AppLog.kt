package com.vht.sdkcore.utils

object AppLog {

    const val EMPTY = ""

    enum class LogLiveView(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        PLAYBACK_STATE(
            ScreenID.LIVE_VIEW,
            ActionID.PLAYBACK_STATE,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        PLAY_LIVE_VIEW_APP_OPTION(
            ScreenID.LIVE_VIEW,
            ActionID.PLAY_LIVEVIEW,
            TcpMethod.METHOD_APP_OPTION,
            EMPTY
        ),
        PLAY_LIVE_VIEW_APP_DESCRIBE(
            ScreenID.LIVE_VIEW,
            ActionID.PLAY_LIVEVIEW,
            TcpMethod.METHOD_APP_DESCRIBE,
            EMPTY
        ),
        PLAY_LIVE_VIEW_APP_PLAY(
            ScreenID.LIVE_VIEW,
            ActionID.PLAY_LIVEVIEW,
            TcpMethod.METHOD_APP_PLAY,
            EMPTY
        ),
        GET_DEVICE_INFO(
            ScreenID.LIVE_VIEW,
            ActionID.GET_DEVICE_INFO,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        GET_VIDEO_INFO(
            ScreenID.LIVE_VIEW,
            ActionID.GET_VIDEO_INFO,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        GET_SDCARD_INFO(
            ScreenID.LIVE_VIEW,
            ActionID.GET_SDCARD_INFO,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        OFF_PRIVACY_MODE(
            ScreenID.LIVE_VIEW,
            ActionID.OFF_PRIVACY_MODE,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        ON_ACTIVE_DEFENSE(
            ScreenID.LIVE_VIEW,
            ActionID.ON_ACTIVE_DEFENSE,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        OFF_ACTIVE_DEFENSE(
            ScreenID.LIVE_VIEW,
            ActionID.OFF_ACTIVE_DEFENSE,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        PTZ_CONTROL(
            ScreenID.LIVE_VIEW,
            ActionID.PTZ_CONTROL,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        GET_WIFI_INFO(
            ScreenID.LIVE_VIEW,
            ActionID.GET_WIFI_INFO,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        KEEP_LIVESTREAM(
            ScreenID.LIVE_VIEW,
            ActionID.KEEP_LIVESTREAM,
            TcpMethod.METHOD_APP_OPTION,
            EMPTY
        ),
        HOLD_SOCKET_SESSION(
            ScreenID.LIVE_VIEW,
            ActionID.HOLD_SOCKET_SESSION,
            TcpMethod.METHOD_APP_OPTION,
            EMPTY
        ),
        GET_SECRET_KEY(
            ScreenID.LIVE_VIEW,
            ActionID.GET_SECRET_KEY,
            TcpMethod.METHOD_DEVICE_CONTROL,
            HttpMethod.GET
        ),
        GET_NONCE(
            ScreenID.LIVE_VIEW,
            ActionID.GET_NONCE,
            TcpMethod.METHOD_REGISTER,
            EMPTY
        ),
        GET_AUTHENTICATION_KEY(
            ScreenID.LIVE_VIEW,
            ActionID.GET_AUT_DATA_KEY,
            TcpMethod.METHOD_REGISTER,
            EMPTY
        ),
        VERIFY_ENCRYPTION(
            ScreenID.LIVE_VIEW,
            ActionID.VERIFY_ENCRYPTION,
            TcpMethod.METHOD_ENCRYPTION_STATUS,
            EMPTY
        ),
        STOP_STREAM(
            ScreenID.LIVE_VIEW,
            ActionID.STOP_STREAM,
            TcpMethod.METHOD_STOP_STREAM,
            EMPTY
        ),
        SET_VIDEO_QUALITY(
            ScreenID.LIVE_VIEW,
            ActionID.SET_VIDEO_QUALITY,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        VOICE_TALK_SETUP_REQUEST(
            ScreenID.LIVE_VIEW,
            ActionID.VOICE_TALK_SETUP_REQUEST,
            TcpMethod.METHOD_APP_RTP_SETUP,
            EMPTY
        ),
        LOG_VOICE_INFO_WHEN_STOP(
            ScreenID.LIVE_VIEW,
            ActionID.LOG_VOICE_INFO_WHEN_STOP,
            TcpMethod.METHOD_DEVICE_CONTROL,
            HttpMethod.POST
        ),
        GET_SUBSCRIBED_CLOUD_PACKAGE(
            ScreenID.LIVE_VIEW,
            ActionID.GET_SUBSCRIBED_CLOUD_PACKAGE,
            TcpMethod.METHOD_DEVICE_CONTROL,
            HttpMethod.POST
        ),
        CLOSE_CONNECTION(
            ScreenID.LIVE_VIEW,
            ActionID.CLOSE_CONNECTION,
            TcpMethod.TCP_CLOSED,
            EMPTY
        ),
        START_LIVE_VIEW(
            ScreenID.LIVE_VIEW,
            ActionID.PLAY_LIVEVIEW,
            EMPTY,
            EMPTY
        ),
        CONTROL_PTZ(
            ScreenID.LIVE_VIEW,
            ActionID.CONTROL_PTZ,
            EMPTY,
            EMPTY
        ),
        VOICE_CALL(
            ScreenID.LIVE_VIEW,
            ActionID.VOICE_CALL,
            EMPTY,
            EMPTY
        ),
        HUMAN_TRACKING(
            ScreenID.LIVE_VIEW,
            ActionID.HUMAN_TRACKING,
            EMPTY,
            EMPTY
        ),
    }

    enum class LogMultiLiveView(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        MULTI_LIVEVIEW(
            ScreenID.MULTI_LIVEVIEW,
            ActionID.MULTI_LIVEVIEW,
            EMPTY,
            EMPTY
        )
    }

    enum class LogPlayBack(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_SUBSCRIBED_CLOUD_PACKAGE(
            ScreenID.PLAY_BACK,
            ActionID.GET_SUBSCRIBED_CLOUD_PACKAGE,
            EMPTY,
            HttpMethod.POST
        )
    }

    enum class LogOut(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        LOG_OUT(
            ScreenID.LOG_OUT,
            ActionID.LOG_OUT,
            EMPTY,
            EMPTY
        )
    }

    enum class LogPlayBackSDCard(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        CHECK_SDCARD_STATUS(
            ScreenID.PLAY_BACK_SDCARD,
            ActionID.CHECK_SDCARD_STATUS,
            TcpMethod.METHOD_DEVICE_CONTROL,
            HttpMethod.POST
        ),
        GET_LIST_SDCARD_VIDEO(
            ScreenID.PLAY_BACK_SDCARD,
            ActionID.GET_LIST_SDCARD_VIDEO,
            TcpMethod.METHOD_DEVICE_CONTROL,
            HttpMethod.POST
        ),
        PLAY_SDCARD_VIDEO(
            ScreenID.PLAY_BACK_SDCARD,
            ActionID.PLAY_SDCARD_VIDEO,
            EMPTY,
            HttpMethod.POST
        ),
        CONTROL_PLAYBACK_STATE(
            ScreenID.PLAY_BACK_SDCARD,
            ActionID.CONTROL_PLAYBACK_STATE,
            EMPTY,
            HttpMethod.POST
        ),
        PAUSE_SDCARD_VIDEO(
            ScreenID.PLAY_BACK_SDCARD,
            ActionID.PAUSE_SDCARD_VIDEO,
            EMPTY,
            HttpMethod.POST
        ),
        RECORD_PLAYBACK(
            ScreenID.PLAY_BACK_SDCARD,
            ActionID.RECORD_PLAYBACK,
            EMPTY,
            HttpMethod.POST
        ),
        PLAYBACK_SDCARD(
            ScreenID.PLAY_BACK_SDCARD,
            ActionID.PLAYBACK_SDCARD,
            EMPTY,
            HttpMethod.POST
        ),
        PLAYBACK_SDCARD_VIDEO(
            ScreenID.PLAY_BACK_SDCARD,
            ActionID.PLAYBACK_SDCARD_VIDEO,
            EMPTY,
            HttpMethod.POST
        ),
        SELECT_CALENDAR(
            ScreenID.PLAY_BACK_SDCARD,
            ActionID.SDCARD_SELECT_CALENDAR,
            EMPTY,
            HttpMethod.POST
        ),
        SEEK_TIME(
            ScreenID.PLAY_BACK_SDCARD,
            ActionID.SDCARD_SEEK_TIME,
            EMPTY,
            HttpMethod.POST
        ),
    }

    enum class LogEvent(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_LIST_CAMERA(
            ScreenID.EVENT,
            ActionID.GET_LIST_CAMERA,
            EMPTY,
            HttpMethod.POST
        ),
        GET_LIST_EVENT(
            ScreenID.EVENT,
            ActionID.GET_LIST_EVENT,
            EMPTY,
            HttpMethod.POST
        ),
        VIEW_EVENT(
            ScreenID.EVENT,
            ActionID.VIEW_EVENT,
            EMPTY,
            HttpMethod.POST
        ),
        DELETE_EVENT(
            ScreenID.EVENT,
            ActionID.DELETE_EVENT,
            EMPTY,
            HttpMethod.POST
        ),
        READ_ALL_EVENT(
            ScreenID.EVENT,
            ActionID.READ_ALL_EVENT,
            EMPTY,
            HttpMethod.POST
        ),
        READ_EVENT(
            ScreenID.EVENT,
            ActionID.READ_EVENT,
            EMPTY,
            HttpMethod.POST
        ),
    }

    enum class LogCamSetting(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_DEVICE_INFO(
            ScreenID.CAM_SETTING,
            ActionID.CHECK_SDCARD_STATUS,
            EMPTY,
            HttpMethod.POST
        ),
        GET_FIRM_INFO(
            ScreenID.CAM_SETTING,
            ActionID.GET_LIST_SDCARD_VIDEO,
            EMPTY,
            HttpMethod.POST
        ),
        VIEW_EVENT(
            ScreenID.CAM_SETTING,
            ActionID.PLAY_SDCARD_VIDEO,
            EMPTY,
            HttpMethod.POST
        ),
        GET_SUBSCRIBED_CLOUD_PACKAGE(
            ScreenID.CAM_SETTING,
            ActionID.GET_SUBSCRIBED_CLOUD_PACKAGE,
            EMPTY,
            HttpMethod.POST
        ),
        GET_SDCARD_INFO(
            ScreenID.CAM_SETTING,
            ActionID.PAUSE_SDCARD_VIDEO,
            EMPTY,
            HttpMethod.POST
        ),
        SET_SCHEDULE_TYPE_A(
            ScreenID.CAM_SETTING,
            ActionID.RECORD_PLAYBACK,
            EMPTY,
            HttpMethod.POST
        ),
        GET_SCHEDULE_TYPE_B(
            ScreenID.CAM_SETTING,
            ActionID.RECORD_PLAYBACK,
            EMPTY,
            EMPTY
        ),
        FLIP_VIDEO(
            ScreenID.CAM_SETTING,
            ActionID.FLIP_VIDEO,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        ON_ONVIF_MODE(
            ScreenID.CAM_SETTING,
            ActionID.ON_ONVIF_MODE,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        OFF_ONVIF_MODE(
            ScreenID.CAM_SETTING,
            ActionID.OFF_ONVIF_MODE,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        SET_MIC_CAMERA(
            ScreenID.CAM_SETTING,
            ActionID.SET_MIC_CAMERA,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        SET_VOLUME_CAMERA(
            ScreenID.CAM_SETTING,
            ActionID.SET_VOLUME_CAMERA,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        ON_HUMAN_TRACKING(
            ScreenID.CAM_SETTING,
            ActionID.ON_HUMAN_TRACKING,
            EMPTY,
            EMPTY
        ),
        OFF_HUMAN_TRACKING(
            ScreenID.CAM_SETTING,
            ActionID.OFF_HUMAN_TRACKING,
            EMPTY,
            EMPTY
        ),
        SET_TIMEZONE(
            ScreenID.CAM_SETTING,
            ActionID.SET_TIMEZONE,
            EMPTY,
            EMPTY
        ),
        ON_PRIVACY_MODE(
            ScreenID.CAM_SETTING,
            ActionID.ON_PRIVACY_MODE,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        OFF_PRIVACY_MODE(
            ScreenID.CAM_SETTING,
            ActionID.OFF_PRIVACY_MODE,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        DELETE_CAMERA(
            ScreenID.CAM_SETTING,
            ActionID.DELETE_CAMERA,
            EMPTY,
            EMPTY
        ),
        CHANGE_CAMERA_NAME(
            ScreenID.CAM_SETTING,
            ActionID.CHANGE_CAMERA_NAME,
            EMPTY,
            EMPTY
        ),
        SET_DEFAULT_CONFIGURATION_CAMERA(
            ScreenID.CAM_SETTING,
            ActionID.SET_DEFAULT_CONFIGURATION_CAMERA,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        FACTORY_RESET_CAMERA(
            ScreenID.CAM_SETTING,
            ActionID.FACTORY_RESET_CAMERA,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        RESTART_CAMERA(
            ScreenID.CAM_SETTING,
            ActionID.RESTART_CAMERA,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        HUMAN_TRACKING(
            ScreenID.CAM_SETTING,
            ActionID.HUMAN_TRACKING,
            EMPTY,
            EMPTY
        ),
        SET_SCHEDULE_TYPE(
            ScreenID.CAM_SETTING,
            ActionID.SET_SCHEDULE_TYPE,
            EMPTY,
            EMPTY
        ),
        GET_SCHEDULE_TYPE(
            ScreenID.CAM_SETTING,
            ActionID.GET_SCHEDULE_TYPE,
            EMPTY,
            EMPTY
        ),
        SET_MOTION_ALARM(
            ScreenID.CAM_SETTING,
            ActionID.SET_MOTION_ALARM,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        SET_VIDEO_QUALITY(
            ScreenID.CAM_SETTING,
            ActionID.SET_VIDEO_QUALITY,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        SET_SECURE_ZONE(
            ScreenID.CAM_SETTING,
            ActionID.SET_SECURE_ZONE,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        GET_LIST_NOTIFICATION(
            ScreenID.CAM_SETTING,
            ActionID.GET_LIST_NOTIFICATION,
            EMPTY,
            EMPTY
        ),
    }

    enum class LogVideoSetting(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_VIDEO_INFO(
            ScreenID.VIDEO_SETTING,
            ActionID.GET_VIDEO_INFO,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        SET_VIDEO_QUALITY(
            ScreenID.VIDEO_SETTING,
            ActionID.SET_VIDEO_QUALITY,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
        SET_RESOLUTIONS(
            ScreenID.VIDEO_SETTING,
            ActionID.SET_RESOLUTIONS,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
    }

    enum class LogSmartFeature(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_DEVICE_INFO(
            ScreenID.SMART_FEATURE,
            ActionID.GET_DEVICE_INFO,
            EMPTY,
            EMPTY
        ),
        GET_NOTIFICATION_STATUS(
            ScreenID.SMART_FEATURE,
            ActionID.GET_NOTIFICATION_STATUS,
            EMPTY,
            EMPTY
        ),
        GET_SECURE_ZONE(
            ScreenID.SMART_FEATURE,
            ActionID.GET_SECURE_ZONE,
            EMPTY,
            EMPTY
        ),
        ON_NOTIFICATION(
            ScreenID.SMART_FEATURE,
            ActionID.ON_NOTIFICATION,
            EMPTY,
            EMPTY
        ),
        OFF_NOTIFICATION(
            ScreenID.SMART_FEATURE,
            ActionID.OFF_NOTIFICATION,
            EMPTY,
            EMPTY
        ),
        SET_HUMAN_DETECTION(
            ScreenID.SMART_FEATURE,
            ActionID.SET_HUMAN_DETECTION,
            EMPTY,
            EMPTY
        ),
        SET_MOTION_DETECTION(
            ScreenID.SMART_FEATURE,
            ActionID.SET_MOTION_DETECTION,
            EMPTY,
            EMPTY
        ),
        SET_MOTION_ALARM(
            ScreenID.SMART_FEATURE,
            ActionID.SET_MOTION_ALARM,
            EMPTY,
            EMPTY
        ),
    }

    enum class LogSecureZone(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        SET_SECURE_ZONE(
            ScreenID.SECURE_ZONE,
            ActionID.SET_SECURE_ZONE,
            TcpMethod.METHOD_DEVICE_CONTROL,
            HttpMethod.POST
        ),
    }

    enum class LogSystemNotification(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_SYSTEM_NOTIFICATION_LIST(
            ScreenID.SYSTEM_NOTIFICATION,
            ActionID.GET_SYSTEM_NOTIFICATION_LIST,
            EMPTY,
            HttpMethod.POST
        ),
        DELETE_NOTIFICATION(
            ScreenID.SYSTEM_NOTIFICATION,
            ActionID.DELETE_NOTIFICATION,
            EMPTY,
            HttpMethod.POST
        ),
        SET_READ_NOTIFICATION(
            ScreenID.SYSTEM_NOTIFICATION,
            ActionID.SET_READ_NOTIFICATION,
            EMPTY,
            HttpMethod.POST
        ),
        SET_READ_ALL_NOTIFICATIONS(
            ScreenID.SYSTEM_NOTIFICATION,
            ActionID.SET_READ_ALL_NOTIFICATIONS,
            EMPTY,
            HttpMethod.POST
        ),
        GET_SYSTEM_NOTIFICATION_LIST_SMART_HOME(
            ScreenID.SYSTEM_NOTIFICATION,
            ActionID.GET_SYSTEM_NOTIFICATION_LIST_SMART_HOME,
            EMPTY,
            HttpMethod.POST
        ),
    }

    enum class LogPlayBackCloud(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_LIST_EVENT_VIDEO(
            ScreenID.PLAY_BACK_CLOUD,
            ActionID.GET_LIST_EVENT_VIDEO,
            EMPTY,
            HttpMethod.POST
        ),
        GET_LIST_24_7_VIDEO(
            ScreenID.PLAY_BACK_CLOUD,
            ActionID.GET_LIST_24_7_VIDEO,
            EMPTY,
            HttpMethod.POST
        ),
        PLAY_S3_LINK(
            ScreenID.PLAY_BACK_CLOUD,
            ActionID.PLAY_S3_LINK,
            EMPTY,
            HttpMethod.POST
        ),
        DOWNLOAD_CLOUD_VIDEO(
            ScreenID.PLAY_BACK_CLOUD,
            ActionID.DOWNLOAD_CLOUD_VIDEO,
            EMPTY,
            HttpMethod.POST
        ),
        PLAY_BACK_CLOUD(
            ScreenID.PLAY_BACK_CLOUD,
            ActionID.PLAY_BACK_CLOUD,
            EMPTY,
            EMPTY
        ),
        SELECT_CALENDAR(
            ScreenID.PLAY_BACK_CLOUD,
            ActionID.CLOUD_SELECT_CALENDAR,
            EMPTY,
            EMPTY
        ),
        SEEK_TIME(
            ScreenID.PLAY_BACK_CLOUD,
            ActionID.CLOUD_SEEK_TIME,
            EMPTY,
            EMPTY
        ),
    }

    enum class LogRegisterAccount(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        CREATE_ACCOUNT(
            ScreenID.REGISTER_ACCOUNT,
            ActionID.CREATE_ACCOUNT,
            EMPTY,
            HttpMethod.POST
        ),
        OTP_VERIFICATION(
            ScreenID.REGISTER_ACCOUNT,
            ActionID.OTP_VERIFICATION,
            EMPTY,
            HttpMethod.POST
        ),
        VERIFY_ACCOUNT_TO_REGISTER(
            ScreenID.REGISTER_ACCOUNT,
            ActionID.VERIFY_ACCOUNT_TO_REGISTER,
            EMPTY,
            HttpMethod.POST
        ),
    }

    enum class LogResetPassword(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        RESET_PASSWORD(
            ScreenID.RESET_PASSWORD,
            ActionID.RESET_PASSWORD,
            EMPTY,
            HttpMethod.PUT
        ),
    }

    enum class LogShareDevice(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_ALL_SHARING_CAMERA_LIST(
            ScreenID.SHARE_DEVICE,
            ActionID.GET_ALL_SHARING_CAMERA_LIST,
            EMPTY,
            EMPTY
        ),
        CANCEL_SHARING_DEVICE(
            ScreenID.SHARE_DEVICE,
            ActionID.CANCEL_SHARING_DEVICE,
            EMPTY,
            EMPTY
        ),
        UPDATE_SHARING_CAMERA(
            ScreenID.SHARE_DEVICE,
            ActionID.UPDATE_SHARING_CAMERA,
            EMPTY,
            EMPTY
        ),
        DELETE_SHARING_CAMERA(
            ScreenID.SHARE_DEVICE,
            ActionID.DELETE_SHARING_CAMERA,
            EMPTY,
            EMPTY
        ),
        UPDATE_SHARED_CAMERA(
            ScreenID.SHARE_DEVICE,
            ActionID.UPDATE_SHARED_CAMERA,
            EMPTY,
            EMPTY
        ),
        GET_ALL_SHARED_CAMERA_LIST(
            ScreenID.SHARE_DEVICE,
            ActionID.GET_ALL_SHARED_CAMERA_LIST,
            EMPTY,
            EMPTY
        ),
        DELETE_SHARED_CAMERA(
            ScreenID.SHARE_DEVICE,
            ActionID.DELETE_SHARED_CAMERA,
            EMPTY,
            EMPTY
        ),
        GET_SHAREABLE_CAMERA_LIST(
            ScreenID.SHARE_DEVICE,
            ActionID.GET_SHAREABLE_CAMERA_LIST,
            EMPTY,
            EMPTY
        ),
        CHECK_SHAREABLE_DEVICE(
            ScreenID.SHARE_DEVICE,
            ActionID.CHECK_SHAREABLE_DEVICE,
            EMPTY,
            EMPTY
        ),
        ADD_SHARING(
            ScreenID.SHARE_DEVICE,
            ActionID.ADD_SHARING,
            EMPTY,
            EMPTY
        ),
    }

    enum class LogChangePassword(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        CHANGE_PASSWORD(
            ScreenID.CHANGE_PASSWORD,
            ActionID.CHANGE_PASSWORD,
            EMPTY,
            HttpMethod.POST
        )
    }

    enum class LogLogin(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        LOGIN(
            ScreenID.LOGIN,
            ActionID.LOGIN,
            EMPTY,
            HttpMethod.POST
        )
    }

    enum class LogKPIActionDeviceManagement(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_DEVICE_LIST(
            ScreenID.DEVICE_MANAGEMENT,
            ActionID.GET_DEVICE_LIST,
            EMPTY,
            EMPTY
        )
    }

    enum class LogKPIActionAddCard(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_DEVICE_IN_CARD(
            ScreenID.ADD_CARD,
            ActionID.GET_DEVICE_IN_CARD,
            EMPTY,
            EMPTY
        ),
        GET_CAMERA_IN_CARD(
            ScreenID.ADD_CARD,
            ActionID.GET_CAMERA_IN_CARD,
            EMPTY,
            EMPTY
        ),
        GET_SCENE_IN_CARD(
            ScreenID.ADD_CARD,
            ActionID.GET_SCENE_IN_CARD,
            EMPTY,
            EMPTY
        ),
    }

    enum class LogKPIActionTabFavorite(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_FAVORITE_LIST(
            ScreenID.TAB_FAVORITE,
            ActionID.GET_FAVORITE_LIST,
            EMPTY,
            EMPTY
        ),
        SAVE_FAVORITE_LIST(
            ScreenID.TAB_FAVORITE,
            ActionID.SAVE_FAVORITE_LIST,
            EMPTY,
            EMPTY
        )
    }

    enum class LogHomeManagement(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_HOME_LIST(
            ScreenID.HOME_MANAGEMENT,
            ActionID.GET_HOME_LIST,
            EMPTY,
            EMPTY
        ),
    }

    enum class LogSDCARD(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        FORMAT_SDCARD(
            ScreenID.SDCARD,
            ActionID.FORMAT_SDCARD,
            TcpMethod.METHOD_DEVICE_CONTROL,
            EMPTY
        ),
    }

    enum class LogRoomManagement(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        ROOM_MANAGEMENT(
            ScreenID.ROOM_MANAGEMENT,
            ActionID.GET_ROOM_LIST,
            EMPTY,
            EMPTY
        ),
    }

    enum class LogAddCamera(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_DEVICE_ATTRIBUTES(
            ScreenID.ADD_CAMERA,
            ActionID.GET_DEVICE_ATTRIBUTES,
            EMPTY,
            EMPTY
        ),
        CHECK_STATUS_CAM(
            ScreenID.ADD_CAMERA,
            ActionID.CHECK_STATUS_CAM,
            EMPTY,
            EMPTY
        ),
        ADD_DEVICE_TO_ROOM(
            ScreenID.ADD_CAMERA,
            ActionID.ADD_DEVICE_TO_ROOM,
            EMPTY,
            EMPTY
        ),
        UPDATE_CAMERA(
            ScreenID.ADD_CAMERA,
            ActionID.UPDATE_CAMERA,
            EMPTY,
            EMPTY
        ),
        GET_FIRM_INFO(
            ScreenID.ADD_CAMERA,
            ActionID.GET_FIRM_INFO,
            EMPTY,
            EMPTY
        ),
        PING_CONNECTION_UPADTE_FIRMWARE(
            ScreenID.ADD_CAMERA,
            ActionID.PING_CONNECTION_UPADTE_FIRMWARE,
            EMPTY,
            EMPTY
        ),
        REQUEST_UPDATE_FIRMWARE(
            ScreenID.ADD_CAMERA,
            ActionID.REQUEST_UPDATE_FIRMWARE,
            EMPTY,
            EMPTY
        ),
        PRECHECK_CAMERA(
            ScreenID.ADD_CAMERA,
            ActionID.PRECHECK_CAMERA,
            EMPTY,
            EMPTY
        ),
    }

    enum class LogUpdateApp(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_LIST_APP_VERSION(
            ScreenID.UPDATE_APP,
            ActionID.GET_LIST_APP_VERSION,
            EMPTY,
            EMPTY
        ),
    }

    enum class LogConnectedCameraInfo(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_DEVICE_INFO(
            ScreenID.CONNECTED_CAMERA_INFO,
            ActionID.GET_DEVICE_INFO,
            EMPTY,
            EMPTY
        ),
        GET_WIFI_INFO(
            ScreenID.CONNECTED_CAMERA_INFO,
            ActionID.GET_WIFI_INFO,
            EMPTY,
            EMPTY
        ),
    }

    enum class LogScheduleNotification(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GET_SCHEDULE_LIST(
            ScreenID.SCHEDULE_NOTIFICATION,
            ActionID.GET_SCHEDULE_LIST,
            EMPTY,
            EMPTY
        ),
        DELETE_SCHEDULE(
            ScreenID.SCHEDULE_NOTIFICATION,
            ActionID.DELETE_SCHEDULE,
            EMPTY,
            EMPTY
        ),
        ON_SCHEDULE(
            ScreenID.SCHEDULE_NOTIFICATION,
            ActionID.ON_SCHEDULE,
            EMPTY,
            EMPTY
        ),
        OFF_SCHEDULE(
            ScreenID.SCHEDULE_NOTIFICATION,
            ActionID.OFF_SCHEDULE,
            EMPTY,
            EMPTY
        ),
        ADD_SCHEDULE(
            ScreenID.SCHEDULE_NOTIFICATION,
            ActionID.ADD_SCHEDULE,
            EMPTY,
            EMPTY
        ),
        EDIT_SCHEDULE(
            ScreenID.SCHEDULE_NOTIFICATION,
            ActionID.EDIT_SCHEDULE,
            EMPTY,
            EMPTY
        ),
    }

    enum class LogDeviceDetail(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        GATEWAY_DETAIL(
            ScreenID.GATEWAY_DETAIL,
            ActionID.GET_DEVICE_BY_ORG,
            EMPTY,
            HttpMethod.GET
        ),
        MOTION_SENSOR_DETAIL(
            ScreenID.MOTION_SENSOR_DETAIL,
            ActionID.GET_HISTORY_MOTION_SENSOR,
            EMPTY,
            HttpMethod.GET
        ),
        DOOR_SENSOR_DETAIL(
            ScreenID.DOOR_SENSOR_DETAIL,
            ActionID.GET_HISTORY_DOOR_SENSOR,
            EMPTY,
            HttpMethod.GET
        )
    }

    enum class LogSceneManagement(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        SCENE_MANAGEMENT(
            ScreenID.SCENE_MANAGE,
            ActionID.GET_SCENE_LIST,
            EMPTY,
            HttpMethod.GET
        ),
        ADD_SCENE_AUTO(
            ScreenID.SCENE_EDIT,
            ActionID.ADD_SCENE_AUTO,
            EMPTY,
            HttpMethod.POST
        ),
        ADD_SCENE_TOUCH(
            ScreenID.SCENE_EDIT,
            ActionID.ADD_SCENE_TOUCH,
            EMPTY,
            HttpMethod.POST
        ),
        EDIT_SCENE(
            ScreenID.SCENE_EDIT,
            ActionID.EDIT_SCENE,
            EMPTY,
            HttpMethod.POST
        ),
        DELETE_SCENE(
            ScreenID.SCENE_EDIT,
            ActionID.DELETE_SCENE,
            EMPTY,
            HttpMethod.DELETE
        ),
        ON_OFF_SCENE(
            ScreenID.SCENE_EDIT,
            ActionID.ON_OFF_SCENE,
            EMPTY,
            HttpMethod.DELETE
        )
    }

    enum class LogControlDeviceIOT(
        val screenID: String,
        val actionID: String,
        val tcpMethod: String,
        val httpMethod: String
    ) {
        CONTROL_CURTAIN(
            screenID = ScreenID.CURTAIN_DETAIL,
            actionID = ActionID.CONTROL_CURTAIN,
            tcpMethod = EMPTY,
            httpMethod = EMPTY
        ),
        ON_OFF_SMART_LIGHT(
            screenID = ScreenID.LAMP_DETAIL,
            actionID = ActionID.CONTROL_ON_OFF_LAMP,
            tcpMethod = EMPTY,
            httpMethod = EMPTY
        ),
        CHANGE_COLOR_SMART_LIGHT(
            screenID = ScreenID.LAMP_DETAIL,
            actionID = ActionID.CONTROL_COLOR_LAMP,
            tcpMethod = EMPTY,
            httpMethod = EMPTY
        ),
        CHANGE_DIM(
            screenID = ScreenID.LAMP_DETAIL,
            actionID = ActionID.CONTROL_BRIGTH_LAMP,
            tcpMethod = EMPTY,
            httpMethod = EMPTY
        ),
        ON_OFF_SWITCH(
            screenID = ScreenID.SWITCH_DETAIL,
            actionID = ActionID.CONTROL_ON_OFF_SWITCH,
            tcpMethod = EMPTY,
            httpMethod = EMPTY
        ),
        OPEN_CLOSE_SMART_LOCK(
            screenID = ScreenID.LOCK_DETAIL,
            actionID = ActionID.CONTROL_OPEN_CLOSE_LOCK,
            tcpMethod = EMPTY,
            httpMethod = EMPTY
        )
    }

    object ScreenID {
        const val EVENT_SCREEN_ID = "EVENT"
        const val CHANGE_PASSWORD = "CHANGE_PASSWORD"
        const val RESET_PASSWORD = "RESET_PASSWORD"
        const val DEVICE_MANAGEMENT = "DEVICE_MANAGEMENT"
        const val LIVE_VIEW = "LIVE_VIEW"
        const val MULTI_LIVEVIEW = "MULTI_LIVEVIEW"
        const val LOGIN = "LOGIN"
        const val REGISTER_ACCOUNT = "REGISTER_ACCOUNT"
        const val PASSWORD = "PASSWORD"
        const val TAB_FAVORITE = "TAB_FAVORITE"
        const val CLOUD = "CLOUD"
        const val SD_CARD = "SD_CARD"
        const val EVENT_SD_CARD = "EVENT_SD_CARD"
        const val EVENT_CLOUD = "EVENT_CLOUD"
        const val ADD_CARD = "ADD_CARD"
        const val PLAY_BACK = "PLAYBACK"
        const val LOG_OUT = "LOG_OUT"
        const val PLAY_BACK_CLOUD = "PLAYBACK_CLOUD"
        const val PLAY_BACK_SDCARD = "PLAYBACK_SDCARD"
        const val EVENT = "EVENT"
        const val CAM_SETTING = "CAM_SETTING"
        const val VIDEO_SETTING = "VIDEO_SETTING"
        const val SMART_FEATURE = "SMART_FEATURE"
        const val SECURE_ZONE = "SECURE_ZONE"
        const val CONNECTED_CAMERA_INFO = "CONNECTED_CAMERA_INFO"
        const val SDCARD = "SDCARD"
        const val CLOUD_PACKAGE = "CLOUD_PACKAGE"
        const val SYSTEM_NOTIFICATION = "SYSTEM_NOTIFICATION"
        const val SHARE_DEVICE = "SHARE_DEVICE"
        const val UPDATE_FIRMWARE = "UPDATE_FIRMWARE"
        const val SCHEDULE_NOTIFICATION = "SCHEDULE_NOTIFICATION"
        const val ADD_CAMERA = "ADD_CAMERA"
        const val UPDATE_APP = "UPDATE_APP"
        const val HOME_MANAGEMENT = "HOME_MANAGEMENT"
        const val ROOM_MANAGEMENT = "ROOM_MANAGEMENT"
        const val GATEWAY_DETAIL = "GATEWAY_DETAIL"
        const val DOOR_SENSOR_DETAIL = "DOOR_SENSOR_DETAIL"
        const val MOTION_SENSOR_DETAIL = "MOTION_SENSOR_DETAIL"
        const val SCENE_MANAGE = "SCENE_MANAGE"
        const val SCENE_EDIT = "SCENE_EDIT"
        const val CURTAIN_DETAIL = "CURTAIN_DETAIL"
        const val LAMP_DETAIL = "LAMP_DETAIL"
        const val SWITCH_DETAIL = "SWITCH_DETAIL"
        const val LOCK_DETAIL = "LOCK_DETAIL"
    }

    object ActionID {
        // Event
        const val EVENT_GET_LIST = "GET_LIST_EVENT"

        // LogOut
        const val LOG_OUT = "LOG_OUT"

        // LiveView
        const val GET_SUBSCRIBED_CLOUD_PACKAGE = "GET_SUBSCRIBED_CLOUD_PACKAGE"
        const val GET_DEVICE_INFO = "GET_DEVICE_INFO"
        const val PLAY_LIVEWVIEW = "PLAY_LIVEWVIEW"
        const val STOP_STREAM = "STOP_STREAM"
        const val TALK = "TALK"
        const val CLOSE_CONNECTION = "CLOSE_CONNECTION"
        const val PTZ_CONTROL = "PTZ_CONTROL"
        const val OFF_PRIVACY_MODE = "OFF_PRIVACY_MODE"
        const val GET_VIDEO_INFO = "GET_VIDEO_INFO"
        const val SET_VIDEO_QUALITY = "SET_VIDEO_QUALITY"
        const val GET_WIFI_INFO = "GET_WIFI_INFO"
        const val ON_ACTIVE_DEFENSE = "ON_ACTIVE_DEFENSE"
        const val OFF_ACTIVE_DEFENSE = "OFF_ACTIVE_DEFENSE"
        const val GET_SDCARD_INFO = "GET_SDCARD_INFO"
        const val PLAYBACK_STATE = "PLAYBACK_STATE"
        const val KEEP_LIVESTREAM = "KEEP_LIVESTREAM"
        const val HOLD_SOCKET_SESSION = "HOLD_SOCKET_SESSION"
        const val GET_SECRET_KEY = "GET_SECRET_KEY"
        const val GET_NONCE = "GET_NONCE"
        const val GET_AUT_DATA_KEY = "GET_AUT_DATA_KEY"
        const val VERIFY_ENCRYPTION = "VERIFY_ENCRYPTION"
        const val VOICE_TALK_SETUP_REQUEST = "VOICE_TALK_SETUP_REQUEST"
        const val LOG_VOICE_INFO_WHEN_STOP = "LOG_VOICE_INFO_WHEN_STOP"
        const val PLAY_LIVEVIEW = "PLAY_LIVEVIEW"
        const val CONTROL_PTZ = "CONTROL_PTZ"
        const val VOICE_CALL = "VOICE_CALL"
        const val MULTI_LIVEVIEW = "MULTI_LIVEVIEW"

        // PLAY BACK
        const val GET_LIST_EVENT_VIDEO = "GET_LIST_EVENT_VIDEO"
        const val GET_LIST_24_7_VIDEO = "GET_LIST_24/7_VIDEO"
        const val PLAY_S3_LINK = "PLAY_S3_LINK"
        const val DOWNLOAD_CLOUD_VIDEO = "DOWNLOAD_CLOUD_VIDEO"
        const val SDCARD_SELECT_CALENDAR = "SDCARD_SELECT_CALENDAR"
        const val CLOUD_SELECT_CALENDAR = "CLOUD_SELECT_CALENDAR"
        const val CLOUD_SEEK_TIME = "CLOUD_SEEK_TIME"

        // PLAYBACK_SDCARD
        const val PLAYBACK_SDCARD = "PLAYBACK_SDCARD"
        const val PLAYBACK_SDCARD_VIDEO = "PLAY_SDCARD_VIDEO"
        const val GET_LIST_SDCARD_VIDEO = "GET_LIST_SDCARD_VIDEO"
        const val PLAY_SDCARD_VIDEO = "PLAY_SDCARD_VIDEO"
        const val CONTROL_PLAYBACK_STATE = "CONTROL_PLAYBACK_STATE"
        const val PAUSE_SDCARD_VIDEO = "PAUSE_SDCARD_VIDEO"
        const val RECORD_PLAYBACK = "RECORD_PLAYBACK"
        const val CHECK_SDCARD_STATUS = "CHECK_SDCARD_STATUS"
        const val PLAY_BACK_CLOUD = "PLAYBACK_CLOUD"
        const val SDCARD_SEEK_TIME = "SDCARD_SEEK_TIME"

        // EVENT
        const val GET_LIST_CAMERA = "GET_LIST_CAMERA"
        const val GET_LIST_EVENT = "GET_LIST_EVENT"
        const val VIEW_EVENT = "VIEW_EVENT"
        const val DELETE_EVENT = "DELETE_EVENT"
        const val READ_ALL_EVENT = "READ_ALL_EVENT"
        const val READ_EVENT = "READ_EVENT"

        // REGISTER_ACCOUNT
        const val CREATE_ACCOUNT = "CREATE_ACCOUNT"
        const val OTP_VERIFICATION = "OTP_VERIFICATION"
        const val VERIFY_ACCOUNT_TO_REGISTER = "VERIFY_ACCOUNT_TO_REGISTER"

        // RESET_PASSWORD
        const val RESET_PASSWORD = "RESET_PASSWORD"
        const val CHANGE_PASSWORD = "CHANGE_PASSWORD"

        // LOGIN
        const val LOGIN = "LOGIN"

        // DEVICE_MANAGEMENT
        const val GET_DEVICE_LIST = "GET_DEVICE_LIST"

        // ADD_CARD
        const val GET_DEVICE_IN_CARD = "GET_DEVICE_IN_CARD"
        const val GET_CAMERA_IN_CARD = "GET_CAMERA_IN_CARD"
        const val GET_SCENE_IN_CARD = "GET_SCENE_IN_CARD"

        // TAB_FAVORITE
        const val GET_FAVORITE_LIST = "GET_FAVORITE_LIST"
        const val SAVE_FAVORITE_LIST = "SAVE_FAVORITE_LIST"

        // SCHEDULE_NOTIFICATION
        const val GET_SCHEDULE_LIST = "GET_SCHEDULE_LIST"
        const val DELETE_SCHEDULE = "DELETE_SCHEDULE"
        const val ON_SCHEDULE = "ON_SCHEDULE"
        const val OFF_SCHEDULE = "OFF_SCHEDULE"
        const val ADD_SCHEDULE = "ADD_SCHEDULE"
        const val EDIT_SCHEDULE = "EDIT_SCHEDULE"


        // CAM_SETTING
        const val SET_SCHEDULE_TYPE = "SET_SCHEDULE_TYPE"
        const val GET_SCHEDULE_TYPE = "GET_SCHEDULE_TYPE"
        const val FLIP_VIDEO = "FLIP_VIDEO"
        const val ON_ONVIF_MODE = "ON_ONVIF_MODE"
        const val OFF_ONVIF_MODE = "OFF_ONVIF_MODE"
        const val SET_MIC_CAMERA = "SET_MIC_CAMERA"
        const val SET_VOLUME_CAMERA = "SET_VOLUME_CAMERA"
        const val ON_HUMAN_TRACKING = "ON_HUMAN_TRACKING"
        const val OFF_HUMAN_TRACKING = "OFF_HUMAN_TRACKING"
        const val SET_TIMEZONE = "SET_TIMEZONE"
        const val ON_PRIVACY_MODE = "ON_PRIVACY_MODE"
        const val DELETE_CAMERA = "DELETE_CAMERA"
        const val CHANGE_CAMERA_NAME = "CHANGE_CAMERA_NAME"
        const val SET_DEFAULT_CONFIGURATION_CAMERA = "SET_DEFAULT_CONFIGURATION_CAMERA"
        const val FACTORY_RESET_CAMERA = "FACTORY_RESET_CAMERA"
        const val RESTART_CAMERA = "RESTART_CAMERA"
        const val HUMAN_TRACKING = "MOTION_TRACKING"
        const val GET_LIST_NOTIFICATION = "GET_LIST_NOTIFICATION"

        // VIDEO_SETTING
        const val SET_RESOLUTIONS = "SET_RESOLUTIONS"

        // SMART_FEATURE
        const val GET_NOTIFICATION_STATUS = "GET_NOTIFICATION_STATUS"
        const val GET_SECURE_ZONE = "GET_SECURE_ZONE"
        const val ON_NOTIFICATION = "ON_NOTIFICATION"
        const val OFF_NOTIFICATION = "OFF_NOTIFICATION"
        const val SET_HUMAN_DETECTION = "SET_HUMAN_DETECTION"
        const val SET_MOTION_DETECTION = "SET_MOTION_DETECTION"
        const val SET_MOTION_ALARM = "SET_MOTION_ALARM"

        // SECURE_ZONE
        const val SET_SECURE_ZONE = "SET_SECURE_ZONE"

        // SDCARD
        const val FORMAT_SDCARD = "FORMAT_SDCARD"

        // CLOUD_PACKAGE
        const val GET_ALL_CLOUD_PACKAGE = "GET_ALL_CLOUD_PACKAGE"
        const val REGISTER_PACKAGE = "REGISTER_PACKAGE"

        // SYSTEM_NOTIFICATION
        const val GET_SYSTEM_NOTIFICATION_LIST = "GET_SYSTEM_NOTIFICATION_LIST"
        const val GET_SYSTEM_NOTIFICATION_LIST_SMART_HOME = "GET_SYSTEM_NOTIFICATION_LIST_SMART_HOME"
        const val DELETE_NOTIFICATION = "DELETE_NOTIFICATION"
        const val SET_READ_NOTIFICATION = "SET_READ_NOTIFICATION"
        const val SET_READ_ALL_NOTIFICATIONS = "SET_READ_ALL_NOTIFICATIONS"

        // SHARE_DEVICE
        const val GET_ALL_SHARING_CAMERA_LIST = "GET_ALL_SHARING_CAMERA_LIST"
        const val CANCEL_SHARING_DEVICE = "CANCEL_SHARING_DEVICE"
        const val UPDATE_SHARING_CAMERA = "UPDATE_SHARING_CAMERA"
        const val DELETE_SHARING_CAMERA = "DELETE_SHARING_CAMERA"
        const val UPDATE_SHARED_CAMERA = "UPDATE_SHARED_CAMERA"
        const val GET_ALL_SHARED_CAMERA_LIST = "GET_ALL_SHARED_CAMERA_LIST"
        const val DELETE_SHARED_CAMERA = "DELETE_SHARED_CAMERA"
        const val GET_SHAREABLE_CAMERA_LIST = "GET_SHAREABLE_CAMERA_LIST"
        const val CHECK_SHAREABLE_DEVICE = "CHECK_SHAREABLE_DEVICE"
        const val ADD_SHARING = "ADD_SHARING"

        // ADD_CAMERA - UPDATE_FIRMWARE
        const val GET_DEVICE_ATTRIBUTES = "GET_DEVICE_ATTRIBUTES"
        const val CHECK_STATUS_CAM = "CHECK_STATUS_CAM"
        const val ADD_CAMERA = "ADD_CAMERA"
        const val ADD_DEVICE_TO_ROOM = "ADD_DEVICE_TO_ROOM"
        const val UPDATE_CAMERA = "UPDATE_CAMERA"
        const val GET_FIRM_INFO = "GET_FIRM_INFO"
        const val PING_CONNECTION_UPADTE_FIRMWARE = "PING_CONNECTION_UPADTE_FIRMWARE"
        const val REQUEST_UPDATE_FIRMWARE = "REQUEST_UPDATE_FIRMWARE"
        const val PRECHECK_CAMERA = "PRECHECK_CAMERA"

        // UPDATE_APP
        const val GET_LIST_APP_VERSION = "GET_LIST_APP_VERSION"

        // HOME_MANAGEMENT
        const val GET_HOME_LIST = "GET_HOME_LIST"

        // ROOM_MANAGEMENT
        const val GET_ROOM_LIST = "GET_ROOM_LIST"

        //GATEWAY_DETAIL
        const val GET_DEVICE_BY_ORG = "GET_DEVICE_BY_ORG"

        //DOOR_SENSOR_DETAIL
        const val GET_HISTORY_DOOR_SENSOR = "GET_HISTORY_DOOR_SENSOR"

        //MOTION_SENSOR_DETAIL
        const val GET_HISTORY_MOTION_SENSOR = "GET_HISTORY_MOTION_SENSOR"

        //SCENE_MANAGER
        const val GET_SCENE_LIST = "GET_SCENE_LIST"
        const val ADD_SCENE_AUTO = "ADD_SCENE_AUTO"
        const val ADD_SCENE_TOUCH = "ADD_SCENE_TOUCH"
        const val EDIT_SCENE = "EDIT_SCENE"
        const val DELETE_SCENE = "DELETE_SCENE"
        const val ON_OFF_SCENE = "ON_OFF_SCENE"

        //CONTROL_IOT_DEVICE
        const val CONTROL_CURTAIN = "CONTROL_CURTAIN"
        const val CONTROL_ON_OFF_LAMP = "CONTROL_ON_OFF_LAMP"
        const val CONTROL_COLOR_LAMP = "CONTROL_COLOR_LAMP"
        const val CONTROL_BRIGTH_LAMP = "CONTROL_BRIGTH_LAMP"
        const val CONTROL_ON_OFF_SWITCH = "CONTROL_ON_OFF_SWITCH"
        const val CONTROL_OPEN_CLOSE_LOCK = "CONTROL_OPEN_CLOSE_LOCK"
    }


    object TcpMethod {
        const val METHOD_DEVICE_CONTROL = "METHOD_DEVICE_CONTROL"
        const val METHOD_REGISTER = "METHOD_REGISTER"
        const val METHOD_ENCRYPTION_STATUS = "METHOD_DEVICE_CONTROL"
        const val METHOD_APP_DESCRIBE = "METHOD_APP_DESCRIBE"
        const val METHOD_APP_PLAY = "METHOD_APP_PLAY"
        const val METHOD_STOP_STREAM = "METHOD_STOP_STREAM"
        const val METHOD_APP_RTP_SETUP = "METHOD_APP_RTP_SETUP"
        const val METHOD_APP_OPTION = "METHOD_APP_OPTION"
        const val TCP_CLOSED = "TCP_CLOSED"
        const val PLAY_BACK_STATE = "CONTROL_PLAYBACK_STATE"
    }

    object HttpMethod {
        const val POST = "POST"
        const val GET = "GET"
        const val PUT = "PUT"
        const val DELETE = "DELETE"
    }

}
