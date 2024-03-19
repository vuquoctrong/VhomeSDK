package com.viettel.vht.sdk.ui.gallery

import android.app.ActionBar
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.Utils.Companion.isVideo
import com.vht.sdkcore.utils.UtilsJava.convertToTimeSecond
import com.vht.sdkcore.utils.dialog.CommonAlertDialog
import com.vht.sdkcore.utils.margin
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentDetailGalleryBinding
import com.viettel.vht.sdk.utils.DebugConfig
import com.viettel.vht.sdk.utils.VLCPlayerUtil
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.videolan.libvlc.MediaPlayer
import java.io.File

@AndroidEntryPoint
class GalleryDetailFragment :
    BaseFragment<FragmentDetailGalleryBinding, GalleryViewModel>(),
    TextureView.SurfaceTextureListener {
    private val TAG: String = GalleryDetailFragment::class.java.simpleName
    private val viewModel: GalleryViewModel by viewModels()

    override val layoutId: Int
        get() = R.layout.fragment_detail_gallery

    override fun getVM(): GalleryViewModel = viewModel

    var player: VLCPlayerUtil? = null
    private var path = ""
    private var isPlaying = false
    private var isReady: Boolean = false
    var eventId = ""
    var deviceId = ""
    var isVideo = false
    var state: STATE = STATE.STOPED

    override fun initView() {
        super.initView()
        binding.toolbar.setOnLeftClickListener {
            findNavController().navigateUp()
        }
        binding.ivDelete.setOnClickListener {
            CommonAlertDialog.getInstanceCommonAlertdialog(requireContext())
                .showDialog()
                .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.confirm_delete))
                .setContent(getString(com.vht.sdkcore.R.string.confirm_delete_content))
                .setTextPositiveButton(com.vht.sdkcore.R.string.string_ok)
                .setTextNegativeButton(com.vht.sdkcore.R.string.text_close)
                .showNegativeAndPositiveButton()
                .setOnNegativePressed {
                    it.dismiss()
                }
                .setOnPositivePressed {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            File(path).delete()
                        }
                        it.dismiss()
                        findNavController().navigateUp()
                    }
                }
        }
        binding.btnPlay.setOnClickListener {
            when (state) {
                STATE.STOPED -> {
                    playVideo()
                    player?.seekTime(binding.seekbar.progress * 1000L)
                }

                STATE.PLAYING -> player?.pause()
                STATE.PAUSE -> player?.play()
            }
        }
        binding.btnLeft.setOnClickListener {
            toggleFullScreenMode()
        }

        binding.btnZoomControl.setOnClickListener {
            toggleFullScreenMode()
        }
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isVideo) {
                        if (isScreenOrientationLandscape()) {
                            toggleFullScreenMode()
                        } else {
                            findNavController().navigateUp()
                        }
                    } else {
                        findNavController().navigateUp()
                    }

                }
            }
        binding.view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                binding.cameraView.zoomOnTouchListeners.onTouch(v, event)
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (binding.btnPlay.isShown) {
                            binding.btnPlay.gone()
                        } else {
                            binding.btnPlay.visible()
                        }
                    }
                }
                return true

            }

        })
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        binding.cameraView.surfaceTextureListener = this
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_VALUE)) {
                path = it.getString(Define.BUNDLE_KEY.PARAM_VALUE) ?: ""
                isVideo = File(path).isVideo()
                DebugConfig.logd(TAG, "initView: isVideo = $isVideo")
                if (File(path).isVideo()) {
                    binding.layoutVideoPlayer.visible()
                    binding.containerSeekbar.visible()
                    binding.imvImage.gone()
                    initVLCPlayer()
                    initVideoSize()
                } else {
                    binding.imvImage.visible()
                    binding.layoutVideoPlayer.gone()
                    binding.containerSeekbar.gone()
                    Glide.with(this).load(path).into(binding.imvImage)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopStream()
        runnable?.let { handler.removeCallbacks(it) }
    }

    private val handler = Handler()
    private var runnable: Runnable? = null

    private fun initVLCPlayer() {
        runnable = Runnable {
            if (state == STATE.PLAYING) {
                binding.tvTimeStart.text =
                    convertToTimeSecond(binding.seekbar.progress.toLong())
                val currentPos = binding.seekbar.progress
                binding.seekbar.progress = currentPos + 1
                runnable?.let {
                    handler.postDelayed(it, 1000)
                }
            }
        }
        val textureView: TextureView = binding.cameraView
        player = VLCPlayerUtil(requireContext(), textureView) { event ->
            when (event?.type) {
                MediaPlayer.Event.Buffering -> {

                }

                MediaPlayer.Event.Opening -> {

                }

                MediaPlayer.Event.Paused -> {
                    lifecycleScope.launchWhenStarted {
                        binding.btnPlay.setImageResource(R.drawable.ic_play_circle)
                        state = STATE.PAUSE
                        runnable?.let { handler.removeCallbacks(it) }
                    }
                }

                MediaPlayer.Event.Playing -> {
                    DebugConfig.logd(message = "initVLCPlayer: Playing")
                    lifecycleScope.launchWhenStarted {
                        binding.progressBar.gone()
                        state = STATE.PLAYING
                        runnable?.let {
                            handler.removeCallbacks(it)
                            handler.post(it)
                        }
                        binding.btnPlay.setImageResource(R.drawable.ic_video_pause)
                    }

                }

                MediaPlayer.Event.TimeChanged -> {
//                    lifecycleScope.launchWhenStarted {
//                        if (state == STATE.PLAYING) {
//                            val currentPos = binding.seekbar.progress
//                            binding.seekbar.progress = currentPos + 1
//                            binding.tvTimeStart.text =
//                                convertToTimeSecond(binding.seekbar.progress.toLong())
//                        }
//                    }
                }

                MediaPlayer.Event.Stopped -> {
                    lifecycleScope.launchWhenStarted {
                        DebugConfig.logd(TAG, "initVLCPlayer: Stopped")
                        binding.seekbar.progress = 0
                        binding.tvTimeStart.text = convertToTimeSecond(0)
                        state = STATE.STOPED
                        binding.btnPlay.setImageResource(R.drawable.ic_play_circle)
                        runnable?.let {
                            handler.removeCallbacks(it)
                        }
                    }

                }
            }
        }
    }


    private fun playVideo() {
        DebugConfig.logd(TAG, "playVideo: $path")
        if (!isReady) {
            return
        }
        binding.btnPlay.gone()
        binding.btnPlay.setImageResource(R.drawable.ic_video_pause)
        player?.playStreamLocal(path)
    }

    private fun stopStream() {
        player?.stopStream()
    }

    private fun toggleFullScreenMode() {

//        //toggle full screen mode
        activity?.resources?.configuration?.orientation?.let { orientation ->
            Log.d(TAG, "toggleFullScreenMode: orientation = $orientation")
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                exitFullScreenMode()
                binding.toolbar.visible()
                binding.ivDelete.visible()
                binding.toolbarFullScreen.gone()
                binding.btnZoomControl.margin(left = 10F, right = 0F)
                binding.btnZoomControl.setImageResource(R.drawable.ic_live_view_full_screen)
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                initFullscreenMode()
                binding.toolbar.gone()
                binding.ivDelete.gone()
                binding.toolbarFullScreen.visible()
                binding.btnZoomControl.margin(left = 10F, right = 50F)
                binding.btnZoomControl.setImageResource(R.drawable.ic_live_view_exit_full_screen)
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
            binding.btnZoomControl.invalidate()
        }
    }

    private fun initVideoSize() {
        val textureView = binding.cameraView
        //  val layoutVideoPlayer = binding.layoutVideoPlayer
        val width = getMinimalScreenWidth()
        val height = getMinimalScreenHeight()

        val params = textureView.layoutParams.also {
            it.width = width
            it.height = height
        }
        textureView.layoutParams = params
//        layoutVideoPlayer.layoutParams.let {
//            it.width = width
//            it.height = height
//        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            DebugConfig.logd(TAG, "onConfigurationChanged: ORIENTATION_LANDSCAPE")
            binding.ivDelete.gone()
            displayFullScreenVideo()

        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
            displayMinimalScreenVideo()
            binding.ivDelete.visible()
            DebugConfig.logd(TAG, "onConfigurationChanged: ORIENTATION_PORTRAIT")

        }
    }

    private fun displayFullScreenVideo() {
        val width = getFullScreenWidth()
        val height = getFullScreenHeight()
        //    val layoutVideoPlayer = binding.layoutVideoPlayer
        val textureView = binding.cameraView
//        layoutVideoPlayer.layoutParams =
//            LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ActionBar.LayoutParams.MATCH_PARENT
//            )

        textureView.layoutParams = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.MATCH_PARENT
        )
        player?.setWindowSize(width, height)
    }


    private fun displayMinimalScreenVideo() {
        val width = getMinimalScreenWidth()
        val height = getMinimalScreenHeight()
        // val layoutVideoPlayer = binding.layoutVideoPlayer
        val textureView = binding.cameraView
        // layoutVideoPlayer.layoutParams = LinearLayout.LayoutParams(width, height)

        textureView.layoutParams = ConstraintLayout.LayoutParams(width, height)

        player?.setWindowSize(width, height)
    }

    private fun File.getMediaDuration(context: Context): Long {
        if (!exists()) return 0
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.parse(absolutePath))
            val duration = retriever.extractMetadata(METADATA_KEY_DURATION)
            retriever.release()

            return duration?.toLongOrNull() ?: 0
        } catch (e: Exception) {
            return 0
        }

    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        isReady = true
        playVideo()
        val duration = (File(path).getMediaDuration(requireContext())) / 1000
        binding.seekbar.max = duration.toInt()
        binding.tvTimeEnd.text = convertToTimeSecond(duration)
        binding.tvTimeStart.text = convertToTimeSecond(0)
        DebugConfig.logd(TAG, "getDuration: ${(File(path).getMediaDuration(requireContext()))}")
        DebugConfig.logd(TAG, "max: ${((File(path).getMediaDuration(requireContext())) / 1000).toInt()}")
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                DebugConfig.logd(TAG, "SeekBar: ${p0?.progress ?: 0}")
                binding.tvTimeStart.text = convertToTimeSecond(binding.seekbar.progress.toLong())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                if (state == STATE.PLAYING) {
                    player?.pause()
                }
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                Handler().postDelayed({
                    if (state == STATE.PAUSE) {
                        player?.play()
                        player?.seekTime((p0?.progress ?: 0) * 1000L)
                    } else if (state == STATE.STOPED) {
                        playVideo()
                        player?.seekTime((p0?.progress ?: 0) * 1000L)
                    }
                }, 800)
            }

        })
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }

    enum class STATE {
        PAUSE, PLAYING, STOPED,
    }
}