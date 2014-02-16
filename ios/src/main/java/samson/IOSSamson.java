//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import playn.ios.IOSPlatform;
import cli.System.IO.File;
import cli.System.IO.Path;
import samson.Samson.Platform;

public class IOSSamson implements Platform
{
    public static IOSSamson register (IOSPlatform platform) {
        IOSSamson samson = new IOSSamson();
        Samson.register(samson);
        return samson;
    }

    public boolean exists (String path) {
        return File.Exists(Path.Combine("assets/", path));
    }
}
