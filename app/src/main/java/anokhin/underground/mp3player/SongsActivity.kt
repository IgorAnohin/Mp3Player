package anokhin.underground.mp3player

import android.view.MotionEvent

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import anokhin.underground.mp3player.MainActivity.Companion.bitMapGlobal
import kotlinx.android.synthetic.main.songs.view.*
import kotlin.concurrent.thread
import com.google.android.exoplayer2.C
import java.util.*

val formatBuilder = StringBuilder();
val formatter = Formatter(formatBuilder, Locale.getDefault());

fun stringForTime(timeMs: Long): String {
    var timeMs = timeMs
    if (timeMs == C.TIME_UNSET) {
        timeMs = 0
    }
    val totalSeconds = (timeMs + 500) / 1000
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60 % 60
    val hours = totalSeconds / 3600
    formatBuilder.setLength(0)
    return if (hours > 0)
        formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
    else
        formatter.format("%02d:%02d", minutes, seconds).toString()
}

class SongsActivity : Activity(), SimpleGestureFilter.SimpleGestureListener {
    companion object {
        var leftBitMapGlobal: Bitmap? = null
        var curBitMapGlobal: Bitmap? = null
        var rightBitMapGlobal: Bitmap? = null

        var callback: MediaControllerCompat.Callback? = null
        var firstTrack: MusicRepository.Track? = null
    }

    lateinit var trackName: TextView
    lateinit var trackTime: TextView
    lateinit var trackPlayingTime: TextView
    lateinit var autorName: TextView

    private var detector: SimpleGestureFilter? = null
    private var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    private var mediaController: MediaControllerCompat? = null
    lateinit var serviceConnection: ServiceConnection

    lateinit var startStopButton: ImageView
    lateinit var skipToNextTrackButton: ImageView
    lateinit var skipToPrevTrackButton: ImageView

    lateinit var prevTrack: ImageView
    lateinit var nextTrack: ImageView
    lateinit var curTrack: ImageView

    lateinit var seekBar: SeekBar

    var playing = false
    var pausing = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("trackName", trackName.text.toString())
        outState.putString("trackTime", trackTime.text.toString())
        outState.putString("trackPlayingTime", trackPlayingTime.text.toString())
        outState.putString("autorName", autorName.text.toString())
//        val tag = singerPhoto.tag as Int?
//        if (tag != null)
//            outState.putInt("songPhoto", tag)
//            outState.putInt("songPhoto", tag)
//        singerPhoto.invalidate()
//        val drawable = singerPhoto.getDrawable() as BitmapDrawable
//        val bitmap = drawable.bitmap
//        outState.putParcelable("bitmap", bitmap)

    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.songs)

        trackName = findViewById(R.id.track_name)
        autorName = findViewById(R.id.track_autor)
        trackTime = findViewById(R.id.whole_time)
        trackPlayingTime = findViewById(R.id.run_time)
        prevTrack = findViewById(R.id.left_image)
        curTrack = findViewById(R.id.center_image)
        nextTrack = findViewById(R.id.right_image)

        // Detect touched area
        detector = SimpleGestureFilter(this, this)
        ///////////////////////
        val killIntentImage = findViewById<ImageView>(R.id.kill_intent)
        killIntentImage.setOnClickListener {
            finish()
        }

        skipToNextTrackButton = findViewById(R.id.skip_to_next)
        skipToNextTrackButton.setOnClickListener {
            if (mediaController != null)
                mediaController!!.transportControls.skipToNext()
        }
        skipToPrevTrackButton = findViewById(R.id.skip_to_prev)
        skipToPrevTrackButton.setOnClickListener {
            if (mediaController != null)
                mediaController!!.transportControls.skipToPrevious()
        }

        startStopButton = findViewById(R.id.second_start_stop_button)
        startStopButton.setOnClickListener {
            if (mediaController != null) {
                if (playing)
                    mediaController!!.transportControls.pause()
                if (pausing)
                    mediaController!!.transportControls.play()
            }
        }

        seekBar = findViewById(R.id.seek_bar)




        if (savedInstanceState != null) {
            trackName.setText(savedInstanceState.getString("trackName", "Track"))
            trackTime.setText(savedInstanceState.getString("trackTime", "42:42"))
            trackPlayingTime.setText(savedInstanceState.getString("trackPlayingTime", "42:42"))
            autorName.setText(savedInstanceState.getString("autorName", "42:42"))

            if (bitMapGlobal != null) {
                Log.i("Own", "Add new biMap " + bitMapGlobal.hashCode())
                curTrack.post {
                    curTrack.setImageBitmap(bitMapGlobal)
                }
                val next_track = MusicRepository.checkNext
                Log.i("Ownn", "Next Track " + next_track?.duration)
                nextTrack.post {
                    nextTrack.setImageBitmap(next_track?.bitmap)
                }
            }

            Log.i("Own", "HEEEERRREEE")
        } else if (firstTrack != null) {
            trackName.setText(firstTrack?.title)
            ////////////////// PLACE FOR TIME CHANGING {TrackTIme}
//            val secs = firstTrack?.duration!!.div(1000).rem(60)
//            val mins = firstTrack?.duration!!.div(1000).div(60)
//            trackTime.text = "-" + mins.toString() + ":" + secs.toString()
//            trackTime.setText(firstTrack?.duration.toString())
            autorName.setText(firstTrack?.artist)
            if (bitMapGlobal != null) {
                Log.i("Own", "Add new biMap " + bitMapGlobal.hashCode())
                curTrack.post {

                    curTrack.setImageBitmap(bitMapGlobal)
//                    singerPhoto.setImageResource(imageId)
//                    singerPhoto.tag = imageId
                }
                val next_track = MusicRepository.checkNext
                Log.i("Ownn", "First Track")
                Log.i("Ownn", "Next Track " + next_track?.duration)
                if (next_track?.bitmap != null)
                    Log.i("Ownn", "Next Track Bitmap!!! " + next_track.duration)
                nextTrack.post {
                    nextTrack.setImageBitmap(next_track?.bitmap)
                }
            }
        }


        callback = object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                if (state == null)
                    return
                playing = state.state == PlaybackStateCompat.STATE_PLAYING
                pausing = state.state == PlaybackStateCompat.STATE_PAUSED
                Log.i("Ownn", "HERE")

                if (playing)
                    startStopButton.post {
                        startStopButton.setImageResource(R.drawable.ic_pause_48)
                    }
                if (pausing)
                    startStopButton.post {
                        startStopButton.setImageResource(R.drawable.ic_play_48)
                    }
            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                super.onMetadataChanged(metadata)
                Log.i("Ownn", "Add new biMap")
                trackName.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
//                val duration = metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
//                if (duration != null) {
//                    val secs = duration.div(1000).rem(60)
//                    val mins = duration.div(1000).div(60)
//                    trackTime.text = "-" + mins.toString() + ":" + secs.toString()
//                }
//                trackTime.text =
//                    .toString()
                autorName.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
//                singerPhoto.setImageBitmap(metadata?.getBitmap(MediaMetadataCompat.METADATA_KEY_ART))
                Log.i("Ownn", "Add new biMap")
                Log.i("Ownn", "Add new biMap")
                val next_track = MusicRepository.checkNext
                Log.i("Ownn", "Next Track " + next_track?.duration)
                nextTrack.post {
                    nextTrack.setImageBitmap(next_track?.bitmap)
                }

                val prev_track = MusicRepository.checkPrev
                Log.i("Ownn", "Next Track " + prev_track?.duration)
                prevTrack.post {
                    prevTrack.setImageBitmap(prev_track?.bitmap)
                }

//                metadata?.getBitmap(MediaMetadataCompat.METADATA_KEY_ART)
//                metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
//                metadata?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
//                metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
//                metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            }
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                playerServiceBinder = service as PlayerService.PlayerServiceBinder
                try {
                    mediaController = MediaControllerCompat(this@SongsActivity, playerServiceBinder!!.mediaSessionToken)
                    mediaController!!.registerCallback(callback!!)
                    callback!!.onPlaybackStateChanged(mediaController!!.playbackState)
                } catch (e: RemoteException) {
                    mediaController = null
                }

            }

            override fun onServiceDisconnected(name: ComponentName) {
                playerServiceBinder = null
                if (mediaController != null) {
                    mediaController!!.unregisterCallback(callback!!)
                    mediaController = null
                }
            }
        }
        seekBar.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                playerServiceBinder?.setProgress(seekBar.progress.toLong())
                return false
            }
        })
        bindService(Intent(this, PlayerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        thread(start = true) {
            while (true) {
                if (playerServiceBinder != null) {
                    val progressTime = playerServiceBinder?.getCurrentDuration()
                    val duration = playerServiceBinder?.getFullDuration()
                    Log.i("Own", "FULL DURATION: " + duration)
                    Log.i("Own", "Past time: " + progressTime)

                    if (progressTime != null && duration != null) {
                        trackTime.text = "-" + stringForTime(duration - progressTime)
                        trackPlayingTime.text = stringForTime(progressTime)
                        seekBar.max = duration.toInt()
                        seekBar.progress = progressTime.toInt()
                    }
                }
                Thread.sleep(1000)
            }
        }

        /////////////////////////
    }

    override fun dispatchTouchEvent(me: MotionEvent): Boolean {
        // Call onTouchEvent of SimpleGestureFilter class
        this.detector!!.onTouchEvent(me)
        return super.dispatchTouchEvent(me)
    }

    override fun onSwipe(direction: Int) {
        var str = ""

        when (direction) {

            SimpleGestureFilter.SWIPE_RIGHT -> {
                if (mediaController != null)
                    mediaController!!.transportControls.skipToPrevious()
            }
            SimpleGestureFilter.SWIPE_LEFT -> {
                if (mediaController != null)
                    mediaController!!.transportControls.skipToNext()
            }
            SimpleGestureFilter.SWIPE_DOWN -> finish()
            SimpleGestureFilter.SWIPE_UP -> {
            }
        }
//        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }

    override fun onDoubleTap() {
//        Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show()
    }

}
