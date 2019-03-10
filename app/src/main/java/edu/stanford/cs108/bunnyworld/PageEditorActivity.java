package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.database.Cursor;
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
import java.util.PriorityQueue;

public class PageEditorActivity extends AppCompatActivity {
    public static final Map<String, BitmapDrawable> stringImgMap = new HashMap<>();
    public static final Map<BitmapDrawable, String> imgStringMap = new HashMap<>();

    private Page page;
    private CustomPageView pagePreview;
    private EditText nameEditText, textEditText, xEditText, yEditText, wEditText, hEditText;
    private CheckBox visibleCheckBox, movableCheckBox;
    private HorizontalScrollView imgScrollView;
    private Spinner imgSpinner;

    //array list of text shapes that is retrieved from PreviewPagesActivity
    private ArrayList<Shape> shapes;
    private DatabaseHelper dbase;
    private int gameId;

    //implementation helpers for undo and redo
    private ArrayList<Shape> undoList;
    private PriorityQueue<Shape> redoList;

    /**
     * Helper method that updates the Spinner to reflect the image clicked by the user
     */
    public static void updateSpinner(Spinner imgSpinner, BitmapDrawable image) {
        String imageName = PageEditorActivity.imgStringMap.get(image);
        ArrayAdapter<String> imgSpinnerAdapter = (ArrayAdapter<String>) imgSpinner.getAdapter();
        imgSpinner.setSelection(imgSpinnerAdapter.getPosition(imageName));
    }
    /**
     * Event handler for when the "Save" button is clicked.
     */
    public void saveChanges(View view) {
        Shape selectedShape = pagePreview.getSelectedShape();
        page.deleteShape(selectedShape);
        page.addShape(updatedShape());
        pagePreview.invalidate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_page);

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
     * Helper method that initializes the relevant views defined in the editor_activity.xml
     * to be referred to later in the code.
     */
    public void initComponents() {
        nameEditText = findViewById(R.id.name);
        textEditText = findViewById(R.id.shapeText);
        xEditText = findViewById(R.id.rectX);
        yEditText = findViewById(R.id.rectY);
        wEditText = findViewById(R.id.width);
        hEditText = findViewById(R.id.height);
        visibleCheckBox = findViewById(R.id.visible);
        movableCheckBox = findViewById(R.id.movable);
        imgSpinner = findViewById(R.id.imgSpinner);
        imgScrollView = findViewById(R.id.presetImages);
        pagePreview = findViewById(R.id.pagePreview1);
        undoList = new ArrayList<Shape>();
        redoList = new PriorityQueue<Shape>();
    }
    /**
     * Helper method that passes relevant data to CustomPageView
     */
    public void initPageView(Page page) {
        if(page == null) {
            page = new Page();
        }
        pagePreview.setPage(page);
        pagePreview.invalidate(); //draw contents of the page
        BitmapDrawable defaultImage = stringImgMap.get(((ArrayAdapter<String>)imgSpinner.getAdapter()).getItem(0));
        pagePreview.setSelectedImage(defaultImage);
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

    //writes the text-shapes into the ivar arrayList of text shapes above
    public Page extractIntentData(Intent intent){
        gameId = intent.getIntExtra("gameId", -1);
        if(!intent.getBooleanExtra("containsItems", false)) return null;
        ArrayList<Integer> shapesId = intent.getIntegerArrayListExtra("ShapesArray");

        //instantiate the text-shapes ivar array
        shapes = new ArrayList<Shape>();
        //populate the shapes list
        for(int id: shapesId){
            TextShape newShape = dbase.getShape(id, pagePreview);
            shapes.add(newShape);
        }

        //create a new page that has the properties of the previous page
        String pageName = intent.getStringExtra("pageName");
        Page newPage = new Page();
        newPage.setName(pageName);
        newPage.setListOfShapes(shapes);
        return newPage;
    }



    /**
     * Creates a shape object using the attributes specified by the user.
     * Based on the image and text attributes, determines which subclass of Shape needs to be instantiated.
     * @return new Shape using the updated attributes
     */
    private Shape updatedShape() {
        String name = nameEditText.getText().toString();
        String text = textEditText.getText().toString();
        boolean visible = visibleCheckBox.isChecked();
        boolean movable = movableCheckBox.isChecked();

        String imageName = imgSpinner.getSelectedItem().toString();
        BitmapDrawable image = stringImgMap.get(imageName);

        float x = Float.parseFloat(xEditText.getText().toString());
        float y = Float.parseFloat(yEditText.getText().toString());
        float width = Float.parseFloat(wEditText.getText().toString());
        float height = Float.parseFloat(hEditText.getText().toString());
        RectF boundingRect = new RectF(x, y, x + width, y + height);

        Shape shape;
        // When only image is provided
        if (image != null && text.isEmpty())
            shape = new ImageShape(pagePreview, boundingRect, image, text, visible, movable, name);
        // When text is provided, it takes precedence over any other object
        else if (!text.isEmpty())
            shape = new TextShape(pagePreview, boundingRect, image, text, visible, movable, name);
        // When neither image nor text is provided
        else
            shape = new RectangleShape(pagePreview, boundingRect, visible, movable, name);

        return shape;
    }

    //save button method
    public void savePage(View view){
        //call the saveSelectedPage method
        ArrayList<Shape> shapesList = pagePreview.getPageShapes();
        //saves all the shapes from the array list populated here
        String pageName = page.getName();

        String cmd = "SELECT * FROM pages WHERE name = '"+ pageName +"';";
        Cursor cursor = dbase.db.rawQuery(cmd, null);
        int pageId = cursor.getInt(2);

        //delete old shapes and re-add new shapes
        dbase.db.execSQL("DELETE FROM shapes WHERE parent_id =" + pageId + ";");
        for(Shape currShape: shapesList){
            //name, parent_id, res_id, x, y, width, height, txtString, scripts, visible, movable
            String name = currShape.getName();
            RectF bounds = currShape.getBounds();
            ArrayList<String> scripts = currShape.getShapeScript();
            String txtString = currShape.getText();
            BitmapDrawable newDrawable = currShape.getImage();
            String drawableName = ""; int res_id = -1;
            if(newDrawable != null) {
                drawableName = imgStringMap.get(newDrawable);
                res_id = getResources().getIdentifier(drawableName, "drawable", getPackageName());
            }
            dbase.addShape(name, pageId, res_id, bounds.left, bounds.top, bounds.width(),
                    bounds.height(), txtString, scripts, currShape.isMovable(), currShape.isVisible());
        }

        //saves the name of the page with it's game id
        dbase.addPage(pageName, gameId);
    }

    //undoes an action performed by the user on the screen
    public void undoChange(View view){
        //accesses the array list of actions and simply deletes the last activity
        pagePreview.undoChange();
    }

    //redo button
    public void redoAction(View view) {
        //accesses the queue and simply adds that object to the arrayList
        pagePreview.redoAction();
    }
}
