package com.codepath.gameswap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

/** A login screen that offers login via username/password */
public class LoginActivity extends AppCompatActivity implements TextView.OnEditorActionListener, TextWatcher, View.OnClickListener {

    public static final String TAG = LoginActivity.class.getSimpleName();
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
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
        btnLogin = findViewById(R.id.btnLogin);
        pbLogin = findViewById(R.id.pbLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnLogin.setBackgroundColor(ContextCompat.getColor(context, R.color.colorButtonDisabled));

        etUsername.setOnEditorActionListener(this);
        etPassword.setOnEditorActionListener(this);

        btnLogin.setOnClickListener(this);
        ClickableSpan registerSpan = new ClickableSpan() {
            @Override
            public void onClick(@NotNull View textView) {
                Intent intent = new Intent(context, RegisterActivity.class);
                startActivity(intent);
            }
            @Override
            public void updateDrawState(@NotNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        setSpanAfterQuestionMark(context, tvRegister, registerSpan);

        etUsername.addTextChangedListener(this);
        etPassword.addTextChangedListener(this);
    }

    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    if (e.getCode() == 101) {
                        Toast.makeText(context, "Username/Password is incorrect", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Issue with login:", e);
                    }
                    pbLogin.setVisibility(View.INVISIBLE);
                    return;
                }
                goToMainActivity();
                Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
            }
        });
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
        if (allFieldsFilled(true)) {
            pbLogin.setVisibility(View.VISIBLE);
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (view.getId() == R.id.btnLogin) {
                loginUser(username, password);
            } else {
                Log.e(TAG, "This click has not been setup yet");
            }
        }
    }

    /** Changes Activity to the main feed  */
    private void goToMainActivity() {
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Changes the color of the Login button if either of the text fields are empty
     */
    private void checkButton() {
        if (allFieldsFilled(false)) {
            btnLogin.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            btnLogin.setBackgroundColor(ContextCompat.getColor(context, R.color.colorButtonDisabled));
        }
    }

    private boolean allFieldsFilled(boolean notifyUser) {
        if (etUsername.getText().toString().trim().isEmpty()) {
            if (notifyUser) {
                Toast.makeText(context, "Must have a username", Toast.LENGTH_SHORT).show();
            }
            return false;
        } else if (etPassword.getText().toString().trim().isEmpty()) {
            if (notifyUser) {
                Toast.makeText(context, "Must have a password", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    public static void setSpanAfterQuestionMark(final Context context, TextView tvBody, ClickableSpan span) {
        String registerText = tvBody.getText().toString();
        SpannableString ss = new SpannableString(registerText);
        int questionIndex = registerText.indexOf('?');
        ss.setSpan(span, questionIndex+1, registerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)),
                questionIndex+1, registerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvBody.setText(ss);
        tvBody.setMovementMethod(LinkMovementMethod.getInstance());
        tvBody.setHighlightColor(Color.TRANSPARENT);
    }
}