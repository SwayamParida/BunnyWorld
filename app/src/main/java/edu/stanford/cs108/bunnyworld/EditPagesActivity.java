package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class EditPagesActivity extends AppCompatActivity {

    private int gameId;
    private DatabaseHelper database;
    private static int count = 0;
    private ScrollView scrollview;
    private String selectedPage;
    private boolean selected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pages);
        Intent editorIntent = getIntent();
        gameId = editorIntent.getIntExtra("Game_id", -1);
        database = DatabaseHelper.getInstance(this);
        scrollview = (ScrollView) findViewById(R.id.scrollview);
        populateScrollView();
    }

    //creates a new page with the current selected game
    public void createNew(View view){
        String pageName = "page" + count;
        count++;
        database.addPage(pageName, gameId);
        Intent newIntent = new Intent(this, EditorActivity.class);
        startActivity(newIntent);
    }

    //opens a preexisting page
    public void deleteSelected(View view) {
        if(selected && !selectedPage.isEmpty()){
            //get the autoincrement id and use that to delete the page shapes
            String cmd = "SELECT * FROM pages WHERE name = " + selectedPage + ";";
            Cursor cursor = database.db.rawQuery(cmd, null);
            int pageId = cursor.getInt(2);
            database.deletePage(pageId);
        }
    }

    //fills the scroll view with the names of the pages
    private void populateScrollView(){
        if(gameId == -1) return;
        String cmd = "SELECT * FROM pages WHERE parent_id = " + gameId + ";";
        Cursor cursor = database.db.rawQuery(cmd, null);
        while(cursor.moveToNext()){
            String newPage = cursor.getString(0);
            if(newPage.isEmpty() || newPage == null) break;
            LinearLayout layout = new LinearLayout(this);
            TextView view1 = new TextView(this);
            view1.setText(newPage);
            TextView view2 = new TextView(this);
            view2.setText(newPage + " contains X shapes");
            layout.addView(view1);
            layout.addView(view2);
            //set an onclick listener for this layout
            layout.setOnClickListener(v -> {
                TextView view = (TextView) layout.getChildAt(0);
                selected = true;
                selectedPage = view.getText().toString();
                Toast.makeText(this, selectedPage + " selected", Toast.LENGTH_SHORT).show();
            });
            scrollview.addView(layout);
        }
    }
}
