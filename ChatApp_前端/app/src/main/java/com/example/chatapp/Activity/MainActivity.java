package com.example.chatapp.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ChatAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "is_Logged_In";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (isLoggedIn) {
            if(preferences.getString("username",null)!=null) {
                // 用户已登录，跳转到通讯录界面
                startActivity(new Intent(this, ContactListActivity.class));
                Log.v("MainActivity", "用户已登录");
            }
            else{
                startActivity(new Intent(this, LoginActivity.class));
            }
        } else {
            // 用户未登录，跳转到登录界面
            startActivity(new Intent(this, LoginActivity.class));
        }

         //关闭当前 Activity
        finish();
    }
}
