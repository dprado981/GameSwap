package com.codepath.gameswap;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.codepath.gameswap.fragments.ChatsFragment;
import com.codepath.gameswap.fragments.ComposeGameFragment;
import com.codepath.gameswap.fragments.ComposePuzzleFragment;
import com.codepath.gameswap.fragments.MapsFragment;
import com.codepath.gameswap.fragments.PostsFragment;
import com.codepath.gameswap.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements ComposeTypeDialog.ComposeTypeDialogListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private Context context;

    private BottomNavigationView bottomNavigation;

    final private FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(R.id.actionHome);
        Fragment fragment = new PostsFragment();
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();

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
                        DialogFragment newFragment = new ComposeTypeDialog();
                        newFragment.show(getSupportFragmentManager(), "type");
                        return true;
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
    }

    @Override
    public void onClick(DialogInterface dialog, int pos) {
        Fragment fragment;
        if (pos == 0) {
            fragment = new ComposePuzzleFragment();
        } else if (pos == 1) {
            fragment = new ComposeGameFragment();
        } else {
            Log.e(TAG, "Not implemented");
            return;
        }
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
    }
}