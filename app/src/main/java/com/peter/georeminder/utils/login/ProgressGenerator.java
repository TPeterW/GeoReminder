package com.peter.georeminder.utils.login;


//        ("`-''-/").___..--''"`-._
//        `6_ 6  )   `-.  (     ).`-.__.`)
//        (_Y_.)'  ._   )  `._ `. ``-..-`
//        _..`--'_..-_/  /--'_.' ,'
//        (il),-''  (li),'  ((!.-'

import com.dd.processbutton.ProcessButton;
import android.os.Handler;

import java.util.Random;

public class ProgressGenerator {

    private int mProgress;

    public boolean isComplete;

    public ProgressGenerator() {
        isComplete = false;
    }

    public void start(final ProcessButton button) {
        final Handler handler = new Handler();

        mProgress += 10;
        button.setProgress(mProgress);
    }

    public void stop(final ProcessButton button) {
        button.setProgress(-1);
    }
}

