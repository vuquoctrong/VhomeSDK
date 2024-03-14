package com.viettel.vht.sdk.ui.listvideocloud

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lib.sdk.struct.H264_DVR_FILE_DATA
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemTitleVideoEventPlaybackBinding
import com.viettel.vht.sdk.databinding.ItemVideoEventPlaybackBinding
import com.viettel.vht.sdk.utils.Config
import com.viettel.vht.sdk.utils.DateHelper
import com.viettel.vht.sdk.utils.DownloadThumbnailManager
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import java.io.File
import java.util.*

class ItemVideoCloudAdapter(
    val recyclerView: RecyclerView,
    val context: Context,
    val devId: String
) :
    ListAdapter<ItemVideo, RecyclerView.ViewHolder>(DIFF_UTIL),
    DownloadThumbnailManager.OnCloudImageListener {

    var actionCheckedMode: (() -> Unit)? = null
    var onItemClick: ((ItemVideo) -> Unit)? = null
    var onItemCheckedChange: ((ItemVideo?) -> Unit)? = null

    private var mapCount: HashMap<Int, Int> = HashMap()

    private var mDownloadThumbnailManager: DownloadThumbnailManager? = null

    init {
        mDownloadThumbnailManager = DownloadThumbnailManager(context, devId, this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_VIDEO -> {
                val binding: ItemVideoEventPlaybackBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_video_event_playback,
                    parent, false
                )
                return VideoViewHolder(binding)
            }

            TYPE_TITLE -> {
                val binding: ItemTitleVideoEventPlaybackBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_title_video_event_playback,
                    parent, false
                )
                return TitleViewHolder(binding)
            }

            else -> {
                val binding: ItemVideoEventPlaybackBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_video_event_playback,
                    parent, false
                )
                return VideoViewHolder(binding)
            }
        }
    }

    fun updateThumbnailURL(position: Int, pathThumbnail: String) {
        currentList.get(position).thumbnail = pathThumbnail
        notifyItemChanged(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? TitleViewHolder)?.let {
            holder.bind(getItem(position))
        }
        (holder as? VideoViewHolder)?.let {
            needRebind(holder, position)
        }
    }

    private fun needRebind(holder: VideoViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            val path = mDownloadThumbnailManager?.downloadThumbnail(
                item.timeStart,
                item.timeEnd,
                position
            )
            Glide.with(root.context).load(path)
                .placeholder(R.drawable.default_image).into(thumb)
            thumb.setTag("thumbnail:$position")
            tvTimeStart.text = DateHelper.toHourMinute(Date(item.timeStart))
            if (item.timeEnd > 0) {
                tvDuration.visible()
                tvDuration.text =
                    DateHelper.getDurationFromMs(item.timeEnd - item.timeStart)
            } else {
                tvDuration.gone()
            }
            imvChecked.isChecked = item.isSelected
            if (item.isModeChecked) {
                imvChecked.visible()
            } else {
                item.isSelected = false
                imvChecked.gone()
            }
            imvChecked.isChecked = item.isSelected
            root.setOnClickListener {
                if (item.isModeChecked) {
                    imvChecked.performClick()
                    onItemCheckedChange?.invoke(item)
                } else {
                    onItemClick?.invoke(item)
                }
            }
            root.setOnLongClickListener {
                val isChecked = item.isSelected
                item.isSelected = !isChecked
                notifyDataSetChanged()
                actionCheckedMode?.invoke()
                true
            }
            imvChecked.setOnClickListener {
                val isChecked = item.isSelected
                item.isSelected = !isChecked
                notifyItemChanged(position)
                onItemCheckedChange?.invoke(item)
            }
        }
    }

    fun changeModeChecked() {
        currentList.forEach {
            if (it.type == TYPE_VIDEO) {
                it.isModeChecked = true
            }
        }
        onItemCheckedChange?.invoke(null)
        notifyDataSetChanged()
    }

    fun doUnchecked() {
        currentList.forEach {
            if (it.type == TYPE_VIDEO) {
                it.isModeChecked = false
                it.isSelected = false
            }
        }
        onItemCheckedChange?.invoke(null)
        notifyDataSetChanged()
    }

    fun unSelectAll() {
        currentList.forEach {
            if (it.type == TYPE_VIDEO) {
                it.isSelected = false
            }
        }
        onItemCheckedChange?.invoke(null)
        notifyDataSetChanged()
    }

    fun selectAll() {
        currentList.forEach {
            if (it.type == TYPE_VIDEO) {
                it.isSelected = true
            }
        }
        onItemCheckedChange?.invoke(null)
        notifyDataSetChanged()
    }

    fun getListChecked(): List<ItemVideo> {
        val listChecked = mutableListOf<ItemVideo>()
        currentList.forEach {
            if (it.type == TYPE_VIDEO) {
                if (it.isSelected) {
                    listChecked.add(it)
                }
            }
        }
        return listChecked
    }

    fun isSelectAll(): Boolean {
        var isSelectAll = true
        currentList.forEach {
            if (it.type == TYPE_VIDEO) {
                if (!it.isSelected) {
                    isSelectAll = false
                    return@forEach
                }
            }
        }
        return isSelectAll
    }

    fun deleteListChecked() {
        val result = mutableListOf<ItemVideo>()
        val temp = mutableListOf<ItemVideo>()
        currentList.forEach {
            if (!it.isSelected) {
                result.add(it)
                temp.add(it)
            }
        }
        temp.forEachIndexed { index, itemVideo ->
            try {
                if (itemVideo.type == TYPE_TITLE) {
                    if (index > 0) {
                        if (temp[index - 1].type == TYPE_TITLE) {
                            result.remove(temp[index - 1])
                        }
                    }
                    if (index == (temp.size - 1)) {
                        result.remove(itemVideo)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        submitList(result)
    }


    class VideoViewHolder(val binding: ItemVideoEventPlaybackBinding) :
        RecyclerView.ViewHolder(binding.root)

    class TitleViewHolder(val binding: ItemTitleVideoEventPlaybackBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: ItemVideo) {
            binding.tvTime.text = item.timeTitle + ":00"
        }
    }

    override fun onDownloadResult(isSuccess: Boolean, seq: Int?, path: String?) {
        if (isSuccess) {
            Log.d("onDownloadResult", "success = " + seq)
            val imv: ImageView? = recyclerView.findViewWithTag("thumbnail:$seq")
            imv?.let {
                context?.let {  ct ->
                    Glide.with(ct).load(path)
                        .placeholder(R.drawable.default_image).into(imv)
                }
            }
            notifyItemChanged(seq ?: 0)
        } else {
            if (currentList.isNullOrEmpty()) return
            if (mapCount.containsKey(seq)) {
                Log.d("onDownloadResult", "mapCount contain seq = "+seq)
                val count = mapCount[seq] ?: 0
                if (count < Config.COUNT_RETRY_THUMBNAIL_JF) {
                    mapCount[seq ?: 0] = count + 1

                    if (seq != null && seq != -1) {
                        path?.let {
                            val name = File(path).name
                            val splitPath = name.split("_")
                            if (!splitPath.isNullOrEmpty()) {
                                val pos =
                                    currentList.indexOfFirst { it.timeStart == splitPath[0].toLong() }
                                Log.d("onDownloadResult", "onDownloadResult: pos = $pos")
                                notifyItemChanged(pos)
                            }
                        }
                    }
                }
            } else {
                mapCount.put(seq ?: 0, 0)
                notifyItemChanged(seq ?: 0)
                Log.d("onDownloadResult", "mapCount not contain")
            }
        }
        Log.d("onDownloadResult", "notifyItemChanged seq"+seq)
//        notifyItemChanged(seq ?: 0)
//        if (isSuccess) {
//            notifyItemChanged(seq ?: 0)
//        } else {
//            if (currentList.isNullOrEmpty()) return
//            if (mapCount.containsKey(seq)) {
//                val count = mapCount[seq] ?: 0
//                if (count < Config.COUNT_RETRY_THUMBNAIL_JF) {
//                    mapCount[seq ?: 0] = count + 1
//                    Log.d("downloadImage", "cloud seq = " + seq)
//
//                    if (seq != null && seq != -1) {
//                        Log.d("CHECK_THUMBNAIL", "onDownloadResult: path = $path")
//                        path?.let {
//                            val name = File(path).name
//                            val splitPath = name.split("_")
//                            if (!splitPath.isNullOrEmpty()) {
//                                val pos =
//                                    currentList.indexOfFirst { it.timeStart == splitPath[0].toLong() }
//                                Log.d("CHECK_THUMBNAIL", "onDownloadResult: pos = $pos")
//                                notifyItemChanged(pos)
//                            }
//                        }
//                    }
//                }
//            } else {
//                mapCount.put(seq ?: 0, 0)
//            }
//        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
    }

    companion object {
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<ItemVideo>() {
            override fun areItemsTheSame(oldItem: ItemVideo, newItem: ItemVideo): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ItemVideo, newItem: ItemVideo): Boolean {
                return false
            }

            override fun getChangePayload(oldItem: ItemVideo, newItem: ItemVideo): Any {
                return Bundle()
            }
        }
        const val TYPE_TITLE = 0
        const val TYPE_VIDEO = 1
    }

}

data class GroupVideo(val time: String, val listItem: MutableList<ItemVideo>)

data class ItemVideo(
    val id: String,
    val timeStart: Long,
    val timeEnd: Long,
    var thumbnail: String,
    var isSelected: Boolean,
    var isModeChecked: Boolean,
    val fileName: String,

    val timeTitle: String = "",
    val type: Int = ItemVideoCloudAdapter.TYPE_VIDEO,

    val video: H264_DVR_FILE_DATA? = null,
)