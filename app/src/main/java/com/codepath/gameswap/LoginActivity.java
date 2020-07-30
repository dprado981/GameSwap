package com.codepath.gameswap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/** A login screen that offers login via username/password */
public class LoginActivity extends AppCompatActivity implements TextView.OnEditorActionListener, TextWatcher, View.OnClickListener {

    public static final String TAG = LoginActivity.class.getSimpleName();
    private EditText etUsername;
    private EditText etPassword;
    private TextView tvConfirmation;
    private Button btnLogin;
    private Button btnRegister;
    private ProgressBar pbLogin;

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;

        if (ParseUser.getCurrentUser() != null) {
            goToMainActivity();
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvConfirmation = findViewById(R.id.tvConfirmation);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        pbLogin = findViewById(R.id.pbLogin);

        etUsername.setOnEditorActionListener(this);
        etPassword.setOnEditorActionListener(this);

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);

        etUsername.addTextChangedListener(this);
        etPassword.addTextChangedListener(this);
    }

    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with login:", e);
                    Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show();
                    pbLogin.setVisibility(View.INVISIBLE);
                    return;
                }
                goToMainActivity();
                Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser(String username, String password) {
        // Create the ParseUser
        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(username);
        user.setPassword(password);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                pbLogin.setVisibility(View.INVISIBLE);
                if (e != null) {
                    Log.e(TAG, "Issue with registration:", e);
                    tvConfirmation.setText(R.string.try_again);
                    fadeIn(tvConfirmation, 1000);
                    fadeOut(tvConfirmation, 1000);
                    return;
                }
                etUsername.setText("");
                etPassword.setText("");
                tvConfirmation.setText(R.string.success);
                fadeIn(tvConfirmation, 1000);
                fadeOut(tvConfirmation, 1000);
                ParseUser.logOutInBackground();
            }
        });
    }

    /** Changes Activity to the main feed  */
    private void goToMainActivity() {
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        // Not allow back button to LoginActivity
        finish();
    }

    /**
     * Fades in the given view over the given duration
     * @param view The view to fade in
     * @param animationDuration The duration, in milliseconds, of the fade
     */
    public static void fadeIn(final View view, long animationDuration) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(animationDuration);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeIn);
    }

    /**
     * Fades out the given view over the given duration
     * @param view The view to fade out
     * @param animationDuration The duration, in milliseconds, of the fade
     */
    public static void fadeOut(final View view, long animationDuration) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(animationDuration);
        fadeOut.setDuration(animationDuration);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeOut);
    }

    /**
     * Changes the color of the Login button if either of the text fields are empty
     */
    private void checkButton() {
        if (!etUsername.getText().toString().isEmpty() && !etUsername.getText().toString().isEmpty()) {
            //btnLogin.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentSecondary));
            //btnRegister.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentSecondary));
        } else {
            //btnLogin.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentSecondaryMuted));
            //btnRegister.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentSecondaryMuted));
        }
    }

    /**
     * Taps the Login button if the user presses the enter from the keyboard or finishes typing
     */
    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if ((event != null
                && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                || (actionId == EditorInfo.IME_ACTION_DONE)) {
            btnLogin.performClick();
        }
        return false;
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { checkButton(); }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { checkButton(); }

    @Override
    public void afterTextChanged(Editable editable) { checkButton(); }

    @Override
    public void onClick(View view) {
        pbLogin.setVisibility(View.VISIBLE);
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        if (view.getId() == R.id.btnLogin) {
            loginUser(username, password);
        } else if (view.getId() == R.id.btnRegister) {
            registerUser(username, password);
        } else {
            Log.e(TAG, "This click has not been setup yet");
        }
    }
}