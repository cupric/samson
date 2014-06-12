//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import samson.notify.JavaNotifier;
import samson.notify.Notifier;

import playn.java.JavaPlatform;

public class JavaSamson extends JvmSamson
{
    public static JavaSamson register (JavaPlatform platform) {
        JavaSamson samson = new JavaSamson();
        Samson.register(samson);
        return samson;
    }

    public JavaSamson () {
        notifier = new JavaNotifier();
    }

    @Override
    public Notifier notifier () {
        return notifier;
    }

    private final JavaNotifier notifier;
}
