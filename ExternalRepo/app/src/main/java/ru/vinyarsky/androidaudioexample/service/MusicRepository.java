package ru.vinyarsky.androidaudioexample.service;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import ru.vinyarsky.androidaudioexample.R;

final class MusicRepository {


    private ArrayList<Track> data = new ArrayList<>();

    private int maxIndex = data.size() - 1;
    private int currentItemIndex = 0;


    public MusicRepository() {
        setMusicFolder(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    void setMusicFolder(String musicFolderPath) {
        Log.i("AmyAPP", "foler path " + musicFolderPath);
        File musicFolder = new File(musicFolderPath);
        if (musicFolder.isDirectory())
            Log.i("AmyAPP", "FOLDER");
        data.clear();
        for (File file : musicFolder.listFiles()) {
            if (file.getName().endsWith(".mp3"))
                try {
                    data.add(
                            new Track(
                                    file.getAbsolutePath()
                            )
                    );
                    Log.i("AmyAPP", "file was created");
                } catch (IllegalArgumentException err) {}

        }
        maxIndex = data.size() - 1;
        currentItemIndex = 0;
    }

    Track getNext() {
        if (currentItemIndex == maxIndex)
            currentItemIndex = 0;
        else
            currentItemIndex++;
        return getCurrent();
    }

    Track getPrevious() {
        if (currentItemIndex == 0)
            currentItemIndex = maxIndex;
        else
            currentItemIndex--;
        return getCurrent();
    }

    Track getCurrent() {
        if (maxIndex == -1)
            return null;
        return data.get(currentItemIndex);
    }

    static class Track {

        private String title;
        private String artist;
        private int bitmapResId;
        private Uri uri;
        private long duration; // in ms

        Track(String trackPath) {
            Log.i("AmyAPP", "my App reseivec " + trackPath);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(trackPath);
//            String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            this.title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            this.artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            this.bitmapResId = R.drawable.image396168;
            this.uri = Uri.parse(trackPath);
            this.duration = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        }

        Track(String title, String artist, int bitmapResId, Uri uri, long duration) {
            this.title = title;
            this.artist = artist;
            this.bitmapResId = bitmapResId;
            this.uri = uri;
            this.duration = duration;
        }

        String getTitle() {
            return title;
        }

        String getArtist() {
            return artist;
        }

        int getBitmapResId() {
            return bitmapResId;
        }

        Uri getUri() {
            return uri;
        }

        long getDuration() {
            return duration;
        }
    }
}
