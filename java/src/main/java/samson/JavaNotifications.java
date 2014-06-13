//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import org.lwjgl.opengl.Display;

import samson.Interval;
import samson.Log;
import samson.Notifications;

/**
 * Notifier for debugging notifications. Issues a log message printout via an Interval.
 */
public class JavaNotifications extends Notifications
{
    @Override
    protected Handle schedule (long when, final Builder builder) {
        final Interval interval = new Interval(Interval.PLAYN) {
            @Override
            public void expired () {
                Log.log.info("Notification!", "message", builder._message);
                dispatch(builder._data, Display.isActive());
            }
        };

        Log.log.info("Scheduling notification", "id", builder._data.get(ID));
        interval.schedule(Math.max(0, when - System.currentTimeMillis()));

        return new Handle() {
            @Override
            public void cancel() {
                Log.log.info("Cancelling notification", "id", builder._data.get(ID));
                interval.cancel();
            }
        };
    }
}
