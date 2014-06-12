//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson.notify;

import cli.MonoTouch.Foundation.NSDate;
import cli.MonoTouch.UIKit.UIApplication;
import cli.MonoTouch.UIKit.UILocalNotification;

public class IOSNotifier extends Notifier
{
    @Override
    public Handle schedule (long when, String message) {
        final UILocalNotification notif = new UILocalNotification();
        notif.set_FireDate(NSDate.FromTimeIntervalSince1970((double)when / 1000));
        notif.set_AlertBody(message);
        return new Handle() {
            @Override
            public void cancel () {
                UIApplication.get_SharedApplication().CancelLocalNotification(notif);
            }
        };
    }
}
