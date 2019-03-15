package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Stack;

import static edu.stanford.cs108.bunnyworld.IntroScreenActivity.emulatorHeight;
import static edu.stanford.cs108.bunnyworld.IntroScreenActivity.emulatorWidth;
import static edu.stanford.cs108.bunnyworld.PageEditorActivity.updateSpinner;
import static edu.stanford.cs108.bunnyworld.PlayGameActivity.inventory;

public class CustomPageViewForPlayer extends View implements BunnyWorldConstants{
    private Page page;
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
            }
        }
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
        }

        // When (x1,y1) = (x2,y2), it implies user simply tapped screen
        if (x1 == x2 && y1 == y2){
            selectShape(page.findLastShape(x1, y1));
            //update the spinner
            if(selectedShape != null){
                int id = selectedShape.getResId();
                String name = dbase.getResourceName(id);
            }
        }
        // When (x1,y1) and (x2,y2) differ, it implies that user performed a drag action
        // When no shape is selected, a drag implies user intends to draw a new ImageShape
        else if (selectedShape == null){
            /* Log.d("tag2","Saved for undo in selectedShape == null");

            Log.d("tag1","Drawing new shape");
            RectF boundingRect = createBounds(x1, y1, x2, y2);
            if(boundingRect.left < 0 || boundingRect.right > this.getWidth() ||
                    boundingRect.top < 0 || boundingRect.bottom > this.getHeight()) return true;
            //get the resource Id of the image
            String latestSelected = getLatestSelected();
            int res_id = dbase.getId(RESOURCE_TABLE, latestSelected, -1);
            if(shapeCountNotStarted && pageId != -1){
                shapeCount = getLatestCount();
                shapeCountNotStarted = false;
                shapeCount++;
            } else shapeCount += 1;
            String shapeName = "Shape "+ shapeCount;
            Shape shape = new ImageShape(this, boundingRect, selectedImage, null,
                    res_id, true, true, shapeName);
            shape.setScript(new Script());
            page.addShape(shape);
            selectShape(shape);
            changesMade = true;
            invalidate();*/
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

                if (newY1 > .7 * emulatorHeight) { //put in inventory (bottom 30% of screen)
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
    public void addShape(Shape shape) {
        changesMade = true;
        page.addShape(shape);
    }
    public void deleteShape(Shape shape) {
        changesMade = true;
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
}
