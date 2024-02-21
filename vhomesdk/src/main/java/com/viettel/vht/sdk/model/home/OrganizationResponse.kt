package com.viettel.vht.sdk.model.home

import com.google.gson.annotations.SerializedName
import com.viettel.vht.sdk.utils.AttributeDataManager
import java.io.Serializable

data class OrganizationResponse(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    var name: String = "",
    @SerializedName("org_id")
    var orgId: String = "",
    @SerializedName("level")
    val level: String = "",
    @SerializedName("role")
    private val _role: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("sub_orgs")
    val subOrg: List<OrganizationResponse>? = null,
    @SerializedName("attributes")
    var attributes: List<AttributeResponse>? = null,
    @SerializedName("index")
    var index: Int = 0,
    var indexHome: Int = 0,
    var isSelected: Boolean = false
) : Serializable {
    val role: OrganizationRole
        get() = when (_role) {
            "viewer" -> OrganizationRole.VIEWER

            "member" -> OrganizationRole.MEMBER

            else -> OrganizationRole.OWNER
        }

    fun getTuyaHomeId(): Long {
        return try {
            attributes.getAttributes(AttributeDataManager.AttributeKey.TUYA_HOME_ID).toLong()
        } catch (e: Exception) {
            -1L
        }
    }

    fun getLatLng(): Pair<Double, Double> {
        return try {
            val lat = attributes.getAttributes(AttributeDataManager.AttributeKey.LAT).ifBlank { "-1.0" }.toDouble()
            val lng = attributes.getAttributes(AttributeDataManager.AttributeKey.LONG).ifBlank { "-1.0" }.toDouble()
            lat to lng
        } catch (e: Exception) {
            -1.0 to -1.0
        }
    }

    private fun List<AttributeResponse>?.getAttributes(key: String): String {
        try {
            this?.forEach {
                if (it.attributeKey == key) return it.valueAsString.ifBlank { it.value.toString() }
            }
            return ""
        } catch (e: Exception) {
            return ""
        }
    }

    fun getCurrentHomeAddress() =
        attributes.getAttributes(AttributeDataManager.AttributeKey.ADDRESS)
}