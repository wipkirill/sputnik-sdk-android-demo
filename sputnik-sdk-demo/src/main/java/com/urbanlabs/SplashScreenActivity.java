package com.urbanlabs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.urbanlabs.ui.widget.CirclePageIndicator;
import com.urbanlabs.ui.widget.SplashScreenAdapter;

/**
 *
 */
public class SplashScreenActivity extends Activity implements ViewPager.OnPageChangeListener, SplashScreenAdapter.SplashScreenListener {
    public static final String PREFS_NAME = "SputnikPrefsFile";
    private ViewPager viewPager_;
    private CirclePageIndicator viewPagerIndicator_;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean splashShowed = settings.getBoolean("splashShowed", false);
        if(splashShowed) {
            onGetStarted(false);
            return;
        }
        setContentView(R.layout.splash_activity);
        viewPager_ = (ViewPager) findViewById(R.id.view_pager);
        viewPagerIndicator_ = (CirclePageIndicator) findViewById(R.id.view_pager_indicator);
        initViewPager();
    }

    private void initViewPager() {
        final SplashScreenAdapter screenAdapter = new SplashScreenAdapter(this);
        screenAdapter.setListener(this);
        viewPager_.setAdapter(screenAdapter);
        viewPagerIndicator_.setOnPageChangeListener(this);
        viewPagerIndicator_.setViewPager(viewPager_);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onGetStarted(boolean storeSplashStatus) {
        if(storeSplashStatus) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("splashShowed", true);
            // Commit the edits!
            editor.commit();
        }

        Intent newIntent = new Intent(SplashScreenActivity.this, MenuActivity.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(newIntent);
        finish();
    }
}
