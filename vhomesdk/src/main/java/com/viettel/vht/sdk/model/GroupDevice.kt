package com.viettel.vht.sdk.model


data class GroupDevice(
    val name: String,
    val icon: Int,
    val devices: List<DeviceDataResponse>

) {

    companion object {
        const val CAMERA = "Camera"
        const val SWITCH = "Công tắc"
        const val SOCKET = "Ổ cắm"
        const val LAMP = "Đèn"
        const val SENSOR = "Cảm biến"
        const val GATEWAY = "Gateway"
        const val IR_DEVICE = "Thiết bị hồng ngoại"
        const val HOUSEHOLD_APPLIANCES = "Thiết bị gia dụng"
        const val OTHER = "Khác"

        fun allGroups(): List<String> {
            return listOf(
                CAMERA, SWITCH, SOCKET, LAMP, GATEWAY, SENSOR, IR_DEVICE, HOUSEHOLD_APPLIANCES, OTHER
            )
        }
    }
}
