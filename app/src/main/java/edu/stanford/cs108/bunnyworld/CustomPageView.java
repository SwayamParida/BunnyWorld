package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
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
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Stack;

import static edu.stanford.cs108.bunnyworld.PageEditorActivity.updateSpinner;

public class CustomPageView extends View implements BunnyWorldConstants{
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

    //get the current number of shapes in the folder
    private int shapeCount = getLatestCount();

    private Stack<Page> stackOfPages;

    public boolean isRectModeEnabled() {
        return rectModeEnabled;
    }

    public void setRectModeEnabled(boolean rectModeEnabled) {
        this.rectModeEnabled = rectModeEnabled;
    }

    public boolean isTextModeEnabled() {
        return textModeEnabled;
    }

    public void setTextModeEnabled(boolean textModeEnabled) {
        this.textModeEnabled = textModeEnabled;
    }

    private boolean rectModeEnabled = false;
    private boolean textModeEnabled = false;

    public void saveForUndo(){
        if(this.page == null) return;

        Page pageClone = new Page("randomName", page.getGameID());
        pageClone.isStarterPage = this.page.isStarterPage;

        ArrayList<Shape> clonedList = (ArrayList<Shape>) this.page.listOfShapes.clone();

        pageClone.listOfShapes = clonedList;
        pageClone.name = this.page.name;
        pageClone.pageID = this.page.pageID;
        pageClone.gameID = this.page.gameID;
        pageClone.backGroundImageName = this.page.backGroundImageName;
        pageClone.pageRender = this.page.pageRender;

        stackOfPages.push(pageClone);
    }

    public CustomPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("width", Integer.toString(getWidth()));
        stackOfPages = new Stack<>();
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
        page.draw(canvas);
    }

    public void setSelectedShape(){
        selectShape(page.findLastShape(x1,y1));
        //set the bools appropraitely
        if(selectedShape != null && selectedShape.getClass() == RectangleShape.class){
            rectModeEnabled = true;
            textModeEnabled = false;
        } else if(selectedShape != null && selectedShape.getClass() == TextShape.class){
            rectModeEnabled = false;
            textModeEnabled = true;
        } else if(selectedShape != null){
            rectModeEnabled = false;
            textModeEnabled = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
            Shape shape = null;
            EditText textEditText = ((PageEditorActivity) getContext()).findViewById(R.id.shapeText);
            String text = textEditText.getText().toString();
            if(!text.isEmpty() && textModeEnabled && selectedShape == null){
                RectF boundingRect = createBounds(x1, y1, x2, y2);
                shapeCount = getLatestCount()+1;
                String shapeName = "Shape_"+ shapeCount;
                shape = new TextShape(this, boundingRect,selectedImage,text,
                        -1,true,true,shapeName);
                page.addShape(shape);
                selectShape(selectedShape);
                changesMade = true;
                textModeEnabled = false;
                invalidate();
                return true;
            }

            setSelectedShape();
            //update the spinner
            if(selectedShape != null && !rectModeEnabled && !textModeEnabled){
                int id = selectedShape.getResId();
                String name = dbase.getResourceName(id);
                updateSpinner(imgSpinner, name);
            }
        }
        // When (x1,y1) and (x2,y2) differ, it implies that user performed a drag action
        // When no shape is selected, a drag implies user intends to draw a new ImageShape
        else if (selectedShape == null){
            saveForUndo();
            RectF boundingRect = createBounds(x1, y1, x2, y2);
            if(boundingRect.left < 0 || boundingRect.right > this.getWidth() ||
                    boundingRect.top < 0 || boundingRect.bottom > this.getHeight()) return true;
            //get the resource Id of the image
            String latestSelected = getLatestSelected();
            int res_id = dbase.getId(RESOURCE_TABLE, latestSelected, -1);
            shapeCount = getLatestCount()+1;
            String shapeName = "Shape_"+ shapeCount;
            Shape shape = null;
            EditText textEditText = ((PageEditorActivity) getContext()).findViewById(R.id.shapeText);
            String text = textEditText.getText().toString();
            //Determine which shape we are supposed to draw based on the mode selected
            if (textModeEnabled) {
                if(text.isEmpty()) return true;
                shape = new TextShape(this, boundingRect, selectedImage, text,
                        -1, true, true, shapeName);
                textModeEnabled = false;
                rectModeEnabled = false;
            } else if (rectModeEnabled) {
                shape = new RectangleShape(this, boundingRect, -1, true,
                         true, shapeName);
                rectModeEnabled = false;
                textModeEnabled = false;
            } else {
                shape = new ImageShape(this, boundingRect, selectedImage, text,
                        res_id, true, true, shapeName);
            }
            page.addShape(shape);
            selectShape(shape);
            changesMade = true;
            invalidate();
        }
        // When a shape is selected, a drag implies user intends to move the selected shape
        else {
            saveForUndo();
            float newX = selectedShape.getX() + (x2 - x1);
            float newX1 = newX + selectedShape.getWidth();
            float newY = selectedShape.getY() + (y2 - y1);
            float newY1 = newY + selectedShape.getHeight();
            //check to see if the image is in the bounds of the preview else don't make changes
            if(newX < 0 || newX1 > this.getWidth() || newY < 0 || newY1 > this.getHeight()) return true;

            //else update the picture to be dragged and update inspector
            RectF newBounds = new RectF(newX, newY, newX1, newY1);
            selectedShape.setBounds(newBounds);
            Shape shape = null;
            if(textModeEnabled){
                shape = new TextShape(this, newBounds, selectedImage, selectedShape.getText(),
                        selectedShape.getResId(), selectedShape.isVisible(), selectedShape.isMovable(), selectedShape.getName());
                textModeEnabled = false;
                rectModeEnabled = false;
            }else if(rectModeEnabled){
                shape = new RectangleShape(this, newBounds, selectedShape.getResId(), selectedShape.isVisible(),
                        selectedShape.isMovable(), selectedShape.getName());
                rectModeEnabled = false;
                textModeEnabled = false;
            }else {
                shape = new ImageShape(this, newBounds, selectedShape.getImage(), selectedShape.getText(),
                        selectedShape.getResId(), selectedShape.isVisible(), selectedShape.isMovable(), selectedShape.getName());
            }
            shape.setScript(selectedShape.getScript());


            page.addShape(shape);
            page.deleteShape(selectedShape);
            selectShape(shape);
            changesMade = true;
            invalidate();
        }
        invalidate();

        return true;
    }

    /**
     *
     * @return returns the current highlighted within the spinner
     */
    public String getLatestSelected(){
        String name;
        if (imgSpinner == null) {
            imgSpinner = ((Activity) getContext()).findViewById(R.id.imgSpinner);
        }
        if (imgSpinner.getSelectedItem() != null)
            return imgSpinner.getSelectedItem().toString();
        return "";
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
        updateInspector(selectedShape);
        invalidate();
    }

    public Shape makeShapeCopy(Shape shape) {
        //check and return proper shape
        Shape newShape = null;
        if(shape.getClass() == RectangleShape.class){ //draw a rect shape
            newShape = new RectangleShape(this, shape.getBounds() , shape.getResId(), shape.isVisible(),
                    shape.isMovable(), shape.getName());

        } else if(shape.getClass() == TextShape.class){
            newShape = new TextShape(this, shape.getBounds(), shape.getImage(), shape.getText(),
                    shape.getResId(), shape.isVisible(), shape.isMovable(), shape.getName());

        } else {
            newShape = new ImageShape(this, shape.getBounds(), shape.getImage(), shape.getText(),
                    shape.getResId(), shape.isVisible(), shape.isMovable(), shape.getName());
        }

        return newShape;
    }

    // Overloading
    public Shape makeShapeCopy(Shape shape, String shapeName, float x, float y) {
        RectF newBounds = new RectF(x, y, shape.getWidth(), shape.getHeight());

        //create the right copy and return that
        Shape newShape = null;
        if(shape.getClass() == RectangleShape.class){ //draw a rect shape
            newShape = new RectangleShape(this, newBounds , shape.getResId(), shape.isVisible(),
                    shape.isMovable(), shape.getName());

        } else if(shape.getClass() == TextShape.class){
            newShape = new TextShape(this, newBounds, shape.getImage(), shape.getText(),
                    shape.getResId(), shape.isVisible(), shape.isMovable(), shape.getName());

        } else {
            newShape = new ImageShape(this, newBounds, shape.getImage(), shape.getText(),
                    shape.getResId(), shape.isVisible(), shape.isMovable(), shape.getName());
        }

        //return the newly created shape
        return newShape;
    }

    /**
     * Spinner is made ivar because we access it multiple times in this activity
     * @param shape the shape to be updated
     */
    public void updateInspector(Shape shape) {
        Activity activity = (Activity) getContext();
        EditText name = activity.findViewById(R.id.name);
        EditText text = activity.findViewById(R.id.shapeText);
        EditText rectX = activity.findViewById(R.id.rectX);
        EditText rectY = activity.findViewById(R.id.rectY);
        EditText width = activity.findViewById(R.id.width);
        EditText height = activity.findViewById(R.id.height);
        CheckBox visible = activity.findViewById(R.id.visible);
        CheckBox movable = activity.findViewById(R.id.movable);
        TextView script = activity.findViewById(R.id.scriptz);
        imgSpinner = activity.findViewById(R.id.imgSpinner);

        if (shape != null) {
            String scriptString = shape.getScript().toString();
            String scriptTextViewText = (!scriptString.isEmpty()) ? scriptString : activity.getString(R.string.script);

            name.setText(shape.getName());
            text.setText(shape.getText());
            rectX.setText(String.format(Locale.US, "%f", shape.getBounds().left));
            rectY.setText(String.format(Locale.US, "%f", shape.getBounds().top));
            width.setText(String.format(Locale.US, "%f", shape.getBounds().right - shape.getBounds().left));
            height.setText(String.format(Locale.US, "%f", shape.getBounds().bottom - shape.getBounds().top));
            script.setText(scriptTextViewText);
            visible.setChecked(shape.isVisible());
            movable.setChecked(shape.isMovable());
            updateSpinner(imgSpinner, shape.getName());
        } else {
            name.setText("");
            text.setText("");
            rectX.setText("");
            rectY.setText("");
            width.setText("");
            height.setText("");
            script.setText(activity.getString(R.string.script));
            visible.setChecked(false);
            movable.setChecked(false);
        }
    }

    private void clearScriptSpinners(LinearLayout rows, Method deleteRowMethod) throws IllegalAccessException, InvocationTargetException {
        for (int rowIndex = 1; rowIndex < rows.getChildCount(); ++rowIndex) {
            LinearLayout row = (LinearLayout) rows.getChildAt(rowIndex);
            View deleteRowButton = row.getChildAt(DELETE_ROW_BUTTON);
            deleteRowMethod.invoke(deleteRowButton);
        }
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
    /**
     * Helper method that computes a new bounding rectangle ensuring that the point in the original shape
     * where user began dragging is the same point in the new shape where the user ends dragging.
     * @param startX x co-ordinate of point in original bounds where the user begins dragging
     * @param startY y co-ordinate of point in original bounds where the user begins dragging
     * @param endX x co-ordinate of point in new bounds where the user ends dragging
     * @param endY y co-ordinate of point in new bounds where the user ends dragging
     * @param originalBounds
     * @return
     */
    private RectF shiftBounds(float startX, float startY, float endX, float endY, RectF originalBounds) {
        float newLeft = endX - (startX - originalBounds.left);
        float newTop = endY - (startY - originalBounds.top);
        float newRight = newLeft + originalBounds.width();
        float newBottom = newTop + originalBounds.height();
        return new RectF(newLeft, newTop, newRight, newBottom);
    }

    //undoes an action performed by the user on the screen
    public Page undoChange(){
        //accesses the array list of actions and simply deletes the last activity

        if(stackOfPages.size() != 0) {
//            this.page.listOfShapes.clear();
            Page lastStepPage = stackOfPages.pop(); //<- this line is an issue...
            Log.d("tag2","stackOfPagesContains " +stackOfPages.size() + " elements");
            this.page = lastStepPage;

            invalidate();
            return this.page;
        }
        return this.page;
    }

    //redo button
    public boolean redoAction(){
        //accesses the queue and simply adds that object to the arrayList
        return true;
    }


    //saves page to database
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
        if(shapeCountNotStarted && pageId == -1) {
            shapeCountNotStarted = false;
            return 0;
        }
        int largestId = 0;

        //loop through the array list of shapes and find the one with the largest Id
        ArrayList<Shape> shapesArr = page.getListOfShapes();
        for(Shape newShape: shapesArr){
            String name = newShape.getName();
            String[] myList = name.split("_");
            int count = 0;
            if (myList.length > 1) {
                count = Integer.parseInt(myList[1]);
            }
            if(count > largestId) largestId = count;
        }

        return largestId;
    }
}
