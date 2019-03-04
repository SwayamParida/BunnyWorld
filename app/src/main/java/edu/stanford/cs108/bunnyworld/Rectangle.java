package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class Rectangle extends Shape {
    //default constructor
    private Paint defaultPaint = new Paint();
    private RectF scaledCoord;
    private int viewWidth;
    private int viewHeight;
    private String name;
    private RectF bounds;
    private String shapeScript = "";
    private boolean visible;
    private boolean moveable;

    //superclass constructor
    public Rectangle(View view, RectF bounds, boolean visible, boolean moveable, String name) {
        //scale before storing the rectF
        super();
        if(name == null || name.equals("")){
            this.name = "shape" + count;
            count++;
        } else this.name = name;
        this.bounds = bounds;
        this.visible = visible;
        this.moveable = moveable;
        this.viewHeight = view.getHeight();
        this.viewWidth = view.getWidth();

        //scale and set new RectF for other canvas sizes
        float newX = bounds.left/viewWidth;
        float newY = bounds.top/viewHeight;
        float newWidth = bounds.width()/viewWidth;
        float newHeight = bounds.height()/viewHeight;
        scaledCoord = new RectF(newX, newY, newX + newWidth, newY + newHeight);
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
    public void draw(Canvas canvas) {
        canvas.drawRect(bounds, defaultPaint);
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

    //provide resize functionality
    public void resizeBounds(RectF newBounds){
        this.bounds = newBounds;
        float newX = newBounds.left/viewWidth;
        float newY = newBounds.top/viewHeight;
        float newWidth = newBounds.width()/viewWidth;
        float newHeight = newBounds.height()/viewHeight;
        scaledCoord = new RectF(newX, newY, newX + newWidth, newY + newHeight);
    }
}

