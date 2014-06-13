//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import samson.Interval;
import samson.Log;
import samson.Notifications;

/**
 * Notifier for debugging notifications. Issues a log message printout via an Interval.
 */
public class JavaNotifications extends Notifications
{
    @Override
    public Handle schedule (long when, final String message) {
        final Interval interval = new Interval() {
            @Override
            public void expired () {
                Log.log.info("Notification!", "message", message);
            }
        };
        interval.schedule(Math.max(0, when - System.currentTimeMillis()));
        return new Handle() {
            @Override
            public void cancel() {
                interval.cancel();
            }
        };
    }
}
