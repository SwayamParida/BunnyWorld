package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;

public class PlayGameActivity extends AppCompatActivity implements BunnyWorldConstants {
    private Page page;
    public static CustomPageViewForPlayer playerPageView;
    public static InventoryView inventory;

    //array list of text shapes that is retrieved from EditPagesActivity
    public DatabaseHelper dbase;
    private int gameId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE|
                SYSTEM_UI_FLAG_FULLSCREEN|SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

        playerPageView = findViewById(R.id.playerPageView);
        inventory = findViewById(R.id.inventory);

        dbase = DatabaseHelper.getInstance(this);
        Log.d("debug", "step 1 success");
        page = extractIntentData(getIntent());
        initPageView();
    }

    public void showToastMethod(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private Page extractIntentData(Intent intent){
        gameId = intent.getIntExtra("Game_id", -1);
        //FOR NOW, ASSUME FIRST PAGE IN DATABASE IS STARTING PAGE
        ArrayList<String> pageNames = dbase.getGamePageNames(gameId);
        if (pageNames.size() == 0) {
            Log.d("debug", "error: no pages read from game");
            return new Page("New Page", gameId);
        }
        String pageName = pageNames.get(0);
        Log.d("debug pageName", pageName);

        Page newPage = new Page(pageName, gameId);
        int pageId = dbase.getId(PAGES_TABLE, pageName, gameId);
        Log.d("debug pageId", Integer.toString(pageId));

        String cmd1 = "SELECT * FROM shapes WHERE parent_id = "+ pageId +";";
        Cursor cursor1 = dbase.db.rawQuery(cmd1, null);

        ArrayList<Integer> shapesId = new ArrayList<Integer>();
        while(cursor1.moveToNext()){
            //get the shape descriptions and add them to the string array
            int shapeId = cursor1.getInt(11);
            shapesId.add(shapeId);
            Log.d("debug", "adding" + Integer.toString(shapeId));
        }

        Log.d("debug", shapesId.toString());

        //instantiate the text-shapes ivar array
        ArrayList<Shape> shapes = new ArrayList<Shape>();


        for(int id: shapesId){
            ImageShape readShape = dbase.getShape(id, playerPageView);
            ImageShape newShape = new ImageShape(playerPageView, readShape.getBounds(),
                    readShape.getImage(), readShape.getText(), readShape.getResId(), readShape.isVisible(),
                    readShape.isMovable(), readShape.getName());
            Log.d("debug width", Float.toString(newShape.getWidth()));
            Log.d("debug height", Float.toString(newShape.getHeight()));
            shapes.add(newShape);
        }
        newPage.setListOfShapes(shapes);
        Log.d("debug", shapes.toString());
        return newPage;
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, IntroScreenActivity.class);
        startActivity(intent);
    }

    private void initPageView() {
        playerPageView.setPage(page);
        playerPageView.setPageId(dbase.getId(PAGES_TABLE, page.getName(), gameId));
        Log.d("debug", Integer.toString(playerPageView.getWidth()));

        playerPageView.invalidate();
    }


    // newArr, pageName, gameId, containsItems
}