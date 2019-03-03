package edu.stanford.cs108.bunnyworld;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

public class Image extends Shape {
    //the file name
    private Bitmap fileName;
    private RectF scaledCoord;
    private Canvas pageCanvas;
    private RectF originalSize;
    private float originalX;
    private float originalY;
    private String name;
    private RectF bounds;
    private String shapeScript = "";
    private boolean visible;
    private boolean moveable;

    //superclass constructor--- canvas refers to main canvas area for the editor
    public Image(Canvas canvas, String name, BitmapDrawable drawable,
                 RectF bounds, boolean visible, boolean moveable){
        super();
        this.name = name;
        this.bounds = bounds;
        this.visible = visible;
        this.moveable = moveable;
        pageCanvas = canvas;
        this.fileName = drawable.getBitmap();
        int width = fileName.getWidth();
        int height = fileName.getHeight();
        originalSize = new RectF(bounds.left, bounds.top,
                bounds.left + width, bounds.top + height);
        originalX = bounds.left;
        originalY = bounds.top;

        //scale and store new rect for other canvas sizes
        float newX = bounds.left/canvas.getWidth();
        float newY = bounds.top/canvas.getHeight();
        float newWidth = bounds.width()/canvas.getWidth();
        float newHeight = bounds.height()/canvas.getHeight();
        scaledCoord = new RectF(newX, newY, newX + newWidth, newY + newHeight);
    }

    //override the shape draw
    public void draw(Canvas canvas, float xPos, float yPos) {
        float width = bounds.width();
        float height = bounds.height();
        RectF newRect = new RectF(xPos, yPos, xPos + width, yPos + height);
        canvas.drawRect(newRect, defaultPaint);
    }

    //Editor activity calls this version of draw
    public void draw() {
        pageCanvas.drawBitmap(fileName, null, bounds, null);
    }

    //functionality to draw original image
    public void drawOriginal(){
        pageCanvas.drawBitmap(fileName, originalX, originalY, null);
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
