//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.io.IOException;

import playn.core.Json;
import playn.core.PlayN;

import static samson.Log.log;

/**
 * Models a set of values used for application configuration, stored in a json file and immutable.
 * Configuration is normally read on application startup and accessed throughout.
 *
 * <p>The common practice is to create, for each package that shares configuration information, a
 * class containing a static config object and static fields or accessors for the values within.
 * For example:
 *
 * <pre>
 * public class Deployment
 * {
 *     public static JsonConfig config = new JsonConfig("deployment");
 *     public static final int foo = config.getValue(Config.INTEGER, "foo", 0);
 *     public static int[] bar () {
 *         return config.getValue(Config.INT_ARRAY, "bars", DEFAULT_BARS);
 *     }
 *     private static final int[] DEFAULT_BARS = {0};
 * }
 * </pre>
 *
 * When loaded, this class will look for <code>com/fribitz/foo.json</code> in the PlayN assets.
 * When <code>fiddles()</code> is called, the json integer value assigned to the key "fiddles"
 * will be returned, or 0 if no value with that name is present.
 */
public class JsonConfig extends Config
{
    /**
     * Constructs a new config object which will obtain configuration information from the
     * json object loaded from the specified asset path.
     */
    public JsonConfig (String path)
    {
        try {
            _props = JsonLoader.loadObject(path + PROPS_SUFFIX);
        } catch (IOException ioe) {
            log.warning("Unable to load configuration", "path", path, "ioe", ioe);
        }

        if (_props == null) {
            // file not found, assume empty
            _props = PlayN.json().createObject();
        }
    }

    @Override
    public String getEntry (String key)
    {
        return _props.getString(key);
    }

    /** Contains the default configuration information. */
    protected Json.Object _props;

    /** The file extension used for configuration files. */
    protected static final String PROPS_SUFFIX = ".json";
}
