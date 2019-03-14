package edu.stanford.cs108.bunnyworld;

import android.graphics.Bitmap;
import android.view.View;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

//Citation: Adapted some of this from code used in handouts/class

public class PreviewCustomView extends View {
    private Paint mPaint;
    private Bitmap bitmap;



    public PreviewCustomView(Context context, AttributeSet attr) {
        super(context, attr);
    }



    public void setText(String txt){
        TextView txtView = (TextView) ((Activity) getContext()).findViewById(R.id.name);
        txtView.setText(txt);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //del
        bitmap = createImage(100, 100, Color.RED);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        //keep
        canvas.drawBitmap(bitmap, 0, 0, mPaint);
        super.onDraw(canvas);

    }


    public static Bitmap createImage(int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        return bitmap;
    }

}

