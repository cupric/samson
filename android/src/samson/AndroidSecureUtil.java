package samson;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import android.app.Activity;
import android.content.Context;

import com.google.common.collect.Maps;

import static samson.Log.log;

/**
 * SecureUtil subclass to be used with Android.
 */
public class AndroidSecureUtil extends JvmSecureUtil
{
    /**
     * Constructs a new Android secure util with the activity.
     */
    public AndroidSecureUtil (Activity activity)
    {
        this(activity.getApplicationContext());
    }

    /**
     * Constructs a new Android secure util with the application context.
     */
    @SuppressWarnings("unchecked")
    public AndroidSecureUtil (Context applicationContext)
    {
        ctx = applicationContext;

        // pull the internal mapping from the device
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(ctx.openFileInput(FILENAME));
            idmap.putAll((Map<String, byte[]>)input.readObject());
        } catch (FileNotFoundException exception) {
            // no keys have been stored so ignore the exception
        } catch (Exception exception) {
            log.error("Failed to pull secure data", exception);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException exception) {
                }
            }
        }
    }

    @Override public byte[] retrieveKey (String id)
    {
        return idmap.get(id);
    }

    @Override public synchronized void storeKey (String id, byte[] key) throws IOException
    {
        idmap.put(id, key);

        // write the internal map with the updates
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE));
            output.writeObject(idmap);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException exception) {
                }
            }
        }
    }

    /** Filename reference in the internal storage for Android. */
    protected static final String FILENAME = "samson.secure";

    /** Reference to the application context. */
    protected final Context ctx;

    /** Reference to the internal mapping stored on the device. */
    protected final Map<String, byte[]> idmap = Maps.newHashMap();
}