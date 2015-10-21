package com.peter.georeminder.utils.searchbox;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.quinny898.library.persistentsearch.SearchBox;

/**
 * Created by Peter on 10/21/15.
 */
public class BackEventEditText extends EditText {

    private com.peter.georeminder.utils.searchbox.SearchBox searchBox;

    private long counter = 0;

    public void setSearchBox(com.peter.georeminder.utils.searchbox.SearchBox searchBox) {
        this.searchBox = searchBox;
    }

    public BackEventEditText(Context context) {
        super(context);
    }

    public BackEventEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackEventEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getApplicationWindowToken(), 0);

                return true;
        }

        return super.onKeyPreIme(keyCode, event);
    }
}
