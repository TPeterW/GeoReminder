package com.peter.georeminder;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.peter.georeminder.utils.login.ProgressGenerator;
import com.peter.georeminder.utils.swipeback.SwipeBackLayout;
import com.peter.georeminder.utils.swipeback.app.SwipeBackActivity;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginScreen extends SwipeBackActivity implements LoaderCallbacks<Cursor>, ProgressGenerator.OnCompleteListener {

    // TODO: log in through google play and other social media

    private static final int READ_CONTACTS_REQUEST_CODE = 0x001;

    // UI references.
    private LinearLayout emailLoginForm;
    private AutoCompleteTextView inputEmail;
    private EditText inputPasswd;
    private ActionProcessButton btnLogIn;
    private Button btnRegister;

    // login
    private boolean isSigningIn;                // user is signing in, button animation on
    private boolean isRegistering;              // user chooses to register

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
        if(Build.VERSION.SDK_INT >= 21){
            getWindow().setEnterTransition(new Slide(GravityCompat.END));
            getWindow().setReturnTransition(new Slide(GravityCompat.END));
            getWindow().setExitTransition(new Slide(GravityCompat.END));
        }

        swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        setupActionBar();

        // Set up the login form.
        emailLoginForm = (LinearLayout) findViewById(R.id.email_login_form);

        inputEmail = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();
        inputPasswd = (EditText) findViewById(R.id.password);

        btnLogIn = (ActionProcessButton) findViewById(R.id.sign_in_button);
        btnLogIn.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        btnRegister = (Button) findViewById(R.id.register_button);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void initEvent() {
        isSigningIn = false;

        btnLogIn.setMode(ActionProcessButton.Mode.ENDLESS);
        btnLogIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSigningIn) {           // has been clicked
                    isSigningIn = false;
                    inputEmail.setEnabled(true);
                    inputPasswd.setEnabled(true);
                    btnLogIn.setText(R.string.action_sign_in_short);
                    btnLogIn.setMode(ActionProcessButton.Mode.PROGRESS);            // cheeky workaround
                    btnLogIn.setProgress(100);
                } else {                      // not currently signing in
                    attemptLogin();
                }
                //TODO: setProgress(-1)显示Error
            }
        });

        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: go to register page
            }
        });
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(inputEmail, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == READ_CONTACTS_REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        ProgressGenerator progressGenerator = new ProgressGenerator(this);

        // Reset errors.
        inputEmail.setError(null);
        inputPasswd.setError(null);

        // Store values at the time of the login attempt.
        String email = inputEmail.getText().toString();
        String password = inputPasswd.getText().toString();

        boolean cancel = false;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            inputPasswd.setError(getString(R.string.error_invalid_password));
            cancel = true;
            inputPasswd.requestFocus();
            YoYo.with(Techniques.Shake)
                    .duration(100)
                    .playOn(emailLoginForm);
        } else if (!isPasswordValid(password)){
            inputPasswd.setError(getString(R.string.error_invalid_password));
            cancel = true;
            inputPasswd.requestFocus();
            YoYo.with(Techniques.Shake)
                    .duration(100)
                    .playOn(emailLoginForm);
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            inputEmail.setError(getString(R.string.error_field_required));
            cancel = true;
            inputEmail.requestFocus();
        } else if (!isEmailValid(email)) {
            inputEmail.setError(getString(R.string.error_invalid_email));
            cancel = true;
            inputEmail.requestFocus();
        }

        if (!cancel) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            isSigningIn = true;
            btnLogIn.setMode(ActionProcessButton.Mode.ENDLESS);
            btnLogIn.requestFocus();

            progressGenerator.start(btnLogIn);
            btnLogIn.setEnabled(false);
            inputEmail.setEnabled(false);
            inputPasswd.setEnabled(false);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnLogIn.setEnabled(true);
                    btnLogIn.setText(R.string.cancel);
                }
            }, 5000);           // if still not logged in in 10 seconds, you get to cancel
            //TODO: log in
        }
    }

    private boolean isEmailValid(String email) {

        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {

        return password.length() >= 6;
    }

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
        //TODO: implement login, if parse login success, then call this method
        btnLogIn.setEnabled(true);
        btnLogIn.setText(R.string.action_sign_in_short);
        isSigningIn = false;
        inputEmail.setEnabled(true);
        inputPasswd.setEnabled(true);
        Log.i("ProgressGen", "OnComplete");
    }

    @Override
    public void onCancel() {
        btnLogIn.setProgress(0);
        btnLogIn.setText(R.string.action_sign_in_short);
        isSigningIn = false;
        Log.i("ProgressGen", "OnCancel");
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
}

