//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

public class Samson
{
    public interface Platform {
        /** Tests if the asset exists at the given path in the application resources. */
        boolean exists (String assetPath);
    }

    public static void register (Platform instance) {
        _instance = instance;
    }

    public static boolean exists (String assetPath) {
        return _instance.exists(assetPath);
    }

    public static Platform platform () {
        return _instance;
    }

    private static Platform _instance;
}
