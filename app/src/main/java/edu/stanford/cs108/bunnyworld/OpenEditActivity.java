package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;

public class OpenEditActivity extends AppCompatActivity implements BunnyWorldConstants {
    //iVars
    private DatabaseHelper dbHelper;
    private String[] fromArray = {"name"};
    private int[] toArray = {android.R.id.text1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_edit);
        dbHelper = DatabaseHelper.getInstance(this); //Get singleton instance of DBHelper class

        //Enters full screen mode
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE |
                SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setupSpinner();

    }

    protected void onResume() {
        super.onResume();
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
            String[] arraySpinner = new String[]{"No game files"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arraySpinner);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

    }

    //Adds a new game to the database according to what the user types edittext. Checks that gamename is not taken.
    public void createNewGame(View view) {
        EditText editText = (EditText) findViewById(R.id.newGameNameEditor);
        String gameName = editText.getText().toString();
        if (gameName.isEmpty()) {
            Toast.makeText(this, "No name entered.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dbHelper.entryExists(GAMES_TABLE, gameName)) {
            Toast.makeText(this, "A game with that name already exists. Use a different name.", Toast.LENGTH_LONG).show();
            return;
        }
        dbHelper.addGameToTable(gameName);
        Intent intent = new Intent(this, EditPagesActivity.class);
        intent.putExtra("Game_id", dbHelper.getId(GAMES_TABLE, gameName, NO_PARENT));
        startActivity(intent);
    }

    public void openGameFile(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.existingGamesSpinner);
        String gameName = spinner.getSelectedItem().toString();
        Intent intent = new Intent(this, EditPagesActivity.class);
        intent.putExtra("Game_id", dbHelper.getId(GAMES_TABLE, gameName, NO_PARENT));
        startActivity(intent);
    }

    //deletes the game from the database
    public void deleteGameFile(View view){
        Spinner spinner = (Spinner) findViewById(R.id.existingGamesSpinner);
        String gameName = spinner.getSelectedItem().toString();
        Toast.makeText(this, "Delete Successful", Toast.LENGTH_SHORT);
        if(gameName.isEmpty()) return;
        //delete that from the database and repopulate the spinner
        dbHelper.deleteGame(gameName);
        Toast.makeText(this, "Delete Successful", Toast.LENGTH_SHORT);

        //populate the spinner with the new game list
        String newCmd = "SELECT * FROM games;";
        Cursor cursor = dbHelper.db.rawQuery(newCmd, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
                cursor, fromArray, toArray, 0);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}

