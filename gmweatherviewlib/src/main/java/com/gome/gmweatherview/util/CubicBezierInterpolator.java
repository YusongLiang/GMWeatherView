package com.gome.gmweatherview.util;

import android.annotation.TargetApi;
import android.graphics.PointF;
import android.os.Build;
import android.view.animation.BaseInterpolator;

/**
 * @author Felix.Liang
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
public class CubicBezierInterpolator extends BaseInterpolator {

    private final static int ACCURACY = 4096;
    private int mLastI = 0;
    private final PointF mCtrlPt1 = new PointF();
    private final PointF mCtrlPt2 = new PointF();

    public CubicBezierInterpolator(float cx1, float cy1, float cx2, float cy2) {
        mCtrlPt1.x = cx1;
        mCtrlPt1.y = cy1;
        mCtrlPt2.x = cx2;
        mCtrlPt2.y = cy2;
    }

    @Override
    public float getInterpolation(float input) {
        float t = input;
        for (int i = mLastI; i < ACCURACY; i++) {
            t = 1.0f * i / ACCURACY;
            double x = cubicCurves(t, 0, mCtrlPt1.x, mCtrlPt2.x, 1);
            if (x >= input) {
                mLastI = i;
                break;
            }
        }
        double value = cubicCurves(t, 0, mCtrlPt1.y, mCtrlPt2.y, 1);
        if (value > 0.999d) {
            value = 1;
            mLastI = 0;
        }
        return (float) value;
    }

    private static double cubicCurves(double t, double value0, double value1, double value2, double value3) {
        double value;
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;
        value = uuu * value0;
        value += 3 * uu * t * value1;
        value += 3 * u * tt * value2;
        value += ttt * value3;
        return value;
    }
}
