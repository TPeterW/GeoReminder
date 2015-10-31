package com.peter.georeminder.utils;


//        ("`-''-/").___..--''"`-._
//        `6_ 6  )   `-.  (     ).`-.__.`)
//        (_Y_.)'  ._   )  `._ `. ``-..-`
//        _..`--'_..-_/  /--'_.' ,'
//        (il),-''  (li),'  ((!.-'

import com.dd.processbutton.ProcessButton;
import android.os.Handler;

import java.util.Random;

public class ProgressGenerator {

    private OnCompleteListener logInCompleteListener;
    private int mProgress;

    public boolean isComplete;

    public ProgressGenerator(OnCompleteListener listener) {
        logInCompleteListener = listener;
        isComplete = false;
    }

    public void start(final ProcessButton button) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgress = 0;
                button.setProgress(mProgress);
            }
        }, 200);
    }


    public interface OnCompleteListener {
        void onComplete();
        void onCancel();
    }
}

