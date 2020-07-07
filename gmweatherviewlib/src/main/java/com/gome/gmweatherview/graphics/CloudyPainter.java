package com.gome.gmweatherview.graphics;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

import com.gome.gmweatherview.base.AbsPainter;

/**
 * @author Felix.Liang
 */
public class CloudyPainter extends AbsPainter {

    private static final int NUMBER_OF_LINE = 16;
    private static final float DELTA_DEGREE = 10;
    private static final int MIN_ALPHA = 80;
    private static final int MAX_ALPHA = 200;
    private Paint mLinePaint;
    private Paint mBgPaint;
    private Path mBasePath;
    private Path mTemp = new Path();
    private int mWaveWidth;
    private int mAmplitude;
    private Camera mCamera = new Camera();
    private Matrix mMatrix = new Matrix();
    private float mXOffset;
    private LinearGradient mShader;
    private final int mCanvasXOffset = dp2px(130);

    public CloudyPainter(Context context) {
        super(context);
        initPaint();
    }

    private void initPaint() {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStrokeWidth(dp2px(1f));
        mBgPaint = new Paint(Paint.DITHER_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h) {
        super.onSizeChanged(w, h);
        mWaveWidth = 900;
        mAmplitude = (int) (mWaveWidth * 0.5f);
        if (mShader == null) {
            mShader = new LinearGradient(0, 0, 0, getHeight(), 0xFF0B8AC6, 0xFFFFFFFF, Shader.TileMode.CLAMP);
            mBgPaint.setShader(mShader);
        }
        initPath();
    }

    private void initPath() {
        if (mBasePath == null) mBasePath = new Path();
        else mBasePath.reset();
        int x = -5 * getWidth();
        mBasePath.moveTo(-5 * getWidth(), 0);
        boolean mOdd = false;
        while (x < getWidth()) {
            mBasePath.rQuadTo(mWaveWidth,
                    mOdd ? -mAmplitude : mAmplitude
                    , 2 * mWaveWidth, 0);
            x += mWaveWidth * 2;
            mOdd = !mOdd;
        }
    }

    @Override
    public void onDetachedFromView() {
        super.onDetachedFromView();
        reset();
    }

    private void reset() {
        mXOffset = 0;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPaint(mBgPaint);
        canvas.rotate(20);
        canvas.translate(getWidth() + mCanvasXOffset, getHeight() / 3);
        canvas.scale(1.1f, 1.1f);
        int alpha = MIN_ALPHA;
        final int dAlpha = (MAX_ALPHA - MIN_ALPHA) / (NUMBER_OF_LINE - 1);
        for (int i = 0; i < NUMBER_OF_LINE; i++) {
            mCamera.save();
            mCamera.translate(-500 + mXOffset, 0, i * 20);
            mCamera.rotateX(i * DELTA_DEGREE);
            mMatrix.reset();
            mCamera.getMatrix(mMatrix);
            mCamera.restore();
            mTemp.reset();
            mBasePath.transform(mMatrix, mTemp);
            mLinePaint.setAlpha(alpha);
            canvas.drawPath(mTemp, mLinePaint);
            alpha += dAlpha;
        }
        mXOffset += 3;
        mXOffset %= (mWaveWidth * 4);
    }
}
