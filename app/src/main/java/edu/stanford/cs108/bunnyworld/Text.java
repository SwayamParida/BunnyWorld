package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Text extends Shape {
    //the file name
    private String txtString;
    private Paint txtPaint = new Paint();
    private Canvas pageCanvas;
    private int textX;
    private int textY;
    private String name;
    private String shapeScript = "";
    private boolean visible;
    private boolean moveable;

    //superclass constructor
    public Text(Canvas canvas, String txtString, int strX, int strY,
                boolean visible, boolean moveable, String name){
        super();
        this.name = name;
        this.visible = visible;
        this.moveable = moveable;
        this.pageCanvas = canvas;
        this.txtString = txtString;
        txtPaint.setColor(Color.BLACK);
        textX = strX;
        textY = strY;
    }

    //Called by any other canvas with new x and y positions for the object
    public void draw(Canvas canvas, float xPos, float yPos) {
        canvas.drawText(txtString, xPos, yPos, txtPaint);
    }

    //called by the pageEditorView class
    public void draw(Canvas canvas) {
        pageCanvas.drawText(txtString, textX, textY, txtPaint);
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
}
