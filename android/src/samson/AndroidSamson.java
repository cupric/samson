//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;


public class AndroidSamson extends JvmSamson
{
    public AndroidSamson () {
        notifier = new AndroidNotifications();
    }

    @Override
    public Notifications notifications () {
        return notifier;
    }

    private AndroidNotifications notifier;
}
