package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class RectangleShape extends Shape {
    //default constructor
    private RectF scaledCoord;

    private Paint defaultPaint = new Paint();

    //superclass constructor
    public RectangleShape(View view, RectF bounds, int resourceId, boolean visible, boolean movable, String name) {
        //scale before storing the rectF
        super(view, bounds, null, null, resourceId, visible, movable, name);
        defaultPaint.setColor(Color.rgb(211,211,211));

        //scale and set new RectF for other canvas sizes
        float newX = bounds.left/viewWidth;
        float newY = bounds.top/viewHeight;
        float newWidth = bounds.width()/viewWidth;
        float newHeight = bounds.height()/viewHeight;
        scaledCoord = new RectF(newX, newY, newX + newWidth, newY + newHeight);
    }

    //CustomPageView calls this to display shapes
    @Override
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
    @Override
    public void draw(Canvas canvas) {
        canvas.drawRect(bounds, defaultPaint);
        //super.draw(canvas);
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

    //set and get the paint object for this rectangle shape
    public void setDefaultPaint(Paint newPaint){defaultPaint = newPaint;}
    public Paint getDefaultPaint(){return defaultPaint;}
}

