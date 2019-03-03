package edu.stanford.cs108.bunnyworld;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.Map;

public class EditorActivity extends AppCompatActivity {
    private Map<String, BitmapDrawable> imageMap = new HashMap<>();
    private String[] imageNames = {
            "carrot", "carrot2", "death", "duck", "fire", "mystic"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        initImages();
    }

    private void initImages() {
        BitmapDrawable images[] = {
                (BitmapDrawable) getResources().getDrawable(R.drawable.carrot),
                (BitmapDrawable) getResources().getDrawable(R.drawable.carrot2),
                (BitmapDrawable) getResources().getDrawable(R.drawable.death),
                (BitmapDrawable) getResources().getDrawable(R.drawable.duck),
                (BitmapDrawable) getResources().getDrawable(R.drawable.fire),
                (BitmapDrawable) getResources().getDrawable(R.drawable.mystic),
        };
        for (String imageName : imageNames) {

        }
    }

    public void onClick(View view) {
        String name = ((EditText) findViewById(R.id.name)).getText().toString();
        String text = ((EditText) findViewById(R.id.shapeText)).getText().toString();
        boolean visible = ((CheckBox) findViewById(R.id.visible)).isChecked();
        boolean movable = ((CheckBox) findViewById(R.id.movable)).isChecked();

        String imageName = ((Spinner) findViewById(R.id.imgSpinner)).getSelectedItem().toString();
    }
}
