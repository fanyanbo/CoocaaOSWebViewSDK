package coocaa.plugin.api.pay.sky.api;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;




public class BaseActivity extends Activity
{
    public float mDensity;
    public int widthPixels;
    public int heightPixels;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        widthPixels = getResolution()[0];
        heightPixels = getResolution()[1];
        mDensity = getDensity();
        setResolutionDiv();
        setDpiDiv();
    }

    public static float resolutionDiv;
    public static float dipDiv;

    public static int getResolutionValue(int value)
    {
        int r_value = (int) (value / resolutionDiv);
        return r_value;
    }

    public static int getTextDpiValue(int value)
    {
        int r_value = (int) (value / dipDiv);
        return r_value;
    }

    public void setDpiDiv()
    {
        float density = mDensity;
        dipDiv = resolutionDiv * density;
    }

    public void setResolutionDiv()
    {
        int width = widthPixels;
        if (width == 1920)
        {
            resolutionDiv = 1;
        }
        if (width == 1366)
        {
            resolutionDiv = (float) 1.4;

        }
        if (width == 1280)
        {
            resolutionDiv = (float) 1.5;
        }
    }

    public int[] getResolution()
    {
        int[] resolution = new int[2];
        DisplayMetrics metrics = new DisplayMetrics();
        metrics = this.getApplicationContext().getResources().getDisplayMetrics();
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        resolution[0] = widthPixels;
        resolution[1] = heightPixels;
        return resolution;
    }

    public float getDensity()
    {
        DisplayMetrics metrics = new DisplayMetrics();
        metrics = this.getApplicationContext().getResources().getDisplayMetrics();
        float mDensity = metrics.density;
        return mDensity;
    }
}
