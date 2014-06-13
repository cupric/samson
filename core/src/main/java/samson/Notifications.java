//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

public abstract class Notifications
{
    public static interface Handle {
        public void cancel ();
    }

    public abstract Handle schedule (long when, String message);
}
