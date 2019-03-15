package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.widget.Toast;


public class PreviewPagesActivity extends AppCompatActivity implements BunnyWorldConstants {

    private int gameId;
    private ScrollView scrollview;
    private String selectedPage;
    private TextView selectedView;
    private boolean selected;
    private DatabaseHelper dbase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_pages);
        dbase = DatabaseHelper.getInstance(this);
        Intent editorIntent = getIntent();
        gameId = editorIntent.getIntExtra("Game_id", -1);
        scrollview = (ScrollView) findViewById(R.id.scrollview);
        populateScrollView();
    }

    /**
     * gets the last member of the page and use parsing to get the count
     */
    public int getShapesCount(){
        int id = dbase.getLatestCount(PAGES_TABLE, gameId);
        if(id == 0) return id;

        //else parse the string to get the actual count
        String cmd = "SELECT * FROM pages WHERE parent_id =" + gameId +";";
        Cursor cursor = dbase.db.rawQuery(cmd, null);
        //cursor.moveToLast();
        int largestId = 0;
        while(cursor.moveToNext()){
            String name = cursor.getString(0);
            String[] myList = name.split(" ");
            int count = 0;
            if (myList.length > 1) {
                count = Integer.parseInt(myList[1]);
            }
            if(count > largestId) largestId = count;
        }
        return largestId;
    }

    //creates a new page with the current selected game
    //doesn't explicitly handle scrollview because onCreate method will do that
    public void createNew(View view){
        int count = getShapesCount() + 1;
        String pageName = "Page " + count;
        Intent newIntent = new Intent(this, PageEditorActivity.class);
        newIntent.putExtra("containsItems", false);
        newIntent.putExtra("pageName", pageName);
        newIntent.putExtra("gameId", gameId);
        startActivity(newIntent);
        //update the scrollview so that changes persist when you return
        scrollview.removeAllViews();
        populateScrollView();
    }

    //opens a preexisting page
    //explicitly updates scrollview with the populate method
    public void deleteSelected(View view) {
        if(selected){
            //get the autoincrement id and use that to delete the page shapes
            //get the id instead
            int pageId = dbase.getId(PAGES_TABLE, selectedPage, gameId);
            dbase.deletePage(pageId);

            //set booleans to false
            selected = false;
            selectedPage = null;
            //call populate layout again to repopulate layout after clearing scrollView
            scrollview.removeAllViews();
            populateScrollView();
        }
    }

    //fills the scroll view with the names of the pages
    //OVERWRITTEN TO INSERT THE NEW EDITS
    //I NEED TO DECOMPOSE BUT I'LL DO IT LATER
    private void populateScrollView(){
        if(gameId == -1) return;
        String cmd = "SELECT * FROM pages WHERE parent_id = " + gameId + ";";
        Cursor cursor = dbase.db.rawQuery(cmd, null);
        LinearLayout mainVertical = new LinearLayout(this);
        mainVertical.setOrientation(LinearLayout.VERTICAL);
        mainVertical.setMinimumWidth(LinearLayout.LayoutParams.MATCH_PARENT);

        //an array list of linear layouts to get the current ones that are not full
        ArrayList<LinearLayout> linearLayouts = new ArrayList();
        HashMap<LinearLayout, Integer> linearLayoutSize = new HashMap<LinearLayout, Integer>();

        //loops through the cursor and populates the appropraite views
        LinearLayout laySize; int size = 0; boolean addedNew = false;
        while(cursor.moveToNext()){
            String newPage = cursor.getString(0);
            if(newPage.isEmpty() || newPage == null) break;
            if(!linearLayouts.isEmpty()){
                //get old layout
                laySize = linearLayouts.get(linearLayouts.size() - 1);
                size = linearLayoutSize.get(laySize);
                if(size > 4) {
                    addedNew = true;
                    laySize = new LinearLayout(this);
                    laySize.setOrientation(LinearLayout.HORIZONTAL);
                    laySize.setMinimumWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                    laySize.setMinimumHeight(500);
                    linearLayoutSize.put(laySize, 1);
                    linearLayouts.add(laySize);
                } else {
                    addedNew = false;
                    size = size + 1;
                    linearLayoutSize.put(laySize, size);
                }
            } else {
                addedNew = true;
                //use the new horizontal layout
                laySize = new LinearLayout(this);
                laySize.setOrientation(LinearLayout.HORIZONTAL);
                laySize.setMinimumWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                laySize.setMinimumHeight(500);
                linearLayoutSize.put(laySize, 1);
                linearLayouts.add(laySize);
            }

            //create a vertical layout
            LinearLayout verticalLay = new LinearLayout(this);
            verticalLay.setOrientation(LinearLayout.VERTICAL);
            verticalLay.setMinimumHeight(500); verticalLay.setMinimumWidth(500);
            //create text view
            TextView textView = new TextView(this);
            textView.setText(newPage);
            textView.setTextSize(24);
            textView.setGravity(Gravity.CENTER);

            //create image view
            ImageView myImage = new ImageView(this);
            //get the bitmap rendering of the page
            int pageId = dbase.getId(PAGES_TABLE, newPage, gameId);
            Bitmap imgBitmap = dbase.getPageRendering(pageId);
            if(imgBitmap != null){
                Bitmap newBitmap = Bitmap.createScaledBitmap(imgBitmap, 300,300, false);
                myImage.setImageBitmap(newBitmap);
            } else //use the placeholder icon
                myImage.setImageResource(R.drawable.edit_icon);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            myImage.setLayoutParams(params);
//            myImage.getLayoutParams().height = 400;
//            myImage.getLayoutParams().width = 400;

            //set onclick listeners for the image and text
            textView.setOnClickListener(v->{
                //set previous selected text to blue
                if(selected){
                    selectedView.setTextColor(Color.GRAY);
                }
                selected = true;
                selectedView = textView;
                selectedPage = textView.getText().toString();
                textView.setTextColor(Color.BLUE);
            });
            myImage.setOnClickListener(v->{
                //set previous selected text to blue
                if(selected){
                    selectedView.setTextColor(Color.GRAY);
                }
                selected = true;
                selectedView = textView;
                selectedPage = textView.getText().toString();
                textView.setTextColor(Color.BLUE);
            });
            verticalLay.addView(myImage);
            verticalLay.addView(textView);

            //add a new vertical layout to this current horizontal layout
            laySize.addView(verticalLay);

            if(addedNew) mainVertical.addView(laySize);
        }
        scrollview.addView(mainVertical);
    }

    //sets the required properties of the layouts
    public void setProperties(LinearLayout lay, String orient, int width, int height){
        //set orientation
        if(orient.equals("vertical")) lay.setOrientation(LinearLayout.VERTICAL);
        else if(orient.equals("horizontal")) lay.setOrientation(LinearLayout.HORIZONTAL);
        //set the width
        lay.setMinimumWidth(width);
        //set the height
        if(height != 0) lay.setMinimumHeight(height);
    }

    //opens the selected page in the page view
    public void openSelected(View view){
        //Get the list of shapes from the database
        if(selectedPage == null) return;
        int pageId = dbase.getId(PAGES_TABLE, selectedPage, gameId);

        //get all the children shapes and write them to the preview and process them there
        String cmd1 = "SELECT * FROM shapes WHERE parent_id = "+ pageId +";";
        Cursor cursor1 = dbase.db.rawQuery(cmd1, null);

        //pass all the shapes to the activity editor and fill the screen
        Intent intent = new Intent(this, PageEditorActivity.class);
        ArrayList<Integer> newArr = new ArrayList<Integer>();
        while(cursor1.moveToNext()){
            //get the shape descriptions and add them to the string array
            int shapeId = cursor1.getInt(11);
            newArr.add(shapeId);
        }

        //pass the array into the intent and move it into the PageEditorActivity
        intent.putIntegerArrayListExtra("ShapesArray", newArr);
        intent.putExtra("pageName", selectedPage);
        intent.putExtra("gameId", gameId);
        intent.putExtra("containsItems", true);
        startActivity(intent);
    }

    //on resume method
    @Override
    public void onResume(){
        super.onResume();
        scrollview.removeAllViews();
        populateScrollView();
    }
}
