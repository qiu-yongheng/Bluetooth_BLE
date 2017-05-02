package com.a520it.rocket.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author qiuyongheng
 * @time 2017/2/20  14:09
 * @desc ${TODD}
 */

public class FloatView extends View{

    private Paint paint;

    public FloatView(Context context) {
        super(context);
        initPaint();
    }

    public FloatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        paint.setDither(true);//开启防抖动
        paint.setStyle(Paint.Style.STROKE);//设置实心
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(200, 200, 100, paint);
    }
}
