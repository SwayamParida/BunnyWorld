package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class Text extends Shape {
    //the file name
    private String txtString;
    private Paint txtPaint = new Paint();
    private int viewWidth;
    private int viewHeight;
    private float textX;
    private float textY;
    private String name;
    private String shapeScript = "";
    private boolean visible;
    private boolean moveable;

    //superclass constructor
    public Text(View view, String txtString, float strX, float strY,
                boolean visible, boolean moveable, String name){
        super();
        this.name = name;
        this.visible = visible;
        this.moveable = moveable;
        this.viewHeight = view.getHeight();
        this.viewWidth = view.getWidth();
        this.txtString = txtString;
        txtPaint.setColor(Color.BLACK);
        textX = strX/viewWidth;
        textY = strY/viewHeight;
    }

    //Called by any other canvas with new x and y positions for the object
    @Override
    public void draw(Canvas canvas, float xPos, float yPos) {
        canvas.drawText(txtString, xPos, yPos, txtPaint);
    }

    //called by any other canvas except the pageEditorView class
    @Override
    public void draw(Canvas canvas) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        canvas.drawText(txtString, textX*width, textY*height, txtPaint);
    }

    //update the script for the object
    public void updateScript(String newScript){
        shapeScript = newScript;
    }

    //returns the script for this object
    public String getShapeScript(){
        return shapeScript;
    }

    //bool indicating if the shape is visible
    public boolean isVisible(){
        return visible;
    }

    //returns bool indicating if the shape must be moveable
    public boolean isMoveable(){
        return moveable;
    }

    // sets the name of the shape
    public void setName(String name){
        this.name = name;
    }

    //returns the name of this shape
    public String getName(){
        return name;
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
