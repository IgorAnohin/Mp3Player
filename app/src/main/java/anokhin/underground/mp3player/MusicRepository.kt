package anokhin.underground.mp3player

import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log

import java.io.File
import java.util.ArrayList
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.io.ByteArrayInputStream
import java.time.Duration


class MusicRepository {
    companion object {
        val data = ArrayList<Track>()
        var maxIndex = data.size - 1
        var currentItemIndex = 0
        var resources: Resources? = null

        val checkNext: Track?
            get() = if (data.size == 0)
                        null
                    else if (currentItemIndex == maxIndex)
                        data[0]
                    else
                        data[currentItemIndex+1]
        val checkPrev: Track?
            get() = if (data.size == 0)
                null
            else if (currentItemIndex == 0)
                data[maxIndex]
            else
                data[currentItemIndex-1]
    }

    val next: Track?
        get() {
            if (currentItemIndex == maxIndex)
                currentItemIndex = 0
            else
                currentItemIndex++
            return current
        }

    val previous: Track?
        get() {
            if (currentItemIndex == 0)
                currentItemIndex = maxIndex
            else
                currentItemIndex--
            return current
        }

    val current: Track?
        get() = if (maxIndex == -1) null else data[currentItemIndex]

    fun setMusicFolder(musicFolderPath: String) {
        Log.i("AmyAPP", "foler path $musicFolderPath")
        val musicFolder = File(musicFolderPath)
        if (musicFolder.isDirectory)
            Log.i("AmyAPP", "FOLDER")
        data.clear()
        for (file in musicFolder.listFiles()) {
            if (file.name.endsWith(".mp3"))
                try {
                    data.add(Track(file.absolutePath))
                    Log.i("AmyAPP", "file was created")
                } catch (err: IllegalArgumentException) {
                }

        }
        maxIndex = data.size - 1
        currentItemIndex = 0
    }

    class Track {

        var title: String? = null
            private set
        var album: String? = null
            private set
        var artist: String? = null
            private set
        var bitmapResId: Int = 0
            private set
        var uri: Uri? = null
            private set
        var bitmap: Bitmap? = null
            private set
        var duration: Long = 0
            private set // in ms

        constructor(trackPath: String) {
            Log.i("AmyAPP", "my App reseivec $trackPath")
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(trackPath)

            this.album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            this.title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            this.artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            this.bitmapResId = R.drawable.image396168
            this.uri = Uri.parse(trackPath)
            this.duration = java.lang.Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))

            if (mmr.embeddedPicture != null) {
                val inputStream = ByteArrayInputStream(mmr.embeddedPicture)
                this.bitmap = BitmapFactory.decodeStream(inputStream)
            } else {
                this.bitmap = BitmapFactory.decodeResource(MusicRepository.resources, bitmapResId)
                val file = File(trackPath).parentFile
                val files = file.listFiles()

                val rightImages = files.filter{ it.name.startsWith(title.toString())}
                for (im in rightImages) {
                    Log.i("Own", "Image " + im?.absolutePath)
                    val bMap = BitmapFactory.decodeFile(im?.absolutePath)
                    this.bitmap = bMap
                    if (this.bitmap == null)
                        Log.i("Own", "Fuck")
                    else break
                }
                if (this.bitmap == null) {
                    val images = files.filter { it.name.endsWith(".jpg") }
                    for (im in images) {
                        Log.i("Own", "Image " + im?.absolutePath)
                        val bMap = BitmapFactory.decodeFile(im?.absolutePath)
                        this.bitmap = bMap
                        if (this.bitmap == null)
                            Log.i("Own", "Fuck")
                        else break
                    }
                }


            }
        }
        constructor(album: String?, title: String, artist: String?, duration: Long?, bitmap: Bitmap?) {
            this.album = album
            this.title = title
            this.artist = artist
            this.bitmap = bitmap
            if (duration != null)
                this.duration = duration
        }
    }
}