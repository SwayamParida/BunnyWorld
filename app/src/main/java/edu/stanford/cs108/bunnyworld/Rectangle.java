package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Rectangle extends Shape {
    //default constructor
    private Paint defaultPaint = new Paint();
    private RectF scaledCoord;
    private Canvas pageCanvas;
    private String name;
    private RectF bounds;
    private String shapeScript = "";
    private boolean visible;
    private boolean moveable;

    //superclass constructor
    public Rectangle(Canvas canvas, String name, RectF bounds, boolean visible, boolean moveable) {
        //scale before storing the RECTF
        super();
        this.name = name;
        this.bounds = bounds;
        this.visible = visible;
        this.moveable = moveable;
        pageCanvas = canvas;

        //scale and set new RectF for other canvas sizes
        float newX = bounds.left/canvas.getWidth();
        float newY = bounds.top/canvas.getHeight();
        float newWidth = bounds.width()/canvas.getWidth();
        float newHeight = bounds.height()/canvas.getHeight();
        scaledCoord = new RectF(newX, newY, newX + newWidth, newY + newHeight);
        defaultPaint.setColor(Color.rgb(211, 211, 211));
    }

    //PageView calls this to display shapes
    public void draw(Canvas canvas, float xPos, float yPos) {
        //scale them and then store it
        float screenWidth = canvas.getWidth();
        float screenHeight = canvas.getHeight();
        float newX = scaledCoord.left*screenWidth;
        float newY = scaledCoord.top*screenHeight;
        float newWidth = scaledCoord.width()*screenWidth;
        float newHeight = scaledCoord.height()*screenHeight;
        RectF newRect = new RectF(newX, newY, newX + screenWidth, newY + screenHeight);
        canvas.drawRect(newRect, defaultPaint);
    }

    //Editor activity calls this version of draw
    public void draw() {
        pageCanvas.drawRect(bounds, defaultPaint);
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

