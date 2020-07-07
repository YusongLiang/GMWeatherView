package com.gome.gmweatherview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.gome.gmweatherview.R;
import com.gome.gmweatherview.base.Painter;
import com.gome.gmweatherview.util.PainterFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Felix.Liang
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class WeatherView extends SurfaceView implements SurfaceHolder.Callback {

    @IntDef({TYPE_NONE, TYPE_SUNNY, TYPE_CLOUDY,
            TYPE_OVERCAST, TYPE_SANDY, TYPE_SNOWY,
            TYPE_RAINY, TYPE_FOGGY, TYPE_HAZY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface WeatherType {
    }

    public static final int TYPE_NONE = 0;
    public static final int TYPE_CLOUDY = 1;
    public static final int TYPE_SUNNY = 2;
    public static final int TYPE_OVERCAST = 3;
    public static final int TYPE_SANDY = 4;
    public static final int TYPE_SNOWY = 5;
    public static final int TYPE_RAINY = 6;
    public static final int TYPE_FOGGY = 7;
    public static final int TYPE_HAZY = 8;

    private final SurfaceHolder mHolder;
    private int mType;
    private Painter mPainter;
    private OnWeatherChangeListener mOnWeatherChangeListener;
    private UpdateThread mThread;
    private boolean mRunning = true;
    private boolean mStarted;
    private boolean mVisible;

    private Paint mClearPaint;

    public WeatherView(Context context) {
        this(context, null);
    }

    public WeatherView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
        mHolder = getHolder();
        mHolder.addCallback(this);
        initView();
    }

    private void initView() {
        mClearPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WeatherView, defStyleAttr, defStyleRes);
        final int type = array.getInt(R.styleable.WeatherView_type, TYPE_NONE);
        setupPainter(type);
        array.recycle();
    }

    private void setupPainter(int type) {
        Painter painter = PainterFactory.getInstance(getContext(), type);
        setPainter(painter);
        if (mOnWeatherChangeListener != null)
            mOnWeatherChangeListener.onWeatherChanged(type);
    }

    public void setWeather(@WeatherType int type) {
        if (mType != type) {
            mType = type;
            setupPainter(type);
        }
    }

    private void setPainter(@Nullable Painter painter) {
        if (mPainter != null) mPainter.onDetachedFromView();
        mPainter = painter;
        if (mPainter != null) {
            mPainter.onAttachedToView();
            if (getWidth() != 0 || getHeight() != 0)
                mPainter.setSize(getWidth(), getHeight());
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStarted(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mPainter != null) mPainter.setSize(width, height);
        setStarted(true);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        setStarted(false);
    }

    private void startUpdate() {
        if (mThread == null) {
            mThread = new UpdateThread();
            mThread.start();
            if (mPainter != null) mPainter.onAttachedToView();
        }
    }

    private void stopUpdate() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
            if (mPainter != null) mPainter.onDetachedFromView();
        }
    }

    private class UpdateThread extends Thread {

        private boolean paused;

        @Override
        public void run() {
            while (mRunning) {
                try {
                    Canvas canvas = mHolder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawPaint(mClearPaint);
                        drawPainter(canvas);
                        try {
                            mHolder.unlockCanvasAndPost(canvas);
                        } catch (IllegalArgumentException e) {
                            break;
                        }
                    }
                    Thread.sleep(1);
                    synchronized (this) {
                        while (paused) {
                            wait();
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        void updatePause() {
            paused = true;
        }

        synchronized void updateResume() {
            paused = false;
            notify();
        }

        boolean isPaused() {
            return paused;
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;
        updateRunning();
        mHolder.removeCallback(this);
    }

    private void updateRunning() {
        boolean running = mVisible && mStarted;
        if (mRunning != running) {
            mRunning = running;
            if (running) {
                startUpdate();
            } else {
                stopUpdate();
            }
        }
    }

    public void setStarted(boolean started) {
        if (mStarted != started) {
            mStarted = started;
            updateRunning();
        }
    }

    private void drawPainter(Canvas canvas) {
        if (mPainter != null)
            mPainter.draw(canvas);
        else canvas.drawColor(Color.WHITE);
    }

    public void setOnWeatherChangeListener(OnWeatherChangeListener listener) {
        mOnWeatherChangeListener = listener;
    }

    public interface OnWeatherChangeListener {

        void onWeatherChanged(@WeatherType int type);
    }
}
