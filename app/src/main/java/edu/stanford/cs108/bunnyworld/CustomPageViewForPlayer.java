package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

import static edu.stanford.cs108.bunnyworld.IntroScreenActivity.emulatorHeight;
import static edu.stanford.cs108.bunnyworld.IntroScreenActivity.emulatorWidth;
import static edu.stanford.cs108.bunnyworld.PageEditorActivity.updateSpinner;
import static edu.stanford.cs108.bunnyworld.PlayGameActivity.inventory;

public class CustomPageViewForPlayer extends View implements BunnyWorldConstants{
    public static Page page;
    private int pageId = -1;
    private DatabaseHelper dbase = DatabaseHelper.getInstance(getContext());
    private BitmapDrawable selectedImage;
    private Shape selectedShape;
    private Spinner imgSpinner;
    private boolean changesMade = false;
    // Co-ordinates of user touches - populated in onTouchEvent()
    private boolean shapeCountNotStarted = true;
    private float x1, x2, y1, y2;
    private float xOffset, yOffset;
    private boolean firstDraw = true;
    private ArrayList<Shape> shapesToHighlight = new ArrayList<Shape>();

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    private int gameId;

    //get the current number of shapes in the folder
    private int shapeCount = getLatestCount();

    //implementation helpers for undo and redo
//    private ArrayList<Shape> undoList = new ArrayList<Shape>();
//    private PriorityQueue<Shape> redoList = new PriorityQueue<Shape>();


    public CustomPageViewForPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("width", Integer.toString(getWidth()));
        xOffset = 0;
        yOffset = 0;
    }

    public void setSelectedImage(BitmapDrawable selectedImage) {
        this.selectedImage = selectedImage;
    }
    public Shape getSelectedShape() {
        return selectedShape;
    }
    public void setPage(Page other) {
        page = other;
    }
    @Override
    public void onDraw(Canvas canvas) {
        for (Shape shape : page.getListOfShapes()) {
            if (shape.isVisible()) {
                Log.d("list", shape.toString());
                shape.draw(canvas);
                Log.d("xyz", "Border of " + shape.getName() + " is " + shape.greenBorder);
                if (shape.greenBorder) {
                    Log.d("xyz", "trying to draw green border");
                    canvas.drawRect(shape.getBounds().left - 5, shape.getBounds().top - 5, shape.getBounds().right + 5, shape.getBounds().bottom + 5, Shape.greenPaint);
                }
            }
            if (firstDraw) {
                if (shape.getScript() != null && !shape.getScript().getOnEnterActions().isEmpty()) {
                    checkForScriptsEnter(shape);
                }
            }
        }
        firstDraw = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("width start touch", Integer.toString(getWidth()));
        Log.d("width start touch", Integer.toString(getHeight()));
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();

                // On Drop Event
                for (Shape targetShape : shapesToHighlight) {
                    Log.d("abc", "Target Shape: " + targetShape.getBounds().toString());
                    Log.d("abc", x2 + ", " + y2);
                    if (targetShape.getBounds().contains(x2, y2)) {
                        Log.d("abc", selectedShape.toString() + " triggered " + targetShape.toString());
                        checkForScriptsOnDrop(targetShape, selectedShape);
                    }
                }

                unHighlight();
        }

        // When (x1,y1) = (x2,y2), it implies user simply tapped screen
        if (x1 == x2 && y1 == y2){
            selectShape(page.findLastShape(x1, y1));
            if (selectedShape != null){
                checkForScripts(selectedShape);
                highlightAllGreen(selectedShape);
            }
        }


        // When (x1,y1) and (x2,y2) differ, it implies that user performed a drag action
        // When no shape is selected, a drag implies user intends to draw a new ImageShape
        else if (selectedShape == null){

        }
        // When a shape is selected, a drag implies user intends to move the selected shape
        else {
            if (selectedShape.isMovable() && selectedShape.isVisible()) {
                Log.d("width", Integer.toString(getWidth()));
                float newX = selectedShape.getX() + (x2 - x1);
                float newX1 = newX + selectedShape.getWidth();
                float newY = selectedShape.getY() + (y2 - y1);
                float newY1 = newY + selectedShape.getHeight();
                //check to see if the image is in the bounds of the preview else don't make changes
                if (newX < 0)  {
                    newX1 += (Math.abs(newX));
                    newX = 0;
                }
                if (newX1 >= emulatorWidth) {
                    newX -= (Math.abs(newX1 - (emulatorWidth - 1)));
                    newX1 = emulatorWidth - 1;
                }
                if (newY < 0) {
                    newY1 += (Math.abs(newY));
                    newY = 0;
                }
                if (newY1 >= emulatorHeight) {
                    newY -= (Math.abs(newY1 - (emulatorHeight - 1)));
                    newY1 = emulatorHeight - 1;
                }

                RectF newBounds = new RectF(newX, newY, newX1, newY1);
                selectedShape.setBounds(newBounds);
                Shape shape = new ImageShape(this, newBounds, selectedShape.getImage(), selectedShape.getText(),
                        selectedShape.getResId(), selectedShape.isVisible(), selectedShape.isMovable(), selectedShape.getName());

                if (newY1 > .75 * emulatorHeight) { //put in inventory (bottom 25% of screen)
                    inventory.addToInventory(shape);
                    Log.d("adding to inventory", inventory.inventoryItems.toString());
                }
                else {
                    page.addShape(shape);
                    selectShape(shape);
                }
                page.deleteShape(selectedShape);
                invalidate();
            }
        }
        invalidate();

        return true;
    }




    /**
     * Helper method that handles all the steps associated with shape selection and deselection
     * @param toSelect When not-null, this Shape is selected. When null, the current selection is cleared.
     */
    public void selectShape(Shape toSelect) {
        if (selectedShape != null) {
            selectedShape.setSelected(false);
        }
        if (toSelect != null)
            toSelect.setSelected(true);
        selectedShape = toSelect;
        invalidate();
    }

    public Shape makeShapeCopy(Shape shape) {
        return new ImageShape(this, shape.getBounds(), shape.getImage(), shape.getText(),
                shape.getResId(), shape.isVisible(), shape.isMovable(), shape.getName());
    }

    // Overloading
    public Shape makeShapeCopy(Shape shape, String shapeName, float x, float y) {
        RectF newBounds = new RectF(x, y, shape.getWidth(), shape.getHeight());
        return new ImageShape(this, newBounds, shape.getImage(), shape.getText(),
                shape.getResId(), shape.isVisible(), shape.isMovable(), shapeName);
    }


    /**
     * Helper method that creates a RectF object, enforcing that left <= right and top <= bottom.
     * @param x1 One of the horizontal components
     * @param y1 One of the vertical components
     * @param x2 One of the horizontal components
     * @param y2 One of the vertical components
     * @return Validated RectF object
     */
    private RectF createBounds(float x1, float y1, float x2, float y2) {
        RectF boundingRect = new RectF();
        boundingRect.left = Math.min(x1, x2);
        boundingRect.right = Math.max(x1, x2);
        boundingRect.top = Math.min(y1, y2);
        boundingRect.bottom = Math.max(y1, y2);
        return boundingRect;
    }

    public ArrayList<Shape> getPageShapes(){ return page.getListOfShapes(); }

    //getters and setters for changes made boolean
    public boolean getChangesMadeBool(){return changesMade;}
    public void setChangesMadeBool(boolean bool){changesMade = bool;}

    //getters and setters for the pageId
    public void setPageId(int pageId){this.pageId = pageId;}
    public void addShape(Shape other) {
        Shape shape = new ImageShape(this, other.getBounds(), other.getImage(), other.getText(),
                other.getResId(), other.isVisible(), other.isMovable(), other.getName());
        page.addShape(shape);
        invalidate();
    }
    public void deleteShape(Shape shape) {
        page.deleteShape(shape);
    }

    //gets the latest selected and parses the name correctly
    public int getLatestCount(){
        if(pageId == -1) return 0;

        //else parse the string to get the actual count
        String cmd = "SELECT * FROM shapes WHERE parent_id =" + pageId +";";
        Cursor cursor = dbase.db.rawQuery(cmd, null);
        cursor.moveToLast();
        String name = cursor.getString(0);
        String[] myList = name.split(" ");
        int count = 0;
        if (myList.length > 1) {
            count = Integer.parseInt(myList[1]);
        }
        return count;
    }

    private void goTo(String pageName) {
        Log.d("anmol", "Trying to go to: " + pageName);
        int goToPageId = dbase.getId(PAGES_TABLE, pageName, gameId);
        Page nextPage = new Page(pageName, gameId);
        ArrayList<Shape> pageShapes = dbase.getPageShapes(goToPageId, this);
        Log.d("anmol", "pageShapes: " + pageShapes.size());

        nextPage.listOfShapes = (ArrayList<Shape>)pageShapes.clone();
        nextPage.pageID = goToPageId;
        this.page = nextPage;

        Log.d("anmol", "Next Page Shapes: " + nextPage.listOfShapes.size());


        invalidate();
        for (Shape shape : page.listOfShapes) {
            checkForScriptsEnter(shape);
        }
    }

    private void play(String soundName) {
        int soundId = dbase.getId(RESOURCE_TABLE, soundName, NO_PARENT);
        File mediafile = dbase.getAudioFile(soundId);
        Log.d("anmol", "Trying to play: " + mediafile.getName() + " at: " + mediafile.getAbsolutePath() );

        //create a new media player
        MediaPlayer mPlayer = new MediaPlayer();
//obtain the source
        try {
            mPlayer.setDataSource(mediafile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
//prepare the player
        try {
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
//now start the player to begin playing the audio
        mPlayer.start();

//

//        try {
//            MediaPlayer mp;
//        mp.setDataSource(mediafile.getAbsolutePath());
//            mp=MediaPlayer.create("f", mediafile.getAbsolutePath());
//            mp.start();
//
////            mp.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void highlightAllGreen(Shape shapeBeingDragged){
        Log.d("xyz", "Highlight green method called!");
        if(shapeBeingDragged == null){
            Log.d("xyz", "shapeBeingDraggedIsNull");
        }
        for(Shape shp : page.getListOfShapes()){
            if(shp == null) {
                Log.d("xyz", "Shapes in page are null");
                continue;
            }
            Script shapeScript = shp.getScript();
            Log.d("xyz", shapeScript.toString());
            Map<String, List<Action>> actionsList = shapeScript.getOnDropMap();
            for(String shapeName : actionsList.keySet()){
                if(shapeName.equals(shapeBeingDragged.getName())){
                    //Add to list of shapes to make green
                    shp.greenBorder = true;
                    Log.d("xyz", "Set green border of " + shp.getName() + " to true");
                    shapesToHighlight.add(shp);
                    invalidate();
                }
            }
        }
    }

    private void unHighlight(){
        for(Shape shp : shapesToHighlight){
            //remove green border
            shp.greenBorder = false;
        }
        invalidate();
        shapesToHighlight.clear();
    }

    private void hide(String shapeName) {
        for (Shape curr: page.listOfShapes) {
            if (curr.getName().equals(shapeName)) {
                curr.setVisible(false);
                break;
            }
        }
        invalidate();
    }

    private void show(String shapeName) {
        for (Shape curr: page.listOfShapes) {
            if (curr.getName().equals(shapeName)) {
                curr.setVisible(true);
                break;
            }
        }
        invalidate();
    }

    private void checkForScripts(Shape shape) {
        if(shape.isVisible() == false) return;
        Script scr = shape.getScript();
        if (scr != null) {
            for (Action action : scr.getOnClickActions()) {
                switch (action.getVerb()) {
                    case "goto":
                        goTo(action.getModifier());
                        break;
                    case "play":
                        play(action.getModifier());
                        break;
                    case "hide":
                        hide(action.getModifier());
                        break;
                    case "show":
                        show(action.getModifier());
                        break;
                }
            }
        }
    }

    private void checkForScriptsEnter(Shape shape) {
        Script scr = shape.getScript();
        if (scr != null) {
            for (Action action : scr.getOnEnterActions()) {
                switch (action.getVerb()) {
                    case "goto":
                        goTo(action.getModifier());
                        break;
                    case "play":
                        play(action.getModifier());
                        break;
                    case "hide":
                        hide(action.getModifier());
                        break;
                    case "show":
                        show(action.getModifier());
                        break;
                }
            }
        }
    }



    private void checkForScriptsOnDrop(Shape targetShape, Shape triggerShape) {
        Script targetScript = targetShape.getScript();

        Map<String, List<Action>> mapOfAllActions = targetScript.getOnDropMap();
        List<Action> actionsListz = mapOfAllActions.get(triggerShape.getName());

        Log.d("abc",actionsListz.toString());


        for (Action action : actionsListz) {
            switch (action.getVerb()) {
                case "goto":
                    goTo(action.getModifier());
                    break;
                case "play":
                    play(action.getModifier());
                    break;
                case "hide":
                    hide(action.getModifier());
                    break;
                case "show":
                    show(action.getModifier());
                    break;
            }
        }
    }
}



