package edu.stanford.cs108.bunnyworld;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.ArrayList;

public class Page {

    private static int count = 0;
    private boolean isStarterPage = false;
    private ArrayList<Shape> listOfShapes = new ArrayList<>();
    private String name;
    private int pageID, gameID;
    private String backGroundImageName;
    private Bitmap pageRender;

    public boolean getIsStarterPage(){
        return this.isStarterPage;
    }

    public void setIsStarterPage(boolean starterPg){
        this.isStarterPage = starterPg;
    }

    public void addShape(Shape shp){
        if(shp != null) listOfShapes.add(shp);
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

    public int getPageID() {
        return pageID;
    }

    public int getGameID() {
        return gameID;
    }

    //method that adds in a list of shapes --- a setter
    public void setListOfShapes(ArrayList<Shape> arrList){
        listOfShapes = arrList;
    }

    //gets the list of shapes
    public ArrayList<Shape> getListOfShapes(){
        return listOfShapes;
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

    //a constructor that sets the name
    public Page(String name, int count){
        if(name != null) this.name = name;
        else
            this.name = "Page"+count;
    }

    public void setPageRender(Bitmap pageRender) {
        this.pageRender = pageRender;
    }

    public Bitmap getPageRender(){
        return this.pageRender;
    }
}
