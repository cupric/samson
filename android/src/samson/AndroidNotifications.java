//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import samson.Notifications;

public class AndroidNotifications extends Notifications
{
    @Override
    public void cancelAll () {
        // TODO: implement
    }

    @Override
    protected Handle schedule (long when, Builder builder) {
        // TODO: implement
        return new Handle() {
            @Override
            public void cancel () {
            }
        };
    }
}
