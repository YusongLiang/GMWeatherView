package com.gome.gmweatherview.base;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import com.gome.gmweatherview.util.SizeTransformer;

/**
 * @author Felix.Liang
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class AbsPainter implements Painter {

    private int mWidth;
    private int mHeight;
    private Context mContext;
    protected Paint mDefaultPaint;

    public AbsPainter(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onAttachedToView() {
        if (mWidth != 0 || mHeight != 0) onSizeChanged(mWidth, mHeight);
    }

    @Override
    public void onDetachedFromView() {
    }

    @Override
    public void setSize(int w, int h) {
        if (mWidth != w || mHeight != h) {
            mWidth = w;
            mHeight = h;
            onSizeChanged(w, h);
        }
    }

    protected void onSizeChanged(int w, int h) {
    }

    protected int getWidth() {
        return mWidth;
    }

    protected int getHeight() {
        return mHeight;
    }

    protected Context getContext() {
        return mContext;
    }

    protected int dp2px(float dp) {
        return SizeTransformer.dip2px(mContext, dp);
    }

    public Paint getDefaultPaint() {
        if (mDefaultPaint == null) {
            mDefaultPaint = new Paint();
            mDefaultPaint.setColor(Color.GREEN);
            mDefaultPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mDefaultPaint.setStrokeWidth(dp2px(3));
        }
        return mDefaultPaint;
    }
}
