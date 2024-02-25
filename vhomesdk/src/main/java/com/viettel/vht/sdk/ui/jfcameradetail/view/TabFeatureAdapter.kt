package com.viettel.vht.sdk.ui.jfcameradetail.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.viettel.vht.sdk.databinding.ItemTabFeatureJfcameraBinding
import com.viettel.vht.sdk.utils.custom.IconFeatureJFCamera

class TabFeatureAdapter constructor(
    context: Context,
    private val onClickListener: (TabFeature) -> Unit,
    private val onActiveChanged: ((TAB_ID, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<TabFeatureHolder>() {
    private var mData = mutableListOf<TabFeature>()
    private var isDeviceShare = false

    private val layoutInflater by lazy {
        LayoutInflater.from(context)
    }

    fun setDataList(data: List<TabFeature>) {
        mData.clear()
        mData.addAll(data)
        notifyDataSetChanged()
    }

    fun setDeviceShare(isDeviceShare: Boolean = false){
        this.isDeviceShare = isDeviceShare
        notifyDataSetChanged()
    }

    fun updateStateItem(tabId: TAB_ID, state: Boolean) {
        mData.find {
            tabId == it.id
        }?.isActived = state
        notifyItemChanged(mData.indexOfFirst {
            tabId == it.id
        })
    }

    fun getStateItem(tabId: TAB_ID) = mData.find { tabId == it.id }?.isActived ?: false

    fun updateEnableItem(tabId: TAB_ID, isEnabled: Boolean) {
        mData.find {
            tabId == it.id
        }?.isEnabled = isEnabled
        notifyItemChanged(mData.indexOfFirst {
            tabId == it.id
        })
    }

    fun getEnableItem(tabId: TAB_ID) = mData.find { tabId == it.id }?.isEnabled ?: false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabFeatureHolder {
        return TabFeatureHolder(
            ItemTabFeatureJfcameraBinding.inflate(layoutInflater, parent, false),
            onClickListener,
            onActiveChanged,
            isDeviceShare
        )
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(holder: TabFeatureHolder, position: Int) {
        holder.bindData(mData[position])
    }
}

class TabFeatureHolder(
    val binding: ItemTabFeatureJfcameraBinding,
    private val onClickListener: (TabFeature) -> Unit,
    private val onActiveChanged: ((TAB_ID, Boolean) -> Unit)? = null,
    val isDeviceShare: Boolean = false
) : RecyclerView.ViewHolder(binding.root) {

    fun bindData(data: TabFeature) {
        binding.item.setIsDeviceShare(isDeviceShare = isDeviceShare)
        binding.item.setText(data.text)
        binding.item.setIcon(data.icon)
        binding.item.enable(data.isEnabled)
        binding.item.active(data.isActived)
        binding.item.state = data.stateActive
        binding.item.onActiveChanged = {
            onActiveChanged?.invoke(data.id, it)
        }

        binding.item.setOnAction(object : IconFeatureJFCamera.OnAction {
            override fun onClick() {
                onClickListener(data)
            }

        })
    }

}

data class TabFeature(
    val id: TAB_ID,
    val icon: Int,
    val text: String,
    val stateActive: Boolean,
    var isActived: Boolean = false,
    var isEnabled: Boolean = true,
)

enum class TAB_ID {
    FEATURE_HI,
    FEATURE_MOTION_TRACKING,
    FEATURE_PRIVATE,
    FEATURE_DEFENCE,
    FEATURE_MULTIPLE,
    FEATURE_SHARE,
    FEATURE_GALLERY,
}