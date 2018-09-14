package coocaa.plugin.api.pay.sky.api;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skyworth.ui.api.SkyWithBGLoadingView;

public class LoadingView extends LinearLayout
{
    private TextView text;

    private SkyWithBGLoadingView loadingWithBGView;

    public LoadingView(Context context)
    {
        super(context);
        this.setGravity(Gravity.CENTER);
        loadingWithBGView = new SkyWithBGLoadingView(context);
        this.addView(loadingWithBGView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
    }

    public void setLoadingText(String str)
    {
        text.setText(str);
    }

    public void setLoadingText(int strId)
    {
        text.setText(strId);
    }

    @Override
    public void setVisibility(int visibility)
    {
        if (visibility == View.VISIBLE)
        {
            loadingWithBGView.showLoading();
        } else
        {
            loadingWithBGView.dismissLoading();
        }
        super.setVisibility(visibility);
    }
}
