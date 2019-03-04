package edu.stanford.cs108.bunnyworld;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

public class Image extends Shape {
    //the file name
    private Bitmap fileName;
    private RectF scaledCoord;
    private int viewWidth;
    private int viewHeight;
    private float originalX;
    private float originalY;
    private String name;
    private boolean drawOriginalDim = false;
    private RectF bounds;
    private String shapeScript = "";
    private boolean visible;
    private boolean moveable;

    //superclass constructor--- canvas refers to main canvas area for the editor
    public Image(View view, BitmapDrawable drawable, RectF bounds,
                 boolean visible, boolean moveable, String name){
        super();
        if( name == null || name.equals("")){
            this.name = "shape" + count;
            count++;
        } else this.name = name;
        this.bounds = bounds;
        this.visible = visible;
        this.moveable = moveable;
        this.viewWidth = view.getWidth();
        this.viewHeight = view.getHeight();
        this.fileName = drawable.getBitmap();
        int width = fileName.getWidth();
        int height = fileName.getHeight();
        originalX = bounds.left;
        originalY = bounds.top;

        //scale and store new image bounds for other canvas sizes
        float newX = originalX/viewWidth;
        float newY = originalY/viewHeight;
        float newWidth = bounds.width()/viewWidth;
        float newHeight = bounds.height()/viewHeight;
        scaledCoord = new RectF(newX, newY, newX + newWidth, newY + newHeight);
    }

    //override the shape draw
    public void draw(Canvas canvas, float xPos, float yPos) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        float newX = scaledCoord.left*width;
        float newY = scaledCoord.top*height;
        if(drawOriginalDim) canvas.drawBitmap(fileName, newX, newY, null);
        else {
            float newWidth = scaledCoord.width()*width;
            float newHeight = scaledCoord.height()*height;
            RectF newBounds = new RectF(newX, newY, newX+newWidth, newY+newHeight);
            canvas.drawBitmap(fileName, null, newBounds, null);
        }
    }

    //Editor activity calls this version of draw
    public void draw(Canvas canvas) {
        canvas.drawBitmap(fileName, null, bounds, null);
        drawOriginalDim = false;
    }

    //functionality to draw original image
    public void drawOriginal(Canvas canvas){
        canvas.drawBitmap(fileName, originalX, originalY, null);
        float newX = scaledCoord.left*canvas.getWidth();
        float newY = scaledCoord.top*canvas.getHeight();
        bounds = new RectF(newX, newY, newX +fileName.getWidth(), newX + fileName.getHeight());
        drawOriginalDim = true;
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
