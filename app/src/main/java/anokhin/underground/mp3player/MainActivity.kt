package anokhin.underground.mp3player

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.Image
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog.*
import java.io.File
import android.provider.MediaStore.Images.Media.getBitmap
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable



class MainActivity : AppCompatActivity() {
    companion object {
        var bitMapGlobal: Bitmap? = null

    }

    val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0
    val CUSTOM_DIALOG_ID = 0
    lateinit var root: File
    lateinit var curFolder: File
    lateinit var backToParent: Button
    lateinit var pickFolder: Button

    lateinit var startStopButton: ImageView
    lateinit var skipTrackButton: ImageView
    lateinit var singerPhoto: ImageView
    lateinit var trackName: TextView
    lateinit var trackTime: TextView

    lateinit var dialogFilesList: ListView
    lateinit var currentFolderText: TextView

    private var playerServiceBinder: PlayerService.PlayerServiceBinder? = null
    private var mediaController: MediaControllerCompat? = null
    private var callback: MediaControllerCompat.Callback? = null
    lateinit var serviceConnection: ServiceConnection
    var playing = false
    var pausing = false

    val fileList = arrayListOf<String>()

    private fun hasPermission(perm: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("trackName", trackName.text.toString())
        outState.putString("trackTime", trackTime.text.toString())
//        val tag = singerPhoto.tag as Int?
//        if (tag != null)
//            outState.putInt("songPhoto", tag)
//            outState.putInt("songPhoto", tag)
//        singerPhoto.invalidate()
//        val drawable = singerPhoto.getDrawable() as BitmapDrawable
//        val bitmap = drawable.bitmap
//        outState.putParcelable("bitmap", bitmap)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = File(Environment.getExternalStorageDirectory().absolutePath)
        curFolder = root

        setContentView(R.layout.activity_main)
        singerPhoto = findViewById(R.id.track_photo)
        trackName = findViewById(R.id.track_name)
        trackTime = findViewById(R.id.song_time)
        skipTrackButton = findViewById(R.id.skip_track)
        skipTrackButton.setOnClickListener {
            if (mediaController != null)
                mediaController!!.transportControls.skipToNext()
        }
        startStopButton = findViewById(R.id.start_stop_button)
        startStopButton.setOnClickListener {
            if (mediaController != null) {
                if (playing)
                    mediaController!!.transportControls.pause()
                if (pausing)
                    mediaController!!.transportControls.play()
            }
        }

        if (savedInstanceState != null) {
//            outState.putString("trackName", trackName.text.toString())
//            outState.putString("trackTime", trackTime.text.toString())
//            outState.putInt("songPhoto", singerPhoto.tag as Int)
//            val bmp = savedInstanceState.getParcelable<Bitmap>("bitmap")

            trackName.setText(savedInstanceState.getString("trackName", "Track"))
            trackTime.setText(savedInstanceState.getString("trackTime", "42:42"))

            Log.i("Own", "HEEEERRREEE")
            if (bitMapGlobal != null) {
                Log.i("Own", "Add new biMap " + bitMapGlobal.hashCode())
                singerPhoto.post {

                    singerPhoto.setImageBitmap(bitMapGlobal)
//                    singerPhoto.setImageResource(imageId)
//                    singerPhoto.tag = imageId
                }
            }
        }

        trackName.isClickable = false
        trackName.setOnClickListener{
            if (bitMapGlobal != null) {
                val intent = Intent(this, SongsActivity::class.java)
                startActivity(intent)
            }
        }
        singerPhoto.isClickable = false
        singerPhoto.setOnClickListener{
            if (bitMapGlobal != null) {
                val intent = Intent(this, SongsActivity::class.java)
                startActivity(intent)
            }
        }




        search_button?.setOnClickListener{

            Log.i("Own", "Here")
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                Log.i("Own", "lol")
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                }

                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

            } else {
                showFolders()
            }
        }

        callback = object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                if (state == null)
                    return
                playing = state.state == PlaybackStateCompat.STATE_PLAYING
                pausing = state.state == PlaybackStateCompat.STATE_PAUSED

                skipTrackButton.isClickable = playing || pausing
                startStopButton.isClickable = playing || pausing
                singerPhoto.isClickable = playing || pausing
                trackName.isClickable = playing || pausing

                if (playing)
                    startStopButton.post {
                        startStopButton.setImageResource(R.drawable.ic_pause_28)
                    }
                if (pausing)
                    startStopButton.post {
                        startStopButton.setImageResource(R.drawable.ic_play_arrow_blue_24dp)
                    }
//                if (state.state == PlaybackStateCompat.STATE_PLAYING)
//                    startStopButton.setImageResource(R.drawable.)
//                playButton.isEnabled = !playing
//                pauseButton.isEnabled = playing
//                stopButton.isEnabled = playing
            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                super.onMetadataChanged(metadata)
                trackName.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                ////////////////// PLACE FOR TIME CHANGING {TrackTIme}
                trackTime.text = metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toString()
                singerPhoto.setImageBitmap(metadata?.getBitmap(MediaMetadataCompat.METADATA_KEY_ART))
                Log.i("Own", "Add new biMap")
                bitMapGlobal = metadata?.getBitmap(MediaMetadataCompat.METADATA_KEY_ART)
                Log.i("Own", "Add new biMap " + bitMapGlobal.hashCode())
//                metadata?.getBitmap(MediaMetadataCompat.METADATA_KEY_ART)
//                metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
//                metadata?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
//                metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
//                metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                SongsActivity.callback?.onMetadataChanged(metadata)
                SongsActivity.firstTrack = MusicRepository.Track("abum",
                    trackName.text.toString(),
                    metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST),
                    metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION),
                    bitMapGlobal)
            }
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                playerServiceBinder = service as PlayerService.PlayerServiceBinder
                try {
                    mediaController = MediaControllerCompat(this@MainActivity, playerServiceBinder!!.mediaSessionToken)
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

        bindService(Intent(this, PlayerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)

//        playButton.setOnClickListener {
//            if (mediaController != null)
//                if (hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                    playerServiceBinder!!.setMusicRepositoryFolder(Environment.getExternalStorageDirectory().absolutePath + "/Slack")
//                    Log.i("AmyAPP", "Add new Folder")
//                }
//            mediaController!!.transportControls.play()
//        }
//
//        pauseButton.setOnClickListener {
//            if (mediaController != null)
//        }
//
//        stopButton.setOnClickListener {
//            if (mediaController != null)
//                mediaController!!.transportControls.stop()
//        }
//
//        skipToNextButton.setOnClickListener {
//            if (mediaController != null)
//                mediaController!!.transportControls.skipToNext()
//        }
//
//        skipToPreviousButton.setOnClickListener {
//            if (mediaController != null)
//                mediaController!!.transportControls.skipToPrevious()
//        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    showFolders()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun showFolders() {
        showDialog(CUSTOM_DIALOG_ID)
    }

    override fun onCreateDialog(id: Int) =
        when (id) {
            CUSTOM_DIALOG_ID -> {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.dialog)
                dialog.setTitle("File Explorer")
                dialog.setCancelable(true)
                dialog.setCanceledOnTouchOutside(true)

                backToParent = dialog.findViewById(R.id.back_to_parent)
                pickFolder = dialog.findViewById(R.id.pick_folder)
                dialogFilesList = dialog.findViewById(R.id.dialog_files_list)
                currentFolderText = dialog.findViewById(R.id.current_folder)

                backToParent.setOnClickListener {
                    listDir(curFolder.parentFile)
                }

                pickFolder.setOnClickListener {
                    dialog.dismiss()
                    if (mediaController != null)
                        if (hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            Log.i("AmyAPP", "UNIQ MES. Path: " + curFolder.absolutePath)
                            try {
                                playerServiceBinder!!.setMusicRepositoryFolder(curFolder.absolutePath)
                            } catch (err: IllegalArgumentException) {
                                Log.i("AmyAPP", "Catched")
                            }
                            Log.i("AmyAPP", "Add new Folder")
                        }
                    mediaController!!.transportControls.play()
                    val intent = Intent(this, SongsActivity::class.java)
                    startActivity(intent)
                }

                dialogFilesList.setOnItemClickListener { parent, view, position, id ->
                    val selected = File(fileList[position])
                    if (selected.isDirectory) {
                        listDir(selected)
                    } else {
                        val toast = Toast.makeText(applicationContext, "Only folders may be picked", Toast.LENGTH_LONG)
                        toast.show()
                    }
                }
                dialog
            }
            else -> {
                null
            }
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog?) {
        super.onPrepareDialog(id, dialog)
        when(id) {
            CUSTOM_DIALOG_ID -> {
                listDir(curFolder)
            }
            else -> {
                // nope
            }
        }
    }

    private fun listDir(file: File) {
        backToParent.isEnabled = !file.equals(root)
        curFolder = file
        currentFolderText.text = curFolder.path

        val files = file.listFiles()
        fileList.clear()

        var haveMp3FileInside = false
        for (f in files) {
            if (f.name.endsWith(".mp3"))
                haveMp3FileInside = true
            fileList.add(f.path)
        }
        pickFolder.isEnabled = haveMp3FileInside

        val directoryListAdapter = ArrayAdapter<String>(this,
            R.layout.simple_list_item_1, fileList)
        dialogFilesList.adapter = directoryListAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        playerServiceBinder = null
        if (mediaController != null) {
            mediaController!!.unregisterCallback(callback!!)
            mediaController = null
        }
        unbindService(serviceConnection)
    }
}
