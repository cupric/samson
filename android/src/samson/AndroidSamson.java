//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import samson.crypto.SecureUtil;

public class AndroidSamson extends JvmSamson
{
    public AndroidSamson (Activity mainActivity) {
        applicationContext = mainActivity.getApplicationContext();
        notifier = new AndroidNotifications(mainActivity);
        secureUtil = new AndroidSecureUtil(mainActivity);
    }

    @Override
    public Notifications notifications () {
        return notifier;
    }

    @Override
    public boolean hasMailAccount () {
        // true if have a valid activity to receive the intent
        return createMailIntent(null) != null;
    }

    @Override
    public void startMailMessage (String subject, String[] to, String body) {
        // translate the to[] into a uri string
        String recipients = null;
        if (to != null && to.length > 0) {
            recipients = to[0];
            for (int ii = 1; ii < to.length; ++ii) {
                recipients += "," + to[ii];
            }
        }

        // build the complete uri. note: splitting the headers and parameters is necessary
        // because Uri.Builder cannot set opaquePart and parameters in a single builder
        Uri.Builder headers = new Uri.Builder().
            scheme("mailto").
            encodedOpaquePart(recipients);
        Uri.Builder parameters = new Uri.Builder().
            appendQueryParameter("subject", subject).
            appendQueryParameter("body", body);
        Uri metadata = Uri.parse(headers.toString() + parameters.toString());

        // attempt to create the mail intent
        Intent intent = createMailIntent(metadata);
        if (intent != null) {
            applicationContext.startActivity(intent);
        }
    }

    @Override public SecureUtil secureUtil () {
        return secureUtil;
    }

    /**
     * Returns an intent to present a mail composer, or null if there are no applicable activities
     * to receive the intent.
     */
    protected Intent createMailIntent (Uri data) {
        // build a chooser from the email action
        Intent intent = Intent.createChooser(new Intent(Intent.ACTION_SENDTO, data), null);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // make sure that we have an activity to resolve this intent
        if (applicationContext.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            return null;
        } else {
            return intent;
        }
    }

    /** Notifications manager. */
    private final AndroidNotifications notifier;

    /** Security util. */
    private final AndroidSecureUtil secureUtil;

    /** Reference to the application context. */
    private final Context applicationContext;
}