package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

abstract class Shape {
    static int count = 0;

    //an Array List for editing the shapes
    static List shapesList = new ArrayList<Shape>();

    //ivars for all shapes
    protected String txtString;
    protected RectF bounds;
    protected boolean selected;
    protected String name;
    protected BitmapDrawable image;
    protected String shapeScript = "";
    protected boolean visible;
    protected boolean movable;
    protected int viewWidth;
    protected int viewHeight;
    protected float originalX;
    protected float originalY;

    private static final Paint outlinePaint = new Paint();
    private static final int PAINT_COLOR = Color.BLUE;
    private static final Paint.Style PAINT_STYLE = Paint.Style.STROKE;
    private static final float STROKE_WIDTH = 15.0f;

    Shape(View view, RectF bounds, BitmapDrawable drawable, String txtString,
          boolean visible, boolean movable, String name){
        if(name == null || name.equals("")){
            this.name = "shape" + count++;
        } else this.name = name;
        this.visible = visible;
        this.movable = movable;
        this.bounds = bounds;
        this.originalX = bounds.left;
        this.originalY = bounds.top;
        this.viewHeight = view.getHeight();
        this.viewWidth = view.getWidth();
        this.image = drawable;
        this.txtString = txtString;

        outlinePaint.setStyle(PAINT_STYLE);
        outlinePaint.setColor(PAINT_COLOR);
        outlinePaint.setStrokeWidth(STROKE_WIDTH);
    }

    //general draw method
    public void draw(Canvas canvas) {
        if (selected)
            canvas.drawRect(bounds, outlinePaint);
    }

    //specific draw for other canvases
    abstract void draw(Canvas canvas, float xPos, float yPos);

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

    @Override
    public String toString() {
        return name;
    }
}