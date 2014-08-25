//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import samson.crypto.SecureUtil;

import playn.java.JavaPlatform;

public class JavaSamson extends JvmSamson
{
    public static JavaSamson register (JavaPlatform platform) {
        JavaSamson samson = new JavaSamson();
        Samson.register(samson);
        return samson;
    }

    public JavaSamson () {
        notifier = new JavaNotifications();
    }

    @Override
    public Notifications notifications () {
        return notifier;
    }

    @Override
    public boolean hasMailAccount () {
        return false;
    }

    @Override
    public void startMailMessage (String subject, String[] to, String body) {
    }

    @Override public SecureUtil secureUtil () {
        return _secureUtil;
    }

    private final JavaNotifications notifier;
    private JvmSecureUtil _secureUtil = new JvmSecureUtil();
}
