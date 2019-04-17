package com.bloomberg.tetris.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {
    // 开关式的按钮 直接在 simple_preference.xml 中注册
    // Activity彻底运行起来之后的回调
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // PreferenceActivity的layout可以理解成为两个部分：其他View和一个id为android.R.id.list的ListView
        // 调用addPreferencesFromResource来完成Preference界面的构建
        addPreferencesFromResource(com.bloomberg.tetris.R.xml.simple_preferences);

        // 设置界面的返回按钮
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // 高级设置的引导对象
        Preference prefAdvance = findPreference("pref_advanced");
        // 设置点击监视 点击时调用onPreferenceClick
        prefAdvance.setOnPreferenceClickListener(this);

        // 触觉反馈持续时间设置 默认为SharedPreference
        Preference prefVibDurOffset = findPreference("pref_vibDurOffset");
        // 获取设置界面中 调节持续时间时用户输入的字符串
        // 默认值在strings.xml中定义
        String timeString = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_vibDurOffset", "");
        if (timeString.equals(""))
            timeString = "0";
        timeString = "" + timeString + " ms";
        // 显示在设置界面中
        prefVibDurOffset.setSummary(timeString);
    }

    // SharedPreference改变时调用此方法（触觉反馈持续时间）
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_vibDurOffset")) {
            // 建立偏好设置类对象 连接id为key的设置id
            Preference connectionPref = findPreference(key);
            // 设置为用户输入的所选值
            String timeString = sharedPreferences.getString(key, "");
            if (timeString.equals(""))
                timeString = "0";
            timeString = "" + timeString + " ms";
            // 显示在设置界面中
            connectionPref.setSummary(timeString);
        }
    }

    //当用户点击菜单当中的某一个选项时，调用该方法
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

    // onPreferenceClick用来当Preference对象被点击时 跳转AdvancedSettingsActivity
    @Override
    public boolean onPreferenceClick(Preference preference) {
        Intent intent = new Intent(this, AdvancedSettingsActivity.class);
        startActivity(intent);
        return true;
    }
}
