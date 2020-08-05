package com.codepath.gameswap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.jetbrains.annotations.NotNull;

/** A login screen that offers login via username/password */
public class RegisterActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener, TextView.OnEditorActionListener {

    public static final String TAG = RegisterActivity.class.getSimpleName();
    private EditText etEmail;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etFirstName;
    private EditText etLastName;
    private Button btnRegister;
    private TextView tvAccountExists;
    private ProgressBar pbRegister;

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;

        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        pbRegister = findViewById(R.id.pbRegister);
        btnRegister = findViewById(R.id.btnRegister);
        tvAccountExists = findViewById(R.id.tvAccountExists);

        etEmail.addTextChangedListener(this);
        etUsername.addTextChangedListener(this);
        etPassword.addTextChangedListener(this);
        etFirstName.addTextChangedListener(this);
        etLastName.addTextChangedListener(this);

        etEmail.setOnEditorActionListener(this);
        etUsername.setOnEditorActionListener(this);
        etPassword.setOnEditorActionListener(this);
        etFirstName.setOnEditorActionListener(this);
        etLastName.setOnEditorActionListener(this);

        btnRegister.setBackgroundColor(ContextCompat.getColor(context, R.color.colorButtonDisabled));
        btnRegister.setOnClickListener(this);
        ClickableSpan accountExistsSpan = new ClickableSpan() {
            @Override
            public void onClick(@NotNull View textView) {
                goToLogin();
            }
            @Override
            public void updateDrawState(@NotNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        LoginActivity.setSpanAfterQuestionMark(context, tvAccountExists, accountExistsSpan);
    }

    private void registerUser(String email, String username, String password, String firstName, String lastName) {
        pbRegister.setVisibility(View.INVISIBLE);
        // Create the ParseUser
        ParseUser user = new ParseUser();
        // Set core properties
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                pbRegister.setVisibility(View.VISIBLE);
                if (e != null) {
                    int errorCode = e.getCode();
                    if (errorCode == 202) {
                        Toast.makeText(context, "Username already in use", Toast.LENGTH_SHORT).show();
                        pbRegister.setVisibility(View.INVISIBLE);
                        etUsername.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(etUsername, InputMethodManager.SHOW_IMPLICIT);
                        return;
                    } else if (errorCode == 203) {
                        Toast.makeText(context, "Email already in use", Toast.LENGTH_SHORT).show();
                        pbRegister.setVisibility(View.INVISIBLE);
                        etEmail.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(etEmail, InputMethodManager.SHOW_IMPLICIT);
                        return;
                    }
                    Log.e(TAG, "code: " + errorCode);
                    Log.e(TAG, "Issue with registration:", e);
                    return;
                }
                etUsername.setText("");
                etPassword.setText("");
                ParseUser.logOutInBackground();
                Toast.makeText(context, "Success! Log in to start!", Toast.LENGTH_SHORT).show();
                goToLogin();
            }
        });
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
            pbRegister.setVisibility(View.VISIBLE);
            String email = etEmail.getText().toString();
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            String firstName = etFirstName.getText().toString();
            String lastName = etLastName.getText().toString();
            if (view.getId() == R.id.btnRegister) {
                registerUser(email, username, password, firstName, lastName);
            } else {
                Log.e(TAG, "This click has not been setup yet");
            }
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
            btnRegister.performClick();
        }
        return false;
    }


    /** Changes Activity to the main feed  */
    private void goToLogin() {
        Intent intent = new Intent(context, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Changes the color of the Register button if either of the text fields are empty
     */
    private void checkButton() {
        if (allFieldsFilled(false)) {
            btnRegister.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            btnRegister.setBackgroundColor(ContextCompat.getColor(context, R.color.colorButtonDisabled));
        }
    }

    private boolean allFieldsFilled(boolean notifyUser) {
        if (etEmail.getText().toString().isEmpty()) {
            if (notifyUser) {
                Toast.makeText(context, "Must have a email", Toast.LENGTH_SHORT).show();
            }
            return false;
        } else if (etUsername.getText().toString().isEmpty()) {
            if (notifyUser) {
                Toast.makeText(context, "Must have a username", Toast.LENGTH_SHORT).show();
            }
            return false;
        } else if (etPassword.getText().toString().isEmpty()) {
            if (notifyUser) {
                Toast.makeText(context, "Must have a password", Toast.LENGTH_SHORT).show();
            }
            return false;
        } else if (etFirstName.getText().toString().isEmpty()) {
            if (notifyUser) {
                Toast.makeText(context, "Must have a first name", Toast.LENGTH_SHORT).show();
            }
            return false;
        } else if (etLastName.getText().toString().isEmpty()) {
            if (notifyUser) {
                Toast.makeText(context, "Must have a last name", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }
}