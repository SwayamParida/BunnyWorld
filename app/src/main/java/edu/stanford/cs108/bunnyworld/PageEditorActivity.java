package edu.stanford.cs108.bunnyworld;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class PageEditorActivity extends AppCompatActivity {
    public static final Map<String, BitmapDrawable> stringImgMap = new HashMap<>();
    public static final Map<BitmapDrawable, String> imgStringMap = new HashMap<>();

    private Page page;
    private CustomPageView pagePreview;
    private EditText nameEditText, textEditText, xEditText, yEditText, wEditText, hEditText;
    private CheckBox visibleCheckBox, movableCheckBox;
    private HorizontalScrollView imgScrollView;
    private Spinner imgSpinner;

    //array list of text shapes that is retrieved from EditPagesActivity
    private DatabaseHelper dbase;
    private int gameId;
    private boolean savedChanges = false;

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
        if (selectedShape != null) {
            page.deleteShape(selectedShape);
            page.addShape(updatedShape());
            pagePreview.invalidate();
        }
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
        page = extractIntentData(intent);
        initPageView();
    }

    /**
     * Helper method that initializes the relevant views defined in the editor_activity.xml
     * to be referred to later in the code.
     */
    private void initComponents() {
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
        pagePreview = findViewById(R.id.pagePreview);
        undoList = new ArrayList<Shape>();
        redoList = new PriorityQueue<Shape>();
    }
    /**
     * Helper method that passes relevant data to PageView
     */
    private void initPageView() {
        if(page == null) {
            int getLatestCount = dbase.getLatestCount(gameId);
            page = new Page(null,getLatestCount + 1);
            //add page to the database
            addToDatabase();
        }
        pagePreview.setPage(page);
        pagePreview.invalidate(); //draw contents of the page
        BitmapDrawable defaultImage = stringImgMap.get(((ArrayAdapter<String>)imgSpinner.getAdapter()).getItem(0));
        pagePreview.setSelectedImage(defaultImage);
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
        imgSpinner.setAdapter(imgSpinnerAdapter);
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
                BitmapDrawable selectedImage = (BitmapDrawable) ((ImageView) v).getDrawable();
                pagePreview.setSelectedImage(selectedImage);
                updateSpinner(imgSpinner, selectedImage);
            });
            horizontalLayout.addView(imageView);
        }
        imgScrollView.addView(horizontalLayout);
    }

    //writes the text-shapes into the ivar arrayList of text shapes above
    private Page extractIntentData(Intent intent){
        gameId = intent.getIntExtra("gameId", -1);
        if(!intent.getBooleanExtra("containsItems", false)) return null;
        ArrayList<Integer> shapesId = intent.getIntegerArrayListExtra("ShapesArray");

        //instantiate the text-shapes ivar array
        ArrayList<Shape> shapes = new ArrayList<Shape>();
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
        saveBitmapToDataBase(view);
        saveToDatabase();
        savedChanges = true;
    }

    //undoes an action performed by the user on the screen
    public void undoChange(View view){
        //accesses the array list of actions and simply deletes the last activity
        boolean undo = pagePreview.undoChange();
        if(!undo) Toast.makeText(this, "Action undo successful", Toast.LENGTH_SHORT).show();
        savedChanges = false;
    }

    //redo button
    public void redoAction(View view) {
        //accesses the queue and simply adds that object to the arrayList
        boolean redo = pagePreview.redoAction();
        if(!redo) Toast.makeText(this, "Action redo successful", Toast.LENGTH_SHORT).show();
        savedChanges = false;
    }

    //on back pressed update the database by simply calling the save method
    //---FIXED
    @Override
    public void onBackPressed(){
        if(!savedChanges){
            AlertDialog.Builder alertBox = new AlertDialog.Builder(this)
                    .setTitle("Page Edit Changes")
                    .setMessage("Would you like to save changes?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            saveToDatabase();
                            Toast.makeText(getApplicationContext(), "Changes saved", Toast.LENGTH_SHORT).show();
                            PageEditorActivity.super.onBackPressed();
                        }
                    });
            //add the no functionality
            alertBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    PageEditorActivity.super.onBackPressed();
                }
            }).create().show();
        }else super.onBackPressed();
    }

    //method that saves to the database
    public void saveToDatabase(){
        ArrayList<Shape> shapesList = pagePreview.getPageShapes();
        //saves all the shapes from the array list populated here
        String pageName = page.getName();

        String cmd = "SELECT * FROM pages WHERE name = '"+ pageName +"';";
        Cursor cursor = dbase.db.rawQuery(cmd, null);
        cursor.moveToFirst();
        int pageId = cursor.getInt(2);

        //delete old shapes and re-add new shapes
        dbase.db.execSQL("DELETE FROM shapes WHERE parent_id = " + pageId + ";");
        for(Shape currShape: shapesList){
            //name, parent_id, res_id, x, y, width, height, txtString, scripts, visible, movable
            String name = currShape.getName();
            RectF bounds = currShape.getBounds();
            String script;
            if(currShape.getScript() != null) script = currShape.getScript().toString();
            else script = "";
            String txtString = currShape.getText();
            BitmapDrawable newDrawable = currShape.getImage();
            String drawableName = ""; int res_id = -1;
            if(newDrawable != null) {
                drawableName = imgStringMap.get(newDrawable);
                //fix this by moving all the maps to the singleton
                //res_id = getResources().getIdentifier(drawableName, "drawable", getPackageName());
                res_id = 0;
            }
            dbase.addShape(name, pageId, res_id, bounds.left, bounds.top, bounds.width(),
                    bounds.height(), txtString, script, currShape.isMovable(), currShape.isVisible());
        }

        //saves the name of the page with it's game id
        dbase.addPage(pageName, page.getPageRender(), gameId);
//        dbase.addRendering(pageName, page.getName()) //Kivalu has to do this
    }

    //adds the newly created page to the database
    public void addToDatabase(){
        int getLatestCount = dbase.getLatestCount(gameId);
        dbase.addPage(page.getName(), page.getPageRender(), gameId);
    }

    public static Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);
        return bitmap;
    }

    public void saveBitmapToDataBase(View view){
        Bitmap renderToSave = getBitmapFromView(view);
        page.setPageRender(renderToSave);

    }

}
