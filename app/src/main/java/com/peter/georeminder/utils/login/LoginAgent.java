package com.peter.georeminder.utils.login;

import android.content.Context;
import android.util.Log;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 1/26/16.
 *
 * Takes care of logging in
 *
 */
public class LoginAgent {

    private List<LoginListener> callbacks = new ArrayList<>();

    private static LoginAgent instance = new LoginAgent();

    public static LoginAgent getInstance() {
        return instance;
    }

    public void registerListener(Context context) {
        if (!callbacks.contains(context)) {
            Log.i("LoginAgent", "Activity already registered");
            callbacks.add((LoginListener) context);
        }
    }

    public void unregisterListener(Context context) {
        callbacks.remove((LoginListener) context);
    }

    public void logInInBackground(String email, String password) {
        ParseUser.logInInBackground(email, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    for (LoginListener callback : callbacks) {
                        callback.onLoginComplete();
                    }
                } else {
                    for (LoginListener callback : callbacks) {
                        callback.onLoginFail(e);
                    }
                }
            }
        });
    }

    public void registerInBackground(String email, String password) {
        ParseUser user = new ParseUser();
        user.setEmail(email);
        user.setUsername(email);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    for (LoginListener callback : callbacks) {
                        callback.onRegisterComplete();
                    }
                } else {
                    for (LoginListener callback : callbacks) {
                        callback.onRegisterFail(e);
                    }
                }
            }
        });
    }

    public interface LoginListener {

        void onLoginComplete();

        void onLoginFail(ParseException e);

        void onLoginCancelled();

        void onLogoutComplete();

        void onRegisterComplete();

        void onRegisterFail(ParseException e);

    }

}
