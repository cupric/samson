//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.util.Comparator;
import java.util.Locale;

import playn.ios.IOSPlatform;

import cli.MonoTouch.Foundation.NSDictionary;
import cli.MonoTouch.Foundation.NSError;
import cli.MonoTouch.Foundation.NSLocale;
import cli.MonoTouch.MessageUI.MFMailComposeResult;
import cli.MonoTouch.MessageUI.MFMailComposeViewController;
import cli.MonoTouch.MessageUI.MFMailComposeViewControllerDelegate;
import cli.MonoTouch.UIKit.UIApplication;
import cli.MonoTouch.UIKit.UILocalNotification;
import cli.System.IO.File;
import cli.System.IO.Path;

import samson.Samson.Platform;
import samson.crypto.SecureUtil;
import samson.text.DateTimeFormat;
import samson.text.NumberFormat;

public class IOSSamson implements Platform
{
    public static IOSSamson platform () {
        return (IOSSamson)Samson.platform();
    }

    public static IOSSamson register (IOSPlatform platform) {
        IOSSamson samson = new IOSSamson(platform);
        Samson.register(samson);
        return samson;
    }

    /**
     * Lets the samson notifier know that the application has launched. The notifier will check for
     * a notification and dispatch it if one was received.
     */
    public static void finishedLaunching (UIApplication app, NSDictionary options) {
        platform().notifications().finishedLaunching(app, options);
    }

    /**
     * Lets the samson notifier know that a local notification expired. The notifier will dispatch
     * it appropriately.
     */
    public static void receivedLocalNotification (UIApplication app, UILocalNotification notif) {
        platform().notifications().receivedLocalNotification(app, notif);
    }

    @Override
    public void setFormattingLocale (Locale locale) {
        _formats.setLocale(_locale = locale);
    }

    @Override
    public Locale getDeviceLocale () {
        return new Locale(NSLocale.get_CurrentLocale().get_LocaleIdentifier());
    }

    @Override
    public String getPreferredLanguage () {
        return NSLocale.get_PreferredLanguages()[0];
    }

    @Override
    public NumberFormat numberFormat () {
        return getFormats();
    }

    @Override
    public DateTimeFormat dateTimeFormat () {
        return getFormats();
    }

    @Override
    public Comparator<String> stringComparator () {
        return getFormats();
    }

    public boolean exists (String path) {
        return File.Exists(Path.Combine("assets/", path));
    }

    @Override
    public SecureUtil secureUtil () {
        return _secureUtil;
    }

    @Override
    public IOSNotifications notifications () {
        return _notifier;
    }

    @Override
    public boolean hasMailAccount () {
        return _platform.rootViewController() != null &&
                MFMailComposeViewController.get_CanSendMail();
    }

    @Override
    public void startMailMessage (String subject, String[] to, String body) {
        MFMailComposeViewController mail = new MFMailComposeViewController();
        mail.SetToRecipients(to);
        mail.SetSubject(subject);
        mail.SetMessageBody(body, false);
        mail.set_MailComposeDelegate(new MFMailComposeViewControllerDelegate() {
            @Override
            public void Finished (MFMailComposeViewController ctrl, MFMailComposeResult result,
                NSError error) {
                _platform.rootViewController().DismissViewController(true, null);
            }
        });
        _platform.rootViewController().PresentViewController(mail, true, null);
    }

    private IOSSamson (IOSPlatform platform) {
        _platform = platform;
    }

    private IOSFormats getFormats () {
        if (_locale == null) {
            setFormattingLocale(getDeviceLocale());
        }
        return _formats;
    }

    private IOSPlatform _platform;
    private Locale _locale;
    private IOSFormats _formats = new IOSFormats();
    private IOSSecureUtil _secureUtil = new IOSSecureUtil();
    private IOSNotifications _notifier = new IOSNotifications();
}
