//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import playn.java.JavaPlatform;

public class JavaSamson implements Samson.Platform
{
    public static JavaSamson register (JavaPlatform platform) {
        JavaSamson samson = new JavaSamson();
        Samson.register(samson);
        return samson;
    }

    @Override
    public boolean exists (String path) {
        return getClass().getClassLoader().getResource("assets/" + path) != null;
    }
}
