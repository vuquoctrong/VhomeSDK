package com.viettel.vht.sdk.utils

import android.content.Context
import android.net.Uri
import android.view.TextureView
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IVLCVout

const val HW_DECODE_ENABLE = true
const val HW_DECODE_FORCE = false

class VLCPlayerUtil(val context: Context,
                    private val textureView: TextureView,
                    val listener: org.videolan.libvlc.MediaPlayer.EventListener
                    ){

     var mediaPlayer: org.videolan.libvlc.MediaPlayer? = null
    private val optionsCameraInDoor = listOf(
            "-vvv",
            ":fullscreen",
//            "--rtsp-tcp",
            ":network-caching=300",
            ":clock-jitter=0",
            ":clock-synchro=0"
    )

    private val optionsCameraOutDoor = listOf(
        "-vvv",
        ":fullscreen",
//            "--rtsp-tcp",
        ":network-caching=2000",
        ":clock-jitter=0",
        ":clock-synchro=0"
    )

    private val optionsPlaybackCameraInDoor = listOf(
        "-vvv",
        ":fullscreen",
//            "--rtsp-tcp",
        ":network-caching=2000",
        ":clock-jitter=0",
        ":clock-synchro=0"
    )

    private val optionsPlaybackCameraOutDoor = listOf(
        "-vvv",
        ":fullscreen",
//            "--rtsp-tcp",
        ":network-caching=2000",
        ":clock-jitter=0",
        ":clock-synchro=0"
    )

    private val optionsDefault = listOf(
            "-vvv",
            "--rtsp-tcp"
    )
    private var libVLC: LibVLC? = null
    var vlcOut: IVLCVout? = null
        get() = field
        set(value) { field = value }
    private var vlcMedia: Media? = null

    //region Public API
    fun playStream(url: String,serialCamera: String? = null) {
        try {
            DebugConfig.log(message = "play rtsp stream $url")
            stopStream()
            initMedia(url,serialCamera)
            initMediaPlayer()
            initVLCOut()
            play()
        } catch (e: java.lang.Exception) {
            DebugConfig.loge(message = "playStream e ${e.message}")
        }
    }

    fun playStreamPlayback(url: String,serialCamera: String? = null) {
        try {
            DebugConfig.log(message = "play rtsp stream $url")
            stopStream()
            initMediaPlayBack(url,serialCamera)
            initMediaPlayer()
            initVLCOut()
            play()
        } catch (e: java.lang.Exception) {
            DebugConfig.loge(message = "playStream e ${e.message}")
        }
    }

    fun playStreamLocal(url: String) {
        stopStream()
        initMediaLocal(url)
        initMediaPlayer()
        initVLCOut()
        play()
    }

    fun isPlayStream():Boolean{
        return mediaPlayer?.isPlaying?:false
    }

    fun setWindowSize(width:Int,height: Int){
        mediaPlayer?.vlcVout?. setWindowSize(width, height)
    }

    fun pauseStream() {
        try {
            mediaPlayer?.pause()
        }catch (e:Exception){

        }
    }

    fun onStopMedia(){
        try {
            mediaPlayer?.stop()
        }catch (e:Exception){

        }
    }

    fun resumeStream() {
        try {
            mediaPlayer?.play()
        }catch (e:Exception){

        }
    }

    fun seekTime(time:Long) {
        mediaPlayer?.setTime(time)
    }

    fun getDuration()= mediaPlayer?.length

    fun getPosition()= mediaPlayer?.position

    fun stopStream() {
        try {
            mediaPlayer?.time = 0L
            mediaPlayer?.stop()
            libVLC?.release()
//            vlcOut?.detachViews()
            vlcMedia?.release()
//            mediaPlayer?.detachViews()
//            mediaPlayer?.vlcVout?.detachViews()
            mediaPlayer?.release()
            mediaPlayer = null
            libVLC = null
            vlcOut = null
            vlcMedia = null
            DebugConfig.logd(message = "stopStream")
        } catch (e: Exception) {
            DebugConfig.loge(message = "e ${e.message}")
        }
    }
    //endregion Public API

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer(libVLC).apply {
            this.media = vlcMedia
            setEventListener(listener)
            setVideoTrackEnabled(true)
        }
    }

    private fun initVLCOut() {
        try {
            val width = textureView.width
            val height = textureView.height
            vlcOut = mediaPlayer?.vlcVout?.apply {
                setVideoView(textureView)
                setWindowSize(width, height)
                attachViews()
            }
        }catch (e:Exception){
            DebugConfig.loge(message = "Error VLC: ${e.message}")
        }

    }

    private fun initMedia(url: String,serialCamera: String? = null) {
        libVLC = LibVLC(context)
        vlcMedia = Media(libVLC, Uri.parse(url)).apply {
            if(serialCamera.isNullOrEmpty()){
                addMediaOption(this, optionsCameraInDoor)
            }else{
                if(serialCamera.contains("CNME1")){
                    // camera outdoor
                    addMediaOption(this, optionsCameraOutDoor)
                }else{
                    // camera indoor
                    addMediaOption(this, optionsCameraInDoor)
                }
            }

            setHWDecoderEnabled(HW_DECODE_ENABLE, HW_DECODE_FORCE)
        }
    }


    private fun initMediaPlayBack(url: String,serialCamera: String? = null) {
        libVLC = LibVLC(context)
        vlcMedia = Media(libVLC, Uri.parse(url)).apply {
            if(serialCamera.isNullOrEmpty()){
                addMediaOption(this, optionsPlaybackCameraInDoor)
            }else{
                if(serialCamera.contains("CNME1")){
                    // camera outdoor
                    addMediaOption(this, optionsPlaybackCameraOutDoor)
                }else{
                    // camera indoor
                    addMediaOption(this, optionsPlaybackCameraInDoor)
                }

            }

            setHWDecoderEnabled(HW_DECODE_ENABLE, HW_DECODE_FORCE)
        }
    }

    private fun initMediaLocal(url: String) {
        libVLC = LibVLC(context)
        vlcMedia = Media(libVLC, url).apply {
            addMediaOption(this, optionsDefault)
            setHWDecoderEnabled(false, false)
        }
    }



    private fun addMediaOption(media: Media?, options: List<String>) {
        for (option in options) {
            media?.addOption(option)
        }
    }
     fun getVolumeMedia():Int?{
         var volume = 0
         try {
             mediaPlayer?.let {
                 volume =  it.volume
             }
         }catch (e:Exception){
             DebugConfig.log("VLCPlayerUtil",e.message?:"")
         }
         return volume
    }
     fun setVolumeMedia(volume: Int){
         try {
             mediaPlayer?.let {
                 it.volume = volume
             }
         }catch (e:Exception){
             DebugConfig.log("VLCPlayerUtil",e.message?:"")
         }

    }

    fun setRecordVideo(path: String?):Boolean?{
        return mediaPlayer?.record(path)
    }
    fun stopRecordVideo(){
        try {
            mediaPlayer?.record(null)
        }catch (e: Exception){
            DebugConfig.log("stopRecordVideo",e.message?:"")
        }
    }

    fun play() {
        mediaPlayer?.play()
    }

    fun pause() {
        mediaPlayer?.pause()
    }
}