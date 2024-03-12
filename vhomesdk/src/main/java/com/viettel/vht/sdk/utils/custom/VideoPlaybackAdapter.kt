package com.viettel.vht.sdk.utils.custom

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemPlaybackCloudCameraBinding
import com.viettel.vht.sdk.utils.DateHelper
import com.viettel.vht.sdk.utils.DownloadThumbnailManager
import java.util.*

class VideoPlaybackAdapter(
    private val context: Context,
    private val onItemClick: (Int, EventHistory) -> Unit?,
    private val onItemChangeSelect: (Int) -> Unit?,
    private val devJFId: String? = null
) :
    ListAdapter<EventHistory, VideoPlaybackAdapter.VideoViewHolder>(object :
        DiffUtil.ItemCallback<EventHistory>() {
        override fun areItemsTheSame(
            oldItem: EventHistory,
            newItem: EventHistory
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: EventHistory,
            newItem: EventHistory
        ): Boolean {
            return false
        }

        override fun getChangePayload(
            oldItem: EventHistory,
            newItem: EventHistory
        ): Any {
            return Bundle()
        }
    }), DownloadThumbnailManager.OnCloudImageListener {

    private var mDownloadThumbnailManager: DownloadThumbnailManager? = null

    init {
        devJFId?.let {
            mDownloadThumbnailManager = DownloadThumbnailManager(context, devJFId, this)
        }
    }


    private var positionSelected = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding: ItemPlaybackCloudCameraBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_playback_cloud_camera,
            parent, false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: VideoViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.firstOrNull() is Bundle) {
            needRebind(holder, position)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        needRebind(holder, position)
        notNeedRebind(holder, position)
    }

    fun setPositionSelected(position: Int) {
        positionSelected = position
        notifyDataSetChanged()
    }

    fun getPositionSelected() = positionSelected

    fun submitList(list: List<EventHistory>, positionSelected: Int) {
        this.positionSelected = positionSelected
        super.submitList(list)
    }

    private fun notNeedRebind(holder: VideoViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            if (devJFId != null) {
                //TODO VUHT hard code camera JF
                val path = mDownloadThumbnailManager?.downloadThumbnail(
                    item.timeStart,
                    item.timeEnd,
                    position
                )
                Glide.with(root.context).load(path)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image).into(thumb)
            } else {
                Glide.with(root.context).load(item.thumbnailUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image).into(thumb)
            }
            tvTimeStart.text = DateHelper.toHourMinute(Date(item.timeStart))
            tvDuration.text =
                DateHelper.getDurationFromMs(item.timeEnd - item.timeStart)
        }
    }

    private fun needRebind(holder: VideoViewHolder, position: Int) {
        holder.binding.apply {
            holder.binding.viewBorder.visibility =
                if (position == positionSelected) View.VISIBLE else View.GONE
            root.setOnClickListener { onItemClick.invoke(position, getItem(position)) }
        }
    }

    class VideoViewHolder(val binding: ItemPlaybackCloudCameraBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onDownloadResult(isSuccess: Boolean, seq: Int?, path: String?) {
        try {

            if (seq != null && seq != -1) {
                if (isSuccess) {
                    notifyItemChanged(seq)
                } else {
                    if (currentList.isNullOrEmpty()) return
                    mDownloadThumbnailManager?.downloadThumbnail(
                        currentList[seq].timeStart,
                        currentList[seq].timeEnd,
                        seq
                    )
                }
            }
        } catch (e: Exception) {
            Log.d("TAG", "onDownloadResult: Error = $e")
        }
    }

}