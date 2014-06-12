//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import samson.notify.AndroidNotifier;
import samson.notify.Notifier;

public class AndroidSamson extends JvmSamson
{
    public AndroidSamson () {
        notifier = new AndroidNotifier();
    }

    @Override
    public Notifier notifier () {
        return notifier;
    }

    private AndroidNotifier notifier;
}
