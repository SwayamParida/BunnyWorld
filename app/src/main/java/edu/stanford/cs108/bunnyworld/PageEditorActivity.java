package edu.stanford.cs108.bunnyworld;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

public class PageEditorActivity extends AppCompatActivity implements BunnyWorldConstants {
    private Page page;
    private CustomPageView pagePreview;
    private EditText nameEditText, textEditText, xEditText, yEditText, wEditText, hEditText;
    private CheckBox visibleCheckBox, movableCheckBox;
    private HorizontalScrollView imgScrollView;
    private Spinner imgSpinner, verbSpinner, modifierSpinner, eventSpinner, actionSpinner;

    //array list of text shapes that is retrieved from EditPagesActivity
    private DatabaseHelper database;
    private int gameId;
    private boolean savedChanges = false;

    //implementation helpers for undo and redo
    private ArrayList<Shape> undoList;
    private PriorityQueue<Shape> redoList;

    /**
     * Helper method that updates the Spinner to reflect the image clicked by the user
     */
    public static void updateSpinner(Spinner spinner, String text) {
        ArrayAdapter<String> spinnerAdapter = (ArrayAdapter<String>) spinner.getAdapter();
        spinner.setSelection(spinnerAdapter.getPosition(text));
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
        database = DatabaseHelper.getInstance(this);
        initComponents();
        populateImgSpinner();
        populateImgScrollView();
        populateVerbSpinner();
        populateEventSpinner();
        initModifierSpinner();

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
        verbSpinner = findViewById(R.id.verb1);
        modifierSpinner = findViewById(R.id.modifier1);
        eventSpinner = findViewById(R.id.event1);
        actionSpinner = findViewById(R.id.action1);

        undoList = new ArrayList<>();
        redoList = new PriorityQueue<>();
    }
    /**
     * Helper method that passes relevant data to PageView
     */
    private void initPageView() {
        if(page == null) {
            int getLatestCount = database.getLatestCount(gameId);
            page = new Page(null,getLatestCount + 1);
            //add page to the database
            addToDatabase();
        }
        pagePreview.setPage(page);
        pagePreview.invalidate();
        // Set a default image to be selected so that something is drawn when nothing is selected
        String defaultImageName = (((ArrayAdapter<String>)imgSpinner.getAdapter()).getItem(0));
        int defaultImageID = database.getId(RESOURCE_TABLE, defaultImageName, -1);
        pagePreview.setSelectedImageID(defaultImageID);
    }
    /**
     * Populates the spinner with the list of image choices.
     * Reference: https://www.tutorialspoint.com/android/android_spinner_control.htm
     */
    private void populateImgSpinner() {
        // Create an array adapter using the items in imageNames
        ArrayAdapter<String> imgSpinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(/*FIXME: Helper method that gets all image names*/)
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
        for (BitmapDrawable image : /*FIXME: Helper method that gets all images*/) {
            ImageView imageView = new ImageView(this);
            imageView.setImageDrawable(image);
            imageView.setOnClickListener(v -> {
                BitmapDrawable selectedImage = (BitmapDrawable) ((ImageView) v).getDrawable();
                pagePreview.setSelectedImageID(selectedImage);
                updateSpinner(imgSpinner, /*FIXME: Helper method that gets image ID from image*/);
            });
            horizontalLayout.addView(imageView);
        }
        imgScrollView.addView(horizontalLayout);
    }
    private void initModifierSpinner() {
        verbSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (Script.actionVerbs[position]) {
                    case "goto":
                        break;
                    case "play":
                        populateModifierSpinner(audioNames);
                        break;
                    case "hide": case "show":
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
    private void populateVerbSpinner() {
        ArrayAdapter<String> verbSpinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList(Script.actionVerbs)
        );
        verbSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        verbSpinner.setAdapter(verbSpinnerAdapter);
    }
    private void populateEventSpinner() {
        ArrayAdapter<String> eventSpinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList(Script.triggerEvents)
        );
        eventSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        eventSpinner.setAdapter(eventSpinnerAdapter);
    }
    private void populateModifierSpinner(String[] list) {
        ArrayAdapter<String> modSpinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList(list)
        );
        modSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        modifierSpinner.setAdapter(modSpinnerAdapter);
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
            ImageShape newShape = database.getShape(id, pagePreview);
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
        int imageID = database.getId(RESOURCE_TABLE, imageName, -1);
        BitmapDrawable image = new BitmapDrawable(database.getImage(imageID));

        float x = Float.parseFloat(xEditText.getText().toString());
        float y = Float.parseFloat(yEditText.getText().toString());
        float width = Float.parseFloat(wEditText.getText().toString());
        float height = Float.parseFloat(hEditText.getText().toString());
        RectF boundingRect = new RectF(x, y, x + width, y + height);

        Shape shape;
        // When only image is provided
        if (image != null && text.isEmpty())
            shape = new ImageShape(pagePreview, boundingRect, imageID, image, text, visible, movable, name);
        // When text is provided, it takes precedence over any other object
        else if (!text.isEmpty())
            shape = new TextShape(pagePreview, boundingRect, imageID, image, text, visible, movable, name);
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
        Cursor cursor = database.db.rawQuery(cmd, null);
        cursor.moveToFirst();
        int pageId = cursor.getInt(2);

        //delete old shapes and re-add new shapes
        database.db.execSQL("DELETE FROM shapes WHERE parent_id = " + pageId + ";");
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
                // FIXME: imgStringMap needs to be removed and replaced with database lookup idioms
                drawableName = imgStringMap.get(newDrawable);
                //fix this by moving all the maps to the singleton
                //res_id = getResources().getIdentifier(drawableName, "drawable", getPackageName());
                res_id = 0;
            }
            database.addShape(name, pageId, res_id, bounds.left, bounds.top, bounds.width(),
                    bounds.height(), txtString, script, currShape.isMovable(), currShape.isVisible());
        }

        //saves the name of the page with it's game id
        database.addPage(pageName, page.getPageRender(), gameId);
//        dbase.addRendering(pageName, page.getName()) //Kivalu has to do this
    }

    //adds the newly created page to the database
    public void addToDatabase(){
        int getLatestCount = database.getLatestCount(gameId);
        database.addPage(page.getName(), page.getPageRender(), gameId);
    }

    public static Bitmap getBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
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
