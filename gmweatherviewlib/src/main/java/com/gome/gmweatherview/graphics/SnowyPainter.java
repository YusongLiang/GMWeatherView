package com.gome.gmweatherview.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import com.gome.gmweatherview.base.AbsPainter;

/**
 * @author Felix.Liang
 */
public class SnowyPainter extends AbsPainter {

    private static final int NUMBER_OF_LINES = 7;
    private static final double COS = Math.cos(Math.toRadians(30));
    private static final double SIN = Math.sin(Math.toRadians(30));
    private Shader mShader;
    private Paint mBgPaint;
    private Paint mSmallSnowflakePaint;
    private Paint mSnowflakePaint;
    private Paint mAxlePaint;
    private Paint mEdgePaint;
    private float mRotateDegree;
    private final int mMaxRadius = dp2px(100);
    private final float mAxleLength = 1.5f * mMaxRadius;
    private final int mDeltaRadius = dp2px(10);
    private final int mMinRadius = mMaxRadius - mDeltaRadius * NUMBER_OF_LINES;
    private float mCurrentRadius;
    private final float mEdgeRadius = mMaxRadius * 1.1f;
    private final float mEdge2Radius = mMaxRadius * 1.3f;
    private final float mEdgeLength = mMaxRadius * 0.2f;
    private final float mEdge2Length = mMaxRadius * 0.1f;
    private float mX = (float) (mEdgeLength * COS);
    private float mX2 = (float) (mEdge2Length * COS);
    private float mY = (float) (mEdgeRadius + mEdgeLength * SIN);
    private float mY2 = (float) (mEdge2Radius + mEdge2Length * SIN);
    private PathMeasure mPathMeasure;
    private Bitmap mSmallSnowBmp;
    private int mSmallSnowflakeBorderSize = dp2px(100);
    private float[] mPos = new float[2];
    private float[] mTan = new float[2];
    private float mRouteLength;
    private int mCurrentDistance;
    private float mSnowOffset;

    public SnowyPainter(Context context) {
        super(context);
        init();
    }

    private void init() {
        initPaints();
    }

    private void initPaints() {
        mBgPaint = new Paint(Paint.DITHER_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSmallSnowflakePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSmallSnowflakePaint.setFilterBitmap(true);
        mSnowflakePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSnowflakePaint.setColor(Color.WHITE);
        mSnowflakePaint.setStrokeWidth(dp2px(1.5f));
        mSnowflakePaint.setStyle(Paint.Style.STROKE);
        mSnowflakePaint.setStrokeCap(Paint.Cap.ROUND);
        mAxlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAxlePaint.setColor(0xFFCAD6D9);
        mAxlePaint.setStyle(Paint.Style.STROKE);
        mAxlePaint.setAlpha(100);
        mAxlePaint.setStrokeWidth(dp2px(2));
        mEdgePaint = new Paint();
        mEdgePaint.set(mAxlePaint);
        mAxlePaint.setShader(new RadialGradient(0, 0, mAxleLength, Color.WHITE, 0x80CAD6D9, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onSizeChanged(int w, int h) {
        super.onSizeChanged(w, h);
        if (mShader == null) {
            mShader = new LinearGradient(0, 0, 0, getHeight(), 0xFFDCE5E9, 0xFF7A9593, Shader.TileMode.CLAMP);
            mBgPaint.setShader(mShader);
        }
        mCurrentRadius = mMaxRadius;
        if (mPathMeasure == null)
            initPathMeasure();
    }

    private void initPathMeasure() {
        Path route = new Path();
        route.moveTo(getWidth() / 4, getHeight() / 4);
        route.rCubicTo(-getWidth() / 16, getHeight() / 6, getWidth() / 3, getHeight() / 5, 0, getHeight() / 2);
        mPathMeasure = new PathMeasure();
        mPathMeasure.setPath(route, false);
        mRouteLength = mPathMeasure.getLength();
        mSnowOffset = mRouteLength / 2;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPaint(mBgPaint);
        drawSmallSnowflake(canvas, true);
        drawSmallSnowflake(canvas, false);
        drawMainSnowflake(canvas);
    }

    private void drawMainSnowflake(Canvas canvas) {
        canvas.save();
        canvas.translate(getWidth() * 0.8f, getHeight() * 0.15f);
        canvas.rotate(mRotateDegree);
        drawSnowflake(canvas);
        mCurrentRadius++;
        if (mCurrentRadius > mMaxRadius) {
            mCurrentRadius = mMinRadius + (mCurrentRadius - mMaxRadius);
        }
        mRotateDegree += 0.05;
        canvas.restore();
    }

    private void drawSmallSnowflake(Canvas canvas, boolean isFirst) {
        if (mPathMeasure != null) {
            canvas.save();
            float distance;
            if (!isFirst) distance = (mCurrentDistance + mSnowOffset) % mRouteLength;
            else distance = mCurrentDistance;
            mPathMeasure.getPosTan(distance, mPos, mTan);
            canvas.translate(mPos[0], mPos[1]);
            float degree = (float) Math.toDegrees(Math.atan2(mTan[1], mTan[0]));
            canvas.rotate(degree);
            if (mSmallSnowBmp == null) {
                initSmallSnowBmp();
            }
            int alpha = getCurrentAlpha(distance / mRouteLength);
            mSmallSnowflakePaint.setAlpha(alpha);
            canvas.drawBitmap(mSmallSnowBmp, -mSmallSnowBmp.getWidth() / 2, -mSmallSnowBmp.getHeight() / 2, mSmallSnowflakePaint);
            canvas.restore();
            if (isFirst) {
                mCurrentDistance += 2;
                mCurrentDistance %= mRouteLength;
            }
        }
    }

    private void drawSnowflake(Canvas canvas) {
        float r = mCurrentRadius;
        for (int j = 0; j < NUMBER_OF_LINES; j++) {
            final int alpha = getSnowLineAlpha(r);
            mSnowflakePaint.setAlpha(alpha);
            float x = (float) (r * COS);
            float y = (float) (r * (1 + SIN));
            for (int i = 0; i < 6; i++) {
                canvas.drawLine(-x, y, 0, r, mSnowflakePaint);
                canvas.drawLine(0, r, x, y, mSnowflakePaint);
                canvas.drawLine(-mX, mY, 0, mEdgeRadius, mEdgePaint);
                canvas.drawLine(0, mEdgeRadius, mX, mY, mEdgePaint);
                canvas.drawLine(-mX2, mY2, 0, mEdge2Radius, mEdgePaint);
                canvas.drawLine(0, mEdge2Radius, mX2, mY2, mEdgePaint);
                canvas.drawLine(0, 0, 0, mAxleLength, mAxlePaint);
                canvas.rotate(60);
            }
            r += mDeltaRadius;
            if (r > mMaxRadius) {
                r = mMinRadius + (r - mMaxRadius);
            }
        }
    }

    private void initSmallSnowBmp() {
        mSmallSnowBmp = Bitmap.createBitmap(mSmallSnowflakeBorderSize, mSmallSnowflakeBorderSize, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(mSmallSnowBmp);
        canvas.translate(mSmallSnowflakeBorderSize / 2, mSmallSnowflakeBorderSize / 2);
        float scale = mSmallSnowflakeBorderSize / (mAxleLength * 2);
        canvas.scale(scale, scale);
        drawSnowflake(canvas);
    }

    private int getSnowLineAlpha(float radius) {
        float distance = mMaxRadius - radius;
        float fraction = distance / (mMaxRadius - mMinRadius);
        return getCurrentAlpha(1 - fraction);
    }

    private int getCurrentAlpha(float fraction) {
        if (fraction < 0.2f) return (int) (200 * 5 * fraction);
        else return (int) (200 * (-1.25 * fraction + 1.25));
    }
}
