package com.gome.gmweatherview.entity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;

import java.util.Random;

/**
 * @author Felix.Liang
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Ripple {

    private Random sRandom = new Random();
    private int numberOfRipple;
    private Paint[] mPaints;
    private float x;
    private float y;
    private ValueAnimator[] rippleAnimators = new ValueAnimator[3];
    private float[] radius = new float[3];
    private float maxRadius;
    private OnRippleChangeListener mRippleListener;
    private AnimatorSet mRippleSet = new AnimatorSet();
    private boolean mStop;

    public Ripple(float cx, float cy) {
        x = cx;
        y = cy;
        updateVariables();
        if (mPaints == null) {
            mPaints = new Paint[3];
            for (int i = 0; i < mPaints.length; i++) {
                if (mPaints[i] == null) {
                    mPaints[i] = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
                    mPaints[i].setColor(Color.WHITE);
                    mPaints[i].setStyle(Paint.Style.STROKE);
                    mPaints[i].setStrokeWidth(4);
                }
            }
        }
    }

    public void rippleOut() {
        startRippleAnimator();
    }

    private void startRippleAnimator() {
        initRippleAnimator();
        int duration = (int) (maxRadius * 17);
        int delay = sRandom.nextInt(duration / 2);
        for (int i = 0; i < numberOfRipple; i++) {
            radius[i] = 0;
            rippleAnimators[i].setDuration(duration);
            rippleAnimators[i].setStartDelay(delay * (i + 1));
            rippleAnimators[i].removeAllListeners();
            if (i == numberOfRipple - 1) {
                rippleAnimators[i].addListener(mAnimatorListener);
            }
            mRippleSet.play(rippleAnimators[i]);
        }
        mStop = false;
        mRippleSet.start();
    }

    private void initRippleAnimator() {
        for (int i = 0; i < rippleAnimators.length; i++) {
            if (rippleAnimators[i] == null) {
                final int finalI = i;
                rippleAnimators[finalI] = ValueAnimator.ofFloat(0, 1);
                rippleAnimators[finalI].setInterpolator(new DecelerateInterpolator());
                rippleAnimators[finalI].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        float v = (float) animator.getAnimatedValue();
                        radius[finalI] = v * maxRadius;
                        mPaints[finalI].setAlpha((int) (180 * (1 - v)));
                    }
                });
            }
        }
    }

    private Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animation) {
            if (mRippleListener != null && !mStop) mRippleListener.onRippleEnd(Ripple.this);
        }

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };

    public void draw(Canvas canvas) {
        for (int i = 0; i < numberOfRipple; i++) {
            canvas.drawCircle(x, y, radius[i], mPaints[i]);
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setLocation(float cx, float cy) {
        x = cx;
        y = cy;
        updateVariables();
    }

    private void updateVariables() {
        final float f = sRandom.nextFloat();
        if (f < 0.6f) numberOfRipple = 1;
        else if (f < 0.95f) numberOfRipple = 2;
        else numberOfRipple = 3;
        final float f2 = sRandom.nextFloat();
        if (f2 < 0.5f)
            maxRadius = sRandom.nextInt(50) + 60;
        else if (f2 < 0.8f)
            maxRadius = sRandom.nextInt(150) + 60;
        else maxRadius = sRandom.nextInt(350) + 60;
    }

    public void setOnRippleListener(OnRippleChangeListener listener) {
        mRippleListener = listener;
    }

    public interface OnRippleChangeListener {

        void onRippleEnd(Ripple ripple);
    }

    public void stop() {
        mStop = true;
        if (mRippleSet != null && mRippleSet.isRunning())
            mRippleSet.end();
    }
}
