//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.util.Map;

import com.google.common.collect.Maps;

import cli.MonoTouch.Foundation.NSDictionary;
import cli.MonoTouch.Foundation.NSMutableDictionary;
import cli.MonoTouch.Foundation.NSObject;
import cli.MonoTouch.Foundation.NSString;

/**
 * Useful things for dealing with monotouch from java.
 */
public class IOSTypes
{
    /**
     * Creates a hash map copied from an iOS dictionary.
     */
    public static Map<String, String> toMap (NSDictionary dict) {
        Map<String, String> result = Maps.newHashMap();
        for (NSObject key : dict.get_Keys()) {
            NSObject val = dict.ObjectForKey(key);
            if (val != null) {
                result.put(key.ToString(), val.ToString());
            }
        }
        return result;
    }

    /**
     * Creates an iOS dictionary copied from a hash map.
     */
    public static NSDictionary toDict (Map<String, String> map)
    {
        NSMutableDictionary result = new NSMutableDictionary();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            result.Add(new NSString(entry.getKey()), new NSString(entry.getValue()));
        }
        return result;
    }
}
