package com.viettel.vht.sdk.ui.jfcameradetail.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import com.lib.sdk.bean.preset.ConfigGetPreset
import com.lib.sdk.bean.tour.TourBean
import com.vht.sdkcore.pref.RxPreferences
import com.viettel.vht.sdk.databinding.LayoutPresetViewPagerBinding
import com.viettel.vht.sdk.utils.invisible
import com.viettel.vht.sdk.utils.visible

class PresetViewPager(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private val ITEM_PER_PAGE = 4
    private val MAX_SIZE = 8

    var binding: LayoutPresetViewPagerBinding
    var viewPagerAdapter: PresetViewPagerAdapter

    var datas = mutableListOf<ItemPreset>()
    var currentPage = 0
    var isStatePatrol = false

    var addPresetCallback: ((Int) -> Unit)? = null
    var changeModeCallback: ((Boolean) -> Unit)? = null
    var onItemCheckedChange: ((ItemPreset?) -> Unit)? = null
    var onSelectItemPatrol: ((ItemPreset) -> Unit)? = null
    var onPatrolFull: (() -> Unit)? = null
    var onLongClickItem: ((ItemPreset?) -> Unit)? = null
    var onClickItem: ((ItemPreset) -> Unit)? = null
    var presetsTour = mutableListOf<TourBean>()

    init {
        binding = LayoutPresetViewPagerBinding.inflate(LayoutInflater.from(context), this, true)
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
            }
        })
        viewPagerAdapter = PresetViewPagerAdapter()
        binding.viewPager.adapter = viewPagerAdapter
        val tabLayoutMediator = TabLayoutMediator(
            binding.tabDots, binding.viewPager, true
        ) { _, _ -> }
        tabLayoutMediator.attach()
    }

    fun setState(isPatrol: Boolean) {
        isStatePatrol = isPatrol
        viewPagerAdapter.setState(isStatePatrol)
        viewPagerAdapter.setDatas(mapDatas())
    }

    fun setUpList(devId: String, presets: List<ConfigGetPreset>, rxPreferences: RxPreferences) {
        datas = presets.map {
            ItemPreset(it, rxPreferences.getImagePreset("$devId-${it.Id}"), false)
        }.toMutableList()
        viewPagerAdapter.addPresetCallback = addPresetCallback
        viewPagerAdapter.onItemCheckedChange = onItemCheckedChange
        viewPagerAdapter.onSelectItemPatrol = onSelectItemPatrol
        viewPagerAdapter.onPatrolFull = onPatrolFull
        viewPagerAdapter.onLongClickItem = onLongClickItem
        viewPagerAdapter.onClickItem = onClickItem
        viewPagerAdapter.changeModeCallback = {
            setCheckMode(true)
        }
        viewPagerAdapter.setDatas(mapDatas())
    }

    private fun mapDatas(): MutableList<List<ItemPreset>> {
        viewPagerAdapter.presetsTour = presetsTour.toMutableList()
        val list = datas.toMutableList()
        // Thêm item Add
        if (list.size != MAX_SIZE && !isStatePatrol) {
            list.add(ItemPreset(ConfigGetPreset().apply { Id = -1 }, "", false))
        }
        // Thêm item Blank
        val surplus = list.size % ITEM_PER_PAGE
        Log.d("TAG", "mapDatas: surplus = $surplus")
        if (surplus != 0 || list.size == 0) {
            for (i in 0 until (ITEM_PER_PAGE - surplus)) {
                list.add(ItemPreset(ConfigGetPreset().apply { Id = -2 }, "", false))
            }
        }
        Log.d("TAG", "mapDatas: list = ${list.size}")
        val newList = list.chunked(ITEM_PER_PAGE)
        if (newList.size <= 1) {
            binding.tabDots.invisible()
        } else {
            binding.tabDots.visible()
        }
        return newList.toMutableList()
    }

    fun updateDataTour(presetsTour: MutableList<TourBean>) {
        this.presetsTour = presetsTour.toMutableList()
        viewPagerAdapter.setDatas(mapDatas())
    }

    private fun scrollToCurrentPage() {
        if (currentPage >= viewPagerAdapter.itemCount) {
            currentPage = viewPagerAdapter.itemCount
        }
        binding.viewPager.setCurrentItem(currentPage, false)
    }

    fun setCheckMode(isCheckMode: Boolean) {
        viewPagerAdapter.isCheckMode = isCheckMode
        viewPagerAdapter.notifyDataSetChanged()
        changeModeCallback?.invoke(isCheckMode)
    }

    fun isCheckMode() = viewPagerAdapter.isCheckMode

    fun getListItemChecked(): MutableList<ItemPreset> {
        val listItemChecked = mutableListOf<ItemPreset>()
        val list = viewPagerAdapter.getDatas()
        list.forEach {
            listItemChecked.addAll(it.filter { it.isChecked })
        }
        return listItemChecked
    }

    fun getListItem(): MutableList<ItemPreset> {
        val listItem = mutableListOf<ItemPreset>()
        val list = viewPagerAdapter.getDatas()
        list.forEach {
            listItem.addAll(it)
        }
        return listItem
    }

    fun selectAll() {
        viewPagerAdapter.getDatas().forEach {
            it.forEach {
                if (it.item.Id != -1 && it.item.Id != -2) {
                    it.isChecked = true
                }
            }
        }
        viewPagerAdapter.notifyDataSetChanged()
        onItemCheckedChange?.invoke(null)
    }

    fun unSelectAll() {
        viewPagerAdapter.getDatas().forEach {
            it.forEach { it.isChecked = false }
        }
        viewPagerAdapter.notifyDataSetChanged()
        onItemCheckedChange?.invoke(null)
    }

    fun updateAdded(item: ItemPreset) {
        datas.add(item)
        viewPagerAdapter.setDatas(mapDatas())
        onItemCheckedChange?.invoke(null)
        scrollToCurrentPage()
    }

    fun updateItem(item: ItemPreset) {
        datas.find { it.item.Id == item.item.Id }.apply {
            this?.item?.PresetName = item.item.PresetName
            this?.thumbnail = item.thumbnail
        }
        viewPagerAdapter.setDatas(mapDatas())
        scrollToCurrentPage()
    }

    fun updateDeleted(list: MutableList<Int>) {
        datas.removeIf { item ->
            list.any { id -> id == item.item.Id }
        }
        viewPagerAdapter.setDatas(mapDatas())
        onItemCheckedChange?.invoke(null)
        setCheckMode(false)
        scrollToCurrentPage()
    }

}
