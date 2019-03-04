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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Locale;

public class PageView extends View {
    private Page page;
    private BitmapDrawable selectedImage;
    private Shape selectedShape;

    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas) {
        page.draw(canvas);
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
    private float x1, x2, y1, y2;
    // FIXME: (x1,y1) and (y1,y2) are not updated properly, so their persistent values cause a bug where every click creates an image
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (selectedImage == null)
            return true;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                // Prevents premature processing before both new co-ordinates are obtained
                return true;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                break;
        }

        // When (x1,y1) = (x2,y2), it implies user simply tapped screen
        if (x1 == x2 && y1 == y2) {
            selectShape(page.findLastShape(x1, y1));
        }
        // If (x1,y1) and (x2,y2) differ, it implies that user performed a drag action
        else {
            RectF boundingRect = getBoundingRect(x1, x2, y1, y2);
            Shape shape = new ImageShape(this, boundingRect, selectedImage, null, true, true, null);
            updateInspector(shape);
            page.addShape(shape);
        }
        invalidate();

        return true;
    }
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
    private RectF getBoundingRect(float x1, float x2, float y1, float y2) {
        RectF boundingRect = new RectF();
        boundingRect.left = Math.min(x1, x2);
        boundingRect.right = Math.max(x1, x2);
        boundingRect.top = Math.min(y1, y2);
        boundingRect.bottom = Math.max(y1, y2);
        return boundingRect;
    }
}
