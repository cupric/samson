//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import android.app.Activity;
import samson.crypto.SecureUtil;

public class AndroidSamson extends JvmSamson
{
    public AndroidSamson (Activity mainActivity) {
        notifier = new AndroidNotifications(mainActivity);
        secureUtil = new AndroidSecureUtil(mainActivity);
    }

    @Override
    public Notifications notifications () {
        return notifier;
    }

    @Override
    public boolean hasMailAccount () {
        // TODO: implement
        return false;
    }

    @Override
    public void startMailMessage (String subject, String[] to, String body) {
        // TODO: implement
    }

    @Override public SecureUtil secureUtil () {
        return secureUtil;
    }

    private final AndroidNotifications notifier;
    private final AndroidSecureUtil secureUtil;
}
