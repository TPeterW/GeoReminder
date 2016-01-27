package com.peter.georeminder.utils.login;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.facebook.login.widget.LoginButton;
import com.peter.georeminder.R;

/**
 * Created by Peter on 1/5/16.
 *
 */
public class FBLoginButton extends LoginButton {
    public FBLoginButton(Context context) {
        super(context);
    }

    public FBLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FBLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setInternalOnClickListener(null);
        super.setOnClickListener(l);
        setText(R.string.login_with_facebook);
    }
}
