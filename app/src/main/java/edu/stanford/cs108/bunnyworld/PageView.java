package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

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

    private float x1, x2, y1, y2;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (selectedImage == null)
            return true;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                y2 = event.getY();
                break;
        }

        // If user taps the screen, indicating a selection
        if (x1 == x2 && y1 == y2) {
            selectShape(page.findLastShape(x1, y1));
        }

        RectF boundingRect = new RectF(x1, y1, x2, y2);
        Shape shape = new ImageShape(this, selectedImage, boundingRect, true, true, null);
        updateInspector(shape);
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
        ((EditText) findViewById(R.id.name)).setText(shape.getName());
        ((EditText) findViewById(R.id.shapeText)).setText(shape.getText());
        ((CheckBox) findViewById(R.id.visible)).setChecked(shape.isVisible());
        ((CheckBox) findViewById(R.id.movable)).setChecked(shape.isMovable());
        updateImgSpinner();
    }

    /**
     * Helper method that updates the Spinner to reflect the image clicked by the user
     */
    private void updateImgSpinner() {
        Spinner imgSpinner = findViewById(R.id.imgSpinner);
        String imageName = EditorActivity.imgStringMap.get(selectedImage);
        ArrayAdapter<String> imgSpinnerAdapter = (ArrayAdapter<String>) imgSpinner.getAdapter();
        imgSpinner.setSelection(imgSpinnerAdapter.getPosition(imageName));
    }
}
