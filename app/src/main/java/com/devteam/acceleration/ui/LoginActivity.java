package com.devteam.acceleration.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.devteam.acceleration.R;
import com.devteam.acceleration.jabber.JabberChat;
import com.devteam.acceleration.jabber.JabberModel;
import com.devteam.acceleration.jabber.JabberParams;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import java.io.IOException;


/**
 * A login screen that offers login via emailView/password.
 */
public class LoginActivity extends AppCompatActivity {

    static {
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .penaltyLog()
                .penaltyDeath()
                .build()
        );
    }

    private static final String TAG = LoginActivity.class.getSimpleName();

    // UI references.
    private AutoCompleteTextView jabberIdView;
    private EditText passwordView;
    private EditText emailView;
    private EditText userNameView;
    private View progressView;
    private View loginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        if (prefs.getBoolean(JabberParams.LOGGED_IN, false)) {
            showChatActivity();
        }

        initCallback();

        setContentView(R.layout.activity_login);
        // Set up the login form.
        jabberIdView = (AutoCompleteTextView) findViewById(R.id.jabber_account);
        emailView = (EditText) findViewById(R.id.email);
        userNameView = (EditText) findViewById(R.id.user_name);

        passwordView = (EditText) findViewById(R.id.password);

        final Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        final EditText form = (EditText) findViewById(R.id.email);
        final Button cancel = (Button) findViewById(R.id.back_to_sign_in_button);
        final Button registration = (Button) findViewById(R.id.register_button);
        final Button registerSignIn = (Button) findViewById(R.id.register_sign_in_button);

        //Sign in
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO comment this intent and uncomment attemptLogin();
//                final Intent sw = new Intent(LoginActivity.this, ChatActivity.class);
//                sw.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(sw);
                attemptLogin();
            }
        });

        //Show all for registration
        registration.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                emailView.setVisibility(View.VISIBLE);
                userNameView.setVisibility(View.VISIBLE);
                form.setVisibility(View.VISIBLE);

                mEmailSignInButton.setVisibility(View.GONE);
                registration.setVisibility(View.GONE);
                cancel.setVisibility(View.VISIBLE);
                registerSignIn.setVisibility(View.VISIBLE);
            }
        });

        //Registration
        registerSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                registration();
            }
        });

        //Back to Sign In
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                emailView.setVisibility(View.GONE);
                userNameView.setVisibility(View.GONE);
                form.setVisibility(View.GONE);

                mEmailSignInButton.setVisibility(View.VISIBLE);
                registration.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
                registerSignIn.setVisibility(View.GONE);
            }
        });
        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
    }

    private void initCallback() {
        JabberChat.getJabberChat().bindCallback(new JabberChat.Callback() {
            @Override
            public void onCallback(Message message, Exception e) {
                if (e == null) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    prefs.edit().putBoolean(JabberParams.LOGGED_IN, true).apply();
                    showChatActivity();
                } else if (e instanceof XMPPException) {
                    Toast.makeText(LoginActivity.this, "User already exists", Toast.LENGTH_LONG).show();
                    jabberIdView.setError("Not valid");
                } else if (e instanceof SmackException || e instanceof IOException || e instanceof InterruptedException) {
                    Toast.makeText(LoginActivity.this, "Couldn't connect to server", Toast.LENGTH_LONG).show();
                }
                showProgress(false);
            }
        });
    }

    private void showChatActivity() {
        final Intent sw = new Intent(LoginActivity.this, ChatActivity.class);
        sw.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(sw);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        startChatIfLogin();
    }

    @Override
    protected void onDestroy() {
        JabberChat.getJabberChat().unbindCallback();
        super.onDestroy();
    }


    private void startChatIfLogin() {
        showProgress(false);
        if (JabberChat.connectionState.equals(JabberChat.ConnectionState.AUTHENTICATED)) {
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
        }
    }

    private void registration() {
        jabberIdView.setError(null);
        passwordView.setError(null);
        emailView.setError(null);
        userNameView.setError(null);

        boolean cancel = false;

        String jabberId = jabberIdView.getText().toString();
        String password = passwordView.getText().toString();
        String email = emailView.getText().toString();
        String username = userNameView.getText().toString();

        if (TextUtils.isEmpty(jabberId) || !isEmailValid(jabberId)) {
            cancel = true;
            jabberIdView.setError("Not valid");
        }
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            cancel = true;
            passwordView.setError("Not valid");
        }
        if (TextUtils.isEmpty(email) || !isEmailValid(email)) {
            cancel = true;
            emailView.setError("Not valid");
        }
        if (TextUtils.isEmpty(username)) {
            cancel = true;
            userNameView.setError("Not valid");
        }

        if (!cancel) {
            showProgress(true);
            saveCredentials(true);
            JabberChat.getJabberChat().createAccount();

        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid emailView, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        jabberIdView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String email = jabberIdView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) && isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid emailView address.
        if (TextUtils.isEmpty(email)) {
            jabberIdView.setError(getString(R.string.error_field_required));
            focusView = jabberIdView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            jabberIdView.setError(getString(R.string.error_invalid_email));
            focusView = jabberIdView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            saveCredentials(false);
            JabberChat.getJabberChat().loginToChat();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        if (password.length() >= 1)
            return true;
        return false;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void saveCredentials(boolean isRegistration) {
        Log.d(TAG, "saveCredentials() called.");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String password = new String(Hex.encodeHex(DigestUtils.sha(passwordView.getText().toString())));

        JabberModel jabberModel = new JabberModel();
        jabberModel.setJabberId(jabberIdView.getText().toString());
        jabberModel.setPassword(password);
        jabberModel.setEmail(emailView.getText().toString());
        jabberModel.setName(userNameView.getText().toString());

        JabberChat.getJabberChat().setJabberModel(jabberModel);

        prefs.edit()
                .putString(JabberParams.JABBER_ID, jabberIdView.getText().toString())
                .putString(JabberParams.USER_PASSWORD, password)
                .putBoolean(JabberParams.isRegistration, isRegistration)
                .putBoolean(JabberParams.LOGGED_IN, false).apply();


        if (isRegistration) {
            prefs.edit().putString(JabberParams.USER_NAME, userNameView.getText().toString())
                    .putString(JabberParams.USER_EMAIL, emailView.getText().toString()).apply();
        }
    }
}