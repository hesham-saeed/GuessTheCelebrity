package com.example.guessthecelebrity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CelebrityActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_celebrity);

        FragmentManager fm = getSupportFragmentManager();
        Fragment celebrityFragment = fm.findFragmentById(R.id.fragment_container);

        if (celebrityFragment == null) {
            celebrityFragment = new CelebrityFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, celebrityFragment)
                    .commit();
        }
    }
}
