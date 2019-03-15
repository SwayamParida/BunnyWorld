package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.Dimension;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.VideoView;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;

public class IntroScreenActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private VideoView myVideoView;
    private Thread myThread;
    public static int emulatorWidth, emulatorHeight;

    //Ike was here
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screen);

        Display screensize = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screensize.getSize(size);
        emulatorWidth = size.x;
        emulatorHeight = size.y;
    }

    protected void onResume() {
        super.onResume();
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
        mediaPlayer.setOnCompletionListener(completionListener);

        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE|
                SYSTEM_UI_FLAG_FULLSCREEN|SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    public void openEditChooser(View view) {
        mediaPlayer.stop();
        myVideoView.stopPlayback();
        Intent intent = new Intent(this, GameLoaderActivity.class);
        intent.putExtra("playing", false);
        startActivity(intent);
    }

    //Experimental: Testing Bing Image Retrieval
    public void openPlayChooser(View view) {
//        Random random = new Random();
//        int rnum;
//        try {
//            myThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        ImageView imageView = (ImageView) findViewById(R.id.playImageView);
//        ArrayList<Bitmap> images = BingSearch.getImages();
//
//        rnum = random.nextInt(images.size());
//        imageView.setImageBitmap(images.get(rnum));
        mediaPlayer.stop();
        myVideoView.stopPlayback();
        //Intent intent = new Intent(this, SearchForImageActivity.class);
        Intent intent = new Intent(this, GameLoaderActivity.class);  // Game selector
        //intent.putExtra("gameId", 1);
        intent.putExtra("playing", true);
        startActivity(intent);
    }

}
