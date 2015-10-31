package com.peter.georeminder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.peter.georeminder.utils.SettingsFragment;

/**
 * Created by Peter on 10/9/15.
 * Simple settings page that loads the SettingsFragment
 */

//        ."".    ."",
//        |  |   /  /
//        |  |  /  /
//        |  | /  /
//        |  |/  ;-._
//        }  ` _/  / ;
//        |  /` ) /  /
//        | /  /_/\_/\
//        |/  /      |
//        (  ' \ '-  |
//        \    `.  /
//        |      |
//        |      |

public class SettingScreen extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}