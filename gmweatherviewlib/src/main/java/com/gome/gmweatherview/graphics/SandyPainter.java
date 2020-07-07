package com.gome.gmweatherview.graphics;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;

import com.gome.gmweatherview.base.AbsPainter;

/**
 * @author Felix.Liang
 */
public class SandyPainter extends AbsPainter {

    private static final int NUMBER_OF_LINE = 18;
    private static final int DELTA_DEGREE = 10;
    private Paint mBgPaint;
    private LinearGradient mShader;
    private Paint mLinePaint;
    private Camera mCamera = new Camera();
    private Matrix mMatrix = new Matrix();
    private int mXOffset;
    private int mWaveWidth = dp2px(350);
    private float mAmplitude = (int) (mWaveWidth * 0.2f);
    private int mDeltaX = dp2px(3f);
    private float mDegree;
    private float[] mSrcPoints = new float[450];
    private float[] mDstPoints = new float[450];

    public SandyPainter(Context context) {
        super(context);
        initPaints();
    }

    private void initPaints() {
        mBgPaint = new Paint(Paint.DITHER_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mLinePaint.setColor(0xFF5B4923);
        mLinePaint.setStrokeWidth(dp2px(1.5f));
    }

    @Override
    protected void onSizeChanged(int w, int h) {
        super.onSizeChanged(w, h);
        if (mShader == null) {
            mShader = new LinearGradient(0, 0, 0, getHeight(), 0xFF877849, 0xFFBFBDA0, Shader.TileMode.CLAMP);
            mBgPaint.setShader(mShader);
        }
        initPoints();
    }

    private void initPoints() {
        float omega = (float) (Math.PI / mWaveWidth);
        for (int i = 0; i < mSrcPoints.length; i++) {
            if (i % 2 == 0) {
                mSrcPoints[i] = i * mDeltaX - mWaveWidth;
            } else {
                float pre = mSrcPoints[i - 1];
                mSrcPoints[i] = (float) (Math.sin(omega * pre) * mAmplitude);
            }
        }
    }

    private void transformPoints(Matrix matrix) {
        matrix.mapPoints(mDstPoints, mSrcPoints);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPaint(mBgPaint);
        canvas.rotate(-20);
        canvas.translate(0, getHeight() / 2);
        float d = 0, offset = 0;
        for (int i = 0; i < NUMBER_OF_LINE; i++) {
            mCamera.save();
            mCamera.translate(200 + mXOffset + offset, 0, 0);
            mCamera.rotateX(d + mDegree);
            mMatrix.reset();
            mCamera.getMatrix(mMatrix);
            mCamera.restore();
            canvas.save();
            transformPoints(mMatrix);
            canvas.drawPoints(mDstPoints, mLinePaint);
            canvas.restore();
            d += DELTA_DEGREE;
            offset += 20;
        }
        mDegree -= 0.5;
        mDegree %= 360;
        mXOffset--;
        mXOffset %= (mWaveWidth * 2);
    }
}
