package com.gome.gmweatherview.graphics;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.SweepGradient;

import com.gome.gmweatherview.base.AbsPainter;

/**
 * @author Felix.Liang
 */
public class OvercastPainter extends AbsPainter {

    private static final int NUMBER_OF_LINE = 20;
    private static final float DELTA_DEGREE = 8;
    private Paint mLinePaint;
    private Paint mBgPaint;
    private Path mBasePath;
    private Path mTemp = new Path();
    private int mWaveWidth;
    private int mAmplitude;
    private Camera mCamera = new Camera();
    private Matrix mMatrix = new Matrix();
    private float mXOffset;
    private LinearGradient mBgShader;
    private SweepGradient mLightShader;
    private int[] mColors = {0xFF43484D, 0xFFFFFFFF, 0xFF43484D};
    private float[] mPositions = {0.35f, 0.5f, 0.65f};
    private int mCanvasXOffset = dp2px(130);

    public OvercastPainter(Context context) {
        super(context);
        initPaint();
    }

    private void initPaint() {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(dp2px(1));
        mBgPaint = new Paint(Paint.DITHER_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h) {
        super.onSizeChanged(w, h);
        mWaveWidth = dp2px(300);
        mAmplitude = (int) (mWaveWidth * 0.7f);
        if (mBgShader == null) {
            mBgShader = new LinearGradient(0, 0, 0, getHeight(), 0xFF495052, 0xFFA2B2B4, Shader.TileMode.CLAMP);
            mBgPaint.setShader(mBgShader);
        }
        if (mLightShader == null) {
            Matrix matrix = new Matrix();
            matrix.preRotate(-90);
            mLightShader = new SweepGradient(getHeight() / 3, mCanvasXOffset, mColors, mPositions);
            mLightShader.setLocalMatrix(matrix);
            mLinePaint.setShader(mLightShader);
        }
        initPath();
    }

    private void initPath() {
        if (mBasePath == null) mBasePath = new Path();
        else mBasePath.reset();
        int x = (int) (-5f * getWidth());
        mBasePath.moveTo((float) (-5 * getWidth()), 0);
        boolean mOdd = true;
        while (x < getWidth()) {
            mBasePath.rQuadTo(mWaveWidth, mOdd ? -mAmplitude : mAmplitude,
                    2 * mWaveWidth, 0);
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
        canvas.translate(getWidth() / 2 - mCanvasXOffset, getHeight() / 3);
        canvas.drawPaint(mBgPaint);
        final int dAlpha = 255 / (NUMBER_OF_LINE - 1);
        int alpha = 255;
        mCamera.save();
        float off = 0, off2 = 0;
        for (int i = 0; i < NUMBER_OF_LINE; i++) {
            mCamera.save();
            mCamera.translate(mXOffset + off2, off2, off);
            mCamera.rotateX(i * DELTA_DEGREE);
            mMatrix.reset();
            mCamera.getMatrix(mMatrix);
            mCamera.restore();
            mTemp.reset();
            mBasePath.transform(mMatrix, mTemp);
            mLinePaint.setAlpha(alpha);
            canvas.drawPath(mTemp, mLinePaint);
            alpha -= dAlpha;
            off2 += 5;
            off += 25;
        }
        mCamera.restore();
        mXOffset += 3;
        mXOffset %= (mWaveWidth * 4);
    }
}
