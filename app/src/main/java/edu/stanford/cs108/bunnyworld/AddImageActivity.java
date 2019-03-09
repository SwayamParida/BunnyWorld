package edu.stanford.cs108.bunnyworld;

import android.graphics.Bitmap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;

public class AddImageActivity extends AppCompatActivity implements BunnyWorldConstants {
    private DatabaseHelper dbHelper;
    private Bitmap bitmap;
    public static Boolean succeeded = true;
    public static String addedImgName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE|
                SYSTEM_UI_FLAG_FULLSCREEN|SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        succeeded = true;
        addedImgName = "";
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        bitmap = SearchForImageActivity.currentImage;
        if (bitmap == null) {
            succeeded = false;
            finish();
        }
        dbHelper = DatabaseHelper.getInstance(AddImageActivity.this);
        ImageView imageView = findViewById(R.id.imgResView);
        imageView.setImageBitmap(bitmap);
    }

    public void addImage(View view) {
        EditText editText = findViewById(R.id.imgResEditor);
        String imgName = editText.getText().toString();
        if (imgName.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();
        if (dbHelper.addResource(imgName, IMAGE, bitmapdata)) {
            succeeded = true;
            addedImgName = imgName;
            finish();
        }
    }
}
