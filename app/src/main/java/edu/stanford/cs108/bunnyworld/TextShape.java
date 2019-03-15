package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class TextShape extends Shape {
    //the file name
    private Paint txtPaint;
    private RectangleShape rectBackground;
    private Paint rectPaint;
    private float textWidth;
    private float textHeight;
    private float textSize = 70f;
    private RectF scaledCoord;
    private RectF newBounds;

    //superclass constructor
    public TextShape(View view, RectF bounds, BitmapDrawable image, String txtString, int resourceId,
                     boolean visible, boolean movable, String name){
        super(view, bounds, image, txtString, resourceId, visible, movable, name);

        //get the text size from the paint
        txtPaint = new Paint();
        txtPaint.setTextSize(textSize);
        txtPaint.setColor(Color.BLACK);
        Rect txtRect = new Rect();
        txtPaint.getTextBounds(txtString, 0, txtString.length(), txtRect);
        textWidth = txtRect.width();
        textHeight = txtRect.height();

        //recompute the proper bounds and add it in....MAYBE WITH OFFSETS(?)
        float startX = bounds.left;
        float startY = bounds.top;
        float endX = startX + textWidth;
        float endY =  startY + textHeight;
        newBounds = new RectF(startX, startY, endX, endY);
        this.bounds = newBounds;

       // create a new Rect shape and place a text within it
        rectBackground = new RectangleShape(view, newBounds, resourceId, visible, movable, name);
        rectPaint = new Paint();
        rectPaint.setColor(Color.TRANSPARENT);
        rectBackground.setDefaultPaint(rectPaint);

        //Go ahead and set your scaled-coords to store the scaled text pos
        //scale and set new RectF for other canvas sizes
        float newX = startX/viewWidth;
        float newY = startY/viewHeight;
        float newEndX = endX/viewWidth;
        float newEndY = endY/viewHeight;
        scaledCoord = new RectF(newX, newY, newEndX, newEndY);
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
        //change the origin of the drawing to the shifted/offsetted shapes
        float newOriginX = bounds.left;
        float newOriginY = bounds.top + textHeight;
        canvas.drawText(txtString, newOriginX, newOriginY , txtPaint);
    }

    //provide ability to change text properties
    public void changeText(String fontName, String fontSize, String fontStyle){
        //set boolean to true
        //create a new paint object and replace default paint
        // paint.setTypeface(); // takes plain/bold/DEFAULT_BOLD etc
        // paint.setTypeface(Typeface.create("Arial",Typeface.ITALIC));
        // paint.setTextSize(); //takes floating pt number
    }

    //return the new bounds
    public RectF getNewBounds(){return newBounds;}
}
