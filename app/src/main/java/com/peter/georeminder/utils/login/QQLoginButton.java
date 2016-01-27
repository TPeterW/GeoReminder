package com.peter.georeminder.utils.login;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.Button;

import com.peter.georeminder.R;

/**
 * Created by Peter on 1/6/16.
 *
 */
public class QQLoginButton extends Button {
    public QQLoginButton(Context context) {
        this(context, (AttributeSet) null);
    }

    public QQLoginButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16842824);
    }

    public QQLoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setupButton();
    }

    @TargetApi(21)
    private void setupButton() {
        Resources res = this.getResources();
        super.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(getContext(), R.mipmap.ic_qq_login), null, null, null);
        super.setCompoundDrawablePadding(res.getDimensionPixelSize(R.dimen.qq_login_btn_drawable_padding));
        super.setText(R.string.login_with_qq);
        super.setTextColor(ContextCompat.getColor(getContext(), R.color.qqText));
        super.setTypeface(Typeface.DEFAULT_BOLD);
//        super.setPadding(res.getDimensionPixelSize(R.dimen.qq_login_btn_left_padding), 0, res.getDimensionPixelSize(R.dimen.qq_login_btn_right_padding), 0);
        super.setBackgroundResource(R.drawable.qq__login_btn);
        if(Build.VERSION.SDK_INT >= 21) {
            super.setAllCaps(false);
        }
    }


}
