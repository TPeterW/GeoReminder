package com.peter.georeminder;

import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.text.TextUtils;
import android.transition.Slide;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.dd.processbutton.iml.ActionProcessButton;
import com.peter.georeminder.utils.login.LoginAgent;
import com.peter.georeminder.utils.login.LoginAgent.LoginListener;
import com.peter.georeminder.utils.login.ProgressGenerator;
import com.peter.georeminder.utils.swipeback.SwipeBackLayout;
import com.peter.georeminder.utils.swipeback.app.SwipeBackActivity;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * Created by Peter on 1/5/16.
 *
 */
public class RegisterScreen extends SwipeBackActivity implements LoaderCallbacks<Cursor>, LoginListener {

    // TODO: log in through google play and other social media
    // TODO: enable registration

    private static final int READ_CONTACTS_REQUEST_CODE     = 0x001;

    public static final int REGISTER_SUCCESS                = 0x103;
    public static final int REGISTER_CANCELLED              = 0x104;

    // UI references.
    private LinearLayout emailRegisterForm;
    private AutoCompleteTextView inputEmail;
    private EditText inputPasswd;
    private EditText inputConfirmPasswd;
    private ActionProcessButton btnRegister;

    private ProgressGenerator progressGenerator;

    // login
    private boolean isRegistering;                // user is signing in, button animation on

    // Swipe Back
    private SwipeBackLayout swipeBackLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        initView();

        initEvent();
    }

    private void initView() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(GravityCompat.END).excludeTarget(android.R.id.statusBarBackground, true));
            getWindow().setReturnTransition(new Slide(GravityCompat.END).excludeTarget(android.R.id.statusBarBackground, true));
            getWindow().setExitTransition(new Slide(GravityCompat.END).excludeTarget(android.R.id.statusBarBackground, true));
        }

        swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        setupActionBar();

        // Set up the login form.
        emailRegisterForm = (LinearLayout) findViewById(R.id.email_register_form);

        inputEmail = (AutoCompleteTextView) findViewById(R.id.register_email);
        populateAutoComplete();
        inputPasswd = (EditText) findViewById(R.id.register_password);
        inputConfirmPasswd = (EditText) findViewById(R.id.register_confirm_password);

        btnRegister = (ActionProcessButton) findViewById(R.id.register_button);
        btnRegister.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void initEvent() {
        setResult(RESULT_CANCELED);
        isRegistering = false;

        btnRegister.setMode(ActionProcessButton.Mode.ENDLESS);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRegistering) {           // has been clicked
                    isRegistering = false;
                    inputEmail.setEnabled(true);
                    inputPasswd.setEnabled(true);
                    btnRegister.setText(R.string.action_sign_in_short);
                    btnRegister.setMode(ActionProcessButton.Mode.PROGRESS);            // cheeky workaround
                    btnRegister.setProgress(100);
                } else {                      // not currently signing in
                    attemptRegister();
                }
                // NOTE: setProgress(-1)显示Error
            }
        });
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
            try {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } catch (Exception e) {
                // do nothing again
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        progressGenerator = new ProgressGenerator();

        // Reset errors.
        inputEmail.setError(null);
        inputPasswd.setError(null);

        // Store values at the time of the login attempt.
        String email = inputEmail.getText().toString();
        String password = inputPasswd.getText().toString();
        String confirmPassword = inputConfirmPasswd.getText().toString();

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

        // Check if passwords match
        if (!confirmPassword.equals(password)) {
            inputConfirmPasswd.setError(getString(R.string.error_password_does_not_match));
            inputConfirmPasswd.requestFocus();
            shake();
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        isRegistering = true;
        btnRegister.requestFocus();

        progressGenerator.start(btnRegister);
        btnRegister.setEnabled(false);
        inputEmail.setEnabled(false);
        inputPasswd.setEnabled(false);
        inputConfirmPasswd.setEnabled(false);

        LoginAgent.getInstance().registerInBackground(email, password);
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
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
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onLoginComplete() {
        // nothing here
    }

    @Override
    public void onLoginFail(Exception e) {
        // nothing here
    }

    @Override
    public void onLoginCancelled() {
        // nothing here
    }

    @Override
    public void onLogoutComplete() {
        // nothing here
    }

    @Override
    public void onRegisterComplete() {
        Log.i("RegisterScreen", "Register complete");
        btnRegister.setEnabled(true);
        btnRegister.setText(R.string.success);
        btnRegister.setProgress(100);
        isRegistering = false;
        inputEmail.setEnabled(true);
        inputPasswd.setEnabled(true);
        inputConfirmPasswd.setEnabled(true);

        // set result and go back
        setResult(REGISTER_CANCELLED);
        swipeBackLayout.scrollToFinishActivity();
    }

    @Override
    public void onRegisterFail(Exception e) {
        Log.i("RegisterScreen", "Register fail");
        btnRegister.setProgress(0);
        btnRegister.setText(R.string.action_sign_in_short);
        isRegistering = false;

        e.printStackTrace();
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
                new ArrayAdapter<>(RegisterScreen.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        inputEmail.setAdapter(adapter);
    }

    private void shake() {
        YoYo.with(Techniques.Shake)
                .duration(100)
                .playOn(emailRegisterForm);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                swipeBackLayout.scrollToFinishActivity();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onResume() {
        LoginAgent.getInstance().registerListener(this);
        Log.d("RegisterScreen", "Registered listener");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LoginAgent.getInstance().unregisterListener(this);
        Log.d("RegisterScreen", "Unregistered listener");
        super.onPause();
    }
}
