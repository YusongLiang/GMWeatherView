package com.gome.gmweatherview.base;

import android.graphics.Canvas;

/**
 * @author Felix.Liang
 */
public interface Painter {

    void onAttachedToView();

    void onDetachedFromView();

    void setSize(int w, int h);

    void draw(Canvas canvas);
}
