package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class PreviewPagesActivity extends AppCompatActivity {

    private int gameId;
    private DatabaseHelper database;
    private static int count = 0;
    private ScrollView scrollview;
    private String selectedPage;
    private TextView selectedView;
    private boolean selected;
    private static final int PAGEVIEWWIDTH = 2560/5;
    private DatabaseHelper dbase;

    //an array list of linear layouts to get the current ones that are not full
    private ArrayList<LinearLayout> linearLayouts = new ArrayList();
    //a map to keep track of which pages are in which layouts
    private HashMap<String, LinearLayout> linearLayMap = new HashMap<String, LinearLayout>();
    //each linear layout contains a max of 5 items
    private static final int LAYOUT_SIZE = 5;
    private HashMap<LinearLayout, Integer> linearLayoutSize = new HashMap<LinearLayout, Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_pages);
        dbase = DatabaseHelper.getInstance(this);
        Intent editorIntent = getIntent();
        gameId = editorIntent.getIntExtra("Game_id", -1);
        database = DatabaseHelper.getInstance(this);
        scrollview = (ScrollView) findViewById(R.id.scrollview);
        populateScrollView();
    }

    //creates a new page with the current selected game
    //doesn't explicitly handle scrollview because onCreate method will do that
    public void createNew(View view){
        String pageName = "page" + count;
        count++;
        boolean insertSuccessful = database.addPage(pageName, gameId);
        Intent newIntent = new Intent(this, PageEditorActivity.class);
        newIntent.putExtra("containsItems", false);
        newIntent.putExtra("gameId", gameId);
        startActivity(newIntent);
        //update the scrollview so that changes persist when you return
        scrollview.removeAllViews();
        populateScrollView();
    }

    //opens a preexisting page
    //explicitly updates scrollview with the populate method
    public void deleteSelected(View view) {
        if(selected && !selectedPage.isEmpty()){
            //get the autoincrement id and use that to delete the page shapes
            String cmd = "SELECT * FROM pages WHERE name = '" + selectedPage + "';";
            Cursor cursor = database.db.rawQuery(cmd, null);
            cursor.moveToFirst();
            int pageId = cursor.getInt(2);
            database.deletePage(pageId);

            //set booleans to false
            selected = false;
            selectedPage = "";
            //call populate layout again to repopulate layout after clearing scrollView
            scrollview.removeAllViews();
            populateScrollView();
        }
    }

    //fills the scroll view with the names of the pages
    //OVERWRITTEN TO INSERT THE NEW EDITS
    private void populateScrollView(){
        if(gameId == -1) return;
        String cmd = "SELECT * FROM pages WHERE parent_id = " + gameId + ";";
        Cursor cursor = database.db.rawQuery(cmd, null);
        LinearLayout mainVertical = new LinearLayout(this);
        setProperties(mainVertical, "vertical", PAGEVIEWWIDTH*4, 0);

        //loops through the cursor and populates the appropraite views
        while(cursor.moveToNext()){
            String newPage = cursor.getString(0);
            if(newPage.isEmpty() || newPage == null) break;
            TextView textView = new TextView(this);
            textView.setText(newPage);
            textView.setTextSize(24);
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
            mainVertical.addView(textView);
        }
        scrollview.addView(mainVertical);
    }


    //sets the required properties of the layouts
    public void setProperties(LinearLayout lay, String orient, int width, int height){
        //set orientation
        if(orient.equals("vertical")) lay.setOrientation(LinearLayout.VERTICAL);
        else if(orient.equals("horizontal")) lay.setOrientation(LinearLayout.HORIZONTAL);
        //set the width
        lay.setMinimumHeight(width);
        //set the height
        if(height != 0) lay.setMinimumHeight(height);
    }

    //opens the selected page in the page view
    public void openSelected(View view){
        //Get the list of shapes from the database
        if(selectedPage == null) return;
        String cmd = "SELECT * FROM pages WHERE name = '"+ selectedPage +"';";
        Cursor cursor = dbase.db.rawQuery(cmd,null);
        cursor.moveToFirst();
        int pageId = cursor.getInt(2);

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
}
