package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public class TextShape extends Shape {
    //the file name
    private Paint txtPaint = new Paint();
    private float textX;
    private float textY;

    //superclass constructor
    public TextShape(View view, RectF bounds, int imageID, BitmapDrawable image, String txtString,
                     boolean visible, boolean movable, String name){
        super(view, bounds, imageID, image, txtString, visible, movable, name);
        txtPaint.setColor(Color.BLACK);
        this.viewHeight = view.getHeight();
        this.viewWidth = view.getWidth();
        textX = bounds.left/viewWidth;
        textY = bounds.right/viewHeight; // Shouldn't this be bounds.top/viewHeight?
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
        canvas.drawText(txtString, textX*width, textY*height, txtPaint);
    }

    //provide ability to change text properties
    public void changeText(String fontName, String fontSize, String fontStyle){
        //set boolean to true
        //create a new paint object and replace default paint
        // paint.setTypeface(); // takes plain/bold/DEFAULT_BOLD etc
        // paint.setTypeface(Typeface.create("Arial",Typeface.ITALIC));
        // paint.setTextSize(); //takes floating pt number
    }
}
