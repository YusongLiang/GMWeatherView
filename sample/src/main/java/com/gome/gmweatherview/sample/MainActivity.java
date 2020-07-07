package com.gome.gmweatherview.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.gome.gmweatherview.widget.WeatherView;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private WeatherView weatherView;
    private ViewPager pager;
    private String[] titles = {"多云", "晴天", "阴天", "沙尘", "雪", "雨", "雾", "雾霾", "默认"};
    private Fragment[] fragments;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        initWindow();
    }

    private void initWindow() {
        final Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weatherView = (WeatherView) findViewById(R.id.weather_view);
        pager = (ViewPager) findViewById(R.id.pager);
        fragments = new SampleFragment[titles.length];
        for (int i = 0; i < fragments.length; i++) {
            fragments[i] = new SampleFragment();
        }
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return titles.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titles[position];
            }
        });
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.addOnTabSelectedListener(this);
        tabs.setupWithViewPager(pager);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        final int position = tab.getPosition();
        int type;
        switch (position) {
            case 0:
                type = WeatherView.TYPE_CLOUDY;
                break;
            case 1:
                type = WeatherView.TYPE_SUNNY;
                break;
            case 2:
                type = WeatherView.TYPE_OVERCAST;
                break;
            case 3:
                type = WeatherView.TYPE_SANDY;
                break;
            case 4:
                type = WeatherView.TYPE_SNOWY;
                break;
            case 5:
                type = WeatherView.TYPE_RAINY;
                break;
            case 6:
                type = WeatherView.TYPE_FOGGY;
                break;
            case 7:
                type = WeatherView.TYPE_HAZY;
                break;
            default:
                type = WeatherView.TYPE_NONE;
        }
        weatherView.setWeather(type);
        pager.setCurrentItem(position, true);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }
}
