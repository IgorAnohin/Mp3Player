package anokhin.underground.mp3player

import android.Manifest
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

class MainActivity : AppCompatActivity() {

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

    val fileList = arrayListOf<String>()

    private fun hasPermission(perm: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = File(Environment.getExternalStorageDirectory().absolutePath)
        curFolder = root

        setContentView(R.layout.activity_main)
        trackName = findViewById(R.id.track_name)
        trackTime = findViewById(R.id.song_time)
        skipTrackButton = findViewById(R.id.skip_track)
        skipTrackButton.setOnClickListener {
            if (mediaController != null)
                mediaController!!.transportControls.skipToNext()
        }
        startStopButton = findViewById(R.id.start_stop_button)
        startStopButton.setOnClickListener {
            if (mediaController != null)
                mediaController!!.transportControls.pause()
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
                val playing = state.state == PlaybackStateCompat.STATE_PLAYING
                skipTrackButton.isClickable = playing
                startStopButton.isClickable = playing || state.state == PlaybackStateCompat.STATE_PAUSED
//                if (state.state == PlaybackStateCompat.STATE_PLAYING)
//                    startStopButton.setImageResource(R.drawable.)
//                playButton.isEnabled = !playing
//                pauseButton.isEnabled = playing
//                stopButton.isEnabled = playing
            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                super.onMetadataChanged(metadata)
                trackName.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                metadata?.getBitmap(MediaMetadataCompat.METADATA_KEY_ART)
                metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                metadata?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
                metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
                metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
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

    override fun onCreateDialog(id: Int): Dialog? {
        var dialog: Dialog? = null

        when (id) {
            CUSTOM_DIALOG_ID -> {
                dialog = Dialog(this)
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
            }
            else -> {

            }
        }

        return dialog
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
