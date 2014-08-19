//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.util.Map;

import com.google.common.collect.Maps;

import react.RList;

/**
 * Interface for scheduling and handling notifications. Applications schedule notifications by
 * creating a builder with {@link #builder()}, setting the desired fields, then invoking {@link
 * Builder#schedule(long)}. The notification may be canceled. Incoming notifications may be polled
 * or listened to using {@link #arrived()}.
 * <p>Note: if an application schedules notifications, it must also listen for them and clear the
 * queue.</p>
 */
public abstract class Notifications
{
    /**
     * Refers to a previously scheduled notification and allows it to be cancelled. See {@link
     * Builder#schedule(long)}.
     */
    public static interface Handle {
        /**
         * Cancels the notification referenced by this handle.
         */
        void cancel ();
    }

    /**
     * An incoming notification.
     */
    public static final class Incoming {
        /** The data returned by the system. This should match the data provided to the builder. */
        public final Map<String, String> data;

        /** Whether the app was active when the notification was received. */
        public final boolean wasActive;

        /**
         * Gets the id of the notification. This is not required to be present or unique. Reflects
         * the value given ni {@link Builder#id(String)}.
         */
        public String id () {
            return data.get(ID);
        }

        private Incoming (Map<String, String> data, boolean wasActive) {
            this.data = data;
            this.wasActive = wasActive;
        }
    }

    /**
     * Builds notifications.
     */
    public class Builder {
        /**
         * Sets the id of the notification. This is implemented as a {@link #data(String, String)}
         * call with a fixed key. It is not required, but useful for most applications.
         *
         * <p>Applicable platforms: iOS, Android</p>
         */
        public Builder id (String id) {
            return data(ID, id);
        }

        /**
         * Sets the sound path to use. For iOS, this is the path to a sound in the main bundle. By
         * default, the system attempts to use the default notification sound.
         * TODO: support Android
         *
         * <p>Applicable platforms: iOS</p>
         */
        public Builder soundPath (String path) {
            _soundPath = path;
            return this;
        }

        /**
         * Sets the notification to be silent.
         * TODO: support Android
         *
         * <p>Applicable platforms: iOS</p>
         */
        public Builder silent () {
            _silent = true;
            return this;
        }

        /**
         * Sets the message to display when the notification is shown to the user. This is required
         * for Android.
         *
         * <p>Applicable platforms: iOS, Android</p>
         */
        public Builder message (String message) {
            _message = message;
            return this;
        }

        /**
         * Sets the icon resource to display when the notification is shown to the user. This is
         * required for Android.
         *
         * <p>Applicable platforms: Android</p>
         */
        public Builder icon (int resource) {
            _icon = resource;
            return this;
        }

        /**
         * Sets the title to display when the notification is shown to the user. This is required
         * for Android.
         *
         * <p>Applicable platforms: Android</p>
         */
        public Builder title (String title) {
            _title = title;
            return this;
        }

        /**
         * Sets the notification to vibrate or not.
         *
         * <p>Applicable platforms: Android</p>
         */
        public Builder vibrate (boolean vibrate) {
            _vibrate = vibrate;
            return this;
        }

        /**
         * Sets a data field that is not shown to the user but is handed back to the application
         * via {@link Incoming#data}.
         */
        public Builder data (String key, String value) {
            _data.put(key, value);
            return this;
        }

        /**
         * Schedules the notification to popup at the given epoch milliseconds. For example,
         * {@code System.currentTimeMillis() + 60000} for one minute from now.
         */
        public Handle schedule (long time) {
            return Notifications.this.schedule(time, this);
        }

        @Override public String toString () {
            return "Builder [_message=" + _message + ", _soundPath=" + _soundPath + ", _silent="
                + _silent + ", _data=" + _data + "]";
        }

        /** The message. */
        protected String _message;

        /** Icon on the Android notification. */
        protected int _icon;

        /** Title used on the Android notification. */
        protected String _title;

        /** The sound path. */
        protected String _soundPath;

        /** Whether the notification is silent. */
        protected boolean _silent;

        /** Whether the notification should vibrate the device. */
        protected boolean _vibrate = true;

        /** The data to associate. */
        protected Map<String, String> _data = Maps.newHashMap();
    }

    /**
     * Creates a new builder for a notification. Once the fields are set, {@link
     * Builder#schedule(long)} fires the notification off to the system.
     */
    public Builder builder () {
        return new Builder();
    }

    /**
     * List of notifications received from the system. Applications should remove from this
     * appropriately, either using a listener, or periodically.
     */
    public RList<Incoming> arrived () {
        return _incoming;
    }

    /**
     * Cancels all pending notifications.
     */
    public abstract void cancelAll ();

    protected void dispatch (Map<String, String> data, boolean active) {
        _incoming.add(new Incoming(data, active));
    }

    protected abstract Handle schedule (long when, Builder builder);

    /** The list of notifications received from the system. */
    protected final RList<Incoming> _incoming = RList.create();

    /** Key for our notification id value. */
    protected static final String ID = "notifier_id";
}
