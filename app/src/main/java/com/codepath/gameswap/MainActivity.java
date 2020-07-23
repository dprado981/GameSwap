package com.codepath.gameswap;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.codepath.gameswap.fragments.ChatsFragment;
import com.codepath.gameswap.fragments.ComposeFragment;
import com.codepath.gameswap.fragments.MapsFragment;
import com.codepath.gameswap.fragments.PostsFragment;
import com.codepath.gameswap.fragments.ProfileFragment;
import com.codepath.gameswap.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private BottomNavigationView bottomNavigation;

    final private FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.actionHome:
                        fragment = new PostsFragment();
                        break;
                    case R.id.actionMap:
                        fragment = new MapsFragment();
                        break;
                    case R.id.actionCompose:
                        fragment = new ComposeFragment();
                        break;
                    case R.id.actionChat:
                        fragment = new ChatsFragment();
                        break;
                    case R.id.actionProfile:
                    default:
                        fragment = new ProfileFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
                return true;
            }
        });

        bottomNavigation.setSelectedItemId(R.id.actionHome);
        Fragment fragment = new PostsFragment();
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
    }
}