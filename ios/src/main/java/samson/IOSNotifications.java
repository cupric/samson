//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import samson.Log;
import samson.Notifications;
import cli.MonoTouch.Foundation.NSDate;
import cli.MonoTouch.Foundation.NSDictionary;
import cli.MonoTouch.UIKit.UIApplication;
import cli.MonoTouch.UIKit.UIApplicationState;
import cli.MonoTouch.UIKit.UILocalNotification;

/**
 * Notifications for iOS.
 */
public class IOSNotifications extends Notifications
{
    /**
     * Called to process notifications from launch, i.e. a user tapped a notification, which caused
     * the app to launch.
     */
    public void finishedLaunching (UIApplication app, NSDictionary options) {
        if (options == null) {
            return;
        }

        UILocalNotification notif = (UILocalNotification)
                options.ObjectForKey(UIApplication.get_LaunchOptionsLocalNotificationKey());
        if (notif == null) {
            return;
        }

        dispatch(IOSTypes.toMap(notif.get_UserInfo()), false);
        Log.log.debug("Got local notification on launch", "message", notif.get_AlertBody());
    }

    public void receivedLocalNotification (UIApplication app, UILocalNotification notif) {
        Log.log.debug("Received local notification", "state", app.get_ApplicationState(),
            "message", notif.get_AlertBody());
        dispatch(IOSTypes.toMap(notif.get_UserInfo()),
            app.get_ApplicationState().Value == UIApplicationState.Active);
    }

    @Override
    public void cancelAll () {
        UIApplication.get_SharedApplication().CancelAllLocalNotifications();
    }

    @Override
    protected Handle schedule (long when, Builder builder) {
        final UILocalNotification notif = new UILocalNotification();
        notif.set_FireDate(NSDate.FromTimeIntervalSince1970((double)when / 1000));
        notif.set_AlertBody(builder._message);
        if (!builder._silent) {
            notif.set_SoundName(builder._soundPath == null ?
                UILocalNotification.get_DefaultSoundName().ToString() : builder._soundPath);
        }
        notif.set_UserInfo(IOSTypes.toDict(builder._data));
        UIApplication.get_SharedApplication().ScheduleLocalNotification(notif);
        return new Handle() {
            @Override
            public void cancel () {
                UIApplication.get_SharedApplication().CancelLocalNotification(notif);
            }
        };
    }
}
