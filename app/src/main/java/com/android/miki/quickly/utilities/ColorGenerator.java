package com.android.miki.quickly.utilities;


import android.graphics.Color;
import android.util.Log;

import java.util.Random;

/**
 * Created by mpokr on 6/1/2017.
 */

public class ColorGenerator {

    private static final String TAG = "ColorGenerator";

    public ColorGenerator() {

    }

    public int[] generateRandomColors(int baseColor, int numColors) {
        if (numColors < 1) {
            return null;
        }
        int[] randomColors = new int[numColors];
        //int white = Color.rgb(255, 255, 255);
        Random random = new Random();
        for (int i = 0; i < randomColors.length; i++) {
            int red = random.nextInt(256);
            int green = random.nextInt(256);
            int blue = random.nextInt(256);

            // mix the color
            int baseRed = (baseColor >> 16) & 0xff;
            int baseGreen = (baseColor >> 8) & 0xff;
            int baseBlue = (baseColor) & 0xff;
            red = (red + baseRed) / 2;
            green = (green + baseGreen) / 2;
            blue = (blue + baseBlue) / 2;

            randomColors[i] = Color.rgb(red, green, blue);
        }
        return randomColors;
    }

    public int[] goldenRationPalette(int baseColor, int numColors) {
        int[] colors = new int[numColors];
        Random r = new Random();
        float goldenRatio = 0.618033988749895f;

        int baseRed = (baseColor >> 16) & 0xff;
        int baseGreen = (baseColor >> 8) & 0xff;
        int baseBlue = (baseColor) & 0xff;

        for (int i = 0; i < numColors; i++) {
            float offset = r.nextFloat();
            float hue = offset + (goldenRatio * i) % 1 * baseRed;
            Log.d(TAG, hue + "");
            colors[i] = Color.HSVToColor(new float[]{hue , 0.5f, 0.85f}); // (hue, saturation, value). Value controls how light/dark the color is.
        }
        return colors;
    }


}
