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
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Locale;
import java.util.PriorityQueue;

import static edu.stanford.cs108.bunnyworld.PageEditorActivity.updateSpinner;

public class CustomPageView extends View implements BunnyWorldConstants{
    private Page page;
    private int pageId;
    private DatabaseHelper dbase = DatabaseHelper.getInstance(getContext());
    private BitmapDrawable selectedImage;
    private Shape selectedShape;
    private Spinner imgSpinner;
    private boolean changesMade = false;
    // Co-ordinates of user touches - populated in onTouchEvent()
    private float x1, x2, y1, y2;
    private float xOffset, yOffset;

    //get the current number of shapes in the folder
    private int shapeCount = dbase.getLatestCount(SHAPES_TABLE, pageId);

    //implementation helpers for undo and redo
    private ArrayList<Shape> undoList = new ArrayList<Shape>();
    private PriorityQueue<Shape> redoList = new PriorityQueue<Shape>();

    public CustomPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        xOffset = 0;
        yOffset = 0;
    }

    public void setSelectedImage(BitmapDrawable selectedImage) {
        this.selectedImage = selectedImage;
    }
    public Shape getSelectedShape() {
        return selectedShape;
    }
    public void setPage(Page page) {
        this.page = page;
    }
    @Override
    public void onDraw(Canvas canvas) {
        page.draw(canvas);
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
            selectShape(page.findLastShape(x1, y1));
            //update the spinner
            if(selectedShape != null){
                int id = selectedShape.getResId();
                String name = dbase.getResourceName(id);
                updateSpinner(imgSpinner, name);
            }
        }
        // When (x1,y1) and (x2,y2) differ, it implies that user performed a drag action
        // When no shape is selected, a drag implies user intends to draw a new ImageShape
        else if (selectedShape == null){
            Log.d("tag1","Drawing new shape");
            RectF boundingRect = createBounds(x1, y1, x2, y2);
            if(boundingRect.left < 0 || boundingRect.right > this.getWidth() ||
                    boundingRect.top < 0 || boundingRect.bottom > this.getHeight()) return true;
            //get the resource Id of the image
            String latestSelected = getLatestSelected();
            int res_id = dbase.getId(RESOURCE_TABLE, latestSelected, -1);
            shapeCount += 1;
            String shapeName = "Shape "+ shapeCount;
            Shape shape = new ImageShape(this, boundingRect, selectedImage, null,
                    res_id, true, true, shapeName);
            page.addShape(shape);
            selectShape(shape);
            updateInspector(shape);
            changesMade = true;
            //updateInspector(shape);
        }
        // When a shape is selected, a drag implies user intends to move the selected shape
        else {
            float newX = selectedShape.getX() + (x2 - x1);
            float newX1 = newX + selectedShape.getWidth();
            float newY = selectedShape.getY() + (y2 - y1);
            float newY1 = newY + selectedShape.getHeight();
            //check to see if the image is in the bounds of the preview else don't make changes
            if(newX < 0 || newX1 > this.getWidth() || newY < 0 || newY1 > this.getHeight()) return true;

            //else update the picture to be dragged and update inspector
            RectF newBounds = new RectF(newX, newY, newX1, newY1);
            selectedShape.setBounds(newBounds);
            Shape shape = new ImageShape(this, newBounds, selectedShape.getImage(), selectedShape.getText(),
                    selectedShape.getResId(), selectedShape.isVisible(), selectedShape.isMovable(), selectedShape.getName());
            page.addShape(shape);
            page.deleteShape(selectedShape);
            selectShape(shape);
            updateInspector(shape);
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
        if(imgSpinner != null) name = imgSpinner.getSelectedItem().toString();
        else {
            imgSpinner = ((Activity) getContext()).findViewById(R.id.imgSpinner);
            name = imgSpinner.getSelectedItem().toString();
        }
        return name;
    }

    /**
     * Helper method that handles all the steps associated with shape selection and deselection
     * @param toSelect When not-null, this Shape is selected. When null, the current selection is cleared.
     */
    private void selectShape(Shape toSelect) {
        if (selectedShape != null) {
            selectedShape.setSelected(false);
        }
        if (toSelect != null)
            toSelect.setSelected(true);
        selectedShape = toSelect;
        updateInspector(selectedShape);
        invalidate();
    }

    /**
     * Spinner is made ivar because we access it multiple times in this activity
     * @param shape the shape to be updated
     */
    private void updateInspector(Shape shape) {
        EditText name = ((Activity) getContext()).findViewById(R.id.name);
        EditText text = ((Activity) getContext()).findViewById(R.id.shapeText);
        EditText rectX = ((Activity) getContext()).findViewById(R.id.rectX);
        EditText rectY = ((Activity) getContext()).findViewById(R.id.rectY);
        EditText width = ((Activity) getContext()).findViewById(R.id.width);
        EditText height = ((Activity) getContext()).findViewById(R.id.height);
        CheckBox visible = ((Activity) getContext()).findViewById(R.id.visible);
        CheckBox movable = ((Activity) getContext()).findViewById(R.id.movable);
        imgSpinner = ((Activity) getContext()).findViewById(R.id.imgSpinner);

        if (name != null) {
            if (selectedShape != null) {
                name.setText(shape.getName());
                text.setText(shape.getText());
                rectX.setText(String.format(Locale.US, "%f", shape.getBounds().left));
                rectY.setText(String.format(Locale.US, "%f", shape.getBounds().top));
                width.setText(String.format(Locale.US, "%f", shape.getBounds().right - shape.getBounds().left));
                height.setText(String.format(Locale.US, "%f", shape.getBounds().bottom - shape.getBounds().top));
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
                visible.setChecked(false);
                movable.setChecked(false);
            }
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
    public boolean undoChange(){
        //accesses the array list of actions and simply deletes the last activity
        int size = undoList.size();
        Shape currentRemove = null;
        if(size != 0 && size != 1) currentRemove = undoList.remove(size - 1);
        if(currentRemove == null) return false;

        //get shape name of last element and find old version in undoList
        //add the removed shape to the queue in case they hit redo
        String shapeName = currentRemove.getName();
        Shape toAdd = undoList.get(size - 2);
        redoList.add(currentRemove);

        //add to the arrayList of the page and call invalidate on PagePreview
        ArrayList<Shape> pageShape = page.getListOfShapes();
        int pageSize = pageShape.size() - 1; //for loop might be slightly optimized
        for(int i = pageSize; i > 0; i--){
            String name = pageShape.get(i).getName();
            if(name.equals(shapeName)) {
                pageShape.remove(i);
                pageShape.add(i, toAdd);
                break;
            }
        }
        //update the page arrayList and call invalidate() to redraw shapes
        page.setListOfShapes(pageShape);
        this.invalidate();
        return true;
    }

    //redo button
    public boolean redoAction(){
        //accesses the queue and simply adds that object to the arrayList
        int size = redoList.size();
        Shape currentToAdd = null;
        if(size != 0) currentToAdd = redoList.poll();
        if(currentToAdd == null) return false;

        //get shape name of first element and find old version in pageShapes
        //add the removed shape to the arrayList of the undoList in case they hit undo
        String shapeName = currentToAdd.getName();
        undoList.add(currentToAdd);

        //add to the arrayList of the page and call invalidate on PagePreview
        ArrayList<Shape> pageShape = page.getListOfShapes();
        int pageSize = pageShape.size() - 1; //for loop might be slightly optimized
        for(int i = pageSize; i > 0; i--){
            String name = pageShape.get(i).getName();
            if(name.equals(shapeName)) {
                pageShape.remove(i);
                pageShape.add(i, currentToAdd);
                break;
            }
        }
        //update the page arrayList and call invalidate() to redraw shapes
        page.setListOfShapes(pageShape);
        this.invalidate();
        return true;
    }

    //saves page to database
    public ArrayList<Shape> getPageShapes(){ return page.getListOfShapes(); }

    //getters and setters for changes made boolean
    public boolean getChangesMadeBool(){return changesMade;}
    public void setChangesMadeBool(boolean bool){changesMade = bool;}

    //getters and setters for the pageId
    public void setPageId(int pageId){this.pageId = pageId;}
}
