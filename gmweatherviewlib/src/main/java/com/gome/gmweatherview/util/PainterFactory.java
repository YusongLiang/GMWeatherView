package com.gome.gmweatherview.util;

import android.content.Context;
import android.util.SparseArray;

import com.gome.gmweatherview.graphics.CloudyPainter;
import com.gome.gmweatherview.graphics.FoggyPainter;
import com.gome.gmweatherview.graphics.HazyPainter;
import com.gome.gmweatherview.graphics.OvercastPainter;
import com.gome.gmweatherview.base.Painter;
import com.gome.gmweatherview.graphics.RainyPainter;
import com.gome.gmweatherview.graphics.SandyPainter;
import com.gome.gmweatherview.graphics.SnowyPainter;
import com.gome.gmweatherview.graphics.SunnyPainter;

import static com.gome.gmweatherview.widget.WeatherView.TYPE_CLOUDY;
import static com.gome.gmweatherview.widget.WeatherView.TYPE_FOGGY;
import static com.gome.gmweatherview.widget.WeatherView.TYPE_HAZY;
import static com.gome.gmweatherview.widget.WeatherView.TYPE_NONE;
import static com.gome.gmweatherview.widget.WeatherView.TYPE_OVERCAST;
import static com.gome.gmweatherview.widget.WeatherView.TYPE_RAINY;
import static com.gome.gmweatherview.widget.WeatherView.TYPE_SANDY;
import static com.gome.gmweatherview.widget.WeatherView.TYPE_SNOWY;
import static com.gome.gmweatherview.widget.WeatherView.TYPE_SUNNY;
import static com.gome.gmweatherview.widget.WeatherView.WeatherType;

/**
 * @author Felix.Liang
 */
public class PainterFactory {

    private static SparseArray<Painter> sInstances = new SparseArray<>();

    public static Painter getInstance(Context context, @WeatherType int type) {
        if (type == TYPE_NONE) return null;
        if (sInstances.size() == 0) {
            return createNewInstance(context, type);
        } else {
            Painter painter = sInstances.get(type);
            if (painter == null) painter = createNewInstance(context, type);
            return painter;
        }
    }

    private static Painter createNewInstance(Context context, int type) {
        Painter painter = null;
        switch (type) {
            case TYPE_CLOUDY:
                painter = new CloudyPainter(context);
                break;
            case TYPE_SUNNY:
                painter = new SunnyPainter(context);
                break;
            case TYPE_OVERCAST:
                painter = new OvercastPainter(context);
                break;
            case TYPE_SANDY:
                painter = new SandyPainter(context);
                break;
            case TYPE_SNOWY:
                painter = new SnowyPainter(context);
                break;
            case TYPE_RAINY:
                painter = new RainyPainter(context);
                break;
            case TYPE_FOGGY:
                painter = new FoggyPainter(context);
                break;
            case TYPE_HAZY:
                painter = new HazyPainter(context);
                break;
        }
        if (painter != null) {
            sInstances.put(type, painter);
        }
        return painter;
    }
}
