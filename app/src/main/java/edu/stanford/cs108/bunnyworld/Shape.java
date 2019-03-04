package edu.stanford.cs108.bunnyworld;

import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class Shape {
    static int count = 0;

    //an Array List for editing the shapes
    private static Paint defaultPaint = new Paint();
    static List shapesList = new ArrayList<Shape>();

    Shape(){
        defaultPaint.setColor(Color.rgb(211,211,211));
    }
}