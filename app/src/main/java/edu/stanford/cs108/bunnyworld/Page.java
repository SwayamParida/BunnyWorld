package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;

import java.util.ArrayList;

public class Page {

    private boolean isStarterPage = false;
    private ArrayList<Shape> listOfShapes = new ArrayList<>();
    private String name;
    private String backGroundImageName;

    public boolean getIsStarterPage(){
        return this.isStarterPage;
    }

    public void setIsStarterPage(boolean starterPg){
        this.isStarterPage = starterPg;
    }

    public void addShape(Shape shp){
        listOfShapes.add(shp);
    }

    public void deleteShape(Shape shp){
        listOfShapes.remove(shp);
    }

    public String getName(){
        return this.name;
    }

    public void setName(String newName){
        this.name = newName;
    }

    public void draw(Canvas canvas) {
        for (Shape shape : listOfShapes)
            shape.draw(canvas);
    }
    public Shape findLastShape(float x, float y) {
        Shape lastFound = null;
        for (Shape shape : listOfShapes) {
            if (shape.containsPoint(x, y))
                lastFound = shape;
        }
        return lastFound;
    }


}
