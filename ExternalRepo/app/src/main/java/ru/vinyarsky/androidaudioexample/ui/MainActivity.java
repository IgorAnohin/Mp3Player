package ru.vinyarsky.androidaudioexample.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ru.vinyarsky.androidaudioexample.R;
import ru.vinyarsky.androidaudioexample.service.PlayerService;

public class MainActivity extends AppCompatActivity {

    private PlayerService.PlayerServiceBinder playerServiceBinder;
    private MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback callback;
    private ServiceConnection serviceConnection;


    public final String[] EXTERNAL_PERMS = {Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public final int EXTERNAL_REQUEST = 138;


    public boolean requestForPermission() {

        boolean isPermissionOn = true;
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            if (!canAccessExternalSd()) {
                isPermissionOn = false;
                ActivityCompat.requestPermissions(this, EXTERNAL_PERMS, EXTERNAL_REQUEST);
            }
        }

        return isPermissionOn;
    }

    public boolean canAccessExternalSd() {
        return (hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestForPermission();
        setContentView(R.layout.activity_main);

        final Button playButton = (Button) findViewById(R.id.play);
        final Button pauseButton = (Button) findViewById(R.id.pause);
        final Button stopButton = (Button) findViewById(R.id.stop);
        final Button skipToNextButton = (Button) findViewById(R.id.skip_to_next);
        final Button skipToPreviousButton = (Button) findViewById(R.id.skip_to_previous);

        callback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                if (state == null)
                    return;
                boolean playing = state.getState() == PlaybackStateCompat.STATE_PLAYING;
                playButton.setEnabled(!playing);
                pauseButton.setEnabled(playing);
                stopButton.setEnabled(playing);
            }
        };

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playerServiceBinder = (PlayerService.PlayerServiceBinder) service;
                try {
                    mediaController = new MediaControllerCompat(MainActivity.this, playerServiceBinder.getMediaSessionToken());
                    mediaController.registerCallback(callback);
                    callback.onPlaybackStateChanged(mediaController.getPlaybackState());
                }
                catch (RemoteException e) {
                    mediaController = null;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                playerServiceBinder = null;
                if (mediaController != null) {
                    mediaController.unregisterCallback(callback);
                    mediaController = null;
                }
            }
        };

        bindService(new Intent(this, PlayerService.class), serviceConnection, BIND_AUTO_CREATE);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null)
                    mediaController.getTransportControls().play();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null)
                    mediaController.getTransportControls().pause();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null)
                    mediaController.getTransportControls().stop();
            }
        });

        skipToNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null)
                    mediaController.getTransportControls().skipToNext();
            }
        });

        skipToPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaController != null)
                    mediaController.getTransportControls().skipToPrevious();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerServiceBinder = null;
        if (mediaController != null) {
            mediaController.unregisterCallback(callback);
            mediaController = null;
        }
        unbindService(serviceConnection);
    }
}
