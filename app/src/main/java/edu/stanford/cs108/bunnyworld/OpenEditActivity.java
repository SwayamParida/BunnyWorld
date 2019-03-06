package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;

public class OpenEditActivity extends AppCompatActivity {
    //iVars
    private DatabaseHelper dbHelper;
    private String[] fromArray = {"gameName"};
    private int[] toArray = {android.R.id.text1};
    private OpenEditActivity instance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_edit);
        this.instance = this;
        dbHelper = DatabaseHelper.getInstance(this); //Get singleton instance of DBHelper class

        //Enters full screen mode
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE|
                SYSTEM_UI_FLAG_FULLSCREEN|SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setupSpinner();

    }

    //Populates spinner with database game names
    private void setupSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.existingGamesSpinner);
        if (dbHelper.gameExists()) {
            Cursor cursor = dbHelper.db.rawQuery("SELECT * FROM games;", null);
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, fromArray, toArray, 0);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        } else { //Populates spinner with 'no game files' msg if database is empty
            String[] arraySpinner = new String[] {"No game files"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arraySpinner);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

    }

    //Overrides backbutton pressed to ensure onCreate is called on previous activity (e.g. MainActivity)
    @Override
    public void onBackPressed() {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
    }

    //Adds a new game to the database according to what the user types edittext. Checks that gamename is not taken.
    public void createNewGame(View view) {
        EditText editText = (EditText) findViewById(R.id.newGameNameEditor);
        String gameName = editText.getText().toString();
        if (gameName.isEmpty()) {
            Toast.makeText(this, "No name entered.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dbHelper.entryExists(dbHelper.GAMES_TABLE, gameName)) {
            Toast.makeText(this, "A game with that name already exists. Use a different name.", Toast.LENGTH_LONG).show();
            return;
        }
        dbHelper.addGameToTable(gameName);
        Intent intent = new Intent(this, EditPagesActivity.class);
        intent.putExtra("Game_id", dbHelper.getId(dbHelper.GAMES_TABLE, gameName, dbHelper.NO_PARENT));
        startActivity(intent);
    }

    //Adds
    public void openGameFile(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.existingGamesSpinner);
        Cursor gameCursor = (Cursor) spinner.getSelectedItem();
        String gameName = gameCursor.getString(0);
        Intent intent = new Intent(this, EditPagesActivity.class);
        intent.putExtra("Game_id", dbHelper.getId(dbHelper.GAMES_TABLE, gameName, dbHelper.NO_PARENT));
        startActivity(intent);

    }
}
