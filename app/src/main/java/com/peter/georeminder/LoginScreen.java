package com.peter.georeminder;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NavUtils;
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
import android.view.MenuItem;
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
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.peter.georeminder.utils.login.FBLoginButton;
import com.peter.georeminder.utils.login.LoginAgent;
import com.peter.georeminder.utils.login.LoginAgent.LoginListener;
import com.peter.georeminder.utils.login.ProgressGenerator;
import com.peter.georeminder.utils.login.QQLoginButton;
import com.peter.georeminder.utils.login.TWLoginButton;
import com.peter.georeminder.utils.swipeback.SwipeBackLayout;
import com.peter.georeminder.utils.swipeback.app.SwipeBackActivity;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginScreen extends SwipeBackActivity implements LoaderCallbacks<Cursor>, LoginListener {

    private static final int READ_CONTACTS_REQUEST_CODE     = 0x001;

    private static final int REGISTER_REQUEST_CODE          = 0x010;

    public static final int LOGIN_SUCCESS                   = 0x101;
    public static final int LOGIN_CANCELLED                 = 0x102;

    // UI references.
    private LinearLayout emailLoginForm;
    private AutoCompleteTextView inputEmail;
    private EditText inputPasswd;
    private ActionProcessButton btnLogIn;
    private Button btnRegister;

    private ProgressGenerator progressGenerator;

    private FBLoginButton facebookLoginButton;
    private TWLoginButton twitterLoginButton;
    private QQLoginButton qqLoginButton;
    private Tencent tencent;

    // login
    private boolean isSigningIn;                // user is signing in, button animation on

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
        qqLoginButton = (QQLoginButton) findViewById(R.id.qq_login_button);

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
                if (Build.VERSION.SDK_INT > 21) {
                    getWindow().setExitTransition(null);
                }
                startActivityForResult(toRegisterScreen, REGISTER_REQUEST_CODE, ActivityOptionsCompat.makeSceneTransitionAnimation(LoginScreen.this).toBundle());
            }
        });

        // TODO: so much todo
        facebookLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Login Button", "Facebook");
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginScreen.this, Collections.singletonList("email"), new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user == null) {                 // cancelled
                            Log.d("Facebook Login", "Cancelled");
                        } else if (user.isNew()) {          // newly registered
                            Log.d("Facebook Login", "New: " + user.getEmail());
                            Log.d("Facebook Login", "New: " + user.getUsername());
                            // TODO: set username
                        } else {                            // normal login
                            Log.d("Facebook Login", "Success: " + user.getEmail());
                            Log.d("Facebook Login", "Success: " + user.getUsername());
                            setResult(LOGIN_SUCCESS);
                            swipeBackLayout.scrollToFinishActivity();
                        }

                        if (user != null) {
                            // get Facebook user email
                            GraphRequest request = GraphRequest.newMeRequest(
                                    AccessToken.getCurrentAccessToken(),
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(JSONObject object, GraphResponse response) {
                                            try {
                                                Log.d("Facebook Login", "Email: " + object.getString("email"));
                                            } catch (Exception e) {
                                                Toast.makeText(LoginScreen.this, getString(R.string.failed_fetch_email), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "email");
                            request.setParameters(parameters);
                            request.executeAsync();
                        }

                        if (e != null)
                            showErrorMessage(e);
                    }
                });
            }
        });

        twitterLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Login Button", "Twitter");
                ParseTwitterUtils.logIn(LoginScreen.this, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user == null) {                 // cancelled
                            Log.d("Twitter Login", "Cancelled");
                        } else if (user.isNew()) {          // newly registered
                            // TODO:
                            Log.d("Twitter Login", "New: " + user.getEmail());
                            Log.d("Twitter Login", "New: " + user.getUsername());
                        } else {                            // normal login
                            Log.d("Twitter Login", "Success: " + user.getEmail());
                            Log.d("Twitter Login", "Success: " + user.getUsername());

                            setResult(LOGIN_SUCCESS);
                            swipeBackLayout.scrollToFinishActivity();
                        }

                        Log.d("Twitter Login", ParseTwitterUtils.getTwitter().getScreenName());
                        Log.d("Twitter Login", ParseTwitterUtils.getTwitter().getAuthToken());

                        if (e != null)
                            showErrorMessage(e);
                    }
                });
            }
        });

        qqLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Login Button", "QQ");

                tencent.login(LoginScreen.this, "all", new IUiListener() {          // SCOPE -> "get_user_info"
                    @Override
                    public void onComplete(Object o) {
                        // TODO: figure out how to get username and stuff
                        Log.d("QQ Login", o.toString());
                        JSONObject loginResult = (JSONObject) o;

                        // get QQ user info
                        try {
                            String openId = loginResult.getString("openid");
                            Log.d("QQ Login", "openid: " + openId);
                            String accessToken = loginResult.getString("access_token");
                            Log.d("QQ Login", "access_token: " + accessToken);

                            QQToken token = tencent.getQQToken();
                            token.setOpenId(openId);
//                            token.setAccessToken(accessToken, null);
                            token.setAccessToken(accessToken, getString(R.string.tencent_app_id));

                            UserInfo info = new UserInfo(LoginScreen.this, token);
                            info.getUserInfo(new IUiListener() {
                                @Override
                                public void onComplete(Object o) {
                                    try {
                                        Log.d("QQ User Info", o.toString());
                                        JSONObject userQQProfile = (JSONObject) o;
                                        Log.d("QQ Login", userQQProfile.getString("nickname"));
                                        Log.d("QQ Login", userQQProfile.getString("gender"));
                                        Log.d("QQ Login", userQQProfile.getString("figureurl"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                // {"ret":0,"msg":"","is_lost":0,"nickname":"激鬥龍圖騰","gender":"男","province":"江苏","city":"苏州","figureurl":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105087374\/23AC07C903F608135BA09169B2A53680\/30","figureurl_1":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105087374\/23AC07C903F608135BA09169B2A53680\/50","figureurl_2":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105087374\/23AC07C903F608135BA09169B2A53680\/100","figureurl_qq_1":"http:\/\/q.qlogo.cn\/qqapp\/1105087374\/23AC07C903F608135BA09169B2A53680\/40","figureurl_qq_2":"http:\/\/q.qlogo.cn\/qqapp\/1105087374\/23AC07C903F608135BA09169B2A53680\/100","is_yellow_vip":"0","vip":"0","yellow_vip_level":"0","level":"0","is_yellow_year_vip":"0"}

                                @Override
                                public void onError(UiError uiError) {
                                    Log.d("QQ Login", "User Info Error");
                                    // TODO:
                                }

                                @Override
                                public void onCancel() {
                                    Log.d("QQ Login", "User Info Cancel");
                                    // TODO:
                                }
                            });
                        } catch (Exception e) {
                            Log.e("QQ Login", "Failed to retrive user info");
                            e.printStackTrace();
                        }

                        setResult(LOGIN_SUCCESS);
                        swipeBackLayout.scrollToFinishActivity();
                    }

                    @Override
                    public void onError(UiError uiError) {
                        // TODO:
//                        110201：未登陆
//                        110405：登录请求被限制
//                        110404：请求参数缺少appid
//                        110401：请求的应用不存在
//                        110407：应用已经下架
//                        110406：应用没有通过审核
//                        100044：错误的sign
//                        110500：获取用户授权信息失败
//                        110501：获取应用的授权信息失败
//                        110502：设置用户授权失败
//                        110503：获取token失败
//                        110504：系统内部错误
                        Toast.makeText(LoginScreen.this, uiError.errorMessage, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        // do nothing
                    }
                });
            }
        });

        tencent = Tencent.createInstance(getString(R.string.tencent_app_id), getApplicationContext());

        // TODO: remove later
        if (tencent.isSessionValid()) {
            tencent.logout(this);
            Log.d("Tencent Session Valid", tencent.isSessionValid() + "");
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);      // Show the Up button in the action bar.
            } catch (Exception e) {
                // so just don't set then
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        progressGenerator = new ProgressGenerator();

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

        LoginAgent.getInstance().logInInBackground(email, password);
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
    public void onLoginComplete() {
        Log.i("LoginScreen", "Login complete");
        btnLogIn.setEnabled(true);
        btnLogIn.setText(R.string.success);
        btnLogIn.setProgress(100);
        isSigningIn = false;
        inputEmail.setEnabled(true);
        inputPasswd.setEnabled(true);

        // TODO: go to MainScreen
        setResult(LOGIN_SUCCESS);
        swipeBackLayout.scrollToFinishActivity();
    }

    @Override
    public void onLoginFail(ParseException e) {
        Log.i("LoginScreen", "Login failed");
        inputEmail.setEnabled(true);
        inputPasswd.setEnabled(true);
        isSigningIn = false;

        progressGenerator.stop(btnLogIn);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnLogIn.setEnabled(true);
                btnLogIn.setProgress(0);
            }
        }, 2000);           // change back to normal button in two seconds

        e.printStackTrace();
        showErrorMessage(e);
    }

    @Override
    public void onLoginCancelled() {
        btnLogIn.setProgress(0);
        isSigningIn = false;
        Log.i("LoginScreen", "Login cancelled");
    }

    @Override
    public void onLogoutComplete() {
        // nothing here
    }

    @Override
    public void onRegisterComplete() {
        // nothing here
    }

    @Override
    public void onRegisterFail(ParseException e) {
        // nothing here
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REGISTER_REQUEST_CODE:
                if (resultCode == RegisterScreen.REGISTER_SUCCESS) {
                    Log.d("LoginScreen", "Register success: " + Integer.toHexString(resultCode).toUpperCase());
//                    Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                    setResult(LOGIN_SUCCESS);
                    swipeBackLayout.scrollToFinishActivity();
                }
                break;
        }

        // for Facebook integration
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

        // for Twitter sdk integration
//        twitterLoginButton.onActivityResult(requestCode, resultCode, data);

        Tencent.onActivityResultData(requestCode, resultCode, data, new IUiListener() {
            @Override
            public void onComplete(Object o) {
                Log.d("QQ Activity", "Success");
            }

            @Override
            public void onError(UiError uiError) {
                Log.d("QQ Activity", "Error");
            }

            @Override
            public void onCancel() {
                Log.d("QQ Activity", "Cancel");
            }
        });

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                setResult(LOGIN_CANCELLED);
                swipeBackLayout.scrollToFinishActivity();
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onResume() {
        LoginAgent.getInstance().registerListener(this);
        Log.i("LoginScreen", "Registered listener");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LoginAgent.getInstance().unregisterListener(this);
        Log.i("LoginScreen", "Unregistered listener");
        super.onPause();
    }
}

