package com.peter.georeminder.utils.login;

import android.support.annotation.DrawableRes;

/**
 * Created by Peter on 7/10/16.
 *
 */
public class User {

    private String mName;
    private String mEmail;
    private @DrawableRes int mIcon;
    private long mIdentifier;

    public String getUsername() {
        return mName;
    }

    public User setUsername(String username) {
        mName = username;
        return this;
    }

    public String getEmail() {
        return mEmail;
    }

    public User setEmail(String email) {
        mEmail = email;
        return this;
    }

    public @DrawableRes int getIcon() {
        return mIcon;
    }

    public User setIcon(@DrawableRes int icon) {
        mIcon = icon;
        return this;
    }

    public long getIdentifier() {
        return mIdentifier;
    }

    public User setIdentifier(long identifier) {
        mIdentifier = identifier;
        return this;
    }

}
