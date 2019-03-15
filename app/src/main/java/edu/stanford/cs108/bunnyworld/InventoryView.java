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

import java.lang.reflect.Array;
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

public class InventoryView extends View implements BunnyWorldConstants{
    private Shape selectedShape;
    private float x1, x2, y1, y2;
    static int count = 0;
    public ArrayList<Shape> inventoryItems = new ArrayList<Shape>();

    public InventoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addToInventory(Shape shape) {
        inventoryItems.add(shape);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (Shape shape : inventoryItems) {
            Log.d("inventory", Double.toString(shape.getHeight()));
            Log.d("inventory", Double.toString(shape.getWidth()));
            shape.draw(canvas);
        }
    }

    public Shape findLastShape(float x, float y) {
        Shape lastFound = null;
        for (Shape shape : inventoryItems) {
            if (shape.containsPoint(x, y))
                lastFound = shape;
        }
        return lastFound;
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
            selectShape(findLastShape(x1, y1));
        }
        // When (x1,y1) and (x2,y2) differ, it implies that user performed a drag action
        // When no shape is selected, a drag implies user intends to draw a new ImageShape
        else if (selectedShape == null){
        }
        // When a shape is selected, a drag implies user intends to move the selected shape
        else {
            /*Log.d("width", Integer.toString(getWidth()));
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

            if (newY1 > .7 * emulatorHeight) { //put in inventory (bottom 30% of screen)
                newX = count;
                newY = (float) .9 * emulatorHeight;
                newX1 = newX + 100;
                newY1 = newY + 100;
                count += 100;
            }

            RectF newBounds = new RectF(newX, newY, newX1, newY1);
            selectedShape.setBounds(newBounds);
            Shape shape = new ImageShape(this, newBounds, selectedShape.getImage(), selectedShape.getText(),
                    selectedShape.getResId(), selectedShape.isVisible(), selectedShape.isMovable(), selectedShape.getName());
            page.addShape(shape);
            page.deleteShape(selectedShape);
            selectShape(shape);
            invalidate();*/
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
}
