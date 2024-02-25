package com.viettel.vht.sdk.ui.jfcameradetail.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lib.sdk.bean.tour.TourBean
import com.viettel.vht.sdk.databinding.ItemTabPresetBinding

class PresetViewPagerAdapter() :
    RecyclerView.Adapter<PresetViewPagerAdapter.ViewHolder>() {
    var addPresetCallback: ((Int) -> Unit)? = null
    var changeModeCallback: (() -> Unit)? = null
    var onLongClickItem: ((ItemPreset) -> Unit)? = null
    var onClickItem: ((ItemPreset) -> Unit)? = null
    var onItemCheckedChange: ((ItemPreset?) -> Unit)? = null
    var onSelectItemPatrol: ((ItemPreset) -> Unit)? = null
    var onPatrolFull: (() -> Unit)? = null
    var isCheckMode: Boolean = false
    var isStatePatrol = false
    val listPatrol = mutableListOf<ItemPreset>()
    var list: MutableList<List<ItemPreset>> = mutableListOf()
    var presetsTour = mutableListOf<TourBean>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTabPresetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val presetAdapter = PresetAdapter()
            presetAdapter.isModeCheck = isCheckMode
            presetAdapter.isStatePatrol = isStatePatrol
            presetAdapter.addPresetCallback = addPresetCallback
            presetAdapter.changeModeCallback = changeModeCallback
            presetAdapter.onLongClickItem = onLongClickItem
            presetAdapter.onClickItem = onClickItem
            presetAdapter.onPatrolFull = onPatrolFull
            presetAdapter.onItemCheckedChange = onItemCheckedChange
            presetAdapter.onSelectItemPatrol = onSelectItemPatrol
            presetAdapter.presetsTour = presetsTour.toMutableList()
            rcv.adapter = presetAdapter
            presetAdapter.submitList(list.get(position))
        }
    }

    private fun updateListPatrol(itemPreset: ItemPreset) {
        if (listPatrol.any { it.item.Id == itemPreset.item.Id }) {
            listPatrol.removeIf { it.item.Id == itemPreset.item.Id }
        } else {
            listPatrol.add(itemPreset)
        }
        notifyDataSetChanged()
    }

    fun setDatas(list: List<List<ItemPreset>>) {
        this.list.clear()
        this.list.addAll(list.toMutableList())
        listPatrol.clear()
        notifyDataSetChanged()
    }

    fun setState(isPatrol: Boolean) {
        isStatePatrol = isPatrol
        notifyDataSetChanged()
    }

    fun getDatas(): MutableList<List<ItemPreset>> {
        return list
    }

    class ViewHolder(val binding: ItemTabPresetBinding) : RecyclerView.ViewHolder(binding.root)
}