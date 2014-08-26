//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.io.StringWriter;
import java.util.Set;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.common.collect.Sets;

import playn.core.Json;
import playn.core.PlayN;
import playn.core.json.JsonParserException;

import static samson.Log.log;

/**
 * Notification handler for Android. Implementing application must include the following line into
 * the AndroidManifest within the &lt;application&gt; tag:
 * <br>&lt;receiver android:name=&quot;samson.AndroidNotifications$Receiver&quot; /&gt;
 */
public class AndroidNotifications extends Notifications
{
    /**
     * BroadcastReceiver subclass to present an Android notification.
     */
    public static class Receiver extends BroadcastReceiver {
        @Override public void onReceive (Context context, Intent intent) {
            try {
                AndroidNotifications instance = (AndroidNotifications)Samson.notifications();

                // pull the metadata
                int id = intent.getExtras().getInt(NotificationIntent.EXTRA_KEY_ID);
                Notification notification =
                    (Notification)intent.getExtras().get(NotificationIntent.EXTRA_KEY_NOTIFICATION);

                // remove the pending notification
                instance.removeNotification(id);

                // submit the notification
                instance.notificationManager.notify(id, notification);
            } catch (Exception exception) {
                log.warning("Failed to submit Android notification", exception);
            }
        }
    }

    /**
     * Intent subclass to manage the metadata for a notification.
     */
    public static class NotificationIntent extends Intent {
        /** Extras key for the identifier (int). */
        public static String EXTRA_KEY_ID = "id";

        /** Extras key for the notification (android.app.Notification). */
        public static String EXTRA_KEY_NOTIFICATION = "notification";

        /**
         * Constructs a new NotificationIntent with the appropriate metadata.
         */
        public NotificationIntent (Context context, int id, Notification notification) {
            super(context, Receiver.class);

            // put the metadata
            putExtra(EXTRA_KEY_ID, id);
            putExtra(EXTRA_KEY_NOTIFICATION, notification);

            // must set an action, otherwise metadata will not be passed properly
            setAction(this.getClass().getName());
        }
    }

    /**
     * Constructs a new AndroidNotifications with the main activity to be launched when a
     * notification is tapped.
     */
    public AndroidNotifications (Activity mainActivity) {
        this.activityClass = mainActivity.getClass();
        this.applicationContext = mainActivity.getApplicationContext();
        this.notificationManager =
            (NotificationManager)applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        this.alarmManager =
            (AlarmManager)applicationContext.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public void cancelAll () {
        notificationManager.cancelAll();

        // iterate through the pending notifications and cancel each applicable one
        Intent intent = new Intent(applicationContext, Receiver.class);
        for (int id : getNotifications()) {
            PendingIntent pending = PendingIntent.getBroadcast(applicationContext, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
            if (pending != null) {
                alarmManager.cancel(pending);
            }
        }
        setNotifications(Sets.<Integer>newHashSet());
    }

    @Override
    protected Handle schedule (long when, Builder builder) {
        // pull the id of this notification based off the Builder's string id or an automatically
        // generated identifier
        final int id = builder._data.containsKey(Notifications.ID) ?
            builder._data.get(Notifications.ID).hashCode() : getAutomaticIdentifier();

        // create the notification builder
        NotificationCompat.Builder notification =
            new NotificationCompat.Builder(applicationContext).
                setAutoCancel(true).
                setSmallIcon(builder._icon).
                setContentTitle(builder._title).
                setContentText(builder._message).
                setVibrate(builder._vibrate ? new long[]{100, 200, 200, 200} : new long[]{}).
                setWhen(when);

        // build a simulated task stack so the back button works properly
        TaskStackBuilder stack = TaskStackBuilder.create(applicationContext);
        stack.addParentStack(activityClass);
        stack.addNextIntent(new Intent(applicationContext, activityClass));
        notification.setContentIntent(stack.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));

        // build the pending intent to pass to the alarm receiver
        final PendingIntent pending =
            PendingIntent.getBroadcast(applicationContext, id,
                new NotificationIntent(applicationContext, id, notification.build()), 0);

        // register the pending notification id
        addNotification(id);

        // pass the intent to the alarm receiver
        alarmManager.set(AlarmManager.RTC_WAKEUP, when, pending);

        return new Handle() {
            @Override
            public void cancel () {
                notificationManager.cancel(id);
                alarmManager.cancel(pending);
                removeNotification(id);
            }
        };
    }

    /**
     * Adds a notification identifier to the list of pending notifications.
     */
    protected synchronized void addNotification (int id) {
        Set<Integer> ids = getNotifications();
        if (ids.add(id)) {
            setNotifications(ids);
        }
    }

    /**
     * Removes a notification identifier from the list of pending notifications.
     */
    protected synchronized void removeNotification (int id) {
        Set<Integer> ids = getNotifications();
        if (ids.remove(id)) {
            setNotifications(ids);
        }
    }

    /**
     * Returns a set of existing notification identifiers waiting to be fired.
     */
    protected synchronized Set<Integer> getNotifications () {
        String jsondata = PlayN.storage().getItem(STORAGE_KEY);
        if (jsondata == null) {
            return Sets.newHashSet();
        }

        try {
            Json.Object json = PlayN.platform().json().parse(jsondata);
            return Sets.newHashSet(json.getArray(JSON_KEY_IDS, Integer.class));
        } catch (JsonParserException exception) {
            log.warning("Failed to parse AndroidNotifications JSON", "jsondata", jsondata, exception);
            PlayN.storage().removeItem(STORAGE_KEY);
            return Sets.newHashSet();
        }
    }

    /**
     * Sets the storage value with the given list of identifiers.
     */
    protected synchronized void setNotifications (Set<Integer> ids) {
        // build the json list of identifiers
        Json.Array jsonids = PlayN.json().createArray();
        for (int id : ids) {
            jsonids.add(id);
        }

        // build the json object
        Json.Object json = PlayN.platform().json().createObject();
        json.put(JSON_KEY_IDS, jsonids);

        // write the json object to the json writer
        Json.Writer jsonwriter = PlayN.json().newWriter().useVerboseFormat(false).object();
        json.write(jsonwriter);
        jsonwriter.end();

        // convert json writer to string
        StringWriter stringwriter = new StringWriter();
        stringwriter.write(jsonwriter.write());

        // store the string
        PlayN.storage().setItem(STORAGE_KEY, stringwriter.toString());
    }

    /**
     * Returns an automatically generated identifier for a notification.
     */
    protected synchronized int getAutomaticIdentifier () {
        Set<Integer> ids = getNotifications();

        // iterate through negative values to prevent overlap with manually defined ids
        for (int ii = -1; ii > Integer.MIN_VALUE; --ii) {
            if (ids.contains(ii)) {
                continue;
            }

            return ii;
        }

        return -1;
    }

    /** Key used in PlayN storage to store notification information. */
    private static final String STORAGE_KEY = "samson.AndroidNotifications";

    /** Json key in the storage for notification identifiers. */
    private static final String JSON_KEY_IDS = "ids";

    /** Class of the main activity to be launched when the notification is tapped. */
    private final Class<? extends Activity> activityClass;

    /** Reference to the application-wide context. */
    private final Context applicationContext;

    /** Reference to the notification manager in the application context. */
    private final NotificationManager notificationManager;

    /** Reference to the alarm manager in the application context. */
    private final AlarmManager alarmManager;
}