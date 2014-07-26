//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import org.lwjgl.opengl.Display;

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
    protected Handle schedule (long when, Builder builder) {
        final String message = builder._message;
        final Map<String, String> data = builder._data;

        final Interval interval = new Interval() {
            @Override
            public void expired () {
                dispatch(data, Display.isActive());
                _scheduled.remove(this);
            }
        };

        interval.schedule(Math.max(0, when - System.currentTimeMillis()));
        _scheduled.add(interval);

        return new Handle() {
            @Override
            public void cancel() {
                interval.cancel();
                _scheduled.remove(interval);
            }
        };
    }

    protected List<Interval> _scheduled = Lists.newArrayList();
}
