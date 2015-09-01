package com.exacttarget.alamotte.lamotte_app4;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.exacttarget.alamotte.lamotte_app4.utils.Utils;
import com.exacttarget.etpushsdk.util.EventBus;
import com.exacttarget.etpushsdk.event.RegistrationEvent;
import com.exacttarget.etpushsdk.data.Attribute;
import com.exacttarget.etpushsdk.*;
import android.util.Log;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = Utils.formatTag(MainActivity.class.getSimpleName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        EventBus.getInstance().register(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().unregister(this);
    }

    public void onEvent(final RegistrationEvent event) {
        if (ETPush.getLogLevel() <= Log.DEBUG) {
            Log.i(TAG, "Marketing Cloud update occurred.  You could now save Marketing Cloud details in your own data stores...");
            Log.i(TAG, "Device ID:" + event.getDeviceId());
            Log.i(TAG, "System Token:" + event.getSystemToken());
            Log.i(TAG, "Subscriber key:" + event.getSubscriberKey());

            for (Attribute attribute : (ArrayList<Attribute>) event.getAttributes()) {
                Log.i(TAG, "Attribute " + attribute.getKey() + ": [" + attribute.getValue() + "]");
            }
            Log.i(TAG, "Tags: " + event.getTags());
            Log.i(TAG, "Language: " + event.getLocale());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            // Let JB4A SDK know when each activity paused
            ETPush.activityPaused(this);
        }
        catch (Exception e) {
            if (ETPush.getLogLevel() <= Log.ERROR) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Let JB4A SDK know when each activity resumed(
            ETPush.activityResumed(this);
        }
        catch (Exception e) {
            if (ETPush.getLogLevel() <= Log.ERROR) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

}
