package edu.kit.teco.radarbeacon;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by Chriss on 12.12.2015.
 */
public class Utils {

    public static float dpToPx(Context context, float dp) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics
                ());
    }

    public static int getCenterX(Context context) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        return screenWidth / 2;
    }

    public static int getCenterY(Context context) {
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        return screenHeight / 2 - 60;
    }
}
