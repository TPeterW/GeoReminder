package com.peter.georeminder;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.transition.Slide;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.dd.processbutton.iml.ActionProcessButton;
import com.facebook.login.widget.LoginButton;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.peter.georeminder.utils.login.FBLoginButton;
import com.peter.georeminder.utils.login.ProgressGenerator;
import com.peter.georeminder.utils.login.ProgressGenerator.OnCompleteListener;
import com.peter.georeminder.utils.login.TWLoginButton;
import com.peter.georeminder.utils.swipeback.SwipeBackLayout;
import com.peter.georeminder.utils.swipeback.app.SwipeBackActivity;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginScreen extends SwipeBackActivity implements LoaderCallbacks<Cursor>, OnCompleteListener {

    // TODO: log in through google play and other social media
    // TODO: enable registration

    private static final int READ_CONTACTS_REQUEST_CODE = 0x001;

    private static final int REGISTER_REQUEST_CODE      = 0x010;

    // UI references.
    private LinearLayout emailLoginForm;
    private AutoCompleteTextView inputEmail;
    private EditText inputPasswd;
    private ActionProcessButton btnLogIn;
    private Button btnRegister;

    private FBLoginButton facebookLoginButton;
    private TWLoginButton twitterLoginButton;

    // login
    private boolean isSigningIn;                // user is signing in, button animation on
    private LogInCallback logInCallback;

    // Swipe Back
    private SwipeBackLayout swipeBackLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        initView();

        initEvent();
    }

    private void initView() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(GravityCompat.END).excludeTarget(android.R.id.statusBarBackground, true));
            getWindow().setReturnTransition(new Slide(GravityCompat.END).excludeTarget(android.R.id.statusBarBackground, true));
            getWindow().setReenterTransition(new Slide(GravityCompat.END).excludeTarget(android.R.id.statusBarBackground, true));
            getWindow().setExitTransition(new Slide(GravityCompat.END).excludeTarget(android.R.id.statusBarBackground, true));
        }

        swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        setupActionBar();

        // Set up the login form.
        emailLoginForm = (LinearLayout) findViewById(R.id.email_login_form);

        inputEmail = (AutoCompleteTextView) findViewById(R.id.login_email);
        populateAutoComplete();
        inputPasswd = (EditText) findViewById(R.id.login_password);

        btnLogIn = (ActionProcessButton) findViewById(R.id.sign_in_button);
        btnLogIn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        btnRegister = (Button) findViewById(R.id.btn_register_screen);

        facebookLoginButton = (FBLoginButton) findViewById(R.id.facebook_login_button);
        twitterLoginButton = (TWLoginButton) findViewById(R.id.twitter_login_button);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void initEvent() {
        isSigningIn = false;

        btnLogIn.setMode(ActionProcessButton.Mode.ENDLESS);
        btnLogIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSigningIn)               // is not currently signing in
                    attemptLogin();
                // NOTE: setProgress(-1)显示Error
            }
        });

        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toRegisterScreen = new Intent(LoginScreen.this, RegisterScreen.class);
                if (Build.VERSION.SDK_INT > 21) { getWindow().setExitTransition(null); }
                startActivityForResult(toRegisterScreen, REGISTER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(LoginScreen.this).toBundle());
            }
        });

        // TODO: so much todo
        facebookLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Login Button", "Facebook");
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginScreen.this, null, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null) {                // success
                            // TODO:
                        } else {                        // failure
                            showErrorMessage(e);
                        }
                    }
                });
            }
        });

        twitterLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Login Button", "Twitter");
                ParseTwitterUtils.getTwitter().setScreenName(getString(R.string.app_name));
                ParseTwitterUtils.logIn(LoginScreen.this, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null) {                // success
                            // TODO:
                        } else {                        // failure
                            showErrorMessage(e);
                        }
                    }
                });
            }
        });
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts())
            return;

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            return true;

        if (shouldShowRequestPermissionRationale(READ_CONTACTS))
            Snackbar.make(inputEmail, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
                        }
                    });
        else
            requestPermissions(new String[]{READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);

        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == READ_CONTACTS_REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                populateAutoComplete();
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);      // Show the Up button in the action bar.
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        ParseTwitterUtils.getTwitter().setConsumerKey(getString(R.string.twitter_consumer_key))
                                    .setConsumerSecret(getString(R.string.twitter_consumer_secret));
        ParseTwitterUtils.logInInBackground(this);

        final ProgressGenerator progressGenerator = new ProgressGenerator(this);

        // Reset errors.
        inputEmail.setError(null);
        inputPasswd.setError(null);

        // Store values at the time of the login attempt.
        String email = inputEmail.getText().toString();
        String password = inputPasswd.getText().toString();

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            inputEmail.setError(getString(R.string.error_field_required));
            inputEmail.requestFocus();
            shake();
            return;
        } else if (!isEmailValid(email)) {
            inputEmail.setError(getString(R.string.error_invalid_email));
            inputEmail.requestFocus();
            shake();
            return;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            inputPasswd.setError(getString(R.string.error_invalid_password));
            inputPasswd.requestFocus();
            shake();
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        isSigningIn = true;
        btnLogIn.requestFocus();

        progressGenerator.start(btnLogIn);
        btnLogIn.setEnabled(false);
        inputEmail.setEnabled(false);
        inputPasswd.setEnabled(false);

        // TODO: log in through facebook and twitter

        ParseUser.logInInBackground(email, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {         // logged in
                    Log.e("Log in", "Complete");
                    finish();
                } else {                    // failed
                    Log.e("Log in", "Failed");
                    e.printStackTrace();
                    showErrorMessage(e);
                    progressGenerator.stop(btnLogIn);
                }
            }
        });
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    private void showErrorMessage(ParseException e) {
        switch (e.getCode()) {
            case ParseException.CONNECTION_FAILED:
                Toast.makeText(this, R.string.exception_connection_failed, Toast.LENGTH_SHORT).show();
                break;
            case ParseException.INTERNAL_SERVER_ERROR:
                Toast.makeText(this, R.string.exception_internal_server_error, Toast.LENGTH_SHORT).show();
                break;
            case ParseException.TIMEOUT:
                Toast.makeText(this, R.string.exception_timeout, Toast.LENGTH_SHORT).show();
                break;
            case ParseException.VALIDATION_ERROR:
                Toast.makeText(this, R.string.exception_validation_error, Toast.LENGTH_SHORT).show();
                break;
            case ParseException.INVALID_EMAIL_ADDRESS:
                Toast.makeText(this, R.string.exception_invalid_email, Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, R.string.exception_invalid_login_param, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REGISTER_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.d("Register Result Code", "Success" + resultCode);
                    setResult(RESULT_OK);
                    Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }

        // for Facebook integration
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    /***
     * below is for auto complete
     * @param i
     * @param bundle
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onComplete() {
        btnLogIn.setEnabled(true);
        btnLogIn.setText(R.string.success);
        btnLogIn.setProgress(100);
        isSigningIn = false;
        inputEmail.setEnabled(true);
        inputPasswd.setEnabled(true);
        Log.i("ProgressGen", "onComplete");
    }

    @Override
    public void onFail() {
        inputEmail.setEnabled(true);
        inputPasswd.setEnabled(true);
        isSigningIn = false;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnLogIn.setEnabled(true);
                btnLogIn.setProgress(0);
            }
        }, 2000);           // change back to normal button in two seconds

        Log.i("ProgressGen", "onFail");
    }

    @Override
    public void onCancel() {
        btnLogIn.setProgress(0);
        isSigningIn = false;
        Log.i("ProgressGen", "onCancel");
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginScreen.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        inputEmail.setAdapter(adapter);
    }

    private void shake() {
        YoYo.with(Techniques.Shake)
                .duration(100)
                .playOn(emailLoginForm);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                swipeBackLayout.scrollToFinishActivity();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}

