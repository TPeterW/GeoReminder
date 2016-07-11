package com.peter.georeminder.utils.login;

import android.content.Context;
import android.util.Log;

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

    // TODO:
    public static User getCurrentUser() {
        return null;
    }

    public void unregisterListener(Context context) {
        callbacks.remove((LoginListener) context);
    }

    public void logInInBackground(String email, String password) {
        // TODO:
    }

    public void registerInBackground(String email, String password) {
        // TODO:
    }

    public interface LoginListener {

        void onLoginComplete();

        void onLoginFail(Exception e);

        void onLoginCancelled();

        void onLogoutComplete();

        void onRegisterComplete();

        void onRegisterFail(Exception e);

    }

}
