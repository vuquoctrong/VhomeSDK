package com.viettel.vht.sdk.ui.jfcameradetail.view

import com.bumptech.glide.Glide
import com.lib.sdk.bean.preset.ConfigGetPreset
import com.lib.sdk.bean.tour.TourBean
import com.vht.sdkcore.adapter.BaseAdapter
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemPresetBinding
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import java.io.Serializable

class PresetAdapter :
    BaseAdapter<ItemPreset, ItemPresetBinding>() {
    var addPresetCallback: ((Int) -> Unit)? = null
    var changeModeCallback: (() -> Unit)? = null
    var onLongClickItem: ((ItemPreset) -> Unit)? = null
    var onClickItem: ((ItemPreset) -> Unit)? = null
    var onItemCheckedChange: ((ItemPreset?) -> Unit)? = null
    var onSelectItemPatrol: ((ItemPreset) -> Unit)? = null
    var onPatrolFull: (() -> Unit)? = null
    var isModeCheck: Boolean = false
    var isStatePatrol = false
    var presetsTour = mutableListOf<TourBean>()
    override val itemLayoutRes: Int
        get() = R.layout.item_preset

    override fun bindSingleTime(holder: BaseViewHolder, position: Int) {

    }

    override fun bindMultiTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {
            when (item.item.Id) {
                -1 -> {
                    tvName.gone()
                    imv.setBackgroundResource(R.drawable.bgr_add_preset)
                    imv.setOnClickListener {
                        addPresetCallback?.invoke(position)
                    }
                }

                -2 -> {
                    imv.setBackgroundResource(R.drawable.bgr_blank_preset)
                    tvName.gone()
                }

                else -> {
                    if (isStatePatrol) {
                        bindStatepatrol(this, item, position)
                    } else {
                        bindStateNormal(this, item, position)
                    }
                    tvName.text = item.item.PresetName
                    Glide.with(root.context).load(item.thumbnail)
                        .error(R.drawable.img_default_preset)
                        .placeholder(R.drawable.img_default_preset)
                        .into(imv)
                }
            }
        }
    }

    private fun bindStatepatrol(
        itemPresetBinding: ItemPresetBinding,
        item: ItemPreset,
        position: Int
    ) {
        itemPresetBinding.apply {
            imvCount.visible()
            imvChecked.gone()
            if (presetsTour.size < 3 || presetsTour.any { it.Id == item.item.Id }) {
                imv.alpha = 1f
                val indexPatrol = presetsTour.indexOfFirst { it.Id == item.item.Id }
                val countDrawable = when (indexPatrol >= 0) {
                    true -> {
                        imvCount.text = (indexPatrol + 1).toString()
                        R.drawable.ic_count1
                    }

                    false -> {
                        imvCount.text = ""
                        R.drawable.ic_count0
                    }
                }
                imvCount.setBackgroundResource(countDrawable)
            } else {
                imv.alpha = 0.5f
            }
            imv.setOnClickListener {
                if (presetsTour.size < 3 || presetsTour.any { it.Id == item.item.Id }) {
                    onSelectItemPatrol?.invoke(item)
                } else {
                    onPatrolFull?.invoke()
                }
            }
            imvCount.setOnClickListener {
                if (presetsTour.size < 3 || presetsTour.any { it.Id == item.item.Id }) {
                    onSelectItemPatrol?.invoke(item)
                } else {
                    onPatrolFull?.invoke()
                }
            }
        }
    }

    private fun bindStateNormal(
        itemPresetBinding: ItemPresetBinding,
        item: ItemPreset,
        position: Int
    ) {
        itemPresetBinding.apply {
            imvCount.gone()
            if (isModeCheck) {
                imvChecked.visible()
            } else {
                imvChecked.gone()
            }
            imvChecked.isChecked = item.isChecked
            imv.setOnLongClickListener {
                if (!isModeCheck) {
                    onLongClickItem?.invoke(item)
                }
                return@setOnLongClickListener false
            }
            imv.setOnClickListener {
                if (isModeCheck) {
                    imvChecked.performClick()
                    onItemCheckedChange?.invoke(item)
                } else {
                    onClickItem?.invoke(item)
                }
            }
            imvChecked.setOnClickListener {
                val isChecked = item.isChecked
                item.isChecked = !isChecked
                notifyItemChanged(position)
                onItemCheckedChange?.invoke(item)
            }
        }
    }
}


data class ItemPreset(
    val item: ConfigGetPreset,
    var thumbnail: String,
    var isChecked: Boolean = false
) : Serializable