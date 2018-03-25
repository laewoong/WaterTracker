package com.laewoong.watertracker;

import android.animation.ObjectAnimator;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private WaterView mWaterView;
    private Button mDrinkButton;
    private int mCurPercent;
    private int mTargetPercent;
    private TextView mCurPercentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWaterView = (WaterView)findViewById(R.id.waterView);
        mCurPercentView = (TextView)findViewById(R.id.textview_percent);
        mDrinkButton = (Button)findViewById(R.id.button_drink);

        mTargetPercent = 50;
        setPercent(mTargetPercent);

        mWaterView.setFraction(mCurPercent/100f);

        mDrinkButton.setOnClickListener(new View.OnClickListener() {
            private ObjectAnimator animator;

            @Override
            public void onClick(View view) {

                if(animator != null) {
                    animator.cancel();
                    animator = null;
                    mCurPercent = mTargetPercent;
                }

                if((mCurPercent+5) > 100) {

                    animator = ObjectAnimator.ofInt(MainActivity.this, "percent", mCurPercent, 0);
                    animator.setDuration(300);
                    animator.start();
                    mTargetPercent = 0;
                    mCurPercent = mTargetPercent;
                    mWaterView.updateFraction(mCurPercent);

                    return;
                }

                mTargetPercent = mTargetPercent + 5;
                animator = ObjectAnimator.ofInt(MainActivity.this, "percent", mCurPercent, mTargetPercent);
                animator.setDuration(300);
                animator.start();

                mCurPercent = mTargetPercent;
                mWaterView.updateFraction(mCurPercent/100f);

            }
        });
    }

    public void setPercent(int percent) {
        mCurPercent = percent;
        mCurPercentView.setText(String.valueOf(percent));
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDrinkButton.post(new Runnable() {
            @Override
            public void run() {
                mWaterView.setBubbleStartPoint(new Point((int)(mDrinkButton.getLeft() + mDrinkButton.getMeasuredWidth()/2), (int)(mDrinkButton.getTop() + mDrinkButton.getMeasuredHeight()/2)));
            }
        });

        mWaterView.startAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mWaterView.stopAnimation();
    }
}
