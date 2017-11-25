package com.urbanlabs.ui.widget;


import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.urbanlabs.R;

public class SplashScreenAdapter extends PagerAdapter {
    public static final int[] LAYOUTS = {
            R.layout.splash_page_1,
            R.layout.splash_page_2,
            R.layout.splash_page_3,
            R.layout.splash_page_4
    };

    public static final int PAGE_1 = 0;
    public static final int PAGE_2 = 1;
    public static final int PAGE_3 = 2;
    public static final int PAGE_4 = 3;

    private Context context;

    public void setListener(SplashScreenListener listener_) {
        this.listener_ = listener_;
    }

    private SplashScreenListener listener_;

    public SplashScreenAdapter(Context context) {
        this.context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View view = View.inflate(context, LAYOUTS[position], null);
        container.addView(view);
        if (position == PAGE_4) {
            initGetStartedButtonListener(view);
        }

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
    private void initGetStartedButtonListener(View view) {
        view.findViewById(R.id.get_started_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener_.onGetStarted(true);
            }
        });
    }

    @Override
    public int getCount() {
        return LAYOUTS.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public interface SplashScreenListener {
        void onGetStarted(boolean storeSplashStatus);
    }
}
