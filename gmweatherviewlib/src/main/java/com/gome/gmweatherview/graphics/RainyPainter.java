package com.gome.gmweatherview.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import com.gome.gmweatherview.base.AbsPainter;
import com.gome.gmweatherview.entity.Ripple;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Felix.Liang
 */
public class RainyPainter extends AbsPainter {

    private Shader mShader;
    private Paint mBgPaint;
    private ArrayList<Ripple> mRipples = new ArrayList<>();
    private Random mRandom = new Random();

    public RainyPainter(Context context) {
        super(context);
        init();
    }

    private void init() {
        initPaints();
    }

    private void initPaints() {
        mBgPaint = new Paint(Paint.DITHER_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h) {
        super.onSizeChanged(w, h);
        if (mShader == null) {
            mShader = new LinearGradient(0, 0, 0, getHeight(), 0xFF495976, 0xFF1D253C, Shader.TileMode.CLAMP);
            mBgPaint.setShader(mShader);
        }
        if (mRipples.size() == 0) initRipples();
    }

    private void initRipples() {
        for (int i = 0; i < 15; i++) {
            mRipples.add(new Ripple(mRandom.nextInt(getWidth()), mRandom.nextInt(getHeight())));
        }
        for (Ripple ripple : mRipples) {
            ripple.setOnRippleListener(new Ripple.OnRippleChangeListener() {
                @Override
                public void onRippleEnd(Ripple ripple) {
                    ripple.setLocation(mRandom.nextInt(getWidth()), mRandom.nextInt(getHeight()));
//                    ripple.rippleOut();
                }
            });
            ripple.rippleOut();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPaint(mBgPaint);
        for (Ripple ripple : mRipples) {
            ripple.draw(canvas);
        }
    }

    @Override
    public void onDetachedFromView() {
        super.onDetachedFromView();
        for (Ripple ripple : mRipples) {
            ripple.stop();
        }
    }

    @Override
    public void onAttachedToView() {
        super.onAttachedToView();
        for (Ripple ripple : mRipples) {
            ripple.rippleOut();
        }
    }
}
