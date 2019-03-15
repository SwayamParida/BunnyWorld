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
import android.widget.Toast;

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
import static edu.stanford.cs108.bunnyworld.PlayGameActivity.playerPageView;

public class InventoryView extends View implements BunnyWorldConstants{
    private Shape selectedShape;
    private float x1, x2, y1, y2;
    public int countX = 0;
    public int countY = 0;
    public int rows = 2;
    public ArrayList<Shape> inventoryItems = new ArrayList<Shape>();
    public ArrayList<Shape> thumbnails = new ArrayList<Shape>();

    public InventoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        countX = 0;
    }

    public void addToInventory(Shape shape) {
        inventoryItems.add(shape);
        if (countX == 10) {
            if (countY == (int) ((emulatorHeight * .25) / rows)) {
                rows++;
                //inventory full
            }
            countX = 0;
            countY = (int) ((emulatorHeight * .25) / rows);
        }

        float newX = countX * (emulatorWidth / 10);
        float newX1 = newX + emulatorWidth / 10;
        float newY = countY;
        float newY1 = newY + (float) (emulatorHeight * .25) / rows;

        RectF newBounds = new RectF(newX, newY, newX1, newY1);
        Log.d("onDraw bounds", newBounds.toString());
        Shape thumbnail = new ImageShape(this, newBounds, shape.getImage(), shape.getText(),
                shape.getResId(), shape.isVisible(), shape.isMovable(), shape.getName());
        thumbnails.add(thumbnail);
        reDrawInventory();
        countX++;
        invalidate();
    }

    public void reDrawInventory() {
        countX = 0;
        countY = 0;

        ArrayList<Shape> resetThumbnails = new ArrayList<Shape>();
        for (Shape shape : thumbnails) {
            float newX = countX * (emulatorWidth / 10);
            float newX1 = newX + emulatorWidth / 10;
            float newY = countY;
            float newY1 = newY + (float) (emulatorHeight * .25) / rows;
            if (countX == 9) {
                if (countY == (int) ((emulatorHeight * .25) / rows)) {
                    //inventory full
                } else {
                    countX = 0;
                    countY = (int) ((emulatorHeight * .25) / rows);
                }
            } else {
                countX++;
            }
            RectF newBounds = new RectF(newX, newY, newX1, newY1);
            Log.d("onDraw bounds", newBounds.toString());
            Shape newThumbnail = new ImageShape(this, newBounds, shape.getImage(), shape.getText(),
                    shape.getResId(), shape.isVisible(), shape.isMovable(), shape.getName());
            resetThumbnails.add(newThumbnail);
        }
        thumbnails = resetThumbnails;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (Shape thumbnail : thumbnails) {
            Log.d("onDraw name", thumbnail.getName());
            Log.d("onDraw toString", thumbnail.toString());
            Log.d("onDraw height", Float.toString(thumbnail.getHeight()));
            Log.d("onDraw width", Float.toString(thumbnail.getWidth()));
            Log.d("onDraw movable", Boolean.toString(thumbnail.isMovable()));
            Log.d("onDraw visible", Boolean.toString(thumbnail.isVisible()));
            Log.d("onDraw image", thumbnail.getImage().toString());
            Log.d("onDraw bounds", thumbnail.getBounds().toString());
            thumbnail.draw(canvas);
        }
    }

    public Shape findLastShape(float x, float y) {
        Shape lastFound = null;
        for (Shape shape : inventoryItems) {
            if (shape.containsPoint(x, y))
                lastFound = shape;
        }
        for (Shape shape : thumbnails) {
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

        selectShape(findLastShape(x1, y1));
        if (selectedShape != null && y2 < 0) {
            int num = -1;
            Shape thumbnail = null;
            for (int i =0; i <thumbnails.size(); i++) {
                if (thumbnails.get(i).getName().equals(selectedShape.getName())) {
                    num = i;
                    selectedShape = inventoryItems.get(i);
                    thumbnail = thumbnails.get(i);
                }
            }

            float newX = thumbnail.getX() + (x2 - x1);
            float newX1 = newX + selectedShape.getWidth();
            float newY = selectedShape.getY() + (y2 - y1);
            float newY1 = newY + selectedShape.getHeight();
            /*float newX = selectedShape.getX() + (x2 - x1);
            float newX1 = newX + selectedShape.getWidth();
            float newY = selectedShape.getY() + (y2 - y1);
            float newY1 = newY + selectedShape.getHeight();*/
            //check to see if the image is in the bounds of the preview else don't make changes
            //if (newY1 <= 0) {
                if (newX < 0) {
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
                Log.d("moving out", newBounds.toString());
                selectedShape.setBounds(newBounds);
                Shape shape = new ImageShape(this, newBounds, selectedShape.getImage(), selectedShape.getText(),
                        selectedShape.getResId(), selectedShape.isVisible(), selectedShape.isMovable(), selectedShape.getName());
                playerPageView.addShape(shape);
                thumbnails.remove(num);
                if (num != -1) {
                    inventoryItems.remove(selectedShape);
                }
                reDrawInventory();
            //}
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
