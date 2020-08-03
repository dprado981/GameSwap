package com.codepath.gameswap;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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


public class MainActivity extends AppCompatActivity implements ComposeTypeDialog.ComposeTypeDialogListener, ViewTreeObserver.OnGlobalLayoutListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private boolean isKeyboardShowing;

    private View content;
    private BottomNavigationView bottomNavigation;

    public HomeFragment homeFragment;
    public ComposeTypeDialog composeFragment;
    public ChatsFragment chatsFragment;
    public ProfileFragment profileFragment;

    final private FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        homeFragment = new HomeFragment();
        composeFragment = new ComposeTypeDialog();
        chatsFragment = new ChatsFragment();
        profileFragment = new ProfileFragment();

        content = findViewById(R.id.content);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        isKeyboardShowing = false;
        content.getViewTreeObserver().addOnGlobalLayoutListener(this);

        fragmentManager.beginTransaction().replace(R.id.flContainer, homeFragment).addToBackStack(null).commit();

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment oldFragment = fragmentManager.findFragmentById(R.id.flContainer);
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.actionHome:
                        fragment = homeFragment;
                        break;
                    case R.id.actionCompose:
                        composeFragment.setBottomNav(bottomNavigation);
                        composeFragment.setOldFragment(oldFragment);
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
        if (fragmentManager.getBackStackEntryCount() > 0) {
            super.onBackPressed();
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.flContainer);
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

    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        content.getWindowVisibleDisplayFrame(r);
        int screenHeight = content.getRootView().getHeight();

        // r.bottom is the position above soft keypad or device button.
        // if keypad is shown, the r.bottom is smaller than that before.
        int keypadHeight = screenHeight - r.bottom;

        if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
            // keyboard is opened
            if (!isKeyboardShowing) {
                isKeyboardShowing = true;
                bottomNavigation.setVisibility(View.GONE);
            }
        }
        else {
            // keyboard is closed
            if (isKeyboardShowing) {
                isKeyboardShowing = false;
                bottomNavigation.setVisibility(View.VISIBLE);
            }
        }
    }
}