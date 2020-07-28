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
import com.codepath.gameswap.fragments.ComposeFragment;
import com.codepath.gameswap.fragments.ComposeGameFragment;
import com.codepath.gameswap.fragments.ComposePuzzleFragment;
import com.codepath.gameswap.fragments.ConversationFragment;
import com.codepath.gameswap.fragments.HomeFragment;
import com.codepath.gameswap.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity implements ComposeTypeDialog.ComposeTypeDialogListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private Context context;

    private BottomNavigationView bottomNavigation;

    public HomeFragment homeFragment;
    public DialogFragment composeFragment;
    public ChatsFragment chatsFragment;
    public ProfileFragment profileFragment;

    final private FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        homeFragment = new HomeFragment();
        composeFragment = new ComposeTypeDialog();
        chatsFragment = new ChatsFragment();
        profileFragment = new ProfileFragment();

        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(R.id.actionHome);
        fragmentManager.beginTransaction().replace(R.id.flContainer, homeFragment).addToBackStack(null).commit();

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.actionHome:
                        fragment = homeFragment;
                        break;
                    case R.id.actionCompose:
                        composeFragment.show(getSupportFragmentManager(), "type");
                        return true;
                    case R.id.actionChat:
                        fragment = chatsFragment;
                        break;
                    case R.id.actionProfile:
                    default:
                        fragment = profileFragment;
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

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            super.onBackPressed();
            Fragment currentFragment = manager.findFragmentById(R.id.flContainer);
            if (currentFragment instanceof ProfileFragment) {
                bottomNavigation.setSelectedItemId(R.id.actionProfile);
            } else if (currentFragment instanceof ChatsFragment || currentFragment instanceof ConversationFragment) {
                bottomNavigation.setSelectedItemId(R.id.actionChat);
            } else if (currentFragment instanceof ComposeFragment) {
                bottomNavigation.setSelectedItemId(R.id.actionCompose);
            } else {
                bottomNavigation.setSelectedItemId(R.id.actionHome);
            }
        }

    }

}