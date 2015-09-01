package com.exacttarget.alamotte.lamotte_app4;

import android.app.Application;
import com.exacttarget.alamotte.lamotte_app4.utils.Utils;
import com.exacttarget.etpushsdk.ETPush;
import com.exacttarget.etpushsdk.ETPushConfig;
import com.exacttarget.etpushsdk.ETException;
import com.exacttarget.etpushsdk.event.ReadyAimFireInitCompletedEvent;
import android.util.Log;
import android.content.SharedPreferences;
import android.content.Context;
import com.exacttarget.etpushsdk.util.EventBus;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.preference.PreferenceManager;
import com.exacttarget.etpushsdk.util.ETLogger;
import android.os.Bundle;
import com.exacttarget.etpushsdk.ETNotificationLaunchIntent;
import com.exacttarget.etpushsdk.ETNotifications;
import com.exacttarget.etpushsdk.ETNotificationBuilder;
import com.exacttarget.etpushsdk.ETLocationManager;

import android.app.PendingIntent;
import android.provider.CalendarContract;
import android.support.v4.app.NotificationCompat;



import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by alamotte on 7/29/15.
 */
public class lamotte_app extends Application{

    public static final String ITEM_SPOTLIGHT = "item_spotlight";
    public static final String ONE_DAY_SALE = "one_day_sale";

    private static final String TAG = Utils.formatTag(lamotte_app.class.getSimpleName());
    private static String message;

    private static Context appContext;
    private static boolean quitAppNow = false;

    private static String deviceId = null;

    public static Context context() {
        return appContext;
    }

    public static void setQuitAppNow() {
        quitAppNow = true;
    }

    public static boolean getQuitAppNow() {
        return quitAppNow;
    }

    public static String getDeviceId() {
        return deviceId;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate() {
        super.onCreate();

        appContext = this;

        //only needed until beacons is turned back on
        SharedPreferences prefs = getSharedPreferences("ETPush", Context.MODE_PRIVATE);
        if (prefs.contains("et_proximity_enabled_key")) {
            boolean oldPEK = prefs.getBoolean("et_proximity_enabled_key", false);

            if (oldPEK) {
                prefs.edit().putBoolean("et_proximity_enabled_key", false).commit();
            }
        }

        // EventBus.getInstance()
        //
        //		Register this Application to process events from the SDK.
        EventBus.getInstance().register(this);

        // Initialize Salesforce Journey Builder for Apps SDK
        JB4A_SDK_init();

        if (ETPush.getLogLevel() <= Log.DEBUG) {
            // use log.i to differentiate from SDK
            Log.i(TAG, "onCreate() end.");
        }

        try {
                // opt in for location messages
                ETLocationManager.getInstance().startWatchingLocation();

        } catch (ETException e) {
            if (ETPush.getLogLevel() <= Log.ERROR) {
                Log.e(TAG, e.getMessage(), e);
            }
        }


    }

    @SuppressWarnings("unused")
    public void onEvent(ReadyAimFireInitCompletedEvent event) {
        if (ETPush.getLogLevel() <= Log.DEBUG) {
            Log.i(TAG, "ReadyAimFireInitCompletedEvent started.");
        }

        if (event.isReadyAimFireReady()) {
            // successful bootstrap with SDK
            try {
                ETPush pushManager = ETPush.getInstance();


            // ETPush.getInstance().setNotificationRecipientClass
            //
            //		This call is used to specify which activity is displayed when your customers click on the alert.
            //		This call is optional.  By default, the default launch intent for your app will be displayed.
                pushManager.setNotificationRecipientClass(MainActivity.class);
               // pushManager.setNotificationRecipientClass(MainActivityFragment.class);

            }
            catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }

        } else {
            // unsuccessful bootstrap with SDK
            if (event.getCode() == ETException.RAF_INITIALIZE_ENCRYPTION_FAILURE) {
                message = "ETPush readyAimFire() did not initialize due to an Encryption failure.";
            } else if (event.getCode() == ETException.RAF_INITIALIZE_ENCRYPTION_OPTOUT_FAILURE) {
                message = "ETPush readyAimFire() did not initialize encryption failure and unable to opt-out.";
            } else if (event.getCode() == ETException.RAF_INITIALIZE_EXCEPTION) {
                message = "ETPush readyAimFire() did not initialize due to an Exception.";
            } else {
                message = "ETPush readyAimFire() did not initialize due to an Exception.";
            }
            Log.e(TAG, String.format("ETPush readyAimFire() did not initialize due to an Exception with message: %s and code: %d", event.getMessage(), event.getCode()), event.getException());
            throw new RuntimeException(message);
        }
    }


    @SuppressLint("CommitPrefEdits")
    private void JB4A_SDK_init() {
        try {
            // ETPush.readyAimFire
            //
            //		This call should be completed in your Application Class
            //
            //			enableETAnalytics is set to true to show how Salesforce analytics will save statistics for how your customers use the app
            //			enablePIAnalytics is set to true to show how Predictive Intelligence analytics will save statistics for how your customers use the app (by invitation at this point)
            //			enableLocationManager is set to true to show how geo fencing works within the SDK
            //			enableCloudPages is set to true to test how notifications can send your app customers to different web pages
            //
            //			Your app will have these choices set based on how you want your app to work.
            //
            if (ETPush.getLogLevel() <= Log.DEBUG) {
                Log.i(TAG, "Calling readyAimFire()");
            }

            // checkReceiverExistsInManifest();
            // checkServicesExistInManifest();

            ETPushConfig.Builder pushConfigBuilder = new ETPushConfig.Builder(this);
            pushConfigBuilder
                    .setEtAppId(CONSTS_API.getEtAppId())
                    .setAccessToken(CONSTS_API.getAccessToken())
                    .setGcmSenderId(CONSTS_API.getGcmSenderId())
                    .setAnalyticsEnabled(true)
                    .setPiAnalyticsEnabled(false)
                    .setCloudPagesEnabled(false);

                pushConfigBuilder.setLocationEnabled(true);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            // A production build, which normally doesn't have debugging turned on.
            // However, SDK_ExplorerDebugSettingsActivity allows you to override the debug level.  Set it now.

            boolean enableDebug = sp.getBoolean(CONSTS.KEY_DEBUG_PREF_ENABLE_DEBUG, true);
                pushConfigBuilder.setLogLevel(Log.DEBUG);

                // ETLogger.startCapture(maxMemorySize, maxFileSize)
                //
                //		Since we are in debug mode, make sure to capture the log to a file
                //      We are choosing not to clear the log when we start so that we can get a
                //      continuous log of our testing.
                //
                ETLogger.getInstance().startCapture(this, 100000l, 1000000l, false);


            ETPush.readyAimFire(pushConfigBuilder.build());

            if (ETPush.getLogLevel() <= Log.DEBUG) {
                Log.i(TAG, "readyAimFire() has been called.");
            }

            ETNotifications.setNotificationLaunchIntent(new ETNotificationLaunchIntent() {
                @Override
                public Intent setupLaunchIntent(Context context, Bundle payload) {
                    //
                    // save the push notification received info for later display
                    //
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(lamotte_app.context());
                    SharedPreferences.Editor spEditor = sp.edit();
                    long currTime = Calendar.getInstance().getTimeInMillis();
                    spEditor.putLong(CONSTS.KEY_PUSH_RECEIVED_DATE, currTime);

                    JSONObject jo = new JSONObject();
                    try {
                        for (String key : payload.keySet()) {
                            jo.put(key, payload.get(key));
                        }
                    } catch (Exception e) {
                        if (ETPush.getLogLevel() <= Log.ERROR) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }

                    spEditor.putString(CONSTS.KEY_PUSH_RECEIVED_PAYLOAD, jo.toString());

                    spEditor.commit();

                    // Since the SDK_ExplorerDisplayMessageActivity will show either the last or current message
                    // pass a similar bundle to what is saved in Shared Preferences
                    payload.putLong(CONSTS.KEY_PUSH_RECEIVED_DATE, currTime);
                    payload.putString(CONSTS.KEY_PUSH_RECEIVED_PAYLOAD, jo.toString());

                    //
                    // This override will make sure that the launch intent is the only intent that is launched so only 1 is viewable at a time.
                    // FLAG_ACTIVITY_CLEAR_TOP will close the current Intent (as well as any Activities stacked on top), and then show this new one
                    // (if it is currently open).
                    //
                    // Remove .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) if you want to have a separate Intent be displayed for your notifications
                    // if the app user receives a second notification message while viewing a previous message.
                    //
                    return ETNotifications.setupLaunchIntent(context, payload).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
            });

            ETNotifications.setNotificationBuilder(new ETNotificationBuilder() {
                @Override
                public NotificationCompat.Builder setupNotificationBuilder(Context context, Bundle payload) {
                    NotificationCompat.Builder builder = ETNotifications.setupNotificationBuilder(context, payload);

                    String category = payload.getString("category");
                    if (category != null && !category.isEmpty()) {
                        if (ITEM_SPOTLIGHT.equalsIgnoreCase(category)) {
                            //we need to add the 3 item_spotlight buttons to the notification. Android allows
                            //a max of 3 action buttons on the BigStyle notifications.
                            Intent similarIntent = new Intent(context, MainActivity.class);
                            similarIntent.putExtras(payload);
                            PendingIntent similarPendingIntent = ETNotifications.createPendingIntentWithOpenAnalytics(context, similarIntent, true);
                            //builder.addAction(R.drawable.ic_action_labels, "Similar", similarPendingIntent);


                            } else if (ONE_DAY_SALE.equalsIgnoreCase(category)) {
                            //get custom key for the sale date.
                            String saleDateString = payload.getString("sale_date");
                            if (saleDateString != null && !saleDateString.isEmpty()) {
                                try {
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    df.setTimeZone(TimeZone.getDefault());
                                    Date saleDate = df.parse(saleDateString);

                                    Intent intent = new Intent(Intent.ACTION_INSERT)
                                            .setData(CalendarContract.Events.CONTENT_URI)
                                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, saleDate.getTime())
                                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, saleDate.getTime())
                                            .putExtra(CalendarContract.Events.TITLE, payload.getString("event_title"))
                                            .putExtra(CalendarContract.Events.DESCRIPTION, payload.getString("alert"))
                                            .putExtra(CalendarContract.Events.HAS_ALARM, 1)
                                            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

                                    PendingIntent reminderPendingIntent = PendingIntent.getActivity(context, 38456, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                              //      builder.addAction(R.drawable.ic_action_add_alarm, "Add Reminder", reminderPendingIntent);
                                } catch (ParseException e) {
                                    Log.e(TAG, e.getMessage(), e);
                                }
                            }
                        }
                    }

                    return builder;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

}
