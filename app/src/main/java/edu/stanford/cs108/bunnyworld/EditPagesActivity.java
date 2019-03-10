package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EditPagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pages);


        Intent intent = getIntent();
        long id = intent.getLongExtra("Game_id", 0);

//        DatabaseHelper helper = DatabaseHelper.getInstance();

    }
}
