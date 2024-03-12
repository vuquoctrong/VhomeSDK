package com.viettel.vht.sdk.utils.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.LayoutTimelineVideoCameraBinding
import com.viettel.vht.sdk.utils.DebugConfig
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.coroutines.CoroutineContext

class LayoutTimeLineVideo(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs), CoroutineScope {

    private var maxValue = 24 * TimeLineView.HOUR
    private var minValue = 0 * TimeLineView.HOUR
    private var binding: LayoutTimelineVideoCameraBinding
    private var currentScale = 1f
    private var showThumb = false
    var listEvent: MutableList<EventHistory> = mutableListOf()
    var currentIndex: Int = -1
    var onClickThumb: ((EventHistory?) -> Unit)? = null
    var onCheckedChange: ((Boolean) -> Unit)? = null
    var onValueChange: (() -> Unit)? = null
    var onEndValue: (() -> Unit)? = null
    var onNoEvent: (() -> Unit)? = null
    var onCheckExistVideo: ((EventHistory?) -> Unit)? = null
    var onDownloadVideoEvent: ((EventHistory) -> Unit)? = null
    var onSeekTimeChange: ((EventHistory, Long) -> Unit)? = null
    var onJFSeekTimeChange: ((EventHistory?, Int) -> Unit)? = null
    var devJFId: String? = null
    val adapter: VideoPlaybackAdapter by lazy {
        VideoPlaybackAdapter(
            context,
            this::onItemVideoPlaybackClick,
            this::onItemChangeSelect,
            devJFId
        )
    }

    fun updateThumbnailURL(position: Int, pathThumbnail: String) {
        if (adapter.currentList.size > position) {
            adapter.currentList[position].thumbnailUrl = pathThumbnail
            adapter.notifyItemChanged(position)
        }
    }

    private fun onItemVideoPlaybackClick(position: Int, item: EventHistory) {
        onMovedToEvent(item)
        onItemChangeSelect(position)
    }

    private fun onItemChangeSelect(index: Int) {
        binding.rcvVideo.scrollToPosition(index)
    }

    init {
        if (attrs != null) {
            val styledAttrs =
                context.theme.obtainStyledAttributes(attrs, R.styleable.LayoutTimeLineVideo, 0, 0)
            showThumb = styledAttrs.getBoolean(R.styleable.LayoutTimeLineVideo_show_thumb, false)
        }
        binding = LayoutTimelineVideoCameraBinding.inflate(LayoutInflater.from(context), this, true)
        if (showThumb) {
            binding.rcvVideo.visible()
        } else {
            binding.rcvVideo.gone()
        }
        binding.timeline.setOnValueChangeListener(object : TimeLineView.OnValueChangeListener {
            override fun onValueChange(value: Float) {
                val time = binding.timeline.convertToTimeSecond(value.toLong())
                setTextTime(time)
                val currentEvent = listEvent.find { event ->
                    value >= convertTimeLongToTime(event.timeStart) && value <= convertTimeLongToTime(
                        (event.timeEnd)
                    )
                }
                onCheckedChange(currentEvent)
                onCurrentEventHistory(currentEvent)
                onValueChange?.invoke()
            }

            override fun onRealValueChange(value: Float) {
                val index = listEvent.indexOfFirst { event ->
                    value >= convertTimeLongToTime(event.timeStart) && value <= convertTimeLongToTime(
                        (event.timeEnd)
                    )
                }
                if (index == -1) {
                    onJFSeekTimeChange?.invoke(null, value.toInt())

                    // There is not current event
                    val nextEvent = listEvent.find {
                        convertTimeLongToTime(it.timeStart) > value
                    }
                    if (nextEvent != null) {
                        currentIndex = listEvent.indexOf(nextEvent)
                        // Move to next event
                        Log.d("TIMELINE", "onRealValueChange: onMovedToEvent")
                        // nếu current value đang k ở trong record nào thì sẽ nhảy đến record gần nhất
                        onMovedToEvent(nextEvent)
                    } else {
                        onNoEvent?.invoke()
                        // There is no next event
//                        Toast.makeText(
//                            context,
//                            context.getString(R.string.event_over),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        val preEvent = listEvent.find {
//                            convertTimeLongToTime(it.timeStart) <= binding.timeline.getmSelectorValue()
//                        }
//                        if (preEvent != null) {
//                            // Move to previous event
//                            onMovedToEvent(preEvent)
//                        }
                    }
                } else {
                    // There is current event
                    val currentEvent = listEvent.find { event ->
                        value >= convertTimeLongToTime(event.timeStart) && value <= convertTimeLongToTime(
                            (event.timeEnd)
                        )
                    }
                    if (currentEvent != null) {
                        // Move to current event
//                        onMovedToEvent(currentEvent)
                        // Seek time
                        // nếu current value đang ở tỏng 1 bản record thì sẽ là seek time
                        currentIndex = listEvent.indexOf(currentEvent)
                        onSeekTimeChange?.invoke(currentEvent, value.toLong() * 1000)
                        setSelectedThumb(currentIndex)
                    }
                    onJFSeekTimeChange?.invoke(currentEvent, value.toInt())
                }
            }
        })

        binding.timeline.setValue(
            minValue.toFloat(),
            minValue.toFloat(),
            maxValue.toFloat(),
            currentScale
        )

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentScale =
                        Math.max(0.9f, Math.min(50.0f * progress / binding.seekbar.max, 50.0f))
                    binding.timeline.setValue(
                        binding.timeline.getmSelectorValue(),
                        minValue.toFloat(),
                        maxValue.toFloat(),
                        currentScale
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

        binding.timeline.setOnScaleChanged {
            Log.d("LAYOUT_TIMELINE", "setOnScaleChanged: ")
            currentScale = binding.timeline.getmScale()
            binding.seekbar.progress = (it * binding.seekbar.max / 50.0f).toInt()
        }

//        binding.imvEvent.setOnClickListener {
//            onSelectedThumb()
//        }
    }

    fun hideThumbnail() {
        showThumb = false
        binding.rcvVideo.gone()
    }

    fun setValueWithStartTime(timeStart: Long) {
        val value = convertTimeLongToTime(timeStart)
        val index = listEvent.indexOfFirst { event ->
            value >= convertTimeLongToTime(event.timeStart) && value <= convertTimeLongToTime(
                (event.timeEnd)
            )
        }
        if (index == -1) {
            // There is not current event
            val nextEvent = listEvent.find {
                convertTimeLongToTime(it.timeStart) > value
            }
            if (nextEvent != null) {
                currentIndex = listEvent.indexOf(nextEvent)
                // Move to next event
                onMovedToEvent(nextEvent)
            } else {
                val time = binding.timeline.convertToTimeSecond(value)
                setTextTime(time)
                binding.timeline.setValue(
                    value.toFloat(),
                    minValue.toFloat(),
                    maxValue.toFloat(),
                    currentScale
                )
                onCheckExistVideo?.invoke(null)
            }
        } else {
            // There is current event
            val currentEvent = listEvent.find { event ->
                value >= convertTimeLongToTime(event.timeStart) && value <= convertTimeLongToTime(
                    (event.timeEnd)
                )
            }
            if (currentEvent != null) {
                // Move to current event
//                        onMovedToEvent(currentEvent)
                // Seek time
                val time = binding.timeline.convertToTimeSecond(value)
                setTextTime(time)
                binding.timeline.setValue(
                    value.toFloat(),
                    minValue.toFloat(),
                    maxValue.toFloat(),
                    currentScale
                )
                currentIndex = listEvent.indexOf(currentEvent)
                setSelectedThumb(currentIndex)
                onSeekTimeChange?.invoke(currentEvent, value.toLong() * 1000)
                onJFSeekTimeChange?.invoke(currentEvent, value.toInt())
                Log.d("TAG", "setValueWithStartTime: $value")
            }
        }
    }

    fun getViewDisable() = binding.viewDisable

    fun enableTimeline(isEnabled: Boolean) {
        if (isEnabled) {
            binding.viewDisable.gone()
        } else {
            binding.viewDisable.visible()
        }
    }

    fun getCurrentIndexJF(): Int {
        val value = binding.timeline.getmSelectorValue()
        val index = listEvent.indexOfFirst { event ->
            value >= convertTimeLongToTime(event.timeStart) && value <= convertTimeLongToTime(
                (event.timeEnd)
            )
        }
        return index
    }

    fun getmSelectorValue(): Float {
        return binding.timeline.getmSelectorValue()
    }

    fun checkCurrentValue() {
        val value = binding.timeline.getmSelectorValue()
        val index = listEvent.indexOfFirst { event ->
            value >= convertTimeLongToTime(event.timeStart) && value <= convertTimeLongToTime(
                (event.timeEnd)
            )
        }
        if (index == -1) {
            // There is not current event
            val nextEvent = listEvent.find {
                convertTimeLongToTime(it.timeStart) > value
            }
            if (nextEvent != null) {
                currentIndex = listEvent.indexOf(nextEvent)
                // Move to next event
                Log.d("TIMELINE", "onRealValueChange: onMovedToEvent")
                onMovedToEvent(nextEvent)
            } else {
                onNoEvent?.invoke()
                // There is no next event
//                        Toast.makeText(
//                            context,
//                            context.getString(R.string.event_over),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        val preEvent = listEvent.find {
//                            convertTimeLongToTime(it.timeStart) <= binding.timeline.getmSelectorValue()
//                        }
//                        if (preEvent != null) {
//                            // Move to previous event
//                            onMovedToEvent(preEvent)
//                        }
            }
        } else {
            // There is current event
            val currentEvent = listEvent.find { event ->
                value >= convertTimeLongToTime(event.timeStart) && value <= convertTimeLongToTime(
                    (event.timeEnd)
                )
            }
            if (currentEvent != null) {
                // Move to current event
//                        onMovedToEvent(currentEvent)
                // Seek time
                currentIndex = listEvent.indexOf(currentEvent)
                onSeekTimeChange?.invoke(currentEvent, value.toLong() * 1000)
            }
        }
    }

    fun onSelectedThumb() {
        if (currentIndex == -1) return
        setSelectedThumb(currentIndex)
        onCheckedChange(listEvent.get(currentIndex))
        onClickThumb?.invoke(listEvent.get(currentIndex))
    }

    fun setSelectedThumb(index: Int) {
        if (showThumb) {
            adapter.setPositionSelected(index)
            launch(Dispatchers.Main) {
                delay(100)
                binding.rcvVideo.scrollToPosition(index)
            }
        }
    }

    fun onCheckedChange(currentEvent: EventHistory?) {
        if (currentEvent == null) return
        launch(Dispatchers.Main) {
            binding.imvEvent.borderColor =
                if (currentEvent.isCheckedThumb == true) context.getColor(com.vht.sdkcore.R.color.colorAppPrimary)
                else context.getColor(com.vht.sdkcore.R.color.color_transparent)
        }
    }

    fun onNextEvent() {
        if (currentIndex >= 0 && currentIndex < listEvent.size - 1) {
            currentIndex++
            val nextEvent = listEvent.get(currentIndex)
            Log.d("TIMELINE", "onNextEvent: onMovedToEvent")
            onMovedToEvent(nextEvent)
        } else {
            onEndValue?.invoke()
            if (currentIndex == listEvent.size - 1) {
                onCheckExistVideo?.invoke(listEvent[currentIndex])
            } else {
                onNoEvent?.invoke()
            }
//            Toast.makeText(context, context.getString(R.string.event_over), Toast.LENGTH_SHORT)
//                .show()
        }
    }

    fun onFirtSelectEvent() {
        if (listEvent.isNullOrEmpty()) {
            return
        }
        val firtEvent = listEvent.last()
        Log.d("TIMELINE", "onFirtSelectEvent: ${listEvent.size}")
        onMovedToEvent(firtEvent)
    }

    fun onMovedToEvent(currentEvent: EventHistory) {
        if (currentEvent.eventType == -1) {
            currentIndex++
            onMovedToEvent(listEvent.get(currentIndex))
            return
        }
        currentIndex = listEvent.indexOf(currentEvent)
        val startTime = convertTimeLongToTime(currentEvent.timeStart)
        val time = binding.timeline.convertToTimeSecond(startTime)
        setTextTime(time)
        binding.timeline.setValue(
            startTime.toFloat(),
            minValue.toFloat(),
            maxValue.toFloat(),
            currentScale
        )
        onCheckedChange(currentEvent)
        onCurrentEventHistory(currentEvent)
        onSelectedThumb()
    }


    fun onCurrentEventHistory(currentEvent: EventHistory?) {
        launch(Dispatchers.Main) {
            if (currentEvent?.thumbnailUrl == null) {
                binding.imvEvent.visibility = INVISIBLE
                binding.imvType.visibility = INVISIBLE
            } else {
                // Type
                binding.imvType.visibility = VISIBLE
                // Thumbnail
                binding.imvEvent.visibility = VISIBLE
//                if (currentEvent.thumbnailByteArray != null) {
//                    Glide.with(context).load(currentEvent.thumbnailByteArray)
//                        .apply(
//                            RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
//                                .skipMemoryCache(true)
//                                .error(R.drawable.ic_motion)
//                                .placeholder(R.drawable.ic_motion)
//                        ).into(binding.imvEvent)
//                } else if (!currentEvent.thumbnailUrl.isNullOrEmpty()) {
//                    binding.imvEvent.glideUrlLoader(
//                        currentEvent.thumbnailUrl ?: "",
//                        cookieUrl
//                    )
//                }
            }
            binding.imvEvent.visibility = INVISIBLE
        }
    }

    fun showTextTime(isShow: Boolean) {
        launch {
            binding.tvTime.visibility = if (isShow) VISIBLE else GONE
        }
    }

    fun setTextTime(time: String) {
        launch {
            binding.tvTime.text = time
        }
    }

    fun getTexTime() = binding.tvTime.text.toString()

    fun setList(list: MutableList<EventHistory>, startTime: Long = 0) {
        listEvent.clear()
        listEvent.addAll(list.filter {
            convertTimeLongToTime(it.timeStart) >= minValue && convertTimeLongToTime(it.timeEnd) <= maxValue
        }.sortedBy {
            it.timeStart
        }.apply {
            forEachIndexed { index, eventHistory ->
                if (index != this.size - 1) {
                    if (this[index + 1].timeStart - this[index].timeEnd <= 2000 && this[index + 1].timeStart - this[index].timeEnd > 0) {
                        this[index].timeEnd = this[index + 1].timeStart + 1000
                    }
                }
            }
        })
        binding.timeline.setEvents(
            listEvent.map {
                TimeLineView.Event(
                    it.eventID,
                    convertTimeLongToTime(it.timeStart),
                    convertTimeLongToTime(it.timeEnd),
                    if (it.eventType == 2) Color.parseColor(
                        "#ED1B2F"
                    )
                    else if (it.eventType == 3) Color.parseColor(
                        "#ED1B2F"
                    )
                    else Color.WHITE
                )
            }
        )
        if (showThumb) {
            binding.rcvVideo.adapter = adapter
            adapter.submitList(listEvent)
        }
        if (startTime != 0L) {
            setValueWithStartTime(startTime)
        } else {
            onFirtSelectEvent()
        }
    }

    fun setValue(time: Long) {
        binding.timeline.setValue(
            convertTimeLongToTime(time).toFloat(),
            minValue.toFloat(),
            maxValue.toFloat(),
            currentScale
        )
    }

    fun setMaxMinTime(minTime: Long, maxTime: Long) {
        maxValue = maxTime
        minValue = minTime
        if (maxTime - minTime < 3600) {
            currentScale = 50f
            binding.seekbar.progress = 100
        } else {
            currentScale = 25f
            binding.seekbar.progress = 50
        }
        binding.timeline.setValue(
            ((minValue + maxValue) / 2).toFloat(),
            minValue.toFloat(),
            maxValue.toFloat(),
            currentScale
        )
    }

    fun onFinishedEvent() {
        launch(Dispatchers.Main) {
            binding.imvEvent.borderColor = context.getColor(com.vht.sdkcore.R.color.transparent_sdk)
            onNextEvent()
        }
    }

    fun getCurrentEvent(): EventHistory? {
        if (currentIndex == -1 || listEvent.isEmpty()) return null
        return listEvent.get(currentIndex)
    }

    fun updatevalue(currentValue: Long) {
        if (currentIndex == -1 || listEvent.isEmpty()) return
        val startTime = convertTimeLongToTime(listEvent.get(currentIndex).timeStart)
        binding.timeline.setValue(
            currentValue.toFloat() + startTime,
            minValue.toFloat(),
            maxValue.toFloat(),
            currentScale
        )
        val time =
            binding.timeline.convertToTimeSecond(currentValue + startTime)
        setTextTime(time)
        DebugConfig.log(
            "LAYOUT_TIMELINE",
            "time end: ${convertTimeLongToTime(listEvent.get(currentIndex).timeEnd)} - current: ${currentValue + startTime}"
        )
        if (convertTimeLongToTime(listEvent.get(currentIndex).timeEnd) <= currentValue + startTime) {
            DebugConfig.log("LAYOUT_TIMELINE", "finish")
            onFinishedEvent()
        }
    }

    fun hideListThumb() {
        binding.rcvVideo.gone()
    }

    fun showListThumb() {
        binding.rcvVideo.visible()
    }

    fun updatevalueJF(currentValue: Long) {
        binding.timeline.setValue(
            currentValue.toFloat(),
            minValue.toFloat(),
            maxValue.toFloat(),
            currentScale
        )
        val time =
            binding.timeline.convertToTimeSecond(currentValue)
        setTextTime(time)
        if (currentValue >= maxValue - 1) {
            onNoEvent?.invoke()
        }
    }

    fun notifiDataChanged() {
//        binding.timeline.setEvents(
//            listEvent.map {
//                TimeLineView.Event(
//                    it.eventID,
//                    convertTimeLongToTime(it.timeStart),
//                    convertTimeLongToTime(it.timeEnd),
//                    if (it.eventType == AppEnum.EventTypeHistory.INTRUSION_DETECTION.code) Color.RED
//                    else if (it.eventType == AppEnum.EventTypeHistory.FACE_RECOGNITION.code ||
//                        it.eventType == AppEnum.EventTypeHistory.STRANGER.code
//                    ) Color.GREEN
//                    else if (it.eventType == AppEnum.EventTypeHistory.MOTION.code) Color.GRAY
//                    else Color.WHITE
//                )
//            }
//        )
    }

    fun convertTimeLongToTime(timeLong: Long): Long {
        val date = Date(timeLong)
        val timeString = SimpleDateFormat("HH:mm:ss").format(date)
        val split = timeString.split(":")
        return (split.get(0).toInt() * 3600 + split.get(1).toInt() * 60 + split.get(2)
            .toInt()).toLong()
    }

    fun setVideoPath(id: String?, videoPath: String) {
        Log.d("TIMELINE", "setVideoPath: videoPath = $videoPath")
        listEvent.find { it.id == id }?.apply {
            this.videoPath = videoPath
        }
    }

    fun downloadVideoNextEvent() {
        var index = currentIndex
        Log.d("TIMELINE", "downloadVideoNextEvent: index = $index")
        if (index >= 0 && index < listEvent.size - 1) {
            index++
            val nextEvent = listEvent.get(index)
            onDownloadVideoEvent?.invoke(nextEvent)
        }
    }

    fun setEnabledTimeLine(isEnabled: Boolean = true) {
        binding.timeline.setTouchable(isEnabled)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}