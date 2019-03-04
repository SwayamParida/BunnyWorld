package edu.stanford.cs108.bunnyworld;

import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditorActivity extends AppCompatActivity {
    public static final Map<String, BitmapDrawable> stringImgMap = new HashMap<>();
    public static final Map<BitmapDrawable, String> imgStringMap = new HashMap<>();

    private Page page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        initImageMap();
        populateSpinner();
        populateImgScrollView();
    }
    /**
     * Initializes a HashMap that maps the name of an image to the BitmapDrawable associated with it.
     */
    private void initImageMap() {
        String[] imageNames = { "carrot", "carrot2", "death", "duck", "fire", "mystic" };
        BitmapDrawable[] images = new BitmapDrawable[]{
                (BitmapDrawable) getResources().getDrawable(R.drawable.carrot),
                (BitmapDrawable) getResources().getDrawable(R.drawable.carrot2),
                (BitmapDrawable) getResources().getDrawable(R.drawable.death),
                (BitmapDrawable) getResources().getDrawable(R.drawable.duck),
                (BitmapDrawable) getResources().getDrawable(R.drawable.fire),
                (BitmapDrawable) getResources().getDrawable(R.drawable.mystic),
        };
        for (int i = 0; i < images.length; ++i) {
            stringImgMap.put(imageNames[i], images[i]);
            imgStringMap.put(images[i], imageNames[i]);
        }
    }
    /**
     * Populates the spinner with the list of image choices.
     * Reference: https://www.tutorialspoint.com/android/android_spinner_control.htm
     */
    private void populateSpinner() {
        // Create an array adapter using the items in imageNames
        ArrayAdapter<String> imgSpinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(stringImgMap.keySet())
        );
        // Set spinner dropdown layout
        imgSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Set adapter to spinner
        ((Spinner) findViewById(R.id.imgSpinner)).setAdapter(imgSpinnerAdapter);
    }
    /**
     * Populates a HorizontalScrollView with all the preset images available for the user to create a shape out of
     */
    private void populateImgScrollView() {
        LinearLayout horizontalLayout = new LinearLayout(this);
        for (BitmapDrawable image : stringImgMap.values()) {
            ImageView imageView = new ImageView(this);
            imageView.setImageDrawable(image);
            imageView.setOnClickListener(v -> {
                PageView pagePreview = findViewById(R.id.pagePreview);
                BitmapDrawable selectedImage = (BitmapDrawable) ((ImageView) v).getDrawable();
                pagePreview.setSelectedImage(selectedImage);
            });
            horizontalLayout.addView(imageView);
        }
        ((HorizontalScrollView) findViewById(R.id.presetImages)).addView(horizontalLayout);
    }

    /**
     * Event handler for when the "Save" button is clicked.
     * Creates a shape object using the attributes specified by the user.
     * Based on the image and text attributes, determines which subclass of Shape needs to be instantiated.
     */
    public void saveChanges(View view) {
        String name = ((EditText) findViewById(R.id.name)).getText().toString();
        String text = ((EditText) findViewById(R.id.shapeText)).getText().toString();
        boolean visible = ((CheckBox) findViewById(R.id.visible)).isChecked();
        boolean movable = ((CheckBox) findViewById(R.id.movable)).isChecked();
        ImageView pagePreview = findViewById(R.id.pagePreview);

        String imageName = ((Spinner) findViewById(R.id.imgSpinner)).getSelectedItem().toString();
        BitmapDrawable image = stringImgMap.getOrDefault(imageName, null);

        float x = Float.parseFloat(((EditText) findViewById(R.id.rectX)).getText().toString());
        float y = Float.parseFloat(((EditText) findViewById(R.id.rectY)).getText().toString());
        float shapeWidth = Float.parseFloat(((EditText) findViewById(R.id.width)).getText().toString());
        float shapeHeight = Float.parseFloat(((EditText) findViewById(R.id.height)).getText().toString());
        RectF boundingRect = new RectF(x, y, x + shapeWidth, y + shapeHeight);

        Shape shape;
        // When only image is provided
        if (image != null && text.isEmpty())
            shape = new Image(pagePreview, image, boundingRect, visible, movable, name);
        // When text is provided, it takes precedence over a n
        else if (!text.isEmpty())
            shape = new Text(pagePreview, text, boundingRect.left, boundingRect.top, visible, movable, name);
        // When neither image nor text is provided
        else
            shape = new Rectangle(pagePreview, boundingRect, visible, movable, name);

        // Replace selected shape
        Shape selectedShape = getSelectedShape();
        page.deleteShape(selectedShape);
        page.addShape(selectedShape);
    }
}
