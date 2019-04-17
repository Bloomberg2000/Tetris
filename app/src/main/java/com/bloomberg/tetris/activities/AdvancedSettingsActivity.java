package com.bloomberg.tetris.activities;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class AdvancedSettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 调用addPreferencesFromResource来完成Preference界面的构建
        addPreferencesFromResource(com.bloomberg.tetris.R.xml.advanced_preferences);
        // 设置界面的返回按钮
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 方块生成方式设置区域
        Preference prefRng = findPreference("pref_rng");
        // 显示默认值 定义在 string.xml
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("pref_rng", "").equals("sevenbag"))
            prefRng.setSummary(getResources().getStringArray(com.bloomberg.tetris.R.array.randomizer_preference_array)[0]);
        else
            prefRng.setSummary(getResources().getStringArray(com.bloomberg.tetris.R.array.randomizer_preference_array)[1]);

        //真率限制设置区域
        Preference prefFPSLimit = findPreference("pref_fpslimittext");
        prefFPSLimit.setSummary(PreferenceManager.getDefaultSharedPreferences(this).getString("pref_fpslimittext", ""));
    }

    // SharedPreference改变的处理
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_rng")) {
            // 连接对应的偏好设置
            Preference connectionPref = findPreference(key);
            // 设置并显示用户输入的所选值
            if (sharedPreferences.getString(key, "").equals("sevenbag"))
                connectionPref.setSummary(getResources().getStringArray(com.bloomberg.tetris.R.array.randomizer_preference_array)[0]);
            else
                connectionPref.setSummary(getResources().getStringArray(com.bloomberg.tetris.R.array.randomizer_preference_array)[1]);
        }
        if (key.equals("pref_fpslimittext")) {
            // 连接对应的偏好设置
            Preference connectionPref = findPreference(key);
            // 设置并显示用户输入的所选值
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
