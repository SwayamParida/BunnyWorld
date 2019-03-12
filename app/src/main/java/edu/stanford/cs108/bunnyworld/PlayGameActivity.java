package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import static edu.stanford.cs108.bunnyworld.PageEditorActivity.updateSpinner;

public class PlayGameActivity extends AppCompatActivity {

    public static final Map<String, BitmapDrawable> stringImgMap = new HashMap<>();
    public static final Map<BitmapDrawable, String> imgStringMap = new HashMap<>();
    private Spinner imgSpinner;
    private HorizontalScrollView imgScrollView;
    private CustomPageView pagePreview;
    private ArrayList<Shape> shapes;
    private DatabaseHelper dbase;
    private int gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

        //initialize necessary UIs and helpers
        dbase = DatabaseHelper.getInstance(this);
        initComponents();
        initImageMap();
        populateSpinner();
        populateImgScrollView();

        //access the intents and use that to fill the page
        Intent intent = getIntent();
        Page extractedPage = extractIntentData(intent);
        initPageView(extractedPage);
    }

    /**
     * Initializes a HashMap that maps the name of an image to the BitmapDrawable associated with it.
     */
    public void initImageMap() {
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
    public void populateSpinner() {
        // Create an array adapter using the items in imageNames
        ArrayAdapter<String> imgSpinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(stringImgMap.keySet())
        );
        // Set spinner dropdown layout
        imgSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Set adapter to spinner
        imgSpinner.setAdapter(imgSpinnerAdapter);
    }

    /**
     * Populates a HorizontalScrollView with all the preset images available for the user to create a shape out of
     */
    public void populateImgScrollView() {
        LinearLayout horizontalLayout = new LinearLayout(this);
        for (BitmapDrawable image : stringImgMap.values()) {
            ImageView imageView = new ImageView(this);
            imageView.setImageDrawable(image);
            imageView.setOnClickListener(v -> {
                BitmapDrawable selectedImage = (BitmapDrawable) ((ImageView) v).getDrawable();
                pagePreview.setSelectedImage(selectedImage);
                updateSpinner(imgSpinner, selectedImage);
            });
            horizontalLayout.addView(imageView);
        }
        imgScrollView.addView(horizontalLayout);
    }

    /**
     * Helper method that initializes the relevant views defined in the editor_activity.xml
     * to be referred to later in the code.
     */
    public void initComponents() {
        imgScrollView = findViewById(R.id.inventory);
        pagePreview = findViewById(R.id.pagePreview1);
        imgSpinner = findViewById(R.id.imgSpinner1);
    }

    //writes the text-shapes into the ivar arrayList of text shapes above
    public Page extractIntentData(Intent intent){
        gameId = intent.getIntExtra("gameId", -1);
        if(!intent.getBooleanExtra("containsItems", false)) return null;
        ArrayList<Integer> shapesId = intent.getIntegerArrayListExtra("ShapesArray");

        //instantiate the text-shapes ivar array
        shapes = new ArrayList<Shape>();
        //populate the shapes list
        for(int id: shapesId){
            ImageShape newShape = dbase.getShape(id, pagePreview);
            shapes.add(newShape);
        }

        //create a new page that has the properties of the previous page
        String pageName = intent.getStringExtra("pageName");
        Page newPage = new Page(pageName, -1);
        newPage.setName(pageName);
        newPage.setListOfShapes(shapes);
        return newPage;
    }

    /**
     * Helper method that passes relevant data to CustomPageView
     */
    public void initPageView(Page page) {
        pagePreview.setPage(page);
        pagePreview.invalidate(); //draw contents of the page
        BitmapDrawable defaultImage = stringImgMap.get(((ArrayAdapter<String>)imgSpinner.getAdapter()).getItem(0));
        pagePreview.setSelectedImage(defaultImage);
    }
}
