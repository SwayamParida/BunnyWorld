package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.Arrays;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private VideoView myVideoView;

    //Ike was here
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();
    }

    public void setup() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        //Start background music
        mediaPlayer = MediaPlayer.create(this, R.raw.intro_music);
        mediaPlayer.start();

        //Start background video
        myVideoView = (VideoView) findViewById(R.id.launchVideoView);
        myVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bunny_video));
        myVideoView.start();
        MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() { //Loops video

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();

            }
        };

        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE|
                SYSTEM_UI_FLAG_FULLSCREEN|SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public void openEditChooser(View view) {
        mediaPlayer.stop();
        myVideoView.stopPlayback();
        Intent intent = new Intent(this, OpenEditActivity.class);
        startActivity(intent);
    }

}
