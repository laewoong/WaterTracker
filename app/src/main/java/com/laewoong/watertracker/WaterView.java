package com.laewoong.watertracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by laewoong on 2018. 3. 24..
 *
 * https://laewoong.github.io
 */

public class WaterView extends View {

    private static class WaveInfo {

        public static final int VELOCITY = 30;

        private Path mWavePath;

        private int mWaveWidth;
        private int mWaveHeight;

        private int mStartX;
        private int mVelocity;


        public WaveInfo(int width, int height)
        {
            this(width, height, 0, WaveInfo.VELOCITY);
        }

        public WaveInfo(int width, int height, int startX, int velocity)
        {
            mWavePath = new Path();

            mWaveWidth = width;
            mWaveHeight = height;

            mStartX = startX;
            mVelocity = velocity;
            makeWavePath();
        }

        private void makeWavePath()
        {
            float halfWidth = mWaveWidth /2f;
            float halfHeight = mWaveHeight /2f;

            mWavePath = new Path();
            mWavePath.moveTo(0, halfHeight);

            float offset = halfWidth*0.2f;

            mWavePath.cubicTo(halfWidth, halfHeight-offset,
                    halfWidth, halfHeight+offset,
                    mWaveWidth, halfHeight);

            mWavePath.lineTo(mWaveWidth, mWaveHeight);
            mWavePath.lineTo(0, mWaveHeight);
            mWavePath.close();
        }

        public void drawWavePath(final Canvas canvas, final Paint paint) {

            mStartX += mVelocity;

            if(mStartX > mWaveWidth) {
                mStartX = 0;
            }

            float offset = mWaveWidth - mStartX;
            mWavePath.offset(-offset, 0);
            canvas.drawPath(mWavePath, paint);
            mWavePath.offset(mWaveWidth, 0);
            canvas.drawPath(mWavePath, paint);
            mWavePath.offset(-mStartX, 0);
        }
    }

    private static class BubbleInfo {

        public static final int BUBBLE_POP_ON_PATH_LENGTH = 30;
        public static final int BUBBLE_POP_OFF_PATH_LENGTH = 70;
        public final Point mStartPoint;
        public final Point mEndPoint;

        public Point mCurPoint;

        public final float mBubbleRadius;
        public float mCurBubbleRadius;
        public float mOpacity;

        public boolean mIsPop;
        public boolean mIsEnd;
        public Path mBubblePopPath;
        public float mPhase;


        private final int BUBBLE_POP_DEGREES[] = {0, 90, -90};

        public BubbleInfo(Point startPoint, Point endPoint, float radius, float opacity) {

            mStartPoint = startPoint;
            mEndPoint = endPoint;
            mCurPoint = new Point(startPoint);

            mBubbleRadius = radius;
            mCurBubbleRadius = 0f;

            mOpacity = opacity;
            mIsPop = false;
            mIsEnd = false;

            mPhase = BUBBLE_POP_ON_PATH_LENGTH;

            mBubblePopPath = new Path();
            mBubblePopPath.moveTo(mEndPoint.x, mEndPoint.y);
            mBubblePopPath.lineTo(mEndPoint.x, mEndPoint.y - BUBBLE_POP_OFF_PATH_LENGTH);
        }

        public void setPhase(float phase) {
            mPhase = phase;
        }

        public void drawBubble(final Canvas canvas, final Paint paint, final Paint bubblePopPaint) {

            if(mIsPop == false) {

                int alpha = paint.getAlpha();

                paint.setAlpha((int)(mOpacity*100));
                canvas.drawCircle(mCurPoint.x, mCurPoint.y, mCurBubbleRadius, paint);

                paint.setAlpha(alpha);

                if(mOpacity <= 0) {
                    mIsPop = true;
                }
            }
            else {

                PathEffect effect = new DashPathEffect(new float[] {BUBBLE_POP_ON_PATH_LENGTH, BUBBLE_POP_OFF_PATH_LENGTH }, mPhase);
                bubblePopPaint.setPathEffect(effect);

                for(int d : BUBBLE_POP_DEGREES) {
                    canvas.save();
                    canvas.rotate(d, mEndPoint.x, mEndPoint.y);
                    canvas.translate(0, -10);
                    canvas.drawPath(mBubblePopPath, bubblePopPaint);
                    canvas.restore();
                }
            }
        }
    }

    private Paint mWavePaint;
    private Paint mBubblePaint;
    private Paint mBubblePopPaint;

    private WaveInfo mFrontWaveInfo;
    private WaveInfo mBackWaveInfo;

    private float mFraction;
    private boolean mIsStartAnim;
    private int mCanvasWidth;
    private int mCanvasHeight;
    private int mWaveHeight;
    private List<BubbleInfo> mBubbleList;

    private Point mBubbleStartPoint;

    public WaterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWavePaint.setColor(0xff32bafa);
        mWavePaint.setStyle(Paint.Style.FILL);

        mBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBubblePaint.setColor(0xffffffff);
        mBubblePaint.setStyle(Paint.Style.FILL);


        mBubblePopPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBubblePopPaint.setColor(0xff32bafa);
        mBubblePopPaint.setStyle(Paint.Style.STROKE);
        mBubblePopPaint.setStrokeCap(Paint.Cap.ROUND);
        mBubblePopPaint.setStrokeWidth(10f);

        mIsStartAnim = false;
        mFraction = 0.0f;
        mBubbleList = new LinkedList<BubbleInfo>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCanvasWidth = w;
        mCanvasHeight = h;

        mWaveHeight = (int)((w/2f)*0.2f);

        mFrontWaveInfo = new WaveInfo(w, mWaveHeight);
        mBackWaveInfo = new WaveInfo(w, mWaveHeight, (int)(w*(3/4f)), WaveInfo.VELOCITY);
        mBubbleStartPoint = new Point(mCanvasWidth/2, mCanvasHeight);
    }

    public void startAnimation() {
        mIsStartAnim = true;
    }

    public void stopAnimation() {
        mIsStartAnim = false;
    }

    public void setFraction(float fraction) {
        mFraction = fraction;
    }

    public void setBubbleStartPoint(Point point) {
        mBubbleStartPoint = new Point(point);
    }

    public void updateFraction(float fraction) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "fraction", mFraction, fraction);
        anim.setDuration(1000);
        anim.start();

        int min = 3;
        int max= 5;

        int randomNum = randomRange(min, max);

        mBubbleList.clear();

        int dy = (int)((float)(mCanvasHeight-mWaveHeight) * (1f-fraction) + mWaveHeight/2);

        int cX = (int)(mCanvasWidth/2f);
        int offSet = 300;

        for(int i=0; i<randomNum; i++) {
            int endX = randomRange(cX-offSet, cX + offSet);
            final BubbleInfo b = new BubbleInfo(new Point(mBubbleStartPoint.x, mBubbleStartPoint.y),
                    new Point(endX,dy),50, 1f);
            mBubbleList.add(b);
            ValueAnimator va = ValueAnimator.ofInt(mBubbleStartPoint.y, dy);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (int)valueAnimator.getAnimatedValue();
                    float fraction = valueAnimator.getAnimatedFraction();
                    b.mCurPoint.y = val;
                    b.mCurPoint.x = b.mStartPoint.x + (int)((b.mEndPoint.x - b.mStartPoint.x) * fraction);
                    b.mCurBubbleRadius = b.mBubbleRadius *fraction;
                    b.mOpacity = 1 - fraction;
                }
            });
            va.setInterpolator(new AccelerateDecelerateInterpolator());
            va.setStartDelay(100*i);
            va.setDuration((long)(1500 * mFraction));

            ObjectAnimator popAnim = ObjectAnimator.ofFloat(b, "phase", BubbleInfo.BUBBLE_POP_ON_PATH_LENGTH, -BubbleInfo.BUBBLE_POP_OFF_PATH_LENGTH);
            popAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    b.mIsEnd = true;
                }
            });

            popAnim.setDuration(300);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(va, popAnim);
            animatorSet.start();
        }
    }

    private int randomRange(int n1, int n2) {
        return (int) (Math.random() * (n2 - n1 + 1)) + n1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mWavePaint.setAlpha(100);

        int dy = (int)((float)(mCanvasHeight-mWaveHeight) * (1f-mFraction));
        canvas.save();
        canvas.translate(0, dy);
        mBackWaveInfo.drawWavePath(canvas, mWavePaint);
        mWavePaint.setAlpha(255);
        mFrontWaveInfo.drawWavePath(canvas, mWavePaint);
        canvas.restore();

        // draw wave under part
        canvas.drawRect(0, mWaveHeight + dy -1, mCanvasWidth, mCanvasHeight, mWavePaint);

        //draw bubble pop motion
        if(mBubbleList.isEmpty() == false)
        {
            final int bubbleSize = mBubbleList.size();
            for(BubbleInfo bubble : mBubbleList) {
                bubble.drawBubble(canvas, mBubblePaint, mBubblePopPaint);
            }
        }

        // remove bubble info in the list when motion is over.
        Iterator<BubbleInfo> itor = mBubbleList.iterator();
        while (itor.hasNext()) {
            BubbleInfo bubbleInfo = itor.next(); // must be called before you can call itor.remove()
            if(bubbleInfo.mIsEnd)
            {
                itor.remove();
            }
        }

        if(mIsStartAnim) {

            invalidate();
        }
    }
}
