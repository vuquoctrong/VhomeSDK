package com.viettel.vht.sdk.ui.jfcameradetail.view

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lib.SDKCONST
import com.lib.sdk.bean.alarm.AlarmInfo
import com.vht.sdkcore.adapter.BaseAdapter
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.EventItemJfBinding
import com.viettel.vht.sdk.jfmanager.CloudImageManager
import com.viettel.vht.sdk.utils.AppEnum
import com.viettel.vht.sdk.utils.Config
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date

class JFEventAdapter2(
    val recyclerView: RecyclerView,
    val context: Context,
    val devId: String,
    val listener: (AlarmInfo) -> Unit
) :
    BaseAdapter<AlarmInfo, EventItemJfBinding>(),
    CloudImageManager.OnCloudImageListener {
    private var mCloudImageManager: CloudImageManager? = null
    private var mapCount: HashMap<Int, Int> = HashMap()

    init {
        mCloudImageManager = CloudImageManager(context)
        mCloudImageManager?.setOnCloudImageListener(this)
    }


    override val itemLayoutRes: Int
        get() = R.layout.event_item_jf

    override fun bindSingleTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemBinding.apply {
            val path = mCloudImageManager?.downloadImage(
                devId,
                item,
                SDKCONST.MediaType.VIDEO, position, 0, 0, true
            )
            Glide.with(root.context).load(path).placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(imvThumbnail)
            if (item.event == AppEnum.EventJFType.HI.type) {
                tvEventName.text = context.getString(AppEnum.EventJFType.HI.nameId)
            } else {
                tvEventName.text =
                    context.getString(AppEnum.EventJFType.MOTION_DETECTION.nameId)
            }
            tvDate.text = item.startTime
            root.setOnClickListener {
                listener.invoke(item)
            }
        }
    }

    override fun bindMultiTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)

        holder.itemBinding.apply {
            val path = mCloudImageManager?.downloadImage(
                devId,
                item,
                SDKCONST.MediaType.VIDEO, position, 0, 0, true
            )
            imvThumbnail.setTag("thumbnail:$position")
            Glide.with(root.context).load(path).placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(imvThumbnail)
            if (item.event == AppEnum.EventJFType.HI.type) {
                tvEventName.text = context.getString(AppEnum.EventJFType.HI.nameId)
            } else {
                tvEventName.text =
                    context.getString(AppEnum.EventJFType.MOTION_DETECTION.nameId)
            }
            tvDate.text = SimpleDateFormat("HH:mm:ss").format(Date(parseDate(item.startTime) ?: 0))
            root.setOnClickListener {
                listener.invoke(item)
            }
        }
    }

    private fun parseDate(text: String): Long {
        try {
            val dateFormat = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss"
            )
            return dateFormat.parse(text).time
        } catch (ex: java.lang.Exception) {
            return 0
        }
    }

    override fun onDownloadResult(
        isSuccess: Boolean,
        imagePath: String?,
        bitmap: Bitmap?,
        mediaType: Int,
        seq: Int
    ) {
        if (isSuccess) {
            val imv: ImageView? = recyclerView.findViewWithTag("thumbnail:$seq")
            imv?.let {
                if (context != null) {
                    try {
                        Glide.with(context).load(imagePath)
                            .placeholder(R.drawable.default_image).into(imv)
                    } catch (e: Exception) {
                        Timber.d("${e.message}")
                    }

                }
            }
        } else {
            if (currentList.isNullOrEmpty()) return
            if (mapCount.containsKey(seq)) {
                val count = mapCount[seq] ?: 0
                if (count < Config.COUNT_RETRY_THUMBNAIL_JF) {
                    mapCount[seq] = count + 1
                    Log.d("downloadImage", "seq = " + seq)

                    mCloudImageManager?.downloadImage(
                        devId,
                        currentList[seq],
                        SDKCONST.MediaType.VIDEO, seq, 0, 0, true
                    )
                }
            } else {
                mapCount.put(seq, 0)
            }
        }
    }

    override fun onDeleteResult(isSuccess: Boolean, seq: Int) {

    }
}