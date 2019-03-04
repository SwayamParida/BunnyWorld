package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;

public class OpenEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_edit);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE|
                SYSTEM_UI_FLAG_FULLSCREEN|SYSTEM_UI_FLAG_HIDE_NAVIGATION);


    }

    @Override
    public void onBackPressed() {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
    }

    public void createNewGame(View view) {
        EditText editText = (EditText) findViewById(R.id.newGameNameEditor);
        String gameName = editText.getText().toString();
        if (gameName.isEmpty()) {
            Toast.makeText(this, "No name entered.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (gameExists(gameName)) {
            Toast.makeText(this, "A game with that name already exists. Use a different name.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, EditPagesActivity.class);
        startActivity(intent);
    }

    private boolean gameExists(String gameName) {
        //IMPLEMENT LOGIC TO CHECK IF GAME EXISTS WITHIN SQL DATABASE
        return false;
    }
}
