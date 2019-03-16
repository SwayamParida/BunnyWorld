package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public class TextShape extends Shape {
    //the file name
    private Paint txtPaint;
    private RectangleShape rectBackground;
    private Paint rectPaint;
    private float textWidth;
    private float textHeight;
    private float textSize = 70f;
    private RectF scaledCoords;
    private RectF newBounds;

    //superclass constructor
    public TextShape(View view, RectF bounds, BitmapDrawable image, String txtString, int resourceId,
                     boolean visible, boolean movable, String name){
        super(view, bounds, image, txtString, resourceId, visible, movable, name);

        //get the textsize from the paint
        txtPaint = new Paint();
        txtPaint.setTextSize(textSize);
        txtPaint.setColor(Color.BLACK);
        Rect txtRect = new Rect();
        txtPaint.getTextBounds(txtString, 0, txtString.length(), txtRect);
        textWidth = txtRect.width();
        textHeight = txtRect.height();

        //recompute the proper bounds and add it in
        float newX = bounds.left;
        float newY = bounds.top;
        float endX = newX + textWidth;
        float endY = newY + textHeight;
        newBounds = new RectF(newX, newY, endX, endY);
        this.bounds = newBounds;

        //create a new rectangle shape
        rectBackground = new RectangleShape(view, newBounds,resourceId,visible,movable,name);
        rectPaint = new Paint();
        rectPaint.setColor(Color.TRANSPARENT);
        rectBackground.setDefaultPaint(rectPaint);

        //scale and set new RectF for other canvas
        float startX = newX/viewWidth;
        float startY = newY/viewHeight;
        float newEndX = endX/viewWidth;
        float newEndY = endY/viewHeight;
        scaledCoords = new RectF(startX, startY, newEndX, newEndY);
    }

    //Called by any other canvas with new x and y positions for the object
    @Override
    public void draw(Canvas canvas, float xPos, float yPos) {
        canvas.drawText(txtString, xPos, yPos, txtPaint);
    }

    //called by any other canvas except the pageEditorView class
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        rectBackground.draw(canvas);

        //changes the origin of the text and draw it
        float newOriginX = bounds.left;
        float newOriginY = bounds.top + textHeight;
        canvas.drawText(txtString, newOriginX, newOriginY, txtPaint);
    }

    //returns the new bounds
    public RectF getNewBounds(){return newBounds;}

    //provide ability to change text properties
    public void changeText(String fontName, String fontSize, String fontStyle){
        //set boolean to true
        //create a new paint object and replace default paint
        // paint.setTypeface(); // takes plain/bold/DEFAULT_BOLD etc
        // paint.setTypeface(Typeface.create("Arial",Typeface.ITALIC));
        // paint.setTextSize(); //takes floating pt number
    }
}
