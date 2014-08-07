//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson.text;

import org.junit.Assert;
import org.junit.Test;

public class TestMessages
{
    @Test
    public void testFormats () {
        Object[] args = {"foo", "bar"};
        assertFmt("{0} = {1}", args, "foo = bar");
        assertFmt("{1} = {0}", args, "bar = foo");
        assertFmt("{1}", args, "bar");
        assertFmt("-{1}", args, "-bar");
        assertFmt("{0}-", args, "foo-");
        assertFmt("''{1}''", args, "''bar''");
        assertFmt("{0}", new Object[] {9.2}, "9.2");
        assertFmt("{0}{1}", new Object[] {"{1}", "foo"}, "{1}foo");
        assertFmt("no format here", new Object[] {}, "no format here");
        Assert.assertTrue(Messages.format("missing {0}").startsWith("missing"));
        Assert.assertTrue(Messages.format("extra {0}", args).equals("extra foo"));
    }

    private void assertFmt (String fmt, Object[] args, String expected) {
        Assert.assertEquals(expected, Messages.format(fmt, args));
    }
}
