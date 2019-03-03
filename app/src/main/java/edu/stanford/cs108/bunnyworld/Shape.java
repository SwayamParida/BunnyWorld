package edu.stanford.cs108.bunnyworld;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class Shape {
    //static stack shape for handling undo actions
    static int count = 0;
    static Stack<Shape> Shapes;

    //an Array List for editing the shapes
    static Paint defaultPaint = new Paint();
    static List shapesList = new ArrayList<Shape>();

    Shape(){
        defaultPaint.setColor(Color.rgb(211,211,211));
    }
}