package com.gome.gmweatherview.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.Shader;

import com.gome.gmweatherview.base.AbsPainter;

/**
 * @author Felix.Liang
 */
public class FoggyPainter extends AbsPainter {

    private static final int NUMBER_OF_LINES = 5;
    private static final int NUMBER_OF_SIDES = 6;
    private static final float DELTA_SCALE = 0.13f;
    private Shader mShader;
    private Paint mBgPaint;
    private Paint[] mFogPaints = new Paint[NUMBER_OF_LINES];
    private float[] mScales = new float[NUMBER_OF_LINES];
    private float mMinRadius;
    private Path mBasePath;
    private Matrix mMatrix = new Matrix();
    private Path mLineDst = new Path();
    private float mBaseSpace = dp2px(5);
    private Path mShapePath;
    private final float mMaxScale = 1 + NUMBER_OF_LINES * DELTA_SCALE;

    public FoggyPainter(Context context) {
        super(context);
        init();
    }

    private void init() {
        initScales();
        initPaints();
    }

    private void initScales() {
        for (int i = 0; i < mScales.length; i++) {
            mScales[i] = 1 + i * DELTA_SCALE;
        }
    }

    private void initPaints() {
        mBgPaint = new Paint(Paint.DITHER_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        for (int i = 0; i < mFogPaints.length; i++) {
            mFogPaints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            mFogPaints[i].setStyle(Paint.Style.STROKE);
            mFogPaints[i].setColor(Color.WHITE);
        }
        mShapePath = new Path();
        mShapePath.addCircle(0, 0, 4, Path.Direction.CCW);
    }

    @Override
    protected void onSizeChanged(int w, int h) {
        super.onSizeChanged(w, h);
        if (mShader == null) {
            mShader = new LinearGradient(0, 0, 0, getHeight(), 0xFFABB3B7, 0xFFD0D9DA, Shader.TileMode.CLAMP);
            mBgPaint.setShader(mShader);
        }
        mMinRadius = dp2px(70);
        createPath();
    }

    private void createPath() {
        final float degree = 360f / NUMBER_OF_SIDES;
        float radius = mMinRadius;
        if (mBasePath == null) {
            mBasePath = new Path();
            for (int j = 0; j < NUMBER_OF_SIDES; j++) {
                float currX = (float) (radius * Math.sin(Math.toRadians(j * degree)));
                float currY = (float) (-radius * Math.cos(Math.toRadians(j * degree)));
                if (j == 0) mBasePath.moveTo(currX, currY);
                else mBasePath.lineTo(currX, currY);
            }
            mBasePath.close();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPaint(mBgPaint);
        canvas.translate(getWidth() * 0.55f, getHeight() / 3);
        drawMainLines(canvas);
        drawSubLines(canvas);
    }

    private void drawSubLines(Canvas canvas) {
        canvas.translate(-(1 + mMaxScale) * 0.5f * mMinRadius, mMinRadius * 0.7f);
        canvas.scale(0.6f, 0.6f);
        drawMainLines(canvas);
    }

    private void drawMainLines(Canvas canvas) {
        for (int i = 0; i < mScales.length; i++) {
            float scale = mScales[i];
            canvas.save();
            mMatrix.reset();
            mMatrix.setScale(scale, scale);
            mBasePath.transform(mMatrix, mLineDst);
            updatePathEffect(i, scale);
            canvas.drawPath(mLineDst, mFogPaints[i]);
            canvas.restore();
            float delta = calculateDelta(scale);
            scale += delta;
            if (scale > mMaxScale) {
                scale = 1 + scale % mMaxScale;
            }
            setScale(i, scale);
        }
    }

    private float calculateDelta(float scale) {
        scale--;
        return (float) (scale * 0.0002 + 0.00025);
    }

    private void updatePathEffect(int index, float scale) {
        float space = mBaseSpace * scale;
        PathDashPathEffect effect = new PathDashPathEffect(mShapePath, space, 0, PathDashPathEffect.Style.TRANSLATE);
        mFogPaints[index].setPathEffect(effect);
    }

    private void setScale(int index, float scale) {
        mScales[index] = scale;
        mFogPaints[index].setAlpha(calculateAlpha(scale));
    }

    private int calculateAlpha(float scale) {
        scale -= 1;
        if (scale < (mMaxScale - 1) / 2) {
            return (int) ((2 * 255) / (mMaxScale - 1) * scale);
        } else {
            return (int) ((2 * 255) / (1 - mMaxScale) * (scale - mMaxScale + 1));
        }
    }
}
