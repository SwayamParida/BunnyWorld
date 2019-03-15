package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Display;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static edu.stanford.cs108.bunnyworld.GameLoaderActivity.playing;
import static edu.stanford.cs108.bunnyworld.IntroScreenActivity.emulatorHeight;
import static edu.stanford.cs108.bunnyworld.IntroScreenActivity.emulatorWidth;

abstract class Shape {
    static int count = 0;

    //ivars for all shapes
    protected int res_id;
    protected String txtString;
    protected RectF bounds;
    protected boolean selected;
    protected String name;
    protected BitmapDrawable image;
    protected boolean visible, movable;
    public int viewWidth, viewHeight;
    protected float originalX, originalX2, originalY, originalY2;
    protected Script script;

    private static final Paint outlinePaint = new Paint();
    private static final int PAINT_COLOR = Color.BLUE;
    private static final Paint.Style PAINT_STYLE = Paint.Style.STROKE;
    private static final float STROKE_WIDTH = 15.0f;

    Shape(View view, RectF bounds, BitmapDrawable drawable, String txtString, int resourceId,
          boolean visible, boolean movable, String name){
        if(name == null || name.equals("")){
            this.name = "shape" + count++;
        } else this.name = name;
        this.visible = visible;
        this.movable = movable;
        this.bounds = bounds;
        this.originalX = bounds.left;
        this.originalY = bounds.top;

        if (playing) {
            this.viewWidth = emulatorWidth;
            this.viewHeight = (int) (emulatorHeight * .9);
        }
        else {
            this.viewWidth = emulatorWidth;
            this.viewHeight = (int) (emulatorHeight * .75);
        }

        /*viewHeight = 1285;
       viewWidth = 1664;
        }*/
        this.image = drawable;
        this.txtString = txtString;
        this.res_id = resourceId;

        outlinePaint.setStyle(PAINT_STYLE);
        outlinePaint.setColor(PAINT_COLOR);
        outlinePaint.setStrokeWidth(STROKE_WIDTH);
    }

    //general draw method
    public void draw(Canvas canvas) {
        if (selected) canvas.drawRect(bounds, outlinePaint); }

    //specific draw for other canvases
    abstract void draw(Canvas canvas, float xPos, float yPos);

    //update the script for the object
    public void setScript(Script newScript){ this.script = newScript; }

    //returns the script for this object
    public Script getScript(){ return script; }

    //bool indicating if the shape is visible
    public boolean isVisible(){
        return visible;
    }

    //set the moveable boolean
    public void setVisible(boolean set){
        this.visible = set;
    }

    //returns bool indicating if the shape must be moveable
    public boolean isMovable(){
        return movable;
    }

    //set the moveable boolean
    public void setMovable(boolean bool){
        this.movable = bool;
    }

    // sets the name of the shape
    public void setName(String name){
        this.name = name;
    }

    //returns the name of this shape
    public String getName(){
        return name;
    }

    //checks if the current shape is selected or not
    public boolean isSelected(){
        return selected;
    }

    //sets the boolean selected
    public void setSelected(boolean selected){
        this.selected = selected;
    }

    //checks if in bounds
    public boolean containsPoint(float xPos, float yPos){
        return bounds.contains(xPos, yPos);
    }

    //returns Bitmap
    public BitmapDrawable getImage(){
        return image;
    }

    //update bitmap
    public void updateImage(BitmapDrawable file){
        this.image = file;
    }

    //returns the text string
    public String getText(){
        return txtString;
    }

    //sets the textString
    public void setText(String txt){
        this.txtString = txt;
    }

    //get the bounds of the shape
    public RectF getBounds() {
        return bounds;
    }

    //sets the bounds of the shape
    public void setBounds(RectF bounds) {
        this.bounds = bounds;
    }

    public float getWidth() {
        return bounds.width();
    }

    public float getHeight() {
        return bounds.height();
    }

    public float getX() {
        return bounds.left;
    }

    public float getY() {
        return bounds.top;
    }

    @Override
    public String toString() {
        return name;
    }

    //setters and getters for the resource id just in case
    public int getResId(){
        return res_id;
    }

    public void setResId(int resource_id){
        res_id = resource_id;
    }

    @Override
    public boolean equals(Object obj) {
        Shape cmpObj = (Shape)(obj);
        return (cmpObj.getResId() == this.res_id && cmpObj.getName().equals(this.name));
    }
}