package com.gome.gmweatherview.graphics;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.gome.gmweatherview.base.AbsPainter;
import com.gome.gmweatherview.util.CubicBezierInterpolator;

/**
 * @author Felix.Liang
 */
public class SunnyPainter extends AbsPainter {

    private static final int TAG_SCALE = 1;
    private static final int TAG_ROTATE = 2;
    private static final int NUMBER_OF_LINES = 6;
    private static final int NUMBER_OF_SIDES = 6;
    private static final int MAX_LINE_ALPHA = 180;
    private static final int MIN_LINE_ALPHA = 10;

    private final int mCornerRadius = dp2px(20);
    private final int mLineWidth = 4;
    private Paint[] mLinePaints;
    private float[] mScales;
    private float[] mRotates;
    private Path[] mLines;
    private int[] mColors = {0x00FFFFFF, Color.WHITE, Color.WHITE, 0x00FFFFFF};
    private float[] mPositions = {0.05f, 0.35f, 0.65f, 0.95f};
    private AnimatorSet mAnimatorSet;
    private Paint mBgPaint;
    private int[] mBgColors = {0xFFEF643C, 0xFFDEE3D1, 0xFFD6EAE3};
    private float[] mBgPos = {0, 0.8F, 1};
    private LinearGradient mShader;

    public SunnyPainter(Context context) {
        super(context);
        init();
    }

    @Override
    public void onAttachedToView() {
        super.onAttachedToView();
        if (mAnimatorSet != null) mAnimatorSet.start();
    }

    private void init() {
        mLinePaints = new Paint[NUMBER_OF_LINES];
        mScales = new float[NUMBER_OF_LINES];
        mRotates = new float[NUMBER_OF_LINES];
        for (int i = 0; i < NUMBER_OF_LINES; i++) {
            mScales[i] = 1;
            mRotates[i] = (4 - i) * 2;
        }
        mLines = new Path[NUMBER_OF_LINES];
        for (int i = 0; i < mLines.length; i++) {
            mLines[i] = new Path();
        }
        initPaints();
    }

    private void initPaints() {
        final int dAlpha = (MAX_LINE_ALPHA - MIN_LINE_ALPHA) / (NUMBER_OF_LINES - 1);
        final CornerPathEffect effect = new CornerPathEffect(mCornerRadius);
        final Matrix shadowMatrix = new Matrix();
        shadowMatrix.setRotate(-360 / NUMBER_OF_SIDES);
        final SweepGradient sweepGradient = new SweepGradient(0, 0, mColors, mPositions);
        sweepGradient.setLocalMatrix(shadowMatrix);
        for (int i = 0; i < mLinePaints.length; i++) {
            mLinePaints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLinePaints[i].setStyle(Paint.Style.STROKE);
            mLinePaints[i].setStrokeWidth(mLineWidth);
            mLinePaints[i].setPathEffect(effect);
            mLinePaints[i].setShader(sweepGradient);
            mLinePaints[i].setAlpha(MAX_LINE_ALPHA - dAlpha * i);
        }
        mBgPaint = new Paint(Paint.DITHER_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h) {
        super.onSizeChanged(w, h);
        updatePath();
        if (mShader == null) {
            mShader = new LinearGradient(0, 0, 0, getHeight(), mBgColors, mBgPos, Shader.TileMode.CLAMP);
            mBgPaint.setShader(mShader);
        }
        if (mAnimatorSet == null) {
            mAnimatorSet = getWeatherAnimatorSet();
        }
        mAnimatorSet.start();
    }

    private void updatePath() {
        final float minRadius = (Math.min(getWidth(), getHeight()) / 2 - mLineWidth / 2) * 0.8f;
        final float degree = 360f / NUMBER_OF_SIDES;
        final int dRadius = dp2px(3);
        float radius = minRadius;
        for (int i = 0; i < mLines.length; i++) {
            if (mLines[i] == null) mLines[i] = new Path();
            else mLines[i].reset();
            for (int j = 0; j < NUMBER_OF_SIDES; j++) {
                float currX = (float) (radius * Math.sin(Math.toRadians(j * degree)));
                float currY = (float) (-radius * Math.cos(Math.toRadians(j * degree)));
                if (j == 0) mLines[i].moveTo(currX, currY);
                else mLines[i].lineTo(currX, currY);
            }
            mLines[i].close();
            radius += dRadius;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private AnimatorSet getWeatherAnimatorSet() {
        mAnimatorSet = new AnimatorSet();
        ValueAnimator[][] animators = new ValueAnimator[2][NUMBER_OF_LINES];
        for (int i = 0; i < animators.length; i++) {
            final boolean isRotate = i == 1;
            for (int j = 0; j < animators[i].length; j++) {
                final int finalJ = j;
                animators[i][j] = ValueAnimator.ofFloat(0, 1);
                TimeInterpolator interpolator;
                interpolator = isRotate ? new CubicBezierInterpolator(.2f, .27f, .7f, .92f) : new AccelerateDecelerateInterpolator();
                animators[i][j].setInterpolator(interpolator);
                animators[i][j].setDuration(isRotate ? 40500 : 16200);
                animators[i][j].setRepeatCount(ValueAnimator.INFINITE);
                if (isRotate) animators[i][j].setStartDelay(240 * j);
                animators[i][j].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        if (isRotate)
                            updateView(TAG_ROTATE, finalJ, value);
                        else
                            updateView(TAG_SCALE, finalJ, value);
                    }
                });
            }
        }
        mAnimatorSet.playTogether((Animator[]) animators[0]);
        mAnimatorSet.playTogether((Animator[]) animators[1]);
        return mAnimatorSet;
    }

    @Override
    public void onDetachedFromView() {
        super.onDetachedFromView();
        if (mAnimatorSet != null) mAnimatorSet.end();
    }

    private void updateView(int tag, Object... params) {
        onUpdateWithParameters(tag, params);
    }

    private void onUpdateWithParameters(int tag, Object[] params) {
        final int index = (int) params[0];
        final float value = (float) params[1];
        if (tag == TAG_SCALE) {
            mScales[index] = (float) Math.sin(Math.PI * value) * (index + 1) * 0.04f + 1;
        } else if (tag == TAG_ROTATE) {
            mRotates[index] = 360 * value + (4 - index) * 2;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPaint(mBgPaint);
        canvas.translate(getWidth() / 2, getHeight() * 0.3f);
        drawLines(canvas);
    }

    private void drawLines(Canvas canvas) {
        for (int i = 0; i < mLines.length; i++) {
            canvas.save();
            canvas.scale(mScales[i], mScales[i]);
            canvas.rotate(-mRotates[i]);
            canvas.drawPath(mLines[i], mLinePaints[i]);
            canvas.restore();
        }
    }
}