package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.ClipData;
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
import android.widget.Spinner;

import java.util.Locale;

public class PageView extends View {
    private Page page;
    private BitmapDrawable selectedImage;
    private Shape selectedShape;
    // Co-ordinates of user touches - populated in onTouchEvent()
    private float x1, x2, y1, y2;

    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        if (x1 == x2 && y1 == y2)
            selectShape(page.findLastShape(x1, y1));

        // When (x1,y1) and (x2,y2) differ, it implies that user performed a drag action
        // When no shape is selected, a drag implies user intends to draw a new ImageShape
        else if (selectedShape == null){
            Log.d("tag1","Drawing new shape");
            RectF boundingRect = createBounds(x1, y1, x2, y2);
            Shape shape = new ImageShape(this, boundingRect, selectedImage, null, true, true, null);
            page.addShape(shape);
            updateInspector(shape);
        }
        // When a shape is selected, a drag implies user intends to move the selected shape
        else if (selectedShape.isMovable()){
            Log.d("tag2","Moving shape");
            RectF newBounds = new RectF(x2, y2, x2 + selectedShape.getRectWidth(), y2 +selectedShape.getRectHeight());
            //selectedShape.setBounds(newBounds);
            Shape shape = new ImageShape(this, newBounds, selectedImage, null, true, true, null);
            page.addShape(shape);
            updateInspector(selectedShape);
        }
        invalidate();

        return true;
    }

    private final class MyTouchListener implements OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view);
                view.startDrag(data, shadowBuilder, view, 0);
                view.setVisibility(View.INVISIBLE);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Helper method that handles all the steps associated with shape selection and deselection
     * @param toSelect When not-null, this Shape is selected. When null, the current selection is cleared.
     */
    private void selectShape(Shape toSelect) {
        if (toSelect != null)
            toSelect.setSelected(true);
        if (selectedShape != null) {
            selectedShape.setSelected(false);
        }
        selectedShape = toSelect;
        updateInspector(selectedShape);
        invalidate();
    }
    private void updateInspector(Shape shape) {
        EditText name = ((Activity) getContext()).findViewById(R.id.name);
        EditText text = ((Activity) getContext()).findViewById(R.id.shapeText);
        EditText rectX = ((Activity) getContext()).findViewById(R.id.rectX);
        EditText rectY = ((Activity) getContext()).findViewById(R.id.rectY);
        EditText width = ((Activity) getContext()).findViewById(R.id.width);
        EditText height = ((Activity) getContext()).findViewById(R.id.height);
        CheckBox visible = ((Activity) getContext()).findViewById(R.id.visible);
        CheckBox movable = ((Activity) getContext()).findViewById(R.id.movable);
        Spinner imgSpinner = ((Activity) getContext()).findViewById(R.id.imgSpinner);

        if (selectedShape != null) {
            name.setText(shape.getName());
            text.setText(shape.getText());
            rectX.setText(String.format(Locale.US,"%f", shape.getBounds().left));
            rectY.setText(String.format(Locale.US,"%f", shape.getBounds().top));
            width.setText(String.format(Locale.US,"%f", shape.getBounds().right - shape.getBounds().left));
            height.setText(String.format(Locale.US,"%f", shape.getBounds().bottom - shape.getBounds().top));
            visible.setChecked(shape.isVisible());
            movable.setChecked(shape.isMovable());
            EditorActivity.updateSpinner(imgSpinner, shape.getImage());
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
}
