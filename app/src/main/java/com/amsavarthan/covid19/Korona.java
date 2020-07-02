package com.amsavarthan.covid19;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.androidnetworking.AndroidNetworking;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class Korona extends Application implements SharedPreferences.OnSharedPreferenceChangeListener{

    private SharedPreferences themePreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        ViewPump.init(ViewPump.builder().addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()
                )).build()
        );

        AndroidNetworking.initialize(getApplicationContext());
        themePreferences=getSharedPreferences("ui", Context.MODE_PRIVATE);
        themePreferences.registerOnSharedPreferenceChangeListener(this);
        toggleTheme(themePreferences,"theme");
        AndroidNetworking.initialize(getApplicationContext());

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        themePreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        toggleTheme(sharedPreferences,key);
    }

    private void toggleTheme(SharedPreferences sharedPreferences, String key) {
        String theme=sharedPreferences.getString(key,"light");
        AppCompatDelegate.setDefaultNightMode(theme.equals("light")?MODE_NIGHT_NO:MODE_NIGHT_YES);

    }


}