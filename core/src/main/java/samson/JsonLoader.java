//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.io.IOException;

import playn.core.Json;
import playn.core.PlayN;
import playn.core.json.JsonParserException;

/**
 * Loads a json text resources.
 */
public abstract class JsonLoader<T>
{
    /**
     * Returns a json object parsed from the text resource at the given path, if it exists.
     * Otherwise returns null.
     * @throws IOException if any failure to read or parse the file occurred
     * <p>TODO: html: This won't work for HTML5</p>
     */
    public static Json.Object loadObject (String path)
        throws IOException
    {
        return OBJ.load(path);
    }

    /**
     * Returns a json array parsed from the text resource at the given path, if it exists.
     * Otherwise returns null.
     * @throws IOException if any failure to read or parse the file occurred
     * <p>TODO: html: This won't work for HTML5</p>
     */
    public static Json.Array loadArray (String path)
        throws IOException
    {
        return ARRAY.load(path);
    }

    // No outside construction, instances are just an implementation detail for the load methods
    private JsonLoader () {}
    protected abstract T parse (String text);

    protected T load (String path)
        throws IOException
    {
        if (!Samson.exists(path)) {
            Log.log.warning("Missing json", "path", path);
            return null;
        }

        try {
            return parse(PlayN.assets().getTextSync(path));

        // NOTE: Monotouch IO exceptions show up as Throwable
        } catch (JsonParserException ex) {
            Log.log.warning("Bad json", "path", path, ex);
            throw ex;

        } catch (Throwable ex) {
            throw new IOException(ex);
        }
    }

    protected static JsonLoader<Json.Object> OBJ = new JsonLoader<Json.Object>() {
        @Override
        public Json.Object parse (String text) {
            return PlayN.json().parse(text);
        }
    };

    protected static JsonLoader<Json.Array> ARRAY = new JsonLoader<Json.Array>() {
        @Override
        public Json.Array parse (String text) {
            return PlayN.json().parseArray(text);
        }
    };
}
