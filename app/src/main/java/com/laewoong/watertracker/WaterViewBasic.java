package com.laewoong.watertracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by laewoong on 2018. 3. 24..
 *
 * https://laewoong.github.io
 */

public class WaterViewBasic extends View {

    private static final int VELOCITY = 30;

    private Path    mPath;
    private Paint   mPaint;

    private int mCanvasWidth;
    private int mCanvasHeight;

    private int mStartX;
    private boolean mIsStartAnim;

    public WaterViewBasic(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10f);

        mStartX = 0;
        mIsStartAnim = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCanvasWidth = w;
        mCanvasHeight = h;

        makeWavePath();
    }

    private void makeWavePath() {
        int halfWidth = mCanvasWidth/2;
        int halfHeight = mCanvasHeight/2;

        mPath = new Path();
        mPath.moveTo(0, halfHeight);
        float offset = 200f;
        mPath.cubicTo(halfWidth, halfHeight-offset,
                halfWidth, halfHeight+offset,
                mCanvasWidth, halfHeight);
    }

    public void startAnimation() {
        mIsStartAnim = true;
    }

    public void stopAnimation() {
        mIsStartAnim = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float offset = mCanvasWidth - mStartX;
        mPath.offset(-offset, 0);
        canvas.drawPath(mPath, mPaint);
        mPath.offset(mCanvasWidth, 0);
        canvas.drawPath(mPath, mPaint);
        mPath.offset(-mStartX, 0);

        if(mIsStartAnim) {
            mStartX = mStartX +VELOCITY;

            if(mStartX > mCanvasWidth) {
                mStartX = 0;
            }

            invalidate();
        }
    }
}
