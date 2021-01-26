package com.example.android.brunel_fyp;

import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Once logged in, show a fragment by default
        loadFragment(new Profile());
        // Default selected item in nav bar is the 1st one, so change that
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            Fragment fragment = null;
            switch (menuItem.getItemId()) {
                case R.id.navigation_chatbot:
                    fragment = new Chatbot();
                    break;
                case R.id.navigation_trophies:
                    fragment = new Trophies();
                    break;
                case R.id.navigation_profile:
                    fragment = new Profile();
                    break;
            }
            loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        // Switch fragments
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
}
