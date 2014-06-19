//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.util.List;

import com.google.common.collect.Lists;

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
    public void cancelAll () {
        for (Interval interval : _scheduled) {
            interval.cancel();
        }
        _scheduled.clear();
    }

    @Override
    protected Handle schedule (long when, final Builder builder) {
        final Interval interval = new Interval(Interval.PLAYN) {
            @Override
            public void expired () {
                Log.log.info("Notification!", "message", builder._message);
                dispatch(builder._data, Display.isActive());
                _scheduled.remove(this);
            }
        };

        Log.log.info("Scheduling notification", "id", builder._data.get(ID));
        interval.schedule(Math.max(0, when - System.currentTimeMillis()));
        _scheduled.add(interval);

        return new Handle() {
            @Override
            public void cancel() {
                Log.log.info("Cancelling notification", "id", builder._data.get(ID));
                interval.cancel();
                _scheduled.remove(interval);
            }
        };
    }

    protected List<Interval> _scheduled = Lists.newArrayList();
}
