package com.peter.georeminder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

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

public class SettingsScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent backToMainScreen = new Intent(SettingsScreen.this, MainScreen.class);
                backToMainScreen.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(SettingsScreen.this, backToMainScreen);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}