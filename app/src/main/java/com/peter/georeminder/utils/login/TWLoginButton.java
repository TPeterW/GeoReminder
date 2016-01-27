package com.peter.georeminder.utils.login;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.peter.georeminder.R;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.lang.ref.WeakReference;

/**
 * Created by Peter on 1/5/16.
 *
 */
public class TWLoginButton extends Button {
    public TWLoginButton(Context context) {
        this(context, (AttributeSet) null);
    }

    public TWLoginButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16842824);
    }

    public TWLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setupButton();
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        super.setOnClickListener(onClickListener);
    }

    @TargetApi(21)
    private void setupButton() {
        Resources res = this.getResources();
        super.setCompoundDrawablesWithIntrinsicBounds(res.getDrawable(com.twitter.sdk.android.core.R.drawable.tw__ic_logo_default), null, null, null);
        super.setCompoundDrawablePadding(res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_drawable_padding));
//        super.setText(com.twitter.sdk.android.core.R.string.tw__login_btn_txt);
        super.setText(R.string.login_with_twitter);
        super.setTextColor(res.getColor(com.twitter.sdk.android.core.R.color.tw__solid_white));
        super.setTextSize(0, (float) res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_text_size));
        super.setTypeface(Typeface.DEFAULT_BOLD);
        super.setPadding(res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_left_padding), 0, res.getDimensionPixelSize(com.twitter.sdk.android.core.R.dimen.tw__login_btn_right_padding), 0);
        super.setBackgroundResource(com.twitter.sdk.android.core.R.drawable.tw__login_btn);
        if(Build.VERSION.SDK_INT >= 21) {
            super.setAllCaps(false);
        }
    }
}
