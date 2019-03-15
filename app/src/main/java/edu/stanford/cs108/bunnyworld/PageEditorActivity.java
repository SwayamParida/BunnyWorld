package edu.stanford.cs108.bunnyworld;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PageEditorActivity extends AppCompatActivity implements BunnyWorldConstants {
    private Page page;
    private CustomPageView pagePreview;
    private EditText nameEditText, textEditText, xEditText, yEditText, wEditText, hEditText;
    private CheckBox visibleCheckBox, movableCheckBox;
    private HorizontalScrollView imgScrollView;
    private Spinner imgSpinner, verbSpinner, modifierSpinner, eventSpinner, actionSpinner;
    private LinearLayout actions, triggers;
    private Set<Action> unsavedActions;
    private boolean ignore = false;

    //array list of text shapes that is retrieved from EditPagesActivity
    private DatabaseHelper dbase;
    private int gameId;
    public Shape clipboard;

    /**
     * Helper method that updates the Spinner to reflect the image clicked by the user
     */
    public static void updateSpinner(Spinner spinner, String name) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        spinner.setSelection(adapter.getPosition(name));
    }
    public static void populateSpinner(Spinner spinner, List<String> list) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                spinner.getContext(),
                android.R.layout.simple_spinner_item,
                list
        );
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
    }
    /**
     * Event handler for when the "Update" button is clicked.
     */
    public void saveChanges(View view) {
        if(!ignore) pagePreview.saveForUndo();

        if (pagePreview.getSelectedShape() != null){
            ignore = true;
            page.deleteShape(pagePreview.getSelectedShape());
            page.addShape(updateShape());
            pagePreview.invalidate();
            pagePreview.setPage(page);
        }
        ignore = false;

    }
    public void addTriggerRow(View view) {
        addScriptRow(triggers, this::addTriggerRow, this::deleteTriggerRow, Arrays.asList(TRIGGER_EVENTS), false);
        repopulateActionSpinners();
    }
    public void addActionRow(View view) {
        // Check necessary for when row is added programmatically from CustomPageView
        if (view != null) {
            Action action = getAction((LinearLayout) view.getParent());
            unsavedActions.add(action);
            repopulateActionSpinners();
        }
        addScriptRow(actions, this::addActionRow, this::deleteActionRow, Arrays.asList(ACTION_VERBS), true);
    }
    public void deleteActionRow(View view) {
        Action action = getAction((LinearLayout) view.getParent());
        unsavedActions.remove(action);
        repopulateActionSpinners();
        deleteScriptRow(view);
    }
    public void deleteTriggerRow(View view) {
        deleteScriptRow(view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_page);

        initComponents();

        //initialize necessary UIs and helpers
        dbase = DatabaseHelper.getInstance(this);
        page = extractIntentData(getIntent());
        unsavedActions = new HashSet<>();

        populateSpinner(imgSpinner, dbase.getResourceNames());
        populateSpinner(verbSpinner, Arrays.asList(ACTION_VERBS));
        populateSpinner(eventSpinner, Arrays.asList(TRIGGER_EVENTS));
        initModifierSpinner(verbSpinner, modifierSpinner);
        populateImgScrollView();
        initPageView();
    }

    /**
     * On Resume, the scrollview on the bottom is updated, just in case
     * any values were added/removed.
     */
    @Override
    public void onResume() {
        super.onResume();
        ((ArrayAdapter) imgSpinner.getAdapter()).notifyDataSetChanged();
        populateImgScrollView();
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
        verbSpinner = findViewById(R.id.verb1);
        modifierSpinner = findViewById(R.id.modifier1);
        eventSpinner = findViewById(R.id.event1);
        actionSpinner = findViewById(R.id.action1);
        actions = findViewById(R.id.actions);
        triggers = findViewById(R.id.triggers);
        imgScrollView = findViewById(R.id.presetImages);
        pagePreview = findViewById(R.id.pagePreview);
    }
    /**
     * Helper method that passes relevant data to PageView
     */
    private void initPageView() {
        pagePreview.setPage(page);
        pagePreview.setPageId(dbase.getId(PAGES_TABLE, page.getName(), gameId));
        //update selected image on the preview
        String imgName = (String)((ArrayAdapter)imgSpinner.getAdapter()).getItem(0);
        Bitmap newBitmap = dbase.getImage(imgName);
        //use the database to get the object
        BitmapDrawable defaultImage = new BitmapDrawable(newBitmap);
        pagePreview.setSelectedImage(defaultImage);
        if(!ignore) pagePreview.saveForUndo();
        pagePreview.invalidate();
    }

    /**
     * Populates a HorizontalScrollView with all the preset images available for the user to create a shape out of
     */
    private void populateImgScrollView() {
        LinearLayout horizontalLayout = new LinearLayout(this);
        //get the corresponding images from the list
        imgScrollView.removeAllViews();
        for (String imgName : dbase.getResourceNames()) {
            ImageView imageView = new ImageView(this);
            imageView.setBackgroundResource(R.color.transparent);
            //get the image from the database and create new drawable
            Bitmap imgBitmap = dbase.getImage(imgName);
            imageView.setImageDrawable(new BitmapDrawable(imgBitmap));
            imageView.setOnClickListener(v -> {
                BitmapDrawable selectedImage = (BitmapDrawable) ((ImageView) v).getDrawable();
                pagePreview.setSelectedImage(selectedImage);
                updateSpinner(imgSpinner, imgName);
            });
            horizontalLayout.addView(imageView);
        }
        imgScrollView.addView(horizontalLayout);
    }
    private void repopulateActionSpinners() {
        for (int triggerRowIndex = 0; triggerRowIndex < triggers.getChildCount(); ++triggerRowIndex) {
            LinearLayout triggerRow = (LinearLayout) triggers.getChildAt(triggerRowIndex);
            Spinner actionSpinner = (Spinner) triggerRow.getChildAt(ACTION_SPINNER);
            populateActionSpinner(actionSpinner);
        }
    }
    private void populateActionSpinner(Spinner actionSpinner) {
        List<String> actionStrings = new ArrayList<>();
        unsavedActions.forEach(action -> actionStrings.add(action.toString()));
        populateSpinner(actionSpinner, actionStrings);
    }
    private void initModifierSpinner(Spinner verbSpinner, Spinner modifierSpinner) {
        verbSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                populateSpinner(modifierSpinner, getModifierValues(ACTION_VERBS[position]));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        modifierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Action action = getAction((LinearLayout) modifierSpinner.getParent());
                unsavedActions.add(action);
                repopulateActionSpinners();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
    private List<String> getModifierValues(String verb) {
        switch (verb) {
            case "goto": return dbase.getGamePageNames(page.getGameID());
            case "play": return Arrays.asList(AUDIO_NAMES);
            case "hide": case "show":
                List<Shape> allShapes = page.listOfShapes;
                List<String> shapeNames = new ArrayList<>();
                allShapes.forEach(shape -> shapeNames.add(shape.getName()));
                return shapeNames;
        }
        return new ArrayList<>();
    }
    private void addScriptRow(LinearLayout parentView, View.OnClickListener addRowListener, View.OnClickListener deleteRowListener, List<String> leftSpinnerValues, boolean verbRow) {
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        LinearLayout scriptRow = new LinearLayout(this);
        scriptRow.setOrientation(LinearLayout.HORIZONTAL);
        scriptRow.setLayoutParams(rowParams);

        Spinner leftSpinner = new Spinner(this);
        populateSpinner(leftSpinner, leftSpinnerValues);
        leftSpinner.setLayoutParams(spinnerParams);
        Spinner rightSpinner = new Spinner(this);
        if (verbRow) initModifierSpinner(leftSpinner, rightSpinner);
        rightSpinner.setLayoutParams(spinnerParams);

        Button addScriptRow = new Button(this);
        addScriptRow.setLayoutParams(buttonParams);
        addScriptRow.setText("+");
        addScriptRow.setOnClickListener(addRowListener);
        Button deleteScriptRow = new Button(this);
        deleteScriptRow.setLayoutParams(buttonParams);
        deleteScriptRow.setText("-");
        deleteScriptRow.setOnClickListener(deleteRowListener);

        scriptRow.addView(leftSpinner);
        scriptRow.addView(rightSpinner);
        scriptRow.addView(addScriptRow);
        scriptRow.addView(deleteScriptRow);

        parentView.addView(scriptRow);
    }
    private void deleteScriptRow(View view) {
        LinearLayout scriptRow = (LinearLayout) view.getParent();
        LinearLayout parentView = (LinearLayout) scriptRow.getParent();
        if (parentView.getChildCount() > 1)
            parentView.removeView(scriptRow);
    }
    private Action getAction(LinearLayout actionRow) {
        Spinner verbSpinner = (Spinner) actionRow.getChildAt(VERB_SPINNER);
        Spinner modifierSpinner = (Spinner) actionRow.getChildAt(MODIFIER_SPINNER);
        String verb = (String) verbSpinner.getSelectedItem();
        String modifier = (String) modifierSpinner.getSelectedItem();
        return Action.parseAction(Action.createActionString(verb, modifier));
    }
    //writes the text-shapes into the ivar arrayList of text shapes above
    private Page extractIntentData(Intent intent){
        gameId = intent.getIntExtra("gameId", -1);
        //create a new page that has the properties of the previous page
        String pageName = intent.getStringExtra("pageName");
        getSupportActionBar().setTitle("BunnyWorld Editor: "+ pageName);

        Page newPage = new Page(pageName, gameId);
        pagePreview.setPage(newPage);
        if(!intent.getBooleanExtra("containsItems", false)){
            return newPage;
        }

        ArrayList<Integer> shapesId = intent.getIntegerArrayListExtra("ShapesArray");
        //instantiate the text-shapes ivar array
        ArrayList<Shape> shapes = new ArrayList<Shape>();
        //populate the shapes list
        for(int id: shapesId){
            ImageShape readShape = dbase.getShape(id, pagePreview);
            ImageShape newShape = new ImageShape(pagePreview, readShape.getBounds(),
                    readShape.getImage(), readShape.getText(), readShape.getResId(), readShape.isVisible(),
                    readShape.isMovable(), readShape.getName());
            shapes.add(newShape);
        }
        newPage.setListOfShapes(shapes);
        return newPage;
    }

    /**
     * Creates a shape object using the attributes specified by the user.
     * Based on the image and text attributes, determines which subclass of Shape needs to be instantiated.
     * @return new Shape using the updated attributes
     */
    private Shape updateShape() {
        Log.d("tag2","Saved for undo in updateShape()");
        if(!ignore) pagePreview.saveForUndo();
        String name = nameEditText.getText().toString();
        String text = textEditText.getText().toString();
        String xEdit = xEditText.getText().toString();
        String yEdit = yEditText.getText().toString();
        String wEdit = wEditText.getText().toString();
        String hEdit = hEditText.getText().toString();
        boolean visible = visibleCheckBox.isChecked();
        boolean movable = movableCheckBox.isChecked();

        if(name.isEmpty() || xEdit.isEmpty() || yEdit.isEmpty() || wEdit.isEmpty() || hEdit.isEmpty())
        {
            Toast.makeText(this, "One or more EditText fields are empty", Toast.LENGTH_SHORT).show();
            return null;
        }


        String imageName = imgSpinner.getSelectedItem().toString();
        Bitmap image = dbase.getImage(imageName);

        float x = Float.parseFloat(xEditText.getText().toString());
        float y = Float.parseFloat(yEditText.getText().toString());
        float width = Float.parseFloat(wEditText.getText().toString());
        float height = Float.parseFloat(hEditText.getText().toString());
        RectF boundingRect = new RectF(x, y, x + width, y + height);

        Script script = createScript();

        Shape shape;
        // When only image is provided
        if (image != null && text.isEmpty()) {
            //get the image id and pass it in
            int imgId = dbase.getId(RESOURCE_TABLE, imageName, NO_PARENT);
            shape = new ImageShape(pagePreview, boundingRect, new BitmapDrawable(image), text, imgId, visible, movable, name);
            shape.setScript(script);
            // When text is provided, it takes precedence over any other object
        } else if (!text.isEmpty()) {
            shape = new TextShape(pagePreview, boundingRect, new BitmapDrawable(image), text, -1, visible, movable, name);
            shape.setScript(script);
            // When neither image nor text is provided
        } else {
            shape = new RectangleShape(pagePreview, boundingRect, -1, visible, movable, name);
            shape.setScript(script);
        }
        //Save copy of page
        return shape;
    }
    private Script createScript() {
        Script script = new Script();
        for (int triggerRowIndex = 0; triggerRowIndex < triggers.getChildCount(); ++triggerRowIndex) {
            LinearLayout triggerRow = (LinearLayout) triggers.getChildAt(triggerRowIndex);
            Spinner eventSpinner = (Spinner) triggerRow.getChildAt(EVENT_SPINNER);
            Spinner actionSpinner = (Spinner) triggerRow.getChildAt(ACTION_SPINNER);

            String event = (String) eventSpinner.getSelectedItem();
            String actionString = (String) actionSpinner.getSelectedItem();
            Action action = Action.parseAction(actionString);

            switch (event) {
                case "onClick": script.addOnClickAction(action); break;
                case "onDrop": script.addOnDropAction(action); break;
                case "onEnter": script.addOnEnterAction(action); break;
            }
        }
        return script;
    }

    //save button method
    public void savePage(View view){
        //call the saveSelectedPage method
        savePageBitmap(pagePreview);
        saveToDatabase();
        pagePreview.setChangesMadeBool(false);
    }

    //undoes an action performed by the user on the screen
    public void undoChange(View view){
        //accesses the array list of actions and simply deletes the last activity
        this.page = pagePreview.undoChange();
//        if(undo) Toast.makeText(this, "Action undo successful", Toast.LENGTH_SHORT).show();
        pagePreview.setChangesMadeBool(false);
    }

    //redo button
    public void redoAction(View view) {
        //accesses the queue and simply adds that object to the arrayList
        boolean redo = pagePreview.redoAction();
        if(!redo) Toast.makeText(this, "Action redo successful", Toast.LENGTH_SHORT).show();
        pagePreview.setChangesMadeBool(false);
    }

    @Override
    public void onBackPressed(){
        if(pagePreview.getChangesMadeBool()){
            AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
            alertBox.setTitle("Page Edit Changes");
            alertBox.setMessage("Would you like to save changes?");
            alertBox.setPositiveButton(android.R.string.yes, (arg0, arg1) -> {
                savePageBitmap(pagePreview);
                saveToDatabase();
                pagePreview.setChangesMadeBool(false);
                            PageEditorActivity.super.onBackPressed();
            });
            //add the no functionality
            alertBox.setNegativeButton(android.R.string.no, (arg0, arg1) -> PageEditorActivity.super.onBackPressed());
            alertBox.create().show();
        }
        else {
            super.onBackPressed();
        }
    }

    //method that saves to the database
    public void saveToDatabase(){
        ArrayList<Shape> shapesList = pagePreview.getPageShapes();
        //saves all the shapes from the array list populated here
        String pageName = page.getName();

        //use the game id to access them
        int pageId = dbase.getId(PAGES_TABLE, pageName, gameId);
        if(pageId != -1){
            dbase.db.execSQL("DELETE FROM shapes WHERE parent_id = " + pageId + ";");
        } else {
            boolean success = dbase.addPage(pageName, page.getPageRender(), gameId);
            if(success) pageId = dbase.getId(PAGES_TABLE, pageName, gameId);
        }

        for(Shape currShape: shapesList){
            //name, parent_id, res_id, x, y, width, height, txtString, scripts, visible, movable
            String name = currShape.getName();
            String script;
            if(currShape.getScript() != null) script = currShape.getScript().toString();
            else script = "";
            String txtString = currShape.getText();
            if(txtString == null) txtString = "";
            int resId = currShape.getResId();
            dbase.addShape(name, pageId, resId, currShape.getX(), currShape.getY(), currShape.getWidth(),
                    currShape.getHeight(), txtString, script, currShape.isMovable(), currShape.isVisible());
        }

        //use the page Id to update the thumbnail
        dbase.changePageThumbnail(pageId, page.getPageRender());
    }

    //get the bitmap of the visible view
    private Bitmap getScreenView(View v){
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true);
        Bitmap newBitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return newBitmap;
    }

    //saves the bitmap to the page
    public void savePageBitmap(View view){
        //Bitmap renderToSave = getBitmapFromView(view);
        Bitmap renderToSave = getScreenView(view);
        page.setPageRender(renderToSave);
    }

    public void addImgRes(View view) {
        Intent intent = new Intent(this, SearchForImageActivity.class);
        startActivity(intent);
    }

    public void copy(View view) {
        if(!ignore) pagePreview.saveForUndo();
        Shape selectedShape = pagePreview.getSelectedShape();
        if (selectedShape != null) {
            clipboard = pagePreview.makeShapeCopy(selectedShape);
        }
        pagePreview.invalidate();
    }

    public void cut(View view) {
        if(!ignore) pagePreview.saveForUndo();
        Shape selectedShape = pagePreview.getSelectedShape();
        if (selectedShape != null) {
            clipboard = pagePreview.makeShapeCopy(selectedShape);
        }
        pagePreview.deleteShape(selectedShape);
        pagePreview.invalidate();
    }

    public void paste(View view) {
        if(!ignore) pagePreview.saveForUndo();
        if (clipboard != null) {
            while (repeatName(clipboard.getName())) {
                clipboard.setName(clipboard.getName()+"_copy");
            }
            Shape toBeAdded;
            toBeAdded = pagePreview.makeShapeCopy(clipboard, clipboard.getName(), 0, 0);

            pagePreview.addShape(toBeAdded);
            pagePreview.selectShape(toBeAdded);
            pagePreview.invalidate();
        }
        else {
            Toast.makeText(this, "Nothing to paste!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean repeatName(String name) {
        for (Shape shape : pagePreview.getPageShapes()) {
            if (name.equals(shape.getName())) {
                return true;
            }
        }
        return false;
    }

    public void deleteShape(View view) {
        if(!ignore) pagePreview.saveForUndo();

        Shape selectedShape = pagePreview.getSelectedShape();
        if (selectedShape != null) {
            pagePreview.deleteShape(selectedShape);
        }
        pagePreview.invalidate();
    }

}